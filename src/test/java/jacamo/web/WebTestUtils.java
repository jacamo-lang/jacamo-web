package jacamo.web;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import jacamo.rest.JCMRest;

public class WebTestUtils extends jacamo.util.TestUtils {
    protected static URI uri = null;
    
    public static URI launchRestSystem(String jcm) {
        if (launchSystem(jcm)) {
            uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
        }
        try {
            int i = 100;
            while (i-- > 0) {
                Thread.sleep(100);
                Client client = ClientBuilder.newClient();
                Response response = client.target(uri.toString()).path("agents/bob/aslfiles")
                        .request(MediaType.APPLICATION_JSON).get();
                if (response.readEntity(String.class).toString().contains("bob")) 
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static void stopRestSystem() {
        stopSystem();
    }
}
