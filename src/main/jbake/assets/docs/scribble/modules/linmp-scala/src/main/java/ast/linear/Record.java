package ast.linear;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import ast.local.LocalType;
import ast.name.Role;

public class Record extends HashMap<Role, Type> implements Payload
{
	private static final long serialVersionUID = 1L;
	
	// The local type this record was originated from
	public final LocalType origin;
		
	public Record(LocalType origin, Map<Role, Type> types)
	{
		super(types);
		this.origin = origin;
	}
	
	public Set<Role> roles()
	{
		return this.keySet();
	}
	
	@Override 
	public String toString()
	{
		return "{" + super.toString() + "}";
	}
}
