package jacamo.web;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import jason.infra.centralised.BaseCentralisedMAS;


@Singleton
@Path("/services")
public class RestImplDF extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplDF()).to(RestImplDF.class);
    }


    /** DF **/

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDFHtml() {
        StringWriter so = new StringWriter();
        so.append("<html><head><title>Directory Facilitator State</title></head><body>");
        so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>Directory Facilitator State</p></font>");
                            
        so.append("<table border=\"0\" cellspacing=\"3\" cellpadding=\"6\" >");
        so.append("<tr style='background-color: #ece7e6; font-family: arial;'><td><b>Agent</b></td><td><b>Services</b></td></tr>");
        if (JCMRest.getZKHost() == null) {
            // get DF locally 
            Map<String, Set<String>> df = BaseCentralisedMAS.getRunner().getDF();
            for (String a: df.keySet()) {
                so.append("<tr style='font-family: arial;'><td>"+a+"</td>");
                for (String s: df.get(a)) {
                    so.append("<td>"+s+"<br/></td>");
                }
                so.append("</tr>");
                    
            }
        } else {
            // get DF from ZK
            try {
                for (String s: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+s)) {
                        so.append("<tr style='font-family: arial;'><td>"+a+"</td>");
                        so.append("<td>"+s+"<br/></td>");
                        so.append("</tr>");                 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        so.append("</table></body></html>");

        return so.toString();            
    }

}
