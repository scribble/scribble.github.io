package ast.global;

import ast.name.RecVar;
import ast.name.Role;
import java.util.Set;

public class GlobalEnd implements GlobalType
{
	public GlobalEnd()
	{

	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return new java.util.HashSet<RecVar>();
	}
	
	@Override
	public Set<Role> roles()
	{
		return new java.util.HashSet<Role>();
	}
	
	@Override 
	public String toString()
	{
		return "end";
	}
	
	@Override
	public int hashCode()
	{
		return 997;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof GlobalEnd)
		{
			return true;
		}
		return false;
	}
}
