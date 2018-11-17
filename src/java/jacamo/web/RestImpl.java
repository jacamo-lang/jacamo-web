package jacamo.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Scanner;

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
        so.append("<!DOCTYPE html>\n" + 
                "<html lang=\"en\">\n" + 
                "<head><title>JaCaMo-Rest</title>");
        so.append("<style>"+getStyleCSS()+"</style>");
        so.append("</head><body>\n" + 
                "	<div id=\"root\">\n" + 
                "		<header class=\"row\">\n" + 
                "			<span class=\"logo col-sm-3 col-md\">JaCaMo</span> <a\n" + 
                "				class=\"button col-sm col-md\" href=\"agents/\"\n" + 
                "				target=\"mainframe\"> <svg xmlns=\"http://www.w3.org/2000/svg\"\n" + 
                "					width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\"\n" + 
                "					stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                "					stroke-linejoin=\"round\"\n" + 
                "					style=\"height: 20px; vertical-align: text-top;\">\n" + 
                "				<circle cx=\"12\" cy=\"12\" r=\"11\"/></svg><span>&nbsp;Agents</span>\n" + 
                "\n" + 
                "			</a><a class=\"button col-sm col-md\" href=\"workspaces/\"\n" + 
                "				target=\"mainframe\"> <svg xmlns=\"http://www.w3.org/2000/svg\"\n" + 
                "					width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\"\n" + 
                "					stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                "					stroke-linejoin=\"round\"\n" + 
                "					style=\"height: 20px; vertical-align: text-top;\">\n" + 
                "					<polygon points=\"0 1, 24 1, 24 8, 0 8, 0 16, 24 16, 24 23, 0 23, 0 1, 24 1, 24 23, 0 23\"></polygon></svg><span>&nbsp;Environment</span></a>\n" + 
                "\n" + 
                "			</a><a class=\"button col-sm col-md\" href=\"oe/\"\n" + 
                "				target=\"mainframe\"> <svg xmlns=\"http://www.w3.org/2000/svg\"\n" + 
                "					width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\"\n" + 
                "					stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                "					stroke-linejoin=\"round\"\n" + 
                "					style=\"height: 20px; vertical-align: text-top;\">\n" + 
                "					<polygon points=\"0 1, 10 1, 10 6, 24 6, 24 23, 0 23, 0 6, 10 6, 0 6, 0 1\"></polygon></svg><span>&nbsp;Organisation</span></a>\n" + 
                "\n" + 
                "			<label for=\"doc-drawer-checkbox\" class=\"button drawer-toggle col-sm\"></label>\n" + 
                "\n" + 
                "		</header>\n" + 
                "		<div class=\"second-row\" id=\"full-content\">\n" + 
                "			<iframe id=\"mainframe\" name=\"mainframe\" width=\"100%\" height=\"100%\"\n" + 
                "				frameborder=0></iframe>\n" + 
                "			<br />\n" + 
                "		</div>\n" + 
                "	</div>\n" + 
                "	</div>\n" + 
                "</body>\n" + 
                "</html>");
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

    //TODO: @Path("css/style.css")???
    //@GET
    //@Produces(MediaType.TEXT_PLAIN)
    public String getStyleCSS() {
        StringBuilder so = new StringBuilder();
        Locale loc = new Locale("en", "US");
        try (Scanner scanner = new Scanner(new FileInputStream("src/resources/css/style.css"), "UTF-8")) {
            scanner.useLocale(loc);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                so.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return so.toString();
    }
    
}
