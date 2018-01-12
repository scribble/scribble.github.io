package ast.binary.ops;

import java.util.Collection;

import ast.binary.Branch;
import ast.binary.Case;
import ast.binary.End;
import ast.binary.Rec;
import ast.binary.Select;
import ast.binary.Type;
import ast.binary.Visitor;
import ast.name.Label;
import ast.name.RecVar;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scribble.main.ScribbleException;

/** Full merging operator between binary session types.
 *
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
class FullMerger extends Visitor<Type>
{
	private Collection<String> errors = new java.util.LinkedList<String>();
	private Type visiting, t2; // We will update t2 during visit
	
	/** Merge the two given local types
	 * 
	 * @param t First type to merge
	 * @param u Second type to merge
	 * @return
	 */
	public static Type apply(Type t, Type u) throws ScribbleException
	{
		FullMerger m = new FullMerger(t, u);
		return m.process();
	}
	
	private FullMerger(Type t, Type u)
	{
		visiting = t;
		t2 = u;
	}

	@Override
	protected Type process() throws ScribbleException
	{
		Type res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) merging " +visiting+ " and " +t2+ ": "
				                    + String.join("; ", errors));
	}

	@Override
	protected End visit(End node)
	{
		if (!node.equals(t2))
		{
			cannotMerge(node, t2);
		}
		return node;
	}

	@Override
	protected Branch visit(Branch node)
	{
		if (!(t2 instanceof Branch))
		{
			cannotMerge(node, t2);
			return node;
		}
		
		Branch t2b = (Branch)t2;
		
		Map<Label,Case> newcases = mergeCases(node.cases, t2b.cases,
											  node, t2b, true);
		return new Branch(newcases);
	}
	
	@Override
	protected Select visit(Select node)
	{
		if (!(t2 instanceof Select))
		{
			cannotMerge(node, t2);
			return node;
		}
		
		Select t2s = (Select)t2;
		
		Map<Label,Case> newcases = mergeCases(node.cases, t2s.cases,
											  node, t2s, false);
		return new Select(newcases);
	}
	
	private Map<Label,Case> mergeCases(Map<Label,Case> vcases, Map<Label,Case> t2cases,
									   Type v, Type t2, Boolean forceCommonLabels)
	{
		Set<Label> labsv = vcases.keySet();
		Set<Label> labst2 = t2cases.keySet();
		
		Set<Label> common = new HashSet<>(labsv);
		common.retainAll(labst2); // Labels common to visited and "other"

		Set<Label> onlyv = new HashSet<>(labsv);
		onlyv.removeAll(labst2); // Labels only in the visited branching

		Set<Label> onlyt2 = new HashSet<>(labst2);
		onlyt2.removeAll(labsv); // Labels only in the "other" branching
		
		if (forceCommonLabels && !(onlyv.isEmpty() && onlyt2.isEmpty()))
		{
			// Labels mismatch
			cannotMerge(v, t2);
			return new HashMap<>();
		}
		
		Map<Label,Case> newcases = new HashMap<>();

		for (Label l: common)
		{
			Case vcl = vcases.get(l);
			if (!vcl.pay.equals(t2cases.get(l).pay))
			{
				// Payloads mismatch
				cannotMerge(v, t2);
				return newcases;
			}

			this.t2 = t2cases.get(l).body; // Update t2 before visiting
			newcases.put(l, new Case(vcl.pay, visit(vcl.body)));
		}

		newcases.putAll(onlyv.stream()
				.collect(Collectors.toMap(l -> l, l -> vcases.get(l))));
		newcases.putAll(onlyt2.stream()
				.collect(Collectors.toMap(l -> l, l -> t2cases.get(l))));
		return newcases;
	}

	@Override
	protected Rec visit(Rec node)
	{
		if (!(t2 instanceof Rec))
		{
			cannotMerge(node, t2);
			return node;
		}
		
		Rec t2r = (Rec)t2;
		if (!node.recvar.equals(t2r.recvar))
		{
			cannotMerge(node, t2);
			return node;
		}
		
		t2 = t2r.body;
		return new Rec(node.recvar, visit(node.body));
	}

	@Override
	protected RecVar visit(RecVar node)
	{
		if (!node.equals(t2))
		{
			cannotMerge(node, t2);
		}
		return node;
	}
	
	private void cannotMerge(Type t1, Type t2)
	{
		errors.add("Cannot merge " + t1 + " and " + t2);
	}
}
