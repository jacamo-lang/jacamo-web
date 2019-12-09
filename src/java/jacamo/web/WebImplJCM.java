package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.tools.ant.filters.StringInputStream;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import cartago.AgentIdCredential;
import cartago.ArtifactId;
import cartago.CartagoContext;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.CartagoSession;
import cartago.CartagoWorkspace;
import cartago.IAlignmentTest;
import cartago.ICartagoSession;
import cartago.Op;
import cartago.OpFeedbackParam;
import cartago.WorkspaceId;
import jaca.CAgentArch;
import jaca.CartagoEnvironment;
import jaca.JavaLibrary;
import jaca.NameSpaceOp;
import jacamo.platform.Cartago;
import jacamo.platform.Moise;
import jacamo.project.JaCaMoGroupParameters;
import jacamo.project.JaCaMoOrgParameters;
import jacamo.project.JaCaMoProject;
import jacamo.project.JaCaMoSchemeParameters;
import jacamo.project.JaCaMoWorkspaceParameters;
import jacamo.project.parser.JaCaMoProjectParser;
import jacamo.rest.TranslAg;
import jacamo.rest.TranslEnv;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServices;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgBoard;

@Singleton
@Path("/jcm")
public class WebImplJCM extends AbstractBinder {

    CartagoContext ctx = null;
    
    @Override
    protected void configure() {
        bind(new WebImplJCM()).to(WebImplJCM.class);
    }

    @POST
    public Response runJCM(@FormParam("script") String script, @FormParam("path") String path) {
        try {
            JaCaMoProjectParser parser = new JaCaMoProjectParser(new StringInputStream(script));
            JaCaMoProject project = new JaCaMoProject();

            project = parser.parse(path);

            System.out.println(project.toString());
            
            // create agents
            RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices(); 
            for (AgentParameters ap: project.getAgents()) {
                rt.createAgent(ap.getAgName(), ap.asSource.toString(), ap.agClass.getClassName(), ap.getAgArchClasses(), ap.getBBClass(), ap.getAsSetts(false, false), null);
            }
            
            // TODO: create workspaces, orgs, ...
            
            return Response.ok().entity(project.toString()).build(); 
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
    
    // used to test
    @Path("/form")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getForm() {
        return Response.ok().entity("<html><body>\n" + 
                "	<form action=\"/jcm\" method=\"post\">\n" + 
                "		<p>\n" + 
                "			Directory : <input type=\"text\" name=\"path\" />\n" + 
                "		</p>\n" + 
                "		<p>\n" + 
                "			Script    : <br/><textarea name=\"script\" rows=\"10\" cols=\"40\">" +
                "mas bobandalice {\n" + 
                "\n" + 
                "    agent bob\n" + 
                "    agent alice\n" + 
                "\n" + 
                "}"+
                "</textarea></p>\n" + 
                "		<input type=\"submit\" value=\"Run JCM\" />\n" + 
                "	</form>\n" + 
                "</body></html>").build();
    }
    
    /**
     * Get list of jcm files available to be launched in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectsJSON() {
        Gson gson = new Gson();
        
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".jcm");
            }
        };

        File f = new File("src/jcm");
        List<String> jcmFiles = new ArrayList<>(Arrays.asList(f.list(filter)));

        return Response.ok().entity(gson.toJson(jcmFiles)).build();
    }

    /**
     * Launch a JCM project.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{projectname}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getProjectJSON(@PathParam("projectname") String projectName) {
        try {
            if (ctx == null)
                ctx = CartagoService.startSession(new AgentIdCredential("jacamo-rest"));

            System.out.println("Launching " + projectName);
            File f = new File("src/jcm/" + projectName);
            if (f.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(f));

                JaCaMoProjectParser parser = new JaCaMoProjectParser(in);
                JaCaMoProject project = new JaCaMoProject();

                project = parser.parse("src/jcm/" + projectName);

                // TODO: create organisations
                for (JaCaMoOrgParameters op : project.getOrgs()) {
                    System.out.println("o1: " + op.toString());
                    System.out.println("o2: " + op.getName());
                    if (!CartagoService.getNode().getWorkspaces().contains(op.getName()))
                        CartagoService.createWorkspace(op.getName());
                    
                    WorkspaceId wid = ctx.joinWorkspace(op.getName());
                    op.addParameter("source", project.getOrgPaths().fixPath(op.getParameter("source")));
                    System.out.println("o3: " + op.toString());
                    ArtifactId org = ctx.makeArtifact(wid, op.getName(), OrgBoard.class.getName(), new Object[] { op.getParameter("source") });

                    // schemes
                    for (JaCaMoSchemeParameters s: op.getSchemes()) {
                        OpFeedbackParam<ArtifactId> fb = new OpFeedbackParam<>();
                        ctx.doAction(org, new Op("createScheme", new Object[] { s.getName(), s.getType(), fb} ));
                        //TODO: owner...
                    }

                    // groups
                    for (JaCaMoGroupParameters g: op.getGroups()) {
                        OpFeedbackParam<ArtifactId> fb = new OpFeedbackParam<>();
                        ctx.doAction(org, new Op("createGroup", new Object[] { g.getName(), g.getType(), fb} ));
                        //TODO: subgroups, responsible...
                    }
                }                
                               
                //TODO: agents must focus when it is set
                // create agents
                RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices();
                for (AgentParameters ap : project.getAgents()) {
                    for (int i = 0; i < ap.getNbInstances(); i++) {
                        rt.createAgent(ap.getAgName(), ap.asSource.toString(), ap.agClass.getClassName(),
                                ap.getAgArchClasses(), ap.getBBClass(), ap.getAsSetts(false, false), null);
                        rt.startAgent(ap.getAgName());
                    }
                }

                // create workspaces and artifacts (if not exist)
                for (JaCaMoWorkspaceParameters wp : project.getWorkspaces()) {
                    if (!CartagoService.getNode().getWorkspaces().contains(wp.getName()))
                        CartagoService.createWorkspace(wp.getName());

                    WorkspaceId wid = ctx.joinWorkspace(wp.getName());
                    for (String aName : wp.getArtifacts().keySet()) {
                        if (!artifactExists(wp.getName(), aName)) {
                            ctx.makeArtifact(wid, aName, wp.getArtifacts().get(aName).getClassName());
                        }
                    }

                }

            }

            return Response.ok().entity("Project " + projectName + " lanched!").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }
    
    public boolean artifactExists(String wksName, String artName) throws CartagoException {
        for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
            if (aid.getName().equals(artName)) return true;
        }
        return false;
    }

}
