package ast.local.ops;

import ast.binary.Type;
import ast.binary.Branch;
import ast.binary.Select;
import ast.binary.End;
import ast.binary.Rec;
import ast.binary.ops.Merge;

import ast.local.LocalEnd;
import ast.local.LocalRec;
import ast.local.LocalBranch;
import ast.local.LocalSelect;
import ast.local.LocalCase;
import ast.local.LocalType;
import ast.local.LocalTypeVisitor;
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

public class Projector extends LocalTypeVisitor<ast.binary.Type>
{
	private final LocalType ltype;
	private final Role role;
	private final Merge.Operator merge;
	private Collection<String> errors = new java.util.LinkedList<>();
	
	/** Perform all projections of the given local type.
	 * 
	 * @param l Local type to project
	 * @param merge Merge operator to use
	 * @return A mapping from each role to its projected binary session type
	 * @throws ScribbleException
	 */
	public static Map<Role, Type> apply(LocalType l, Merge.Operator merge) throws ScribbleException
	{
		Set<Role> roles = l.roles();
		Map<Role, ast.binary.Type> res = new java.util.HashMap<>();
		List<String> errors = new java.util.LinkedList<>();
		
		for (Role r: roles)
		{
			Type p;
			try
			{
				p = apply(l, r, merge);
				res.put(r, p);
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
		}
		
		if (!errors.isEmpty())
		{
			throw new ScribbleException("Error(s) projecting " + l + ":"
										+ String.join("; ", errors));
		}
		return res;
	}
	
	/** Project a local type onto the given target role.
	 * 
	 * @param l Local type to project
	 * @param r Destination role to projec
	 * @param merge Merge operator to use
	 * @return The projected binary session type
	 * @throws ScribbleException
	 */
	public static Type apply(LocalType l, Role r, Merge.Operator merge) throws ScribbleException
	{
		Projector p = new Projector(l, r, merge);
		return p.process();
	}
	
	private Projector(LocalType l, Role r, Merge.Operator merge)
	{
		ltype = l;
		role = r;
		this.merge = merge;
	}
	
	@Override
	public Type process() throws ScribbleException
	{
		Type res = visit(ltype);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) projecting " + ltype
									+ " onto " + role + ":"
									+ String.join("; ", errors));
	}

	@Override
	protected End visit(LocalEnd node)
	{
		return new End();
	}

	@Override
	protected Type visit(LocalSelect node)
	{
		Map<Label, ast.binary.Case> cases = node.cases.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey(),
										  e -> visit(e.getValue())));	
		if (role.equals(node.dest))
		{
			return new Select(cases);
		}
		// The current role is not involved in the communication:
		// we need to project the continuations and merge them
		return visit(cases);
	}
	
	@Override
	protected Type visit(LocalBranch node)
	{
		Map<Label, ast.binary.Case> cases = node.cases.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey(),
										  e -> visit(e.getValue())));	
		if (role.equals(node.src))
		{
			return new Branch(cases);
		}
		// The current role is not involved in the communication:
		// we need to project the continuations and merge them
		return visit(cases);
	}
	
	private Type visit(Map<Label, ast.binary.Case> cases)
	{
		Iterator<Type> conts = cases.values().stream().map(c -> c.body).iterator();
		Type res = conts.next();
		while (conts.hasNext())
		{
			try
			{
				res = merge.apply(res, conts.next());
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
		}
		return res;
	}
	
	private ast.binary.Case visit(LocalCase node)
	{
		return new ast.binary.Case(node.pay, visit(node.body));
	}
			
	@Override
	protected Type visit(LocalRec node)
	{
		Type brec = visit(node.body);
		if (brec instanceof RecVar)
		{
			// Projection produces an unguarded recursion, considered as "end"
			return new End();
		}
		return new Rec(node.recvar, brec);
	}

	@Override
	protected RecVar visit(RecVar node)
	{
		return node;
	}
}
