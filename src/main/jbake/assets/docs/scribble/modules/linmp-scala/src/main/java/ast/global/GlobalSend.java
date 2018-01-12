package ast.global;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ast.name.Label;
import ast.name.RecVar;
import ast.name.Role;

public class GlobalSend implements GlobalType
{
	public final Role src;
	public final Role dest;
	public final Map<Label, GlobalSendCase> cases;
	
	public GlobalSend(Role src, Role dest, Map<Label, GlobalSendCase> cases)
	{
		this.src = src;
		this.dest = dest;
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
	public Set<Role> roles()
	{
		Set<Role> roles = cases.values().stream()
				.flatMap((v) -> v.body.roles().stream())
				.collect(Collectors.toSet());
		roles.addAll(java.util.Arrays.asList(src, dest));
		
		return roles;
	}
	
	@Override
	public String toString()
	{
		return this.src + "->" + this.dest + "{" +
				this.cases.entrySet().stream()
					.map((e) -> e.getKey().toString() + e.getValue().toString())
					.collect(Collectors.joining(", ")) + "}";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 29;
		int result = 1;
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		if (!(obj instanceof GlobalSend))
		{
			return false;
		}
		GlobalSend other = (GlobalSend) obj;
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
		if (src == null)
		{
			if (other.src != null)
			{
				return false;
			}
		} else if (!src.equals(other.src))
		{
			return false;
		}
		return true;
	}
}
