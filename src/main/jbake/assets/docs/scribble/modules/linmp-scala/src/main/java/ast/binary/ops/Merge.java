/**
 * 
 */
package ast.binary.ops;

import ast.binary.Type;
import ast.util.BiFunctionWithCE;

import org.scribble.main.ScribbleException;

/** Static methods for merging binary session types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Merge
{
	/** Type of all merge operators.
	 */
	public interface Operator extends BiFunctionWithCE<Type, Type, Type, ScribbleException>
	{
	}
	
	/** Merge two binary types iif they are equal.
	 * 
	 * @param t First type to merge
	 * @param u Second type to merge
	 * @return One of the two arguments
	 * @throws ScribbleException if the arguments are not equal
	 */
	public static Type id(Type t, Type u) throws ScribbleException
	{
		if (t.equals(u)) {
			return t;
		}
		throw new ScribbleException("Cannot merge non-equal types: "+t+" and "+u);
	}
	
	/** Merge two binary types using the full algorithm.
	 * 
	 * @param t First type to merge
	 * @param u Second type to merge
	 * @return One of the two arguments
	 * @throws ScribbleException if the arguments cannot be merged
	 */
	public static Type full(Type t, Type u) throws ScribbleException
	{
		return FullMerger.apply(t, u);
	}
}
