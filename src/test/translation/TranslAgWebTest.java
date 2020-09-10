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
    }
    
    @Test
    public void testGetFullpath() {
        //fail("Not yet implemented");
        TranslAgWeb tAg = new TranslAgWeb();
        
        assertEquals("./src/test/search/", tAg.getFullpath("./src/test/search/astar"));
        assertEquals("./src/agt/walking/", tAg.getFullpath("walking/goto"));
        assertEquals("./src/agt/", tAg.getFullpath("bob.asl"));
        assertEquals("./src/test/", tAg.getFullpath ("/src/test/alice.asl"));
    }
}
