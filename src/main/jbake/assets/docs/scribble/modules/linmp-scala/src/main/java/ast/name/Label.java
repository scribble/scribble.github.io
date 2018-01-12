package ast.name;

public class Label extends NameNode implements Comparable<Label>
{
	public Label(String name)
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
		if (!(o instanceof Label))
		{
			return false;
		}
		return super.equals(o);
	}

	@Override
	public boolean canEqual(Object o)
	{
		return (o instanceof Label);
	}
	
	@Override
	public int hashCode()
	{
		int hash = 911;
		hash = 31 * hash + super.hashCode();
		return hash;
	}

	@Override
	public int compareTo(Label o)
	{
		return this.name.compareTo(o.name);
	}
}
