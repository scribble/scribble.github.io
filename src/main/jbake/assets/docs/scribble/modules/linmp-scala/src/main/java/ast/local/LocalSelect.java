package ast.local;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ast.name.Label;
import ast.name.RecVar;
import ast.name.Role;

public class LocalSelect implements LocalType
{
	//public final Role self;

	public final Role dest;
	public final Map<Label, LocalCase> cases;
	
	//public LocalSelect(Role self, Role dest, Map<MessageLab, LocalCase> cases)
	public LocalSelect(Role dest, Map<Label, LocalCase> cases)
	{
		//this.self = self;
		this.dest = dest;
		this.cases = Collections.unmodifiableMap(cases);
	}
	
	/**
	 * @return the labels of the choice
	 */
	public Set<Label> labels()
	{
		return cases.keySet();
	}
	
	@Override
	public Set<RecVar> freeVariables()
	{
		return cases.values().stream()
				.flatMap((v) -> v.body.freeVariables().stream())
				.collect(Collectors.toSet());
	}
	
	@Override
	public Set<Role> roles()
	{
		Set<Role> roles = cases.values().stream()
				.flatMap((v) -> v.body.roles().stream())
				.collect(Collectors.toSet());
		roles.add(dest);
		
		return roles;
	}
	
	// A ! { l1 : S1, l2 : S2, ... }
	@Override
	public String toString()
	{
		return this.dest + "⊕{" +
				this.cases.entrySet().stream()
					.map((e) -> e.getKey().toString() + e.getValue().toString())
					.collect(Collectors.joining(", ")) + "}";
	}

	@Override
	public int hashCode()
	{
		final int prime = 43;
		int result = 1;
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
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
		if (!(obj instanceof LocalSelect))
		{
			return false;
		}
		LocalSelect other = (LocalSelect) obj;
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
		if (dest == null)
		{
			if (other.dest != null)
			{
				return false;
			}
		} else if (!dest.equals(other.dest))
		{
			return false;
		}
		return true;
	}
}
