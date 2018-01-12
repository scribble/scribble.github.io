/**
 * 
 */
package ast.local.ops;

import org.scribble.main.ScribbleException;

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

import ast.util.ClassTable;

import java.util.Collection;
import java.util.Map;

/**
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 *
 */
public class ScalaMessageExtractor extends LocalTypeVisitor<ClassTable>
{
	public static String MESSAGE_CLASSES_PFX = "Msg";
	
	// What kind of message classes are we extracting?
	private enum Option {
		INPUTS, OUTPUTS
	}
	
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final LocalType visiting;
	private final LocalNameEnv nameEnv;
	private final Option option;
	
	public static ClassTable inputs(LocalType t, LocalNameEnv nameEnv) throws ScribbleException
	{
		ScalaMessageExtractor te = new ScalaMessageExtractor(t, nameEnv, Option.INPUTS);
		
		return te.process();
	}
	
	public static ClassTable outputs(LocalType t, LocalNameEnv nameEnv) throws ScribbleException
	{
		ScalaMessageExtractor te = new ScalaMessageExtractor(t, nameEnv, Option.OUTPUTS);
		
		return te.process();
	}
	
	protected ScalaMessageExtractor(LocalType t, LocalNameEnv env, Option opt)
	{
		visiting = t;
		nameEnv = env;
		option = opt;
	}
	
	@Override
	protected ClassTable process() throws ScribbleException
	{
		ClassTable res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		
		String what = (option == Option.INPUTS ? "input" : "output");
		throw new ScribbleException("Error(s) extracting " + what + " messages of " + visiting + ": "
				                    + String.join("; ", errors));
	}
	
	@Override
	protected ClassTable visit(LocalEnd node)
	{
		return new ClassTable();
	}
	
	@Override
	protected ClassTable visit(LocalBranch node)
	{
		ClassTable res = new ClassTable();
		
		// Further class tables generated from the payloads
		Collection<ClassTable> pclasses = new java.util.LinkedList<>();
		
		String xtnds = "";
		
		if (option == Option.INPUTS)
		{
			if (node.cases.keySet().size() > 1)
			{
				xtnds = MESSAGE_CLASSES_PFX + nameEnv.get(node);
				res.putIdem(xtnds, "sealed abstract class " + xtnds);
			}
			
			for (Map.Entry<Label, LocalCase> e: node.cases.entrySet())
			{
				String cls = e.getKey().name;
				String def = "case class " + cls + "(";
				LocalCase c = e.getValue();

				if (c.pay instanceof BaseType)
				{
					def += "p: " + c.pay; // Directly represent payload type
				}
				else if (c.pay instanceof LocalType)
				{
					def += "p: " + nameEnv.get((LocalType)c.pay);
					// Also generate message classes for the payload
					pclasses.add(visit((LocalType)c.pay));
				}
				else
				{
					throw new RuntimeException("BUG: unsupported payload type: " + c.pay);
				}

				if (c.body instanceof LocalEnd)
				{
					// Nothing to do (continuation omitted)
				}
				else
				{
					def += ", cont: " + nameEnv.get(c.body);
				}

				def += ")";
				def += (xtnds.isEmpty() ? "" : " extends " + xtnds);
				res.putIdem(cls, def);
			}
		}
		else // option == Option.OUTPUTS
		{
			// When generating output messages, only inspect the payload types
			for (LocalCase c: node.cases.values())
			{
				if (c.pay instanceof BaseType)
				{
					// Nothing to do
				}
				else if (c.pay instanceof LocalType)
				{
					pclasses.add(visit((LocalType)c.pay));
				}
				else
				{
					throw new RuntimeException("BUG: unsupported payload type: " + c.pay);
				}
			}
		}
		
		// Visit the continuations
		for (LocalCase c: node.cases.values())
		{
			res.putAllIdem(visit(c.body));
		}
		
		// Finally, also include the message classes for the payloads (if any)
		for (ClassTable ct: pclasses)
		{
			res.putAllIdem(ct);
		}
		
		return res;
	}
	
	@Override
	protected ClassTable visit(LocalSelect node)
	{
		ClassTable res = new ClassTable();
		
		// Further class tables generated from the payloads
		Collection<ClassTable> pclasses = new java.util.LinkedList<>();
		
		String xtnds = "";
		
		if (option == Option.OUTPUTS)
		{
			if (node.cases.keySet().size() > 1)
			{
				// Here we could generate sealed abstract class, but not needed
				// xtnds = MESSAGE_CLASSES_PFX + nameEnv.get(node);
				// res += "sealed abstract class " + xtnds + "\n";
			}
			
			for (Map.Entry<Label, LocalCase> e: node.cases.entrySet())
			{
				String cls = e.getKey().name;
				String def = "case class " + cls + "(";
				LocalCase c = e.getValue();

				if (c.pay instanceof BaseType)
				{
					def += "p: " + c.pay; // Directly represent payload type
				}
				else if (c.pay instanceof LocalType)
				{
					def += "p: " + nameEnv.get((LocalType)c.pay);
					// Also generate message classes for the payload
					pclasses.add(visit((LocalType)c.pay));
				}
				else
				{
					throw new RuntimeException("BUG: unsupported payload type: " + c.pay);
				}
				
				def += ")";
				def += (xtnds.isEmpty() ? "" : " extends " + xtnds);
				res.putIdem(cls, def);
			}
		}
		else // option == Option.INPUTS
		{
			// When generating input messages, only inspect the payload types
			for (LocalCase c: node.cases.values())
			{
				if (c.pay instanceof BaseType)
				{
					// Nothing to do
				}
				else if (c.pay instanceof LocalType)
				{
					pclasses.add(visit((LocalType)c.pay));
				}
				else
				{
					throw new RuntimeException("BUG: unsupported payload type: " + c.pay);
				}
			}
		}
		
		// Visit the continuations
		for (LocalCase c: node.cases.values())
		{
			res.putAllIdem(visit(c.body));
		}
		
		// Finally, also include the message classes for the payloads (if any)
		for (ClassTable ct: pclasses)
		{
			res.putAllIdem(ct);
		}
		
		return res;
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
