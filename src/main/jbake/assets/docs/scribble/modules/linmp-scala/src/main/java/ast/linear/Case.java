package ast.linear;

import java.util.Set;

import ast.name.RecVar;

/** Tuple with payload as first element, and continuation as second element.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class Case
{
	public final Payload payload;
	public final Type cont;
	
	public Case(Payload payload, Type cont)
	{
		this.payload = payload;
		this.cont = cont;
	}
	
	public Set<RecVar> freeVariables()
	{
		// NOTE: here we assume that payloads are closed
		return cont.freeVariables();
	}
	
	@Override
	public String toString()
	{
		return "(" + payload + ", " + cont + ")";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cont == null) ? 0 : cont.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
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
		if (!(obj instanceof Case))
		{
			return false;
		}
		Case other = (Case) obj;
		if (cont == null)
		{
			if (other.cont != null)
			{
				return false;
			}
		} else if (!cont.equals(other.cont))
		{
			return false;
		}
		if (payload == null)
		{
			if (other.payload != null)
			{
				return false;
			}
		} else if (!payload.equals(other.payload))
		{
			return false;
		}
		return true;
	}
}
