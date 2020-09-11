package jacamo.web.translation;

import static org.junit.Assert.*;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

import jacamo.web.WebTestUtils;

public class WebImplAgTest {
    
    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = WebTestUtils.launchRestSystem("src/test/test1.jcm");
    } 
    
    @Test
    public void testGetASLFiles() {
        System.out.println("\n\ntest001GetAgents");
        Response response;
        String rStr;
        
        // Testing ok from agents/
        response = client.target(uri.toString()).path("agents/bob/aslfiles")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/): " + rStr);
        assertTrue(rStr.contains("src/agt/bob.asl"));
        
        client.close();
    }

}
