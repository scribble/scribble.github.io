/**
 * 
 */
package ast.linear.ops;

import java.util.Collection;
import java.util.stream.Collectors;

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

/** Sanitizer for linear types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Sanitizer extends Visitor<Type>
{
	private int lastVarIdx = 0; // Last index used for fresh rec vars
	private Collection<RecVar> usedRecVars = new java.util.HashSet<>();
	private Collection<RecVar> bound = new java.util.HashSet<>();
	private Collection<String> errors = new java.util.LinkedList<>();
	private static Type visiting;
	
	public static Type apply(Type t) throws ScribbleException
	{
		Sanitizer s = new Sanitizer(t);
		return s.process();
	}
	
	private Sanitizer(Type t)
	{
		visiting = t;
	}
	
	@Override
	protected Type process() throws ScribbleException
	{
		Type res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) sanitizing " + visiting + ": "
				                    + String.join(";", errors));
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
	
	private AbstractVariant visitRec(Rec r)
	{
		RecVar var = r.recvar;
		
		if (!r.body.freeVariables().contains(var))
		{
			// The recursion is vacuous: let's skip it
			return visitAbstractVariant(r.body);
		}
		
		if (this.usedRecVars.contains(var))
		{
			// The recursion re-uses a variable, let's alpha-convert
			try
			{
				lastVarIdx++;
				RecVar newvar = new RecVar(var.name+lastVarIdx);
				return visitAbstractVariant(AlphaConverter.apply(r, var, newvar));
			}
			catch (ScribbleException e)
			{
				errors.add(e.toString());
			}
			return r;
		}
		
		// If we are here, the recursion is binding a "fresh" variable
		this.bound.add(var);
		this.usedRecVars.add(var);
		Rec res = new Rec(var, visitAbstractVariant(r.body));
		this.bound.remove(var);
		return res;
	}

	private RecVar visitRecVar(RecVar v)
	{
		// NOTE: we might sanitize open types, so no boundedness check
		// if (!this.bound.contains(v))
		// {
		//	errors.add("Unbound linear types variable: " + v);
		// }
		
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
