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

    public String designPage(String title, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta http-equiv=\"Content-type\" name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\" id=\"doc-content\">\n"); 
        so.append(                  mainContent);
        so.append("				</main>\n"); 
        so.append("			</div>\n");
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        so.append("<script src=\"/js/agent.js\"></script>\n");
        so.append("</html>\n");
        
        return so.toString();
    }
    
    
    /** DF **/

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDFHtml() {
        
        StringWriter mainContent = new StringWriter();
        
        mainContent.append("<div id=\"directory-facilitator\" class=\"card fluid\">\n"); 
        mainContent.append("	<h4 class=\"section double-padded\">Directory Facilitator</h4>\n"); 
        mainContent.append("	<div class=\"section\">\n"); 
        mainContent.append("		<table class=\"striped\">");
        mainContent.append("			<thead><tr><th>Agent</th><th>Services</th></tr></thead>");
        mainContent.append("			<tbody>");
        
        if (JCMRest.getZKHost() == null) {
            // get DF locally 
            Map<String, Set<String>> df = BaseCentralisedMAS.getRunner().getDF();
            for (String a: df.keySet()) {
                mainContent.append("				<tr><td>"+a+"</td>");
                for (String s: df.get(a)) {
                    mainContent.append("				<td>"+s+"<br/></td>");
                }
                mainContent.append("				</tr>");
                    
            }
        } else {
            // get DF from ZK
            try {
                for (String s: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+s)) {
                        mainContent.append("				<tr><td data-label=\"Agent\">"+a+"</td>");
                        mainContent.append("				<td data-label=\"Services\">"+s+"<br/></td>");
                        mainContent.append("				</tr>");                 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mainContent.append("			</tbody>");
        mainContent.append("		</table>");
        mainContent.append("	</div>\n");
        mainContent.append("</div>\n");

        return designPage("jacamo-web - directory facilitator", mainContent.toString());
    }

    
    
}
