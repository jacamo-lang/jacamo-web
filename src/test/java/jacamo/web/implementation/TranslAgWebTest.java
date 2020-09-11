package jacamo.web.implementation;

import static org.junit.Assert.*;

import org.junit.Test;

import jacamo.web.mediation.TranslAgWeb;

public class TranslAgWebTest {
    
    @Test
    public void testGetAgentName() {
        System.out.println("testGetAgentName");
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
        System.out.println("testGetFormattedURI");
        TranslAgWeb tAg = new TranslAgWeb();
        
        assertEquals("./src/test/search/astar.asl", tAg.getFormattedURI("./src/test/search/astar"));
        assertEquals("walking/goto.asl", tAg.getFormattedURI("walking/goto"));
        assertEquals("src/agt/bob.asl", tAg.getFormattedURI("bob.asl"));
        assertEquals("/src/test/alice.asl", tAg.getFormattedURI("%2Fsrc%2Ftest%2Falice.asl"));
        assertEquals("http://acme.com/bob_1.asl", tAg.getFormattedURI("http://acme.com/bob_1"));
        assertEquals("http://acme.com/alice.asl", tAg.getFormattedURI("http://acme.com/alice.asl"));
    }

    @Test
    public void testGetFileBuffer() {
        System.out.println("testGetFileBuffer");
        TranslAgWeb tAg = new TranslAgWeb();
        
        assertNotNull(tAg.getFileBuffer("marcos"));
        assertNull(tAg.getFileBuffer("src/agt/search/astar"));
        assertNull(tAg.getFileBuffer("./src/agt/search/astar"));
        assertNull(tAg.getFileBuffer("search/astar"));
        assertNotNull(tAg.getFileBuffer("bob.asl"));
        assertNull(tAg.getFileBuffer("%2Fsrc%2Ftest%2Falice.asl"));
        assertNotNull(tAg.getFileBuffer("https://raw.githubusercontent.com/jacamo-lang/jacamo-web/master/examples/bob-mary-alice/src/agt/one.asl"));
        assertNotNull(tAg.getFileBuffer("https://raw.githubusercontent.com/jacamo-lang/jacamo-web/master/examples/bob-mary-alice/src/agt/alice"));
    }
    
    
}
