package ast.linear;

import java.util.Set;

import ast.name.RecVar;

/** Linear output type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class End implements Type
{
	public End()
	{
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return java.util.Collections.emptySet();
	}
	
	@Override
	public Type dual()
	{
		return this;
	}
	
	@Override 
	public String toString()
	{
		return "∅";
	}
	
	@Override
	public int hashCode()
	{
		return 997;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof End)
		{
			return true;
		}
		return false;
	}
}
