package ast.local.ops;

import ast.local.LocalBranch;
import ast.local.LocalCase;
import ast.local.LocalEnd;
import ast.local.LocalRec;
import ast.local.LocalSelect;
import ast.local.LocalType;
import ast.local.LocalTypeVisitor;
import ast.name.Label;
import ast.name.RecVar;

import java.util.Map;

import org.scribble.main.ScribbleException;

/** Perform alpha-conversion on a local type AST.
 *
 *  @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class AlphaConverter extends LocalTypeVisitor<LocalType>
{
	private RecVar old; // Original variable
	private RecVar conv; // Alpha-converted variable
	private boolean open = false; // Becomes true when "old" is traversed
	private final LocalType ltype;
	
	/** Sanitize the given local type
	 * 
	 * @param g Local type to be alpha-converted
	 * @param old Old variable name
	 * @param conv New variable name
	 * @return An alpha-converted version of the given local type
	 */
	public static LocalType apply(LocalType g, RecVar old, RecVar conv) throws ScribbleException
	{
		AlphaConverter s = new AlphaConverter(g, old, conv);
		return s.process();
	}
	
	private AlphaConverter(LocalType g, RecVar old, RecVar conv)
	{
		ltype = g;
		this.old = old;
		this.conv = conv;
	}
	
	@Override
	public LocalType process() throws ScribbleException
	{
		return visit(ltype);
	}
	
	@Override
	protected LocalEnd visit(LocalEnd node)
	{
		return node;
	}
	
	@Override
	protected LocalBranch visit(LocalBranch node)
	{
		Map<Label, LocalCase> cases2 = new java.util.HashMap<>();
		for (Map.Entry<Label, LocalCase> x: node.cases.entrySet())
		{
			LocalCase c = x.getValue();
			cases2.put(x.getKey(), new LocalCase(c.pay, visit(c.body)));
		}
		
		return new LocalBranch(node.src, cases2);
	}
	
	@Override
	protected LocalSelect visit(LocalSelect node)
	{
		Map<Label, LocalCase> cases2 = new java.util.HashMap<>();
		for (Map.Entry<Label, LocalCase> x: node.cases.entrySet())
		{
			LocalCase c = x.getValue();
			cases2.put(x.getKey(), new LocalCase(c.pay, visit(c.body)));
		}
		
		return new LocalSelect(node.dest, cases2);
	}

	@Override
	protected LocalRec visit(LocalRec node)
	{
		if (node.recvar.equals(old))
		{
			if (open)
			{
			    // We have found a distinct occurrence of "old": don't convert
			    return node;
			}
			// We have found a top-level occurrence of "old": let's convert
			open = true; // Rememer that "old" is open in the recursion body
			LocalRec res = new LocalRec(conv, visit(node.body));
			open = false; // We are leaving the recursion body
			return res;
		}
		return new LocalRec(conv, visit(node.body));
	}

	@Override
	protected RecVar visit(RecVar node) {
		if ((node.equals(old)) && open) {
			return conv;
		}
		// Here we might also spot unbound occurrences of "old",
		// but we take care of them in other processing steps
		return node;
	}
}
