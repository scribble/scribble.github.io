package ast.linear;

import java.util.Set;

import ast.name.RecVar;

/** Linear input type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class In implements Type
{
	public final AbstractVariant variant;
	
	public In(AbstractVariant v)
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
		return new Out(variant);
	}
	
	@Override 
	public String toString()
	{
		return "I(" + variant + ")";
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
		if (!(obj instanceof In))
		{
			return false;
		}
		In other = (In) obj;
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
