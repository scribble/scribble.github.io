package ast.local;

import java.util.Set;

import ast.name.RecVar;
import ast.name.Role;

public class LocalEnd implements LocalType
{
	//public final Role self;
	
	//public LocalEnd(Role self)
	public LocalEnd()
	{
		//this.self = self;
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
		return 977;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof LocalEnd)
		{
			return true;
		}
		return false;
	}
}
