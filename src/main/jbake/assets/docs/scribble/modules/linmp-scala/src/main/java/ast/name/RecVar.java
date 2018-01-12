package ast.name;

import java.util.Set;

import ast.binary.Type;
import ast.global.GlobalType;
import ast.linear.AbstractVariant;
import ast.linear.Payload;
import ast.local.LocalType;

public class RecVar extends NameNode implements GlobalType, LocalType, Type, AbstractVariant
{
	public RecVar(String name)
	{
		super(name);
	}
	
	@Override
	public Set<Label> labels()
	{
		throw new RuntimeException("BUG: trying to get the labels of " + this);
	}
	
	@Override
	public Payload payload(Label l)
	{
		throw new RuntimeException("BUG: trying to get the payload of " + this);
	}
	
	@Override
	public ast.linear.Type continuation(Label l)
	{
		throw new RuntimeException("BUG: trying to get the continuation of " + this);
	}



	@Override
	public Set<RecVar> freeVariables()
	{
		return java.util.Collections.singleton(this);
	}
	
	@Override
	public Set<Role> roles()
	{
		return java.util.Collections.emptySet();
	}
	
	@Override
	public Type.Action action()
	{
		return Type.Action.input();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof RecVar))
		{
			return false;
		}
		return super.equals(o);
	}
	
	@Override
	public boolean canEqual(Object o)
	{
		return (o instanceof RecVar);
	}
	
	@Override
	public int hashCode()
	{
		int hash = 883;
		hash = 31 * hash + super.hashCode();
		return hash;
	}
}
