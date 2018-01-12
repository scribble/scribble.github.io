package ast.linear.ops;

import java.util.Set;
import java.util.stream.Collectors;

import org.scribble.main.ScribbleException;

import ast.linear.AbstractVariant;
import ast.linear.Case;
import ast.linear.End;
import ast.linear.In;
import ast.linear.NameEnv;
import ast.linear.Out;
import ast.linear.Rec;
import ast.linear.Record;
import ast.linear.Type;
import ast.linear.Variant;
import ast.linear.Visitor;
import ast.name.Label;
import ast.name.RecVar;

/** Build for a standard naming environment which is suitable for a given
 * linear type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class DefaultNameEnvBuilder extends Visitor<NameEnv>
{
	private final Type visiting;
	
	/**
	 * @param t linear type to build the naming environment for
	 * @return a naming environment suitable for {@code t}
	 * @throws ScribbleException in case of errors (e.g., ill-formedness of {@code t})
	 */
	public static NameEnv apply(Type t) throws ScribbleException
	{
		DefaultNameEnvBuilder b = new DefaultNameEnvBuilder(t);
		
		return b.process();
	}
	
	private DefaultNameEnvBuilder(Type t)
	{
		visiting = t;
	}
	
	@Override
	protected NameEnv process() throws ScribbleException
	{
		return visit(visiting);
	}
	
	@Override
	protected NameEnv visit(End node)
	{
		return new NameEnv();
	}

	@Override
	protected NameEnv visit(In node)
	{
		return visit(node.variant);
	}

	@Override
	protected NameEnv visit(Out node)
	{
		return visit(node.variant);
	}
	
	private NameEnv visit(AbstractVariant v)
	{
		// FIXME: what about adding an AbstractVariant visitor?
		if (v instanceof Variant)
		{
			Variant vrnt = (Variant)v;
			NameEnv res = new NameEnv();
			res.put(v, nameChoiceFromLabels(vrnt.cases.keySet()));
			
			for (Case c: vrnt.cases.values())
			{
				if (c.payload instanceof Record)
				{
					Record rec = (Record)c.payload;
					for (Type lt: rec.values())
					{
						res.putAll(visit(lt));
					}
				}
				else if (c.payload instanceof ast.name.BaseType)
				{
					// Nothing to do
				}
				else
				{
					throw new RuntimeException("BUG: unsupported payload type " + c.payload);
				}
				res.putAll(visit(c.cont));
			}
			return res;
		}
		else if (v instanceof Rec)
		{
			AbstractVariant body = ((Rec)v).body;
			NameEnv res = visit(body);
			// Ensure that the recursive variant, its variable and its body
			// are mapped to the same name 
			res.put(v, res.get(body));
			res.put(((Rec)v).recvar, res.get(body));
			return res;
		}
		else if (v instanceof RecVar)
		{
			// The RecVar is associated to a name in the Rec case above
			return new NameEnv();
		}
		else
		{
			throw new RuntimeException("BUG: unsupported variant type " + v);
		}
	}
	
	/** Give a name to a choice among the given set of labels.
	 *
	 * @param labels Label sto chose from
	 * @return a name for the given choice
	 */
	public static String nameChoiceFromLabels(Set<Label> labels)
	{
		// First sort the labels
		java.util.List<String> ls = new java.util.ArrayList<>(new java.util.TreeSet<String>(labels.stream().map(l -> l.name).collect(Collectors.toSet())));
		String base = ls.remove(0);
		return ls.stream().reduce(base, (l1, l2) -> l1 + "Or" + l2);
	}
}
