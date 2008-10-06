package jason.asSyntax;

/** The interface for string terms of the AgentSpeak language
 *  
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface StringTerm extends Term {
    public String getString();
    public int length();
}
