package ast.global;

import ast.name.RecVar;
import org.scribble.main.ScribbleException;

/** Visitor pattern for global type ASTs. 
 *  
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public abstract class GlobalTypeVisitor<A> {
	/**
	 * @param gtype Global type to process
	 */
	protected abstract A process() throws ScribbleException;
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(GlobalEnd node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(GlobalSend node);
		
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(GlobalRec node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(RecVar node);
	
	/** Default catch-all method with dynamic dispatching.
	 * 
	 * @param node Node being visited
	 */
	protected final A visit(GlobalType node)
	{
		if (node instanceof GlobalEnd)
		{
			return visit((GlobalEnd)node);
		}
		if (node instanceof GlobalSend)
		{
			return visit((GlobalSend)node);
		}
		if (node instanceof GlobalRec)
		{
			return visit((GlobalRec)node);
		}
		if (node instanceof RecVar) 
		{
			return visit((RecVar)node);
		}
		throw new RuntimeException("Unsupported node type: " + node);
	}
}
