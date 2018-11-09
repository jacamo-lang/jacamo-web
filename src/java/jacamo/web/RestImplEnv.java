package jacamo.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import cartago.CartagoException;
import cartago.CartagoService;
import jacamo.platform.EnvironmentWebInspector;
import ora4mas.nopl.WebInterface;


@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    static Set<String> hidenArts = new HashSet<>( Arrays.asList(new String[] {
            "node",
            "console",
            "blackboard",
            "workspace",
            "manrepo",
        }));


    @Path("/")
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
            return EnvironmentWebInspector.getArtHtml(wrksName, 
                        CartagoService.getController(wrksName).getArtifactInfo(artName)
                   );
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
            String program = null;
            try {
                program = WebInterface.getDotPath();
            } catch (Exception e) {}
            if (program != null) {
                String dot = getWksAsDot(wrksName);
                if (dot != null && !dot.isEmpty()) {
                    File fin     = File.createTempFile("jacamo-e-", ".dot");
                    File imgFile = File.createTempFile("jacamo-e-", ".svg");

                    FileWriter out = new FileWriter(fin);
                    out.append(dot);
                    out.close();
                    Process p = Runtime.getRuntime().exec(program+" -Tsvg "+fin.getAbsolutePath()+" -o "+imgFile.getAbsolutePath());
                    p.waitFor(2000,TimeUnit.MILLISECONDS);

                    return Response.ok(new FileInputStream(imgFile)).build();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }
    
    protected String getWksAsDot(String wksName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("graph [\n");
            sb.append("rankdir = \"LR\"\n");
            sb.append("]\n");
            sb.append("\tsubgraph cluster_0 {\n");
            sb.append("\t\tlabel=\"" + wksName + "\"\n");
            sb.append("\t\tlabeljust=\"r\"");
            sb.append("\t\tgraph[style=dashed]");           
            for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

                if (info.getId().getArtifactType().equals("cartago.WorkspaceArtifact"))
                    ; // do not print system artifacts
                else if (info.getId().getArtifactType().equals("cartago.tools.Console"))
                    ;
                else if (info.getId().getArtifactType().equals("cartago.ManRepoArtifact"))
                    ;
                else if (info.getId().getArtifactType().equals("cartago.tools.TupleSpace"))
                    ;
                else if (info.getId().getArtifactType().equals("cartago.NodeArtifact"))
                    ;
                else if (info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    ;
                } else {
                    sb.append("\t\t\"" + info.getId().getName() + "\" [ " + "\n\t\t\tlabel = <<b>"
                            + info.getId().getName() + "</b>");
                    sb.append(" :<br/>" + info.getId().getArtifactType()+ ">");
                    //info.getObsProperties().forEach(y -> sb.append(y + "\\n"));
                    //info.getOperations().forEach(y -> sb.append(y + "\\n"));
                    sb.append("\t\t\tshape = \"component\"\n");
                    sb.append("\t\t];\n");
                }
                info.getObservers().forEach(y -> {
                    // do not print agents_body observation
                    if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
                        sb.append("\t\t\"" + y + "\" -> \"" + info.getId().getName() + "\" [arrowhead=\"odot\"];\n");
                    });

            }
            sb.append("\t}\n");
            sb.append("}\n");
            graph = sb.toString();
        } catch (CartagoException e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter("graph.gv", false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
             out.print(graph);
            out.flush();
            out.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }   
        
        return graph;
    }

}
