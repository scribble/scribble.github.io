package ast.linear.ops;

import org.scribble.main.ScribbleException;

import ast.linear.AbstractVariant;
import ast.linear.Case;
import ast.linear.End;
import ast.linear.In;
import ast.linear.NameEnv;
import ast.linear.Out;
import ast.linear.Rec;
import ast.linear.Record;
import ast.linear.Type;
import ast.linear.Variant;
import ast.linear.Visitor;
import ast.local.LocalNameEnv;
import ast.local.LocalType;
import ast.name.BaseType;
import ast.name.Label;
import ast.name.RecVar;
import ast.util.ClassTable;

import java.util.Collection;

/** Build the Scala case classes definitions corresponding to a linear I/O type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class ScalaProtocolExtractor extends Visitor<ClassTable>
{
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final Type visiting;
	private final NameEnv nameEnv;
	
	public static ClassTable apply(Type t) throws ScribbleException
	{
		return apply(t, DefaultNameEnvBuilder.apply(t));
	}
	
	private static ClassTable apply(Type t, NameEnv nameEnv) throws ScribbleException
	{
		ScalaProtocolExtractor te = new ScalaProtocolExtractor(t, nameEnv);
		
		return te.process();
	}
	
	private ScalaProtocolExtractor(Type t, NameEnv nameEnv)
	{
		this.visiting = t;
		this.nameEnv = nameEnv;
	}
	
	@Override
	protected ClassTable process() throws ScribbleException
	{
		ClassTable res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) extracting protocol of " + visiting + ": "
				                    + String.join("; ", errors));
	}

	@Override
	protected ClassTable visit(End node)
	{
		return new ClassTable();
	}

	@Override
	protected ClassTable visit(In node)
	{
		return fromVariant(node.variant, node);
	}
	
	@Override
	protected ClassTable visit(Out node)
	{
		return fromVariant(node.variant, node);
	}
	
	private ClassTable fromVariant(AbstractVariant av, Type node)
	{	
		if (av instanceof Variant)
		{
			Variant v = (Variant)av;
			String xtnd = "";
			// Sort labels before (recursively) generating code
			java.util.List<Label> ls = new java.util.ArrayList<>(new java.util.TreeSet<>(v.cases.keySet()));
			ClassTable res = new ClassTable();
			
			if (ls.size() == 0)
			{
				throw new RuntimeException("BUG: found 0-branches variant " + v);
			}

			if (ls.size() > 1)
			{
				// Only generate an abstract class for more than 1 labels
				xtnd = nameEnv.get(v);
				res.putIdem(xtnd, "sealed abstract class " + xtnd);
			}

			for (Label l: ls)
			{
				Case c = v.cases.get(l);
				res.putAllIdem(fromVariantCase(node, l, c, xtnd));
			}
			
			return res;
		}
		else if (av instanceof Rec)
		{
			return fromVariant(((Rec)av).body, node);
		}
		else if (av instanceof RecVar)
		{
			return new ClassTable();
		}
		else
		{
			throw new RuntimeException("BUG: invalid abstract variant type " + av);
		}
	}
	
	// If not empty, xtnds is the class extended by each case class
	// If skip is true, then no case classes will be generated
	private ClassTable fromVariantCase(Type node, Label l, Case c, String xtnds)
	{
		ClassTable res = new ClassTable();
		
		try {
			String payload;
			
			if (c.payload instanceof BaseType)
			{
				payload = c.payload.toString();
			}
			else if (c.payload instanceof Record)
			{
				// The record was originated from a local type:
				// let's find out its name
				Record rec = (Record)c.payload;
				LocalType origin = rec.origin;
				try {
					// FIXME: what about custom name environments?
					LocalNameEnv env = ast.local.ops.DefaultNameEnvBuilder.apply(origin);
					payload = env.get(origin);
				}
				catch (ScribbleException e)
				{
					errors.add("Cannot determine name of " + c.payload + ": " + e);
					payload = "ERROR";
				}
				
				// Also generate the binary classes for the payload
				for (ast.name.Role r: rec.keySet())
				{
					Type lt = rec.get(r);
					res.putAllIdem(visit(lt));
				}
			}
			else
			{
				throw new RuntimeException("BUG: unsupported payload " + c.payload);
			}

			String cont = "ERROR";
			if (c.cont instanceof End)
			{
				// Do not generate vacuous continuations
				cont = "";
			}
			else
			{
				cont = ScalaChannelTypeExtractor.apply(c.cont, nameEnv);
			}
			
			String def = "case class " + l.name + "(p: " + payload; // Provide ")" below!
			if (cont.isEmpty())
			{
				def +=")";
			}
			else
			{
				def += ")(val cont: " + cont + ")";
			}
			
			if (!xtnds.isEmpty())
			{
				def += " extends " + xtnds;
			}
			res.putIdem(l.name, def);
		}
		catch (ScribbleException e)
		{
			errors.add("Cannot extract protocol of " + node + ": " + e);
			return res;
		}
		
		// Also inspect the continuations
		res.putAllIdem(visit(c.cont));
		
		return res;
	}
}
