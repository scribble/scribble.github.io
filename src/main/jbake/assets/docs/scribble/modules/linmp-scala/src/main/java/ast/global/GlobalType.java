package ast.global;

import ast.name.RecVar;
import ast.name.Role;
import java.util.Set;

public interface GlobalType
{
	/**
	 * @return the free variables in the type.
	 */
	public Set<RecVar> freeVariables();
	
	/**
	 * @return the roles involved in the type.
	 */
	public Set<Role> roles();
}
