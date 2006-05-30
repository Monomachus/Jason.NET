package jason.asSyntax;

/**
 * Represents the "type" of a predicate, e.g.: ask/4
 * 
 * @author jomi
 */
public class PredicateIndicator {

    private String functor;
    private int    arity;
    private String asStr = null;

    public PredicateIndicator(String functor, int arity) {
        this.functor = functor;
        this.arity = arity;
        asStr = functor + "/" + arity;
    }
    public PredicateIndicator(String prefix, PredicateIndicator pi) {
        this.functor = prefix + pi.functor;
        this.arity = pi.arity;
        asStr = functor + "/" + arity;
    }

    public String getFunctor() {
        return functor;
    }
    
    public int getArity() {
        return arity;
    }
    
    public int hashCode() {
        return asStr.hashCode();
    }
    
    public boolean equals(Object o) {
        PredicateIndicator pi = (PredicateIndicator)o;
        return arity == pi.arity && functor.equals(pi.functor);
    }
    
    public String toString() {
        return asStr;
    }
}
