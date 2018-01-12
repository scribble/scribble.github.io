package ast.local;

import org.scribble.main.ScribbleException;

import ast.name.RecVar;

/** Visitor pattern for local type ASTs. 
 *  
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public abstract class LocalTypeVisitor<A> {
	/**
	 * @param gtype Global type to process
	 */
	protected abstract A process() throws ScribbleException;
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(LocalEnd node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(LocalBranch node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(LocalSelect node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(LocalRec node);
	
	/**
	 * @param node Node being visited
	 */
	protected abstract A visit(RecVar node);
	
	/** Default catch-all method with dynamic dispatching.
	 * 
	 * @param node Node being visited
	 */
	protected final A visit(LocalType node)
	{
		if (node instanceof LocalEnd)
		{
			return visit((LocalEnd)node);
		}
		if (node instanceof LocalBranch)
		{
			return visit((LocalBranch)node);
		}
		if (node instanceof LocalSelect)
		{
			return visit((LocalSelect)node);
		}
		if (node instanceof LocalRec)
		{
			return visit((LocalRec)node);
		}
		if (node instanceof RecVar) 
		{
			return visit((RecVar)node);
		}
		throw new RuntimeException("Unsupported node type: " + node);
	}
}
