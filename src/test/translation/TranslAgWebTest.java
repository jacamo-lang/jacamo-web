package translation;

import static org.junit.Assert.*;

import org.junit.Test;

import jacamo.web.mediation.TranslAgWeb;

public class TranslAgWebTest {
    
    @Test
    public void testGetAgentName() {
        //fail("Not yet implemented");
        TranslAgWeb tAg = new TranslAgWeb();
        
        assertEquals("astar", tAg.getAgentName("./src/test/search/astar"));
        assertEquals("goto", tAg.getAgentName("walking/goto"));
        assertEquals("bob", tAg.getAgentName("bob.asl"));
        assertEquals("alice", tAg.getAgentName("/src/test/alice.asl"));
        assertEquals("bob_1", tAg.getAgentName ("http://acme.com/bob_1"));
        assertEquals("alice", tAg.getAgentName ("http://acme.com/alice.asl"));
    }
    
    @Test
    public void testGetFormattedURI() {
        //fail("Not yet implemented");
        TranslAgWeb tAg = new TranslAgWeb();
        
        assertEquals("./src/test/search/astar.asl", tAg.getFormattedURI("./src/test/search/astar"));
        assertEquals("walking/goto.asl", tAg.getFormattedURI("walking/goto"));
        assertEquals("bob.asl", tAg.getFormattedURI("bob.asl"));
        assertEquals("/src/test/alice.asl", tAg.getFormattedURI ("%2Fsrc%2Ftest%2Falice.asl"));
        assertEquals("http://acme.com/bob_1.asl", tAg.getFormattedURI ("http://acme.com/bob_1"));
        assertEquals("http://acme.com/alice.asl", tAg.getFormattedURI ("http://acme.com/alice.asl"));
    }

}
