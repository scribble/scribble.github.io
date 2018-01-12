package ast.linear;

import java.util.HashMap;

/** Naming environment for linear types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class NameEnv extends HashMap<AbstractVariant, String>
{
	private static final long serialVersionUID = 1L;
	
	public NameEnv()
	{
		super();
	}
}
