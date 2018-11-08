package jacamo.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
        String graph = "digraph G {\n" + 
                "\n" + 
                "	subgraph cluster_0 {\n" + 
                "		style=filled;\n" + 
                "		color=lightgrey;\n" + 
                "		node [style=filled,color=white];\n" + 
                "		a0 -> a1 -> a2 -> a3;\n" + 
                "		label = \"process #1\";\n" + 
                "	}\n" + 
                "\n" + 
                "	subgraph cluster_1 {\n" + 
                "		node [style=filled];\n" + 
                "		b0 -> b1 -> b2 -> b3;\n" + 
                "		label = \"process #2\";\n" + 
                "		color=blue\n" + 
                "	}\n" + 
                "	start -> a0;\n" + 
                "	start -> b0;\n" + 
                "	a1 -> b3;\n" + 
                "	b2 -> a3;\n" + 
                "	a3 -> a0;\n" + 
                "	a3 -> end;\n" + 
                "	b3 -> end;\n" + 
                "\n" + 
                "	start [shape=Mdiamond];\n" + 
                "	end [shape=Msquare];\n" + 
                "}";
        return graph;
    }

}
