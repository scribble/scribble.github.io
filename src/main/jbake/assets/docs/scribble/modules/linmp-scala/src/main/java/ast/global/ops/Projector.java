package ast.global.ops;

import ast.global.GlobalEnd;
import ast.global.GlobalRec;
import ast.global.GlobalSend;
import ast.global.GlobalSendCase;
import ast.global.GlobalType;
import ast.global.GlobalTypeVisitor;
import ast.local.LocalType;
import ast.local.ops.Merge;
import ast.name.Label;
import ast.name.RecVar;
import ast.name.Role;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scribble.main.ScribbleException;

public class Projector extends GlobalTypeVisitor<LocalType>
{
	private GlobalType gtype;
	private Role role;
	private Merge.Operator merge;
	private Collection<String> errors = new java.util.LinkedList<>();
	
	/** Perform all projections of the given global type.
	 * 
	 * @param g Global type to project
	 * @param merge Merge operator to use
	 * @return A mapping from each role to its projected local type
	 * @throws ScribbleException
	 */
	public static Map<Role, LocalType> apply(GlobalType g, Merge.Operator merge) throws ScribbleException
	{
		Set<Role> roles = g.roles();
		Map<Role, LocalType> res = new java.util.HashMap<>();
		List<String> errors = new java.util.LinkedList<>();
		
		for (Role r: roles)
		{
			LocalType p;
			try
			{
				p = apply(g, r, merge);
				res.put(r, p);
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
		}
		
		if (!errors.isEmpty())
		{
			throw new ScribbleException("Error(s) projecting " + g + ":"
										+ String.join("; ", errors));
		}
		return res;
	}
	
	/**
	 * 
	 * @param g Global type to project
	 * @param r Role to project
	 * @param merge Merge operator to use
	 * @return The projected local type
	 * @throws ScribbleException
	 */
	public static LocalType apply(GlobalType g, Role r, Merge.Operator merge) throws ScribbleException
	{
		Projector p = new Projector(g, r, merge);
		return p.process();
	}
	
	private Projector(GlobalType g, Role r, Merge.Operator merge)
	{
		gtype = g;
		role = r;
		this.merge = merge;
	}
	
	@Override
	public LocalType process() throws ScribbleException
	{
		LocalType res = visit(gtype);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) projecting " + gtype
									+ " onto " + role + ":"
									+ String.join("; ", errors));
	}

	@Override
	protected ast.local.LocalEnd visit(GlobalEnd node)
	{
		return new ast.local.LocalEnd();
	}

	@Override
	protected LocalType visit(GlobalSend node)
	{
		Map<Label, ast.local.LocalCase> cases = node.cases.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey(),
										  e -> visit(e.getValue())));	
		if (role.equals(node.src))
		{
			return new ast.local.LocalSelect(node.dest, cases);
		}
		else if (role.equals(node.dest))
		{
			return new ast.local.LocalBranch(node.src, cases);
		}
		// The current role is not involved in the communication:
		// we need to project the continuations and merge them
		Iterator<LocalType> conts = cases.values().stream().map(c -> c.body).iterator();
		LocalType lt = conts.next();
		while (conts.hasNext())
		{
			try
			{
				lt = merge.apply(lt, conts.next());
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
		}
		return lt;
	}
	
	private ast.local.LocalCase visit(GlobalSendCase node)
	{
		return new ast.local.LocalCase(node.pay, visit(node.body));
	}
			
	@Override
	protected LocalType visit(GlobalRec node)
	{
		LocalType brec = visit(node.body);
		if (brec instanceof RecVar)
		{
			// Projection produces an unguarded recursion, considered as "end"
			return new ast.local.LocalEnd();
		}
		return new ast.local.LocalRec(node.recvar, brec);
	}

	@Override
	protected RecVar visit(RecVar node)
	{
		return node;
	}
}
