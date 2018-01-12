package ast.util;

import java.util.LinkedHashMap;

/** Class table mapping names to definitions
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class ClassTable extends LinkedHashMap<String, String>
{
	/** Create an empty class table.
	 */
	public ClassTable()
	{
		super();
	}
	
	/** Merge two class tables into a new one, checking that overlapping keys
	 * map to the same value.
	 * 
	 * @throws RuntimeException if some keys have mismatching values
	 */
	public static ClassTable merge(ClassTable ct1, ClassTable ct2) throws RuntimeException
	{
		ClassTable res = new ClassTable();
		
		res.putAllIdem(ct1);
		res.putAllIdem(ct2);
		
		return res;
	}
	
	/** If the specified class is not already associated with a value, associates
	 * it with the given value and returns null, else returns the current value.
	 * This method ensures that, if the key was already associated, the old
	 * and new value are the same.
	 *  
	 * @throws RuntimeException if old value is non-null and differs from new
	 */
	public String putIdem(String cls, String def) throws RuntimeException
	{
		String old = this.putIfAbsent(cls, def);
		if ((old != null) && (!old.equals(def)))
		{
			throw new RuntimeException("Trying to update class definition for "
					+ cls + " with incompatible definition. Old:\n"
					+ old + "\nNew:\n" + def);
		}
		return old;
	}
	
	/** Copies all of the mappings from the specified map to this map, but
	 * ensuring that overlapping keys map to the same value.
	 * 
	 * @param ct Class table to be copied
	 * @throws RuntimeException if some keys have mismatching values
	 */
	public void putAllIdem(ClassTable ct) throws RuntimeException
	{
		for (String cls: ct.keySet())
		{
			this.putIdem(cls, ct.get(cls));
		}
	}
	
	private static final long serialVersionUID = -7942624388400541024L;
}
