package ast.local;

import java.util.HashMap;

/** Naming environment for local types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class LocalNameEnv extends HashMap<LocalType, String>
{
	private static final long serialVersionUID = 1L;
	
	public LocalNameEnv()
	{
		super();
	}
}
