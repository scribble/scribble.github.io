package ast.local.ops;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.scribble.main.ScribbleException;

import ast.linear.AbstractVariant;
import ast.linear.In;
import ast.linear.End;
import ast.linear.Out;
import ast.linear.Record;
import ast.local.ops.DefaultNameEnvBuilder;
import ast.local.LocalBranch;
import ast.local.LocalCase;
import ast.local.LocalEnd;
import ast.local.LocalNameEnv;
import ast.local.LocalRec;
import ast.local.LocalSelect;
import ast.local.LocalType;
import ast.local.LocalTypeVisitor;
import ast.name.BaseType;
import ast.name.Label;
import ast.name.RecVar;
import ast.name.Role;
import ast.util.ClassTable;

public class ScalaProtocolExtractor extends LocalTypeVisitor<ClassTable>
{	
	// Simple pair of a binary type and its naming environment
	protected static class LinearTypeNameEnv
	{
		protected ast.linear.Type t;
		protected final ast.linear.NameEnv env;
		
		public LinearTypeNameEnv(ast.linear.Type t) throws ScribbleException
		{
			this(t, ast.linear.ops.DefaultNameEnvBuilder.apply(t));
		}
		
		public LinearTypeNameEnv(ast.linear.Type t, ast.linear.NameEnv env)
		{
			this.t = t;
			this.env = env;
		}
	}
	
	// Maps a role to its (current) binary type, and its naming environment
	protected static class ChannelTracker extends HashMap<Role, LinearTypeNameEnv>
	{
		private static final long serialVersionUID = 1L;
		
		public ChannelTracker(LocalType t) throws ScribbleException
		{
			super();
			for (Role r: t.roles())
			{
				this.put(r, new LinearTypeNameEnv(t.linear(r)));
			}
		}
	}
	
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final LocalType visiting;
	private final LocalNameEnv nameEnv;
	private ChannelTracker ctracker;
	private List<Role> roles; // Sorted list of involved roles
	
	/** Generate the Scala protocol classes representing a local type.
	 * 
	 * @param t Local type to extract classes for
	 * @return The Scala class definitions representing {@code t}
	 * @throws ScribbleException in case of error
	 */
	public static ClassTable apply(LocalType t) throws ScribbleException
	{
		return apply(t, DefaultNameEnvBuilder.apply(t));
	}
	
	/** Generate the Scala protocol classes representing a local type, using
	 * a given naming environment.
	 *  
	 * @param t Local type to extract classes for
	 * @param nameEnv Naming environment (supposed to be suitable for {@code t})
	 * @return The Scala class definitions representing {@code t}
	 * @throws ScribbleException in case of error
	 */
	public static ClassTable apply(LocalType t, LocalNameEnv nameEnv) throws ScribbleException
	{
		ScalaProtocolExtractor te = new ScalaProtocolExtractor(t, nameEnv);
		
		return te.process();
	}
	
	private ScalaProtocolExtractor(LocalType t, LocalNameEnv nameEnv) throws ScribbleException
	{
		this.visiting = t;
		this.nameEnv = nameEnv;
		this.ctracker = new ChannelTracker(t);
		this.roles = new java.util.ArrayList<>(new java.util.TreeSet<>(ctracker.keySet()));
	}
	
	@Override
	protected ClassTable process() throws ScribbleException
	{
		ClassTable mpProtoClasses = visit(visiting);
		if (!errors.isEmpty())
		{
			throw new ScribbleException("Error(s) extracting protocol of " + visiting + ": " + String.join(";", errors));
		}
		return mpProtoClasses;
	}

	@Override
	protected ClassTable visit(LocalEnd node)
	{
		return new ClassTable();
	}

