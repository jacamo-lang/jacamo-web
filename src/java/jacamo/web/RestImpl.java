package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import jason.ReceiverNotFoundException;

@Singleton
@Path("/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    // HTML interface
    
    public String designPage(String title, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n"); 
        so.append("<html lang=\"en\">\n"); 
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        so.append("     <script src=\"/js/root.js\"></script>\n");
        so.append("	</head>\n");
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<header class=\"row\">\n");
        // logo JaCaMo
        so.append("				<span class=\"logo col-xp-2 col-sm-2 col-md\">JaCaMo</span>\n"); 
        // top menu - button agents
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/agents/\" target=\"mainframe\">\n" +
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<circle cx=\"12\" cy=\"12\" r=\"11\"/>\n" + 
                  "					</svg><span>&nbsp;Agents</span>\n" + 
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/workspaces/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" +  
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 24 1, 24 8, 0 8, 0 16, 24 16, 24 23, 0 23, 0 1, 24 1, 24 23, 0 23\"></polygon>\n" +
                  "					</svg><span>&nbsp;Environment</span>\n" +
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/oe/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 10 1, 10 6, 24 6, 24 23, 0 23, 0 6, 10 6, 0 6, 0 1\"></polygon>\n" + 
                  "					</svg><span>&nbsp;Organisation</span>\n" + 
                  "				</a>\n");
        so.append("             <label for=\"doc-drawer-checkbox\" class=\"button drawer-toggle\" style=\"right: 10px; width: 50px; position: fixed;\"></label>\n");
        so.append("				<input id=\"doc-drawer-checkbox\" class=\"drawer\" value=\"on\" type=\"checkbox\">\n" + 
                  "				<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer\">\n" + 
                  "					<label for=\"doc-drawer-checkbox\" class=\"button drawer-close\"></label>\n" + 
                  "					<h3>Menu</h3>\n" + 
                  "					<a hef=\"agents/\">Agents</a>\n" + 
                  "					<a hef=\"workspaces/\">Environment</a>\n" + 
                  "					<a hef=\"oe/\">Organisation</a>\n" + 
                  "				</nav>\n");
        so.append("			</header>\n"); 
        so.append("			<div class=\"second-row\" id=\"full-content\">\n");
        so.append(                  mainContent);
        so.append("			</div>\n");
        so.append("		</div>\n"); 
        so.append("	</body>\n"); 
        so.append("</html>\n"); 

        return so.toString();
    }

    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getRootHtml() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("				<iframe id=\"mainframe\" name=\"mainframe\" src=\"/agents/\" width=\"100%\" height=\"100%\"\n" + 
                           "					frameborder=0></iframe>\n"); 

        return designPage("JaCamo-web", mainContent.toString());
    }

    private CacheControl cc = new CacheControl();  { cc.setMaxAge(20); } // in seconds

    @Path("/css/style.css")
    @GET
    @Produces("text/css")
    public Response getStyleCSS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/css/style.css");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/css/style.css").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), "text/css").cacheControl(cc).build();
    }
    
    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        StringWriter mainContent = new StringWriter();
        
        mainContent.append("<div id=\"create-agent\" class=\"card fluid\">\n"); 
        mainContent.append("	<h4 class=\"section double-padded\">Create agent</h4>\n"); 
        mainContent.append("	<div class=\"section\">\n"); 
        mainContent.append("		<p>\n");
        mainContent.append("			<input style=\"width: 100%; margin: 0px;\" placeholder=\"enter agent's name ...\" type=\"text\" id=\"createAgent\" onkeydown=\"if (event.keyCode == 13) newAg()\">\n");
        mainContent.append("		</p>\n");
        mainContent.append("		<br/>\n");
        mainContent.append("	</div>\n");
        mainContent.append("</div>\n");

        return designPage("JaCamo-web -  new agent", mainContent.toString());
    }

    @Path("/js/agent.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getJQueryJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/agent.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/js/agent.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }
    
    @Path("/js/root.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getJQueryMinJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/root.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/js/root.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }
    
    @Path("/res/{resourcepathfile}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getResource(@PathParam("resourcepathfile") String resourcepathfile) throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/js/" + resourcepathfile).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }
    
    @Path("/lib/codemirror.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getCodemirrorJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/lib/codemirror-minified/lib/codemirror.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/lib/codemirror-minified/lib/codemirror.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }

    @Path("/lib/codemirror.css")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getCodemirrorCSS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/lib/codemirror-minified/lib/codemirror.css");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/lib/codemirror-minified/lib/codemirror.css").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }

    @Path("/lib/mode/erlang/erlang.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getModeErlangJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/lib/codemirror-minified/mode/erlang/erlang.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/lib/codemirror-minified/mode/erlang/erlang.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }
    
    @Path("/lib/hint/show-hint.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getShowhintJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/lib/codemirror-minified/addon/hint/show-hint.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/lib/codemirror-minified/addon/hint/show-hint.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }    
    
    @Path("/lib/hint/javascript-hint.js")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLanghintJS() {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/lib/codemirror-minified/addon/hint/javascript-hint.js");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/lib/codemirror-minified/addon/hint/javascript-hint.js").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }    
    
}
