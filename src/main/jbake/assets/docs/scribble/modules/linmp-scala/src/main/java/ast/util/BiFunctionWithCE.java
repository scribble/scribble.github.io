package ast.util;

/** Function-like object that could throw a checked exception.
 * 
 * Inspired by: "Throwing Checked Exceptions From Lambdas"
 * by Heinz M. Kabutz 
 * 
 * @see Heinz M. Kabutz. <a href="http://www.javaspecialists.eu/archive/Issue221.html">Throwing Checked Exceptions From Lambdas</a>. 2014
 * @author Alceste Scalas <alceste.scalas@imperial.ac.uk>
 *
 * @param <T> Type of first function argument
 * @param <U> Type of second function argument
 * @param <R> Return type of the function
 * @param <X> Type of exception that could be thrown
 */
@FunctionalInterface
public interface BiFunctionWithCE<T, U, R, X extends Exception> {
  R apply(T t, U u) throws X;
}