	@Override
	protected ClassTable visit(LocalBranch node)
	{
		String className = nameEnv.get(node);
		assert(className != null);
				
		List<String> chanspecs = getChanspecs();
		
		// Save the current tracker status (we'll restore it before returning)
		LinearTypeNameEnv lte = ctracker.get(node.src);
		// Note: we use the fact that we know lte.t is In or Out (not End)
		AbstractVariant v = getCarried(lte.t);
		String vname = lte.env.get(v);
		assert(vname != null);
		
		// Ensure that labels are sorted
		List<Label> labels = new java.util.ArrayList<>(new java.util.TreeSet<>(node.labels()));
		
		ClassTable res = new ClassTable();
		String def = "case class " + className + "(" + String.join(", ", chanspecs) + ") {\n";
		def += "  def receive(implicit timeout: Duration = Duration.Inf)";
		if (labels.size() > 1)
		{
			// We could add the return type annotation, but let's Scala infer
		}
		def += " = {\n" +
				"    " + node.src.name + ".receive(timeout) match {\n";
		
		for (Label l: labels) // Note: node might have less labels wrt v
		{
			// FIXME: here we could simplify if the variant has just one label
			def += "      case m @ " + ScalaEncoder.BINARY_CLASSES_NS + "." + l + "(p) => {\n";
			
			ast.linear.Payload payload = v.payload(l);
			String payloadRepr;
			if ((payload instanceof BaseType) || (payload instanceof Record))
			{
				payloadRepr = "p";
			}
			else
			{
				throw new RuntimeException("BUG: unsupported payload type " + payload);
			}
			
			LocalType contb = node.cases.get(l).body;
			String ret;
			if (contb instanceof LocalEnd)
			{
				ret = l.name + "(" + payloadRepr + ")"; // We are done
			}
			else
			{
				String contClassName = nameEnv.get(contb);
				List<String> contSpecs = new java.util.LinkedList<>();
				for (Role rcont: this.roles)
				{
					if (!rcont.equals(node.src))
					{
						contSpecs.add(rcont.name); // Reuse the channel
					}
					else
					{
						if (v.continuation(l) instanceof End) {
							contSpecs.add("()"); // Role has no continuation
						} else {
							contSpecs.add("m.cont"); // Use the continuation
						}
					}
				}
				ret = l.name + "(" + payloadRepr + ", " + contClassName + "(" + String.join(", ", contSpecs) + "))";
			}
			
			def += ("        " + ret + "\n" +
					"      }\n");
			
		}
		def += ("    }\n" +
				"  }\n" +
				"}");
		res.putIdem(className, def);
		
		// Let's now generate the payload and continuation classes
		for (Label l: labels)
		{
			LocalCase c = node.cases.get(l);
			// Update the channel involved in the interaction
			ctracker.put(node.src, new LinearTypeNameEnv(v.continuation(l), lte.env));
			
			if (c.pay instanceof LocalType)
			{
				res.putAllIdem(visitHO((LocalType)c.pay));
			}
			res.putAllIdem(visit(c.body));
		}
		
		// Restore the channel tracker status before returning
		ctracker.put(node.src, lte);
		
		return res;
	}
	
