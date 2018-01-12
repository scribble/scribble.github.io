package ast.linear;

import java.util.Set;

import ast.name.Label;
import ast.name.RecVar;

/** Base class for (possibly recursive) variant types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public interface AbstractVariant
{
	/**
	 * @return the labels of the variant
	 */
	Set<Label> labels();
	
	/**
	 * @return the payload type carried by the variant for the given label.
	 */
	Payload payload(Label l);
	
	/**
	 * @return the continuation type carried by the variant for the given label.
	 */
	Type continuation(Label l);
	
	/**
	 * @return the free variables in the type.
	 */
	Set<RecVar> freeVariables();
}
