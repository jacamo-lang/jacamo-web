package jacamo.web;

import java.io.StringWriter;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;


@Singleton
@Path("/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    // HTML interface
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getRootHtml() {
        StringWriter so = new StringWriter();
        so.append("<html><head><title>Jason Mind Inspector -- Web View</title></head><body>");
        so.append("<iframe width=\"20%\" height=\"100%\" align=left src=\"/agents\" border=5 frameborder=0 ></iframe>");
        so.append("<iframe width=\"78%\" height=\"100%\" align=left src=\"/agent-mind/no_ag\" name=\"am\" border=5 frameborder=0></iframe>");
        so.append("</body></html>");
        return so.toString();
    }

    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        return  "<html><head><title>new agent form</title></head>"+
                "<input type=\"text\" name=\"name\"  size=\"43\" id=\"inputcmd\" placeholder='enter the name of the agent' onkeydown=\"if (event.keyCode == 13) runCMD()\" />\n" + 
                "<script language=\"JavaScript\">\n" + 
                "    function runCMD() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.open(\"POST\", '/agents/'+document.getElementById('inputcmd').value, false); \n" +
                "        http.send();\n"+
                "        window.location.href = '/agents/'+document.getElementById('inputcmd').value+'/all';\n"+
                "    }\n" + 
                "</script>"+
                "</form></html>";
    }

}
