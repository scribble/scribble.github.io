package ast.linear.ops;

import java.util.Collection;
import java.util.Map;

import org.scribble.main.ScribbleException;

import ast.linear.AbstractVariant;
import ast.linear.End;
import ast.linear.In;
import ast.linear.NameEnv;
import ast.linear.Out;
import ast.linear.Type;
import ast.linear.Visitor;

/** Build the Scala {@code lchannels} type corresponding to a linear I/O type.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class ScalaChannelTypeExtractor extends Visitor<String>
{
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final Type visiting;
	private Map<AbstractVariant, String> nameEnv;
	private final String carriedPrefix;
	
	public static String apply(Type t) throws ScribbleException
	{
		return apply(t, DefaultNameEnvBuilder.apply(t));
	}
	
	protected static String apply(Type t, NameEnv nameEnv) throws ScribbleException
	{
		return apply(t, nameEnv, "");
	}
	
	public static String apply(Type t, NameEnv nameEnv, String carriedPrefix) throws ScribbleException
	{
		ScalaChannelTypeExtractor te = new ScalaChannelTypeExtractor(t, nameEnv, carriedPrefix);
		
		return te.process();
	}
	
	private ScalaChannelTypeExtractor(Type t, NameEnv nameEnv, String carriedPrefix)
	{
		this.visiting = t;
		this.nameEnv = nameEnv;
		this.carriedPrefix = carriedPrefix;
	}
	
	@Override
	protected String process() throws ScribbleException
	{
		String res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) extracting channel type of " + visiting + ": "
				                    + String.join("; ", errors));
	}
	
	@Override
	protected String visit(End node)
	{
		return "Unit";
	}

	@Override
	protected String visit(In node)
	{
		String carried = nameEnv.get(node.variant);
		assert(carried != null);
		return "In[" + carriedPrefix + carried + "]";
	}

	@Override
	protected String visit(Out node)
	{
		String carried = nameEnv.get(node.variant);
		assert(carried != null);
		return "Out[" + carriedPrefix + carried + "]";
	}

}
