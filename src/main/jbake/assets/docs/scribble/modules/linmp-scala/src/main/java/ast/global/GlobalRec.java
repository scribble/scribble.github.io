package ast.global;

import java.util.Set;

import ast.name.RecVar;
import ast.name.Role;

public class GlobalRec implements GlobalType
{
	public final RecVar recvar;
	public final GlobalType body;
	
	public GlobalRec(RecVar recvar, GlobalType body)
	{
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
	public Set<Role> roles()
	{
		return body.roles();
	}
	
	@Override
	public String toString()
	{
		return "μ(" + this.recvar + ")." + this.body;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
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
		if (!(obj instanceof GlobalRec))
		{
			return false;
		}
		GlobalRec other = (GlobalRec) obj;
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
