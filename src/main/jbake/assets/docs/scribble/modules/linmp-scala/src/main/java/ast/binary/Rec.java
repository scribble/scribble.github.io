package ast.binary;

import java.util.Set;

import ast.name.RecVar;

public class Rec implements Type
{
	//public final Role self;
	
	public final RecVar recvar;
	public final Type body;
	
	//public LocalRec(Role self, RecVar recvar, LocalType body)
	public Rec(RecVar recvar, Type body)
	{
		//this.self = self;
		this.recvar = recvar;
		this.body = body;
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		Set<RecVar> res = body.freeVariables();
		res.remove(recvar);
		return res;
	}
	
	@Override
	public Type.Action action()
	{
		return body.action();
	}
	
	@Override
	public String toString()
	{
		return "μ" + this.recvar + ".(" + this.body + ")";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 41;
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
