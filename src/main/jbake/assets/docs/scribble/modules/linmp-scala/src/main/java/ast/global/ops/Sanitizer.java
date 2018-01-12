package ast.global.ops;

import ast.global.GlobalEnd;
import ast.global.GlobalRec;
import ast.global.GlobalSend;
import ast.global.GlobalSendCase;
import ast.global.GlobalType;
import ast.global.GlobalTypeVisitor;
import ast.local.LocalType;
import ast.name.Label;
import ast.name.RecVar;

import java.util.Collection;
import java.util.Map;

import org.scribble.main.ScribbleException;

/** Perform sanity checks on a global type AST
 * 
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Sanitizer extends GlobalTypeVisitor<GlobalType>
{
	private int lastVarIdx = 0; // Last index used for fresh rec vars
	private Collection<RecVar> usedRecVars = new java.util.HashSet<>();
	private Collection<RecVar> bound = new java.util.HashSet<>();
	private Collection<String> errors = new java.util.LinkedList<>();
	private static GlobalType gtype;
	
	/** Sanitize the given global type.
	 * 
	 * A sanitized type all "vacuous" recursions removed, and all recursion
	 * variables pairwise distinct (a form of Ottmann/Barendregt convention).
	 * 
	 * @param g Global type to be sanitized
	 * @return A sanitized version of the given global type
	 * @throws ScribbleException
	 */
	public static GlobalType apply(GlobalType g) throws ScribbleException
	{
		Sanitizer s = new Sanitizer(g);
		return s.process();
	}
	
	private Sanitizer(GlobalType g)
	{
		gtype = g;
	}
	
	@Override
	public GlobalType process() throws ScribbleException
	{
		GlobalType res = visit(gtype);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) validating " + gtype + ": "
				                    + String.join(";", errors));
	}
	
	@Override
	protected GlobalEnd visit(GlobalEnd node)
	{
		return node;
	}
	
	@Override
	protected GlobalSend visit(GlobalSend node)
	{
		Map<Label, GlobalSendCase> cases2 = new java.util.HashMap<Label, GlobalSendCase>();
		for (Map.Entry<Label, GlobalSendCase> x: node.cases.entrySet())
		{
			GlobalSendCase c = x.getValue();
			ast.PayloadType pay = (c.pay == null) ? new ast.name.Unit() : c.pay;
			
			if (c.pay instanceof LocalType)
			{
				try
				{
					pay = ast.local.ops.Sanitizer.apply((LocalType)c.pay);
				}
				catch (ScribbleException e)
				{
					errors.add(e.toString());
				}
			}
			
			cases2.put(x.getKey(), new GlobalSendCase(pay, visit(c.body)));
		}
		
		return new GlobalSend(node.src, node.dest, cases2);
	}

	@Override
	protected GlobalType visit(GlobalRec node)
	{
		RecVar var = node.recvar;
		
		if (!node.body.freeVariables().contains(var))
		{
			// The recursion is vacuous: let's skip it
			return visit(node.body);
		}
		
		if (this.usedRecVars.contains(var))
		{
			// The recursion re-uses a variable, let's alpha-convert
			try
			{
				lastVarIdx++;
				RecVar newvar = new RecVar(var.name+lastVarIdx);
				return visit(AlphaConverter.apply(node, var, newvar));
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
			return node;
		}
		
		// If we are here, the recursion is binding a "fresh" variable
		this.bound.add(var);
		this.usedRecVars.add(var);
		GlobalRec res = new GlobalRec(var, visit(node.body));
		this.bound.remove(var);
		return res;
	}

	@Override
	protected RecVar visit(RecVar node) {
		if (!this.bound.contains(node))
		{
			errors.add("Unbound variable: " + node);
		}
		
		return node;
	}
}
