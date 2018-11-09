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
        so.append("<html><head><title>JaCaMo Web Interface</title></head><body>");
        so.append("<font size=\"+2\">");
        so.append("  <a href='/agents'     target='lf' style='color: red; font-family: arial; text-decoration: none;'> Agents </a>");
        so.append("| <a href='/workspaces' target='lf' style='color: red; font-family: arial; text-decoration: none;'> Environment </a>");
        so.append("| <a href='http://localhost:3271/oe' target='lf' style='color: red; font-family: arial; text-decoration: none;'> Organisation </a>");
        so.append("</font><hr/>");
        so.append("<iframe width=\"20%\" height=\"100%\" align=left id='lf' name='lf' src=\"/agents\" border=5 frameborder=0 ></iframe>");
        so.append("<iframe width=\"78%\" height=\"100%\" align=left id='cf' name='cf' border=5 frameborder=0></iframe>");
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
                "        window.location.href = '/agents/'+document.getElementById('inputcmd').value+'/mind';\n"+
                "    }\n" + 
                "</script>"+
                "</form></html>";
    }

}
