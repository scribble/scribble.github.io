package ast.local;

import java.util.Map;
import java.util.Set;

import ast.PayloadType;
import ast.linear.Record;
import ast.name.RecVar;
import ast.name.Role;

import org.scribble.main.ScribbleException;

public interface LocalType extends PayloadType
{
	/**
	 * @return the free variables in the type.
	 */
	Set<RecVar> freeVariables();
	
	/**
	 * @return the roles involved in the type.
	 */
	Set<Role> roles();
	
	/** Project the local type onto the given role,
	 * returning the binary ("partial") type.
	 * 
	 * @param r Role to project
	 * @return the binary type representing the projection
	 * @throws ScribbleException in case of error
	 */
	default ast.binary.Type partial(Role r) throws ScribbleException
	{
		return ast.local.ops.Projector.apply(this, r, ast.binary.ops.Merge::full);
	}
	
	/** Project the local type into a mapping from roles to partial (binary)
	 * types.
	 * 
	 * @return the mapping from roles to partial types representing the projection
	 * @throws ScribbleException in case of error
	 */
	default Map<Role, ast.binary.Type> partial() throws ScribbleException
	{
		Map<Role, ast.binary.Type> types = new java.util.HashMap<>();
		for (Role r: roles())
		{
			types.put(r, partial(r));
		}
		return types;
	}
	
	/** Project the local type onto the given role,
	 * returning the linear encoding of the resulting binary type type.
	 * 
	 * @param r Role to project
	 * @return the linear type representing the projection
	 * @throws ScribbleException in case of error
	 */
	default ast.linear.Type linear(Role r) throws ScribbleException
	{
		return ast.binary.ops.LinearEncoder.apply(partial(r));
	}
	
	/** Project the local type into a record of linear types (one per role).
	 * 
	 * @return the record of linear types representing the projection
	 * @throws ScribbleException in case of error
	 */
	default Record linear() throws ScribbleException
	{
		Map<Role, ast.linear.Type> types = new java.util.HashMap<>();
		for (Role r: roles())
		{
			types.put(r, linear(r));
		}
		return new Record(this, types);
	}
}
