package ast.local.ops;

import java.util.Collection;
import java.util.Map;

import org.scribble.main.ScribbleException;

import ast.local.LocalBranch;
import ast.local.LocalCase;
import ast.local.LocalEnd;
import ast.local.LocalNameEnv;
import ast.local.LocalRec;
import ast.local.LocalSelect;
import ast.local.LocalType;
import ast.local.LocalTypeVisitor;
import ast.name.Label;
import ast.name.RecVar;

public class DefaultNameEnvBuilder extends LocalTypeVisitor<LocalNameEnv>
{
	public static String MULTIPARTY_CLASSES_PREFIX = "MP";
	
	private Collection<String> errors = new java.util.LinkedList<String>();
	private final LocalType visiting;
	
	public static LocalNameEnv apply(LocalType t) throws ScribbleException
	{
		DefaultNameEnvBuilder b = new DefaultNameEnvBuilder(t);
		
		return b.process();
	}
	
	private DefaultNameEnvBuilder(LocalType t)
	{
		visiting = t;
	}
	
	@Override
	protected LocalNameEnv process() throws ScribbleException
	{
		LocalNameEnv res = visit(visiting);
		if (errors.isEmpty())
		{
			return res;
		}
		throw new ScribbleException("Error(s) assigning names to " + visiting + ": "
				                    + String.join("; ", errors));
	}

	@Override
	protected LocalNameEnv visit(LocalEnd node)
	{
		return new LocalNameEnv();
	}

	@Override
	protected LocalNameEnv visit(LocalBranch node)
	{
		LocalNameEnv res = new LocalNameEnv();
		res.put(node, MULTIPARTY_CLASSES_PREFIX + ast.linear.ops.DefaultNameEnvBuilder.nameChoiceFromLabels(node.cases.keySet()));
		res.putAll(visit(node.cases));
		return res;
	}

	@Override
	protected LocalNameEnv visit(LocalSelect node)
	{
		LocalNameEnv res = new LocalNameEnv();
		res.put(node, MULTIPARTY_CLASSES_PREFIX + ast.linear.ops.DefaultNameEnvBuilder.nameChoiceFromLabels(node.cases.keySet()));
		res.putAll(visit(node.cases));
		return res;
	}

	private LocalNameEnv visit(Map<Label, LocalCase> cases)
	{
		LocalNameEnv res = new LocalNameEnv();
		for (LocalCase c: cases.values())
		{
			if (c.pay instanceof LocalType)
			{
				res.putAll(visit((LocalType)c.pay));
			}
			res.putAll(visit(c.body));
		}
		return res;
	}
	
	@Override
	protected LocalNameEnv visit(LocalRec node)
	{
		LocalType body = node.body;
		LocalNameEnv res = visit(body);
		// Ensure that the recursive type, its variable and its body
		// are mapped to the same name
		res.put(node, res.get(body));
		res.put(node.recvar, res.get(body));
		return res;
	}

	@Override
	protected LocalNameEnv visit(RecVar node)
	{
		// The RecVar is associated to a name in the Rec case above
		return new LocalNameEnv();
	}
	
	
}
