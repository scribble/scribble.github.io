package main;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.scribble.main.ScribbleException;
import org.scribble.sesstype.name.GProtocolName;
import org.scribble.util.ScribParserException;

import ast.ScribProtocolTranslator;
import ast.binary.Type;
import ast.global.GlobalType;
import ast.local.LocalType;
import ast.name.Role;

public class BinarySessionType
{
	public static void main(String[] args) throws ScribbleException, ScribParserException
	{
		Path mainmod = Paths.get(args[0]);
		
		if (args.length < 4)
		{
			throw new IllegalArgumentException("Required arguments: <FILE> <PROTO> <ROLE1> <ROLE2>");
		}
		String simpname = args[1]; // Protocol name
		Role role1 = new Role(args[2]);  // Which role to project from global type?
		Role role2 = new Role(args[3]);  // Which role to project from local type?
		
		ScribProtocolTranslator sbp = new ScribProtocolTranslator();
		GlobalType g = null;
		try
		{
			//g = sbp.parseAndCheck(mainmod, proto);
			g = sbp.parseAndCheck(Main.newMainContext(null, mainmod), new GProtocolName(simpname), ast.local.ops.Merge::full);
			//System.out.println("Translated:\n" + "    " + g);
		}
		catch (ScribParserException | ScribbleException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		GlobalType gs = ast.global.ops.Sanitizer.apply(g);
		//System.out.println("\nSanitized:\n" + "    " + gs);
		
		LocalType lt = ast.global.ops.Projector.apply(gs, role1, ast.local.ops.Merge::full);
		Type pt = ast.local.ops.Projector.apply(lt, role2, ast.binary.ops.Merge::full);
		
		System.out.println(pt);
	}
}
