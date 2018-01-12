package ast.binary;

import java.util.Set;

import ast.name.RecVar;

/** Base for all binary session types.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public interface Type
{
	/** Action of a session type (input or output).
	 */
	static class Action
	{
		public final boolean isInput;
		public final boolean isOutput;
		
		public static Action input() 
		{
			return new Action(true, false);
		}
		
		public static Action output() 
		{
			return new Action(false, true);
		}
		
		public static Action none() 
		{
			return new Action(false, false);
		}
		
		private Action(boolean isInput, boolean isOutput)
		{
			// Cannot be both an input and an output
			assert(!isInput || !isOutput);
			this.isInput = isInput;
			this.isOutput = isOutput;
		}
		
		public boolean isNone()
		{
			return (!isInput) && (!isOutput);
		}
		
		@Override
		public int hashCode()
		{
			return 977;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Action)
			{
				Action other = (Action)obj;
				if ((isInput == other.isInput) && (isOutput == other.isOutput)){
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * @return the free variables in the type.
	 */
	abstract public Set<RecVar> freeVariables();
	
	/**
	 * @return the top-level I/O action performed by the type.
	 */
	abstract public Action action();
}
