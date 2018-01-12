package ast.binary.ops;

import org.scribble.main.ScribbleException;

import ast.binary.Type.Action;
import ast.binary.Branch;
import ast.binary.Case;
import ast.binary.End;
import ast.binary.Rec;
import ast.binary.Select;
import ast.binary.Visitor;
import ast.local.LocalType;
import ast.linear.AbstractVariant;
import ast.linear.In;
import ast.linear.Out;
import ast.linear.Payload;
import ast.linear.Type;
import ast.linear.Variant;
import ast.name.Label;
import ast.name.RecVar;

import java.util.Collection;
import java.util.Map;

/** Encode a binary session type into a linear I/O type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class LinearEncoder extends Visitor<Type>
{	
	private Collection<String> errors = new java.util.LinkedList<String>();
	
	// Binary session type being visited
	private ast.binary.Type visiting;
	
	// Remembers open recursion variables, and maps them to types
	private Map<RecVar, ast.linear.Type> env;
	
	public static Type apply(ast.binary.Type visiting) throws ScribbleException
	{
		return apply(visiting, new java.util.HashMap<RecVar, ast.linear.Type>());
	}
	
	static Type apply(ast.binary.Type visiting, Map<RecVar, ast.linear.Type> env) throws ScribbleException
	{
		LinearEncoder enc = new LinearEncoder(visiting, env);
		return ast.linear.ops.Sanitizer.apply(enc.process());
	}
	
	private LinearEncoder(ast.binary.Type visiting,
			 Map<RecVar, ast.linear.Type> env)
	{
		this.visiting = visiting;
		// Make a deep copy of the provided environment
		this.env = new java.util.HashMap<RecVar, ast.linear.Type>(env);
	}
	
	@Override
	protected Type process() throws ScribbleException
	{
		Type res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) encoding " +visiting+ ": "
				                    + String.join("; ", errors));
	}

	@Override
	protected ast.linear.End visit(End node)
	{
		return new ast.linear.End();
	}

	@Override
	protected In visit(Branch node)
	{
		return new In(cases2variant(node.cases, false));
	}
	
	@Override
	protected Out visit(Select node)
	{
		return new Out(cases2variant(node.cases, true));
	}
	
	// Turn a mapping of label/cases into a variant.
	// If true, "dualCont" causes the continuation to be dualised
	// (needed e.g. for encoding an internal choice)
	// FIXME: the code below is quite similar to LinearRecEncoder.fromCases()
	private Variant cases2variant(Map<Label, Case> cases, boolean dualCont)
	{
		Map<Label, ast.linear.Case> cases2 = new java.util.HashMap<>();
		
		for (Map.Entry<Label, Case> e: cases.entrySet())
		{
			try{
				Label l = e.getKey();
				Case c = e.getValue();
				Payload pay = (c.pay instanceof Payload
						? (Payload)c.pay : ((LocalType)c.pay).linear());
				Type cont = visit(c.body);
				ast.linear.Case cs = new ast.linear.Case(pay,
						dualCont ? cont.dual() : cont);
				cases2.put(l, cs);
			}
			catch (ScribbleException exc)
			{
				errors.add("Error encoding " + e + ": " + exc);
			}
		}
		
		return new Variant(cases2);
	}
	
	// FIXME: the code below is quite similar to LinearRecEncoder.visit(Rec)
	@Override
	protected Type visit(Rec node)
	{
		Action a = node.action();
		
		if (a.isNone())
		{
			errors.add("Vacuous recursion: " + node);
			return new ast.linear.End();
		}
		
		AbstractVariant varnt = new RecVar("ERROR"); // In case of error below
		try
		{
			RecVar recvar = node.recvar;
			if (env.containsKey(recvar))
			{
				errors.add("Variable " + recvar + "bound twice"
						   + " (hint: enforce Barendregt convention before encoding)");
			}
			else
			{
				// We checked a.isNone() above, so now a is either in or out
				env.put(recvar, a.isInput ? new In(recvar) : new Out(recvar));
				varnt = new ast.linear.Rec(recvar, LinearRecEncoder.apply(node.body, env));
				env.remove(recvar);
			}
		}
		catch (ScribbleException e)
		{
			errors.add("Error encoding " + node + ": " + e);
		}
		
		if (a.isInput)
		{
			return new In(varnt);
		}
		else if (a.isOutput)
		{
			return new Out(varnt);
		}
		else
		{
			// Maybe we are being a bit paranoid here
			throw new RuntimeException("BUG: malformed action for "+node+": "+a);
		}
	}

	@Override
	protected Type visit(RecVar node)
	{
		if (!env.containsKey(node))
		{
			errors.add("Unbound variable: " + node);
			return new ast.linear.End();
		}
		return env.get(node);
	}

}
