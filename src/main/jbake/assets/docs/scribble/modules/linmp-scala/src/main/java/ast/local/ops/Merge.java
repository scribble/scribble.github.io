/**
 * 
 */
package ast.local.ops;

import ast.local.LocalType;
import ast.util.BiFunctionWithCE;

import org.scribble.main.ScribbleException;

/** Static methods for merging local types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Merge
{
	/** Type of all merge operators.
	 */
	public interface Operator extends BiFunctionWithCE<LocalType, LocalType, LocalType, ScribbleException>
	{
	}
	
	/** Merge two local types iif they are equal.
	 * 
	 * @param t First type to merge
	 * @param u Second type to merge
	 * @return One of the two arguments
	 * @throws ScribbleException if the arguments are not equal
	 */
	public static LocalType id(LocalType t, LocalType u) throws ScribbleException
	{
		if (t.equals(u)) {
			return t;
		}
		throw new ScribbleException("Cannot merge non-equal types: "+t+" and "+u);
	}
	
	/** Merge two local types using the full algorithm.
	 * 
	 * @param t First type to merge
	 * @param u Second type to merge
	 * @return One of the two arguments
	 * @throws ScribbleException if the arguments cannot be merged
	 */
	public static LocalType full(LocalType t, LocalType u) throws ScribbleException
	{
		return FullMerger.apply(t, u);
	}
}
