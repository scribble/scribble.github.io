package ast.binary;

import org.scribble.main.ScribbleException;

import ast.name.RecVar;

/** Visitor pattern for binary session type ASTs. 
 *  
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public abstract class Visitor<A> {
	/**
	 * @param gtype Global type to process
	 */
	protected abstract A process() throws ScribbleException;
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(End node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(Branch node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(Select node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(Rec node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(RecVar node);
	
	/** Default catch-all method with dynamic dispatching.
	 * 
	 * @param node Node being visited
	 */
	protected final A visit(Type node)
	{
		if (node instanceof End)
		{
			return visit((End)node);
		}
		if (node instanceof Branch)
		{
			return visit((Branch)node);
		}
		if (node instanceof Select)
		{
			return visit((Select)node);
		}
		if (node instanceof Rec)
		{
			return visit((Rec)node);
		}
		if (node instanceof RecVar) 
		{
			return visit((RecVar)node);
		}
		throw new RuntimeException("Unsupported node type: " + node);
	}
}