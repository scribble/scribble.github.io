package ast.name;

public class Role extends NameNode implements Comparable<Role>
{
	public Role(String name)
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
		if (!(o instanceof Role))
		{
			return false;
		}
		return super.equals(o);
	}

	@Override
	public boolean canEqual(Object o)
	{
		return (o instanceof Role);
	}
	
	@Override
	public int hashCode()
	{
		int hash = 887;
		hash = 31 * hash + super.hashCode();
		return hash;
	}
	
	@Override
	public int compareTo(Role o)
	{
		return this.name.compareTo(o.name);
	}
}
