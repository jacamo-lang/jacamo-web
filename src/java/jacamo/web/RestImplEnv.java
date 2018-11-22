package jacamo.web;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jacamo.platform.EnvironmentWebInspector;

@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    int MAX_LENGTH = 30; // max length of strings when printed in graphs
    
    static Set<String> hidenArts = new HashSet<>( Arrays.asList(new String[] {
            "node",
            "console",
            "blackboard",
            "workspace",
            "manrepo",
        }));


    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getWorkspacesHtml() {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>JaCamo-Rest - Environment</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append(getEnvironmentMenu(""));                
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-10 col-lg-10\" id=\"doc-content\">\n"); 
        so.append("					<div id=\"getting-started\" class=\"card fluid\">\n"); 
        so.append("						<h2 class=\"section double-padded\">Getting started</h2>\n"); 
        so.append("						<div class=\"section\">\n"); 
        so.append("							<p>\n" +
                "								Artifacts are <a href=\"http://cartago.sourceforge.net\" target=\"_blank\">CArtAgO</a> entities situated in a workaspace. Agents may use them in their tasks." +
                "							</p> " +        
                "							<br/>\n");
        so.append("						</div>\n");
        so.append("					</div>\n");
        so.append("				</main>\n"); 
        so.append("			</div>\n"); 
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        // copy to 'menucontent' the menu to show on drop down main page menu
        so.append("	<script>\n");
        so.append("		var buttonClose = \"<label for='doc-drawer-checkbox' class='button drawer-close'></label>\";\n"); 
        so.append("		var pageContent = document.getElementById(\"nav-drawer-frame\").innerHTML;\n"); 
        so.append("		var fullMenu = `${buttonClose} ${pageContent}`;\n");
        so.append("		sessionStorage.setItem(\"menucontent\", fullMenu);\n");
        so.append("	</script>\n");
        so.append("</html>\n");
        return so.toString();
    }

    private String getEnvironmentMenu(String string) {
        
        StringWriter so = new StringWriter();

        so.append("				<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n"); 
        so.append("				<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("					</br>\n"); 

        for (String wname: CartagoService.getNode().getWorkspaces()) {
            try {
                so.append("					" + wname + "</br>\n");
                for (ArtifactId aid: CartagoService.getController(wname).getCurrentArtifacts()) {
                    if (hidenArts.contains(aid.getName()))
                        continue;
                    if (aid.getName().endsWith("-body"))
                        continue;
                    String addr = "/workspaces/"+wname+"/"+aid.getName();
                    so.append("					<a href=\"" + addr + "\" id=\"link-to-" + wname + "\" target='mainframe'>" + aid.getName() + "</a>\n");
                }

            } catch (CartagoException e) {
                e.printStackTrace();
            }
        }
        so.append("				</nav>\n");
        
        return so.toString();
    }

    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getArtifactHtml(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            //String img = "<img src='"+artName+"/img.svg' /><br/>";
            
            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);
            StringWriter so = new StringWriter();
            
            so.append("<!DOCTYPE html>\n"); 
            so.append("<html lang=\"en\" target=\"mainframe\">\n"); 
            so.append("	<head>\n");
            so.append("		<title>JaCamo-Rest - Environment: " + wrksName + "</title>\n");
            so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
            so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            so.append("	</head>\n"); 
            so.append("	<body>\n"); 
            so.append("		<div id=\"root\">\n");
            so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 

            so.append(getEnvironmentMenu(wrksName));  
            
            // agent's content
            so.append("				<main class=\"col-xs-12 col-sm-12 col-md-10 col-lg-10\" id=\"doc-content\">\n"); 

            // overview
            so.append("					<div id=\"overview\" class=\"card fluid\">\n"); 
            so.append("						<div class=\"section\">\n");
            so.append("							<center><img src='" + artName + "/img.svg'/></center><br/>\n");
            so.append("						</div>\n");
            so.append("					</div>\n");
            so.append("					<div id=\"artifactinspection\" class=\"card fluid\">\n");
            so.append("						<div class=\"section\">\n"); 
            so.append("							Inspection of artifact <b>" + info.getId().getName() + "</b> in workspace \"" + wrksName + "\n");
            so.append("<table border=0 cellspacing=3 cellpadding=6 style='font-family:verdana'>");
            for (ArtifactObsProperty op: info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl: op.getValues()) {
                    vls.append(v+vl);
                    v = ",";
                }
                so.append("<tr><td>"+op.getName()+"</td><td>"+vls+"</td></tr>");
            }
            so.append("</details></table>");
            so.append("                     </div>\n"); 
            so.append("                 </div>\n"); 
            so.append("             </main>\n"); 
            so.append("         </div>\n");
            so.append("     </div>\n");
            so.append(" </body>\n");
            so.append(" <script language=\"JavaScript\">\n");
            // function to run Jason commands
            so.append(" </script>\n");
            so.append("</html>\n");
            
            return so.toString();
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    EnvironmentWebInspector bend = new EnvironmentWebInspector();
    
    @Path("/{wrksname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getWrksImg(@PathParam("wrksname") String wrksName) {
        try {
            String dot = getWksAsDot(wrksName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }
    
    @SuppressWarnings("finally")
    protected String getWksAsDot(String wksName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            sb.append("\tsubgraph cluster_0 {\n");
            sb.append("\t\tlabel=\"" + wksName + "\"\n");
            sb.append("\t\tlabeljust=\"r\"\n");
            sb.append("\t\tgraph[style=dashed]\n");           
            for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

                if ((info.getId().getArtifactType().equals("cartago.WorkspaceArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.tools.Console")) ||
                    (info.getId().getArtifactType().equals("cartago.ManRepoArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.tools.TupleSpace")) ||
                    (info.getId().getArtifactType().equals("cartago.NodeArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))) {
                    ; // do not print system artifacts
                } else {
                    String s1;
                    s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                            : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                    sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                            + s1 + ":\n");
                    s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
                            : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
                    sb.append(s1 + "\"\n");
                    sb.append("\t\tshape = \"record\"\n");
                    sb.append("\t\t\tURL = \"" + info.getId().getName() + "/img.svg\"\n");
                    sb.append("\t\t];\n");
                }
                info.getObservers().forEach(y -> {
                    // do not print agents_body observation
                    if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                        // print node with defined shape
                        String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH)
                                ? y.getAgentId().getAgentName()
                                : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                        sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
                        sb.append("\t\tURL = \"../../agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                        sb.append("\t];\n");

                        // print arrow
                        sb.append("\t\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName()
                                + "\" [arrowhead=\"odot\"];\n");
                    }
                });

                // linked artifacts
                info.getLinkedArtifacts().forEach(y -> {
                    // linked artifact node already exists if it belongs to this workspace
                    sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName()
                            + "\" [arrowhead=\"onormal\"];\n");
                });
                    
            }
            sb.append("\t}\n");
            sb.append("}\n");
            graph = sb.toString();

        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }

    @Path("/{wrksname}/{artname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getArtImg(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            String dot = getArtAsDot(wrksName,artName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }
    
    @SuppressWarnings("finally")
    protected String getArtAsDot(String wksName, String artName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        ArtifactInfo info;
        try {
            String s1;
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            info = CartagoService.getController(wksName).getArtifactInfo(artName);
            s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                    : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
            sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                    + s1 + ":\n");
            s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
                    : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
            sb.append(s1 + "|");
            
            // observable properties
            info.getObsProperties().forEach(y -> {
                String s2 = (y.toString().length() <= MAX_LENGTH) ? y.toString()
                        : y.toString().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\t" + s2 + "\n");
            });
            sb.append("\t\t\t|");
            
            // operations
            info.getOperations().forEach(y -> {
                String s2 = (y.getOp().getName().length() <= MAX_LENGTH) ? y.getOp().getName()
                        : y.getOp().getName().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\t" + s2 + "\n");
            });   
            sb.append("\t\t\t\"\n");

            sb.append("\t\tshape = \"record\"\n");
            sb.append("\t];\n");

            // linked artifacts
            info.getLinkedArtifacts().forEach(y -> {
                // print node with defined shape
                String s2 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
                        : y.getName().substring(0, MAX_LENGTH) + "...";
                sb.append("\t\"" + y.getName() + "\" [ " + "\n\t\tlabel = \""
                        + s2 + "|");
                s2 = (y.getArtifactType().length() <= MAX_LENGTH) ? y.getArtifactType()
                        : y.getArtifactType().substring(0, MAX_LENGTH) + "...";
                sb.append(s2 + "\"\n");
                sb.append("\t\tURL = \"../" + y.getName() + "/img.svg\"\n");
                sb.append("\t\tshape = \"record\"\n");
                sb.append("\t];\n");

                // print arrow
                sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName()
                      + "\" [arrowhead=\"onormal\"];\n");
            });

            // observers
            info.getObservers().forEach(y -> {
                // do not print agents_body observation
                if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    // print node with defined shape
                    String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH) ? y.getAgentId().getAgentName()
                            : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \""
                            + s2 + "\"\n");
                    sb.append("\t\tURL = \"../../../agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                    sb.append("\t];\n");
                    
                    // print arrow
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName() 
                            + "\" [arrowhead=\"odot\"];\n");
                }
            });
            
            sb.append("}\n");
            graph = sb.toString();
            
        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }
}
