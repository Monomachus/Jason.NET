package jason.asSyntax;

/**
 * Represents the "type" of a predicate, e.g.: ask/4
 * 
 * @author jomi
 */
public class PredicateIndicator {

    private String functor;
    private int    arity;
    private int    hash = 1;
    
    public PredicateIndicator(String functor, int arity) {
        this.functor = functor;
        this.arity = arity;
        calcHash();
    }
    public PredicateIndicator(String prefix, PredicateIndicator pi) {
        this.functor = prefix + pi.functor;
        this.arity = pi.arity;
        calcHash();
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
        if (o != null && o instanceof PredicateIndicator && o.hashCode() == this.hashCode()) {
            final PredicateIndicator pi = (PredicateIndicator)o;
            return arity == pi.arity && functor.equals(pi.functor);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
    private void calcHash() {
        final int PRIME = 31;
        hash = PRIME * hash + arity;
        if (functor != null) {
            hash = PRIME * hash + functor.hashCode();
        }
    }
      
    public String toString() {
        return functor + "/" + arity;
    }
}
