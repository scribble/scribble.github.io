package ast.name;

import ast.PayloadType;

public class BaseType extends NameNode implements PayloadType, ast.linear.Payload
{
	public BaseType(String name)
	{
		super(name);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof BaseType))
		{
			return false;
		}
		return super.equals(o);
	}
	
	@Override
	public boolean canEqual(Object o)
	{
		return (o instanceof BaseType);
	}
	
	@Override
	public int hashCode()
	{
		int hash = 907;
		hash = 31 * hash + super.hashCode();
		return hash;
	}
}
