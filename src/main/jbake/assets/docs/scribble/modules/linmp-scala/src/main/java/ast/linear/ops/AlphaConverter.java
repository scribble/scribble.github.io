package ast.linear.ops;

import org.scribble.main.ScribbleException;

import ast.linear.AbstractVariant;
import ast.linear.Case;
import ast.linear.End;
import ast.linear.In;
import ast.linear.Out;
import ast.linear.Payload;
import ast.linear.Rec;
import ast.linear.Record;
import ast.linear.Type;
import ast.linear.Variant;
import ast.linear.Visitor;
import ast.name.BaseType;
import ast.name.RecVar;

import java.util.stream.Collectors;

public class AlphaConverter extends Visitor<Type>
{
	private RecVar old; // Original variable
	private RecVar conv; // Alpha-converted variable
	private boolean open = false; // Becomes true when "old" is traversed
	static private Type visiting;
	
	/** Alpha-convert the given linear type
	 * 
	 * @param t Linear type to be alpha-converted
	 * @param old Old variable name
	 * @param conv New variable name
	 * @return An alpha-converted version of the given linear type
	 */
	public static Type apply(Type t, RecVar old, RecVar conv) throws ScribbleException
	{
		AlphaConverter s = new AlphaConverter(t, old, conv);
		return s.process();
	}
	
	/** Alpha-convert the given linear type
	 * 
	 * @param av Abstract variant to be alpha-converted
	 * @param old Old variable name
	 * @param conv New variable name
	 * @return An alpha-converted version of the given linear type
	 */
	public static AbstractVariant apply(AbstractVariant av, RecVar old, RecVar conv) throws ScribbleException
	{
		// Visit a dummy linear type
		AlphaConverter s = new AlphaConverter(new In(av), old, conv);
		// FIXME: refactor to avoid the following cast
		return ((In)s.process()).carried();
	}
	
	private AlphaConverter(Type t, RecVar old, RecVar conv)
	{
		visiting = t;
		this.old = old;
		this.conv = conv;
	}
	
	@Override
	protected Type process() throws ScribbleException
	{
		return visit(visiting);
	}

	@Override
	protected End visit(End node)
	{
		return node;
	}

	@Override
	protected In visit(In node)
	{
		AbstractVariant av = node.carried();
		assert(av != null);
		return new In(visitAbstractVariant(av));
	}
	
	@Override
	protected Out visit(Out node)
	{
		AbstractVariant av = node.carried();
		assert(av != null);
		return new Out(visitAbstractVariant(av));
	}
	
	private AbstractVariant visitAbstractVariant(AbstractVariant av)
	{
		if (av instanceof Variant)
		{
			return visitVariant((Variant)av);
		}
		else if (av instanceof Rec)
		{
			return visitRec((Rec)av);
		}
		else if (av instanceof RecVar)
		{
			return visitRecVar((RecVar)av);
		}
		throw new RuntimeException("BUG: invalid variant type: " + av);
	}
	
	private Rec visitRec(Rec r)
	{
		if (r.recvar.equals(old))
		{
			if (open)
			{
			    // We have found a distinct occurrence of "old": don't convert
			    return r;
			}
			// We have found a top-level occurrence of "old": let's convert
			open = true; // Rememer that "old" is open in the recursion body
			Rec newrec = new Rec(conv, visitAbstractVariant(r.body));
			open = false; // We are leaving the recursion body
			return newrec;
		}
		return new Rec(conv, visitAbstractVariant(r.body));
	}

	private RecVar visitRecVar(RecVar v)
	{
		if ((v.equals(old)) && open) {
			return conv;
		}
		// Here we might also spot unbound occurrences of "old"
		return v;
	}
	
	private Variant visitVariant(Variant v)
	{
		Variant newv = new Variant(
				v.cases.entrySet().stream().collect(Collectors.toMap(
					e -> e.getKey(),
			        e -> visitCase(e.getValue())))
			);
		
		return newv;
	}
	
	private Case visitCase(Case c)
	{
		// Here we are assuming that payload is closed
		return new Case(visitPayload(c.payload), visit(c.cont));
	}
	
	private Payload visitPayload(Payload p)
	{
		if (p instanceof BaseType)
		{
			return p; // Nothing to do
		}
		else if (p instanceof Record)
		{
			Record r = (Record)p;
			Record newr = new Record(r.origin,
				r.entrySet().stream().collect(Collectors.toMap(
					e -> e.getKey(),
					e -> visit(e.getValue())))
			);
			
			return newr;
		}
		throw new RuntimeException("BUG: invalid payload type: " + p);
	}
}
