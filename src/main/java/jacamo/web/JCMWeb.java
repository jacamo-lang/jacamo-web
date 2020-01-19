package jacamo.web;

import java.net.InetAddress;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import jacamo.rest.JCMRest;


public class JCMWeb extends JCMRest {


    @Override
    public HttpServer startRestServer(int port) {
        try {
            restServerURI = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
            
            // registering resources
            WebAppConfig rc = new WebAppConfig();
            
            // get a server from factory
            HttpServer s = GrizzlyHttpServerFactory.createHttpServer(restServerURI, rc);
            
            // other possiblecontainers:
            System.out.println("jacamo-web Rest API is running on "+restServerURI);
            return s;
        } catch (javax.ws.rs.ProcessingException e) {  
            System.out.println("trying next port for web server "+(port+1)+". e="+e);
            return startRestServer(port+1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
