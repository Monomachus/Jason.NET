package test;

import jade.lang.acl.ACLMessage;
import jason.infra.jade.JadeAg;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class JadeAgTest extends TestCase {

    public void testKQMLtoACL() {
        assertEquals(JadeAg.kqmlToACL("tell"), ACLMessage.INFORM);
        assertEquals(JadeAg.aclToKqml(JadeAg.kqmlToACL("tell")),"tell");
        
        assertEquals(JadeAg.aclToKqml(ACLMessage.CFP),"cfp");
        assertEquals(JadeAg.kqmlToACL(JadeAg.aclToKqml(ACLMessage.CFP)),ACLMessage.CFP);
    }

}
