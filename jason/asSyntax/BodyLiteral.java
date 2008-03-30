package jason.asSyntax;

public interface BodyLiteral extends Term {

    public enum BodyType {
        none {            public String toString() { return ""; }},
        action {          public String toString() { return ""; }},
        internalAction {  public String toString() { return ""; }},
        achieve {         public String toString() { return "!"; }},
        test {            public String toString() { return "?"; }},
        addBel {          public String toString() { return "+"; }},
        delBel {          public String toString() { return "-"; }},
        delAddBel {       public String toString() { return "-+"; }},
        achieveNF {       public String toString() { return "!!"; }},
        constraint {      public String toString() { return ""; }}
    }

    public BodyType    getBodyType();
    public Term        getBodyTerm();
    public BodyLiteral getBodyNext();

    public boolean     isEmptyBody();
    public int         getPlanSize();

    public void setBodyType(BodyType bt);
    public void setBodyTerm(Term t);
    public void setBodyNext(BodyLiteral bl);
    
    
    public boolean add(BodyLiteral bl);
    public boolean add(int index, BodyLiteral bl);
    public Term removeBody(int index);    
}