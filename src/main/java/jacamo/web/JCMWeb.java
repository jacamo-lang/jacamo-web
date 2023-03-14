package jacamo.web;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.tyrus.server.Server;

import jacamo.rest.JCMRest;
import jacamo.rest.JCMRuntimeServices;
import jacamo.web.config.WebAppConfig;
import jason.runtime.RuntimeServicesFactory;

public class JCMWeb extends JCMRest {

    @SuppressWarnings("unused")
    private static Server ws;
    
    @Override
    public HttpServer startRestServer(int port, int tryc) {
        if (tryc > 20) {
            System.err.println("Error starting web server!");
            return null;
        }
        try {
            restServerURI = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
            
            // registering resources
            WebAppConfig rc = new WebAppConfig();
            
            // get a server from factory
            HttpServer s = GrizzlyHttpServerFactory.createHttpServer(restServerURI, rc);
            
            // other possiblecontainers:
            System.out.println("jacamo-web Rest API is running on "+restServerURI);
            
            ws = startWebsocketServer(8026);
            
            return s;
        } catch (javax.ws.rs.ProcessingException e) {           
            System.out.println("trying next port for rest server "+(port+1)+". e="+e);
            return startRestServer(port+1,tryc+1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Server startWebsocketServer(int port) {
        try {
            Server ws = new Server(InetAddress.getLocalHost().getHostAddress(), port, "/ws", Websockets.class);
            ws.start();            

            URI wsURI = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()+"/ws").port(port).build();
            System.out.println("Websockets started on " + wsURI);
            
            return ws;
        } catch (javax.ws.rs.ProcessingException e) {           
            System.out.println("Unable to start websockets on "+port+". e="+e);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
