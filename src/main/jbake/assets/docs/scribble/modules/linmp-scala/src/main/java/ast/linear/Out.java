package ast.linear;

import java.util.Set;

import ast.name.RecVar;

/** Linear output type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Out implements Type
{
	public final AbstractVariant variant;
	
	public Out(AbstractVariant v)
	{
		variant = v;
	}
	
	/**
	 * @return the variant carried by the type
	 */
	public AbstractVariant carried()
	{
		return variant;
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return variant.freeVariables();
	}
	
	@Override
	public Type dual()
	{
		return new In(variant);
	}
	
	@Override 
	public String toString()
	{
		return "O(" + variant + ")";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variant == null) ? 0 : variant.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Out))
		{
			return false;
		}
		Out other = (Out) obj;
		if (variant == null)
		{
			if (other.variant != null)
			{
				return false;
			}
		} else if (!variant.equals(other.variant))
		{
			return false;
		}
		return true;
	}
}
