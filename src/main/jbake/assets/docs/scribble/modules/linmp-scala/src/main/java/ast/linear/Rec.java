package ast.linear;

import java.util.Set;

import ast.name.Label;
import ast.name.RecVar;

public class Rec implements AbstractVariant
{
	public final RecVar recvar;
	public final AbstractVariant body;
	
	public Rec(RecVar recvar, AbstractVariant body)
	{
		this.recvar = recvar;
		this.body = body;
	}
	
	@Override
	public Set<Label> labels()
	{
		return body.labels();
	}
	
	@Override
	public Payload payload(Label l)
	{
		return body.payload(l);
	}
	
	@Override
	public Type continuation(Label l)
	{
		return body.continuation(l);
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		Set<RecVar> bfv = body.freeVariables();
		bfv.remove(recvar); // Remove bound variable from free vars in body
		return bfv;
	}
	
	@Override
	public String toString()
	{
		return "mu " + recvar + "." + body;
	}

	@Override
	public int hashCode()
	{
		final int prime = 37;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((recvar == null) ? 0 : recvar.hashCode());
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
		if (!(obj instanceof Rec))
		{
			return false;
		}
		Rec other = (Rec) obj;
		if (body == null)
		{
			if (other.body != null)
			{
				return false;
			}
		} else if (!body.equals(other.body))
		{
			return false;
		}
		if (recvar == null)
		{
			if (other.recvar != null)
			{
				return false;
			}
		} else if (!recvar.equals(other.recvar))
		{
			return false;
		}
		return true;
	}
}
