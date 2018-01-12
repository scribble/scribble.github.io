/**
 * 
 */
package ast.local.ops;

import java.util.List;

import org.scribble.main.ScribbleException;

import ast.local.LocalNameEnv;
import ast.local.LocalType;
import ast.name.Role;
import ast.util.ClassTable;

/** Encoder of local session types into Scala classes.
 * 
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 */
public class ScalaEncoder
{
	public static String BINARY_CLASSES_NS = "binary";
	
	/** Encode a local type as Scala class declarations, based on {@code lchannels}.
	 * 
	 * @param t Local type to be encoded
	 * @param pkg Name of the package containing the class definitions
	 * @return A string containing the Scala code of the generated classes
	 * @throws ScribbleException in case of error
	 */
	public static String apply(LocalType t, String pkg) throws ScribbleException
	{
		return apply(t, pkg, ast.local.ops.DefaultNameEnvBuilder.apply(t));
	}
	
	/** Encode a local type as Scala class declarations, based on {@code lchannels}.
	 * 
	 * @param t Local type to be encoded
	 * @param pkg Name of the package containing the class definitions
	 * @param nameEnv Naming environment for local type
	 * @return A string containing the Scala code of the generated classes
	 * @throws ScribbleException in case of error
	 */
	public static String apply(LocalType t, String pkg, LocalNameEnv nameEnv) throws ScribbleException
	{
		ClassTable linProtoClasses = new ClassTable();
		
		// Note that roles is sorted
		List<Role> roles = new java.util.ArrayList<>(new java.util.TreeSet<>(t.roles()));
		
		// Pick roles in alphabetical order from channel tracker
		for (Role r: roles)
		{
			ast.linear.Type lt = t.linear(r);
			linProtoClasses.putAllIdem(ast.linear.ops.ScalaProtocolExtractor.apply(lt));
		}
		
		ClassTable mpProtoClasses = ScalaProtocolExtractor.apply(t, nameEnv);
		
		ClassTable msgInProtoClasses = ScalaMessageExtractor.inputs(t, nameEnv);
		
		ClassTable msgOutProtoClasses = ScalaMessageExtractor.outputs(t, nameEnv);
		
		return ("package " + pkg + "\n\n" +
				"import scala.concurrent.duration.Duration\n" +
				"import lchannels._\n\n" +
				"// Input message types for multiparty sessions\n" +
				String.join("\n", msgInProtoClasses.values()) +
				"\n\n// Output message types for multiparty sessions\n" +
				String.join("\n", msgOutProtoClasses.values()) +
				"\n\n// Multiparty session classes\n" +
				String.join("\n", mpProtoClasses.values()) +
				"\n\n// Classes representing messages (with continuations) in binary sessions\n" +
				"package object " + BINARY_CLASSES_NS + " {\n" +
				String.join("\n", linProtoClasses.values()) +
				"\n}\n"
				);
	}
}