	@Override
	protected ClassTable visit(LocalSelect node)
	{
		String className = nameEnv.get(node);
		assert(className != null);
		
		List<String> chanspecs = getChanspecs();
		
		// Save the current tracker status (we'll restore it before returning)
		LinearTypeNameEnv lte = ctracker.get(node.dest);
		assert(lte != null);
		
		// Note: we use the fact that we know lte.t is In or Out (not End)
		AbstractVariant v = getCarried(lte.t);
		String vname = lte.env.get(v);
		assert(vname != null);
		
		ClassTable res = new ClassTable();
		String def = "case class " + className + "(" + String.join(", ", chanspecs) + ") {\n";
		// Ensure that labels are sorted
		List<Label> labels = new java.util.ArrayList<>(new java.util.TreeSet<>(node.labels()));
		for (Label l: labels) // Note: node might have less labels wrt v
		{
			def += "  def send(v: " + l.name + ") = {\n";
			
			ast.linear.Payload payload = v.payload(l);
			String payloadRepr;
			if ((payload instanceof BaseType) || (payload instanceof Record))
			{
				payloadRepr = "v.p";
			}
			else
			{
				throw new RuntimeException("BUG: unsupported payload type " + payload);
			}
			
			ast.linear.Type cont = v.continuation(l);
			String sendRepr;
			if ((cont instanceof In) || (cont instanceof Out))
			{
				// Let lchannels take care of the continuation
				sendRepr = " !! " + ScalaEncoder.BINARY_CLASSES_NS + "." + l + "(" + payloadRepr + ")_";
			}
			else if (cont instanceof End)
			{
				sendRepr = " ! " + ScalaEncoder.BINARY_CLASSES_NS + "." + l + "(" + payloadRepr + ")";
			}
			else
			{
				throw new RuntimeException("BUG: unsupported continuation type " + cont);
			}
			
			LocalType contb = node.cases.get(l).body;
			assert(contb != null);
			
			String ret;
			if (contb instanceof LocalEnd)
			{
				ret = "()"; // We are done
			}
			else
			{
				String contClassName = nameEnv.get(contb);
				List<String> contSpecs = new java.util.LinkedList<>();
				for (Role rcont: this.roles)
				{
					if (!rcont.equals(node.dest))
					{
						contSpecs.add(rcont.name); // Reuse the channel
					}
					else
					{
						contSpecs.add("cnt"); // Use the continuation
					}
				}
				ret = contClassName + "(" + String.join(", ", contSpecs) + ")";
			}
			
			def += ("    val cnt = " + node.dest.name + sendRepr + "\n" +
					"    " + ret + "\n" +
					"  }\n");
		}
		def += "}";
		res.putIdem(className, def);
		
		// Let's now generate the payload and continuation classes
		for (Label l: labels)
		{
			LocalCase c = node.cases.get(l);
			// Update the channel involved in the interaction
			// Note: we dualise the continuation, since it follows an output!
			ctracker.put(node.dest, new LinearTypeNameEnv(v.continuation(l).dual(), lte.env));
			
			if (c.pay instanceof LocalType)
			{
				res.putAllIdem(visitHO((LocalType)c.pay));
			}
			res.putAllIdem(visit(c.body));
		}
		
		// Restore the channel tracker status before returning
		ctracker.put(node.dest, lte);
		
		return res;
	}
	
	private ClassTable visitHO(LocalType t)
	{
		ClassTable res;
		try
		{
			res = apply(t); // Note that we are not providing a root, now
		}
		catch (ScribbleException e)
		{
			errors.add("Error extracting protocol of " + t + ": " + e);
			res = new ClassTable();
		}
		
		return res;
	}
	
	// Get the variant carried by a linear type t, throwing a runtime exception
	// if t is End
	private AbstractVariant getCarried(ast.linear.Type t)
	{
		if (t instanceof In)
		{
			return ((In)t).carried();
		}
		else if (t instanceof Out)
		{
			return ((Out)t).carried();
		}
		else
		{
			throw new RuntimeException("BUG: expecting In/Out underlying type, got " + t);
		}
	}
	
	// Determine the channel types underlying a multiparty session object
	private List<String> getChanspecs()
	{
		List<String> chanspecs = new java.util.LinkedList<>();
		for (Role r: roles)
		{
			String chanspec = "";
			ast.linear.Type t = ctracker.get(r).t;
			ast.linear.NameEnv env = ctracker.get(r).env;
			chanspec += r.name + ": ";
			try
			{
				chanspec += ast.linear.ops.ScalaChannelTypeExtractor.apply(
						t, env, ScalaEncoder.BINARY_CLASSES_NS + ".");
			}
			catch (ScribbleException e)
			{
				errors.add("Cannot extract channel type of " + t + ": " + e);
				chanspec += "ERROR";
			}
			chanspecs.add(chanspec);
		}
		return chanspecs;
	}
	
	@Override
	protected ClassTable visit(LocalRec node)
	{
		return visit(node.body);
	}

	@Override
	protected ClassTable visit(RecVar node)
	{
		return new ClassTable();
	}

}
