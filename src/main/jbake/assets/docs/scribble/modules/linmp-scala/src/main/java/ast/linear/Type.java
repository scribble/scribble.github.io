package ast.linear;

import java.util.Set;

import ast.name.RecVar;

/** Base for all linear types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public interface Type extends Payload
{	
	/**
	 * @return the free variables in the type.
	 */
	Set<RecVar> freeVariables();
	
	/**
	 * @return the dual of the type
	 */
	Type dual();
}
