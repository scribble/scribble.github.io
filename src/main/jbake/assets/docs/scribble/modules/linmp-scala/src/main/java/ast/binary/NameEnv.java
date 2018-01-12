package ast.binary;

import java.util.HashMap;

/** Naming environment for partial (binary) types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class NameEnv extends HashMap<Type, String>
{
	private static final long serialVersionUID = 1L;
	
	public NameEnv()
	{
		super();
	}
}
