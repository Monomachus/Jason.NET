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
    }
    public PredicateIndicator(String prefix, PredicateIndicator pi) {
        this.functor = prefix + pi.functor;
        this.arity = pi.arity;
    }

    private void setStr() {
        asStr = functor + arity;        
    }

    public String getFunctor() {
        return functor;
    }
    
    public int getArity() {
        return arity;
    }
        
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o != null && o instanceof PredicateIndicator) {
            PredicateIndicator pi = (PredicateIndicator)o;
            return arity == pi.arity && functor.equals(pi.functor);
        } 
        return false;
    }
    
    @Override
    public int hashCode() {
        if (asStr == null) setStr();
        return asStr.hashCode();
    }

    public String toString() {
        return functor + "/" + arity;
    }
}
