package ast.linear;

import org.scribble.main.ScribbleException;

/** Visitor pattern for linear type ASTs. 
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
	protected abstract A visit(In node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(Out node);
	
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
		if (node instanceof In)
		{
			return visit((In)node);
		}
		if (node instanceof Out)
		{
			return visit((Out)node);
		}
		throw new RuntimeException("Unsupported node type: " + node);
	}
}