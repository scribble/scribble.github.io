package ast.binary.ops;

import java.util.Collection;
import java.util.Map;

import org.scribble.main.ScribbleException;

import ast.binary.Branch;
import ast.binary.Case;
import ast.binary.End;
import ast.binary.Rec;
import ast.binary.Select;
import ast.binary.Visitor;
import ast.binary.Type.Action;
import ast.linear.AbstractVariant;
import ast.linear.In;
import ast.linear.Out;
import ast.linear.Payload;
import ast.linear.Type;
import ast.linear.Variant;
import ast.name.Label;
import ast.name.RecVar;

// Inner visitor used inside recursion variables
class LinearRecEncoder extends Visitor<AbstractVariant>
{
	private Collection<String> errors = new java.util.LinkedList<String>();
	
	// Binary session type being visited
	private ast.binary.Type visiting;
	
	// Remembers open recursion variables, and maps them to types
	private Map<RecVar, ast.linear.Type> env = new java.util.HashMap<>();
	
	public static AbstractVariant apply(ast.binary.Type visiting,
										Map<RecVar, ast.linear.Type> env) throws ScribbleException
	{
		LinearRecEncoder enc = new LinearRecEncoder(visiting, env);
		return enc.process();
	}
	
	private LinearRecEncoder(ast.binary.Type visiting,
			 Map<RecVar, ast.linear.Type> env)
	{
		this.visiting = visiting;
		// Make a deep copy of the provided environment
		this.env = new java.util.HashMap<RecVar, ast.linear.Type>(env);
	}
	
	@Override
	protected AbstractVariant process() throws ScribbleException
	{
		AbstractVariant res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) encoding " +visiting+ ": "
				                    + String.join("; ", errors));
	}

	@Override
	protected AbstractVariant visit(End node)
	{
		return bug(node);
	}

	@Override
	protected AbstractVariant visit(Branch node)
	{
		return fromCases(node.cases, false, node);
	}

	@Override
	protected AbstractVariant visit(Select node)
	{
		return fromCases(node.cases, true, node);
	}
	
	// If true, "dualCont" causes the continuation to be dualised
	// (needed e.g. for encoding an internal choice)
	// "node" is only used for error reporting
	private Variant fromCases(Map<Label, Case> cases, boolean dualCont, ast.binary.Type node)
	{
		Map<Label, ast.linear.Case> cases2 = new java.util.HashMap<>();
		
		for (Map.Entry<Label, Case> e: cases.entrySet())
		{
			Label l = e.getKey();
			Case c = e.getValue();
			Payload pay = (c.pay instanceof Payload
						   ? (Payload)c.pay
						   : new ast.name.BaseType("TODO")); // FIXME !!!11
			ast.linear.Case cs;
			try {
				Type cont = LinearEncoder.apply(c.body, env);
				cs = new ast.linear.Case(pay, dualCont ? cont.dual() : cont);
			}
			catch (ScribbleException ex)
			{
				errors.add("Cannot encode " + node + ": " + ex);
				cs = new ast.linear.Case(pay, new ast.linear.End());
			}
			cases2.put(l, cs);
		}
		
		return new Variant(cases2);
	}
	
	@Override
	protected AbstractVariant visit(Rec node)
	{
		Action a = node.action();
		
		if (a.isNone())
		{
			errors.add("Vacuous recursion: " + node);
			return new RecVar("ERROR");
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
		
		if (a.isInput || a.isOutput)
		{
			return varnt;
		}
		else
		{
			// Maybe we are being a bit paranoid here
			throw new RuntimeException("BUG: malformed action for "+node+": "+a);
		}
	}

	@Override
	protected AbstractVariant visit(RecVar node)
	{
		return bug(node);
	}
	
	private AbstractVariant bug(ast.binary.Type node)
	{
		throw new RuntimeException("BUG: LinearRecEncoder should never visit " + node);
	}
}
