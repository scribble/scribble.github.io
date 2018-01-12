package ast.local.ops;

import ast.local.LocalBranch;
import ast.local.LocalCase;
import ast.local.LocalEnd;
import ast.local.LocalRec;
import ast.local.LocalSelect;
import ast.local.LocalType;
import ast.local.LocalTypeVisitor;
import ast.name.Label;
import ast.name.RecVar;

import java.util.Collection;
import java.util.Map;

import org.scribble.main.ScribbleException;

/** Perform sanity checks on a global type AST.
 * 
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Sanitizer extends LocalTypeVisitor<LocalType>
{
	private Collection<RecVar> bound = new java.util.HashSet<RecVar>();
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final LocalType ltype;
	
	/** Sanitize the given global type
	 * 
	 * A sanitized type all "vacuous" recursions removed, and all recursion
	 * variables pairwise distinct (a form of Ottmann/Barendregt convention).
	 * 
	 * @param lt Local type to be sanitized
	 * @return A sanitized version of the given local type
	 * @throws ScribbleException
	 */
	public static LocalType apply(LocalType lt) throws ScribbleException
	{
		Sanitizer s = new Sanitizer(lt);
		return s.process();
	}
	
	private Sanitizer(LocalType lt)
	{
		ltype = lt;
	}
	
	@Override
	protected LocalType process() throws ScribbleException
	{
		LocalType res = visit(ltype);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) validating " + ltype + ": "
				                    + String.join("; ", errors));
	}
	
	@Override
	protected LocalEnd visit(LocalEnd node)
	{
		return node;
	}
	
	@Override
	protected LocalBranch visit(LocalBranch node)
	{
		Map<Label, LocalCase> cases2 = new java.util.HashMap<Label, LocalCase>();
		for (Map.Entry<Label, LocalCase> x: node.cases.entrySet())
		{
			cases2.put(x.getKey(), visit(x.getValue()));
		}
		
		return new LocalBranch(node.src, cases2);
	}
	
	protected LocalCase visit(LocalCase c)
	{
		ast.PayloadType pay = c.pay;
		if (c.pay instanceof LocalType)
		{
			try
			{
				pay = Sanitizer.apply((LocalType)c.pay);
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
		}
		else if (pay == null)
		{
			pay = new ast.name.Unit();
		}
		
		return new LocalCase(pay, visit(c.body));
	}
	
	@Override
	protected LocalSelect visit(LocalSelect node)
	{
		Map<Label, LocalCase> cases2 = new java.util.HashMap<Label, LocalCase>();
		for (Map.Entry<Label, LocalCase> x: node.cases.entrySet())
		{
			cases2.put(x.getKey(), visit(x.getValue()));
		}
		
		return new LocalSelect(node.dest, cases2);
	}

	@Override
	protected LocalType visit(LocalRec node)
	{
		RecVar var = node.recvar;
		
		if (!node.body.freeVariables().contains(var))
		{
			// The recursion is vacuous: let's skip it
			return visit(node.body);
		}
		
		if (this.bound.contains(var))
		{
			// The recursion re-binds a variable, let's alpha-convert
			try
			{
				return visit(AlphaConverter.apply(node, var,
														   new RecVar(var.name+"'")));
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
			return node;
		}
		
		this.bound.add(var);
		LocalRec res = new LocalRec(var, visit(node.body));
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
