package ast.binary;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ast.name.Label;
import ast.name.RecVar;

public class Branch implements Type
{
	//public final Role self;
	
	public final Map<Label, Case> cases;
	
	//public LocalBranch(Role self, Role src, Map<MessageLab, LocalCase> cases)
	public Branch(Map<Label, Case> cases)
	{
		//this.self = self;
		this.cases = Collections.unmodifiableMap(cases);
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return cases.values().stream()
				.flatMap((v) -> v.body.freeVariables().stream())
				.collect(Collectors.toSet());
	}
	
	@Override
	public Type.Action action()
	{
		return Type.Action.input();
	}
	
	// A ? { l1 : S1, l2 : S2, ... }
	@Override
	public String toString()
	{
		return "&{" +
				this.cases.entrySet().stream()
					.map((e) -> "?" + e.getKey().toString() + e.getValue().toString())
					.collect(Collectors.joining(", ")) + "}";
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
		if (!(obj instanceof Branch))
		{
			return false;
		}
		Branch other = (Branch) obj;
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
