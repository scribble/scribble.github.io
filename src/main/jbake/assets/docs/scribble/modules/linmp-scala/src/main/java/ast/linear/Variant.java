package ast.linear;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ast.name.Label;
import ast.name.RecVar;

public class Variant implements AbstractVariant
{
	public final Map<Label, Case> cases;
	
	public Variant(Map<Label, Case> cases)
	{
		assert(cases.size() != 0);
		this.cases = cases;
	}
	
	@Override
	public Set<Label> labels()
	{
		return cases.keySet();
	}
	
	@Override
	public Payload payload(Label l)
	{
		return cases.get(l).payload;
	}
	
	@Override
	public Type continuation(Label l)
	{
		return cases.get(l).cont;
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return cases.values().stream()
				.flatMap((v) -> v.cont.freeVariables().stream())
				.collect(Collectors.toSet());
	}
	
	@Override 
	public String toString()
	{
		return cases.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
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
		if (!(obj instanceof Variant))
		{
			return false;
		}
		Variant other = (Variant) obj;
		if (cases == null)
		{
			if (other.cases != null)
			{
				return false;
			}
		} else if (!cases.equals(other.cases))
		{
			return false;
		}
		return true;
	}
}
