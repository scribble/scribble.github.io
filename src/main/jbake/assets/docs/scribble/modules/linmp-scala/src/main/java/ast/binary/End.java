package ast.binary;

import java.util.Set;

import ast.name.RecVar;

public class End implements Type
{
	//public final Role self;
	
	//public LocalEnd(Role self)
	public End()
	{
		//this.self = self;
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return new java.util.HashSet<RecVar>();
	}
	
	@Override
	public Type.Action action()
	{
		return Type.Action.none();
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
		if (obj instanceof End)
		{
			return true;
		}
		return false;
	}
}
