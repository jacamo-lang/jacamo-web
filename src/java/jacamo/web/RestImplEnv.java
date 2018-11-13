package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
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

        so.append("<html><head><title>CArtAgO (list of artifacts)</title> <meta http-equiv=\"refresh\" content=\"3\"/> </head><body>");
        for (String wname: CartagoService.getNode().getWorkspaces()) {
            try {
                so.append("<br/><a href='/workspaces/"+wname+"/img.svg' target='cf' style='color: red; font-family: arial; text-decoration: none'>"+wname+"</a><br/>");
                for (ArtifactId aid: CartagoService.getController(wname).getCurrentArtifacts()) {
                    if (hidenArts.contains(aid.getName()))
                        continue;
                    if (aid.getName().endsWith("-body"))
                        continue;
                    String addr = "/workspaces/"+wname+"/"+aid.getName();
                    so.append(" - <a href=\""+addr+"\" target='cf' style=\"font-family: arial; text-decoration: none\">"+aid.getName()+"</a><br/>");
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        }
        
        so.append("<hr/>by <a href=\"http://cartago.sourceforge.net\" target=\"_blank\">CArtAgO</a>");
        so.append("</body></html>");        
        return so.toString();
    }

    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getArtifactHtml(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            //String img = "<img src='"+artName+"/img.svg' /><br/>";
            
            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);
            
            StringBuilder out = new StringBuilder("<html>");
            out.append("<center><img src='"+artName+"/img.svg' /><br/></center>");
            out.append("<details><span style=\"color: red; font-family: arial\"><font size=\"+2\">");
            out.append("Inspection of artifact <b>"+info.getId().getName()+"</b> in workspace "+wrksName+"</font></span>");
            out.append("<table border=0 cellspacing=3 cellpadding=6 style='font-family:verdana'>");
            for (ArtifactObsProperty op: info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl: op.getValues()) {
                    vls.append(v+vl);
                    v = ",";
                }
                out.append("<tr><td>"+op.getName()+"</td><td>"+vls+"</td></tr>");
            }
            out.append("</details></table>");
            out.append("</html>");
            
            return out.toString();
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
                            + s1 + "|");
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
            sb.append("\t]\n");
            info = CartagoService.getController(wksName).getArtifactInfo(artName);
            s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                    : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
            sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                    + s1 + "|");
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
