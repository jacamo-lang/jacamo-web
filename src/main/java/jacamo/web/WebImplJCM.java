package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import jacamo.project.JaCaMoOrgParameters;
import jacamo.project.JaCaMoProject;
import jacamo.project.JaCaMoWorkspaceParameters;
import jacamo.project.parser.JaCaMoProjectParser;
import jacamo.project.parser.ParseException;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServices;

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

        try {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return name.endsWith(".jcm");
                }
            };

            File f = new File("src/jcm");
            List<String> jcmFiles = new ArrayList<>(Arrays.asList(f.list(filter)));

            List<Object> projects = new ArrayList<>();
            for (String filename : jcmFiles) {
                File fjcm = new File("src/jcm/" + filename);
                BufferedReader in = new BufferedReader(new FileReader(fjcm));

                JaCaMoProjectParser parser = new JaCaMoProjectParser(in);
                JaCaMoProject project = new JaCaMoProject();
                project = parser.parse("src/jcm/" + filename);
                List<String> ags = new ArrayList<>();
                for (AgentParameters ap : project.getAgents())
                    ags.add(ap.getAgName());
                List<String> wks = new ArrayList<>();
                for (JaCaMoWorkspaceParameters wp : project.getWorkspaces())
                    wks.add(wp.getName());
                List<String> orgs = new ArrayList<>();
                for (JaCaMoOrgParameters or : project.getOrgs())
                    orgs.add(or.getName());

                Map<String, Object> jcm = new HashMap<>();
                jcm.put("jcm", filename);
                jcm.put("agents", ags);
                jcm.put("workspaces", wks);
                jcm.put("organisations", orgs);
                projects.add(jcm);
            }

            return Response.ok().entity(gson.toJson(projects)).build();
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
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

                createEnvironment(project);
                TimeUnit.SECONDS.sleep(1);               
                createOrganisation(project);
                TimeUnit.SECONDS.sleep(1);                      
                createAgs(project);
                TimeUnit.SECONDS.sleep(1);                      
                startAgs(project);
            }

            return Response.ok().entity("Project " + projectName + " lanched!").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    private void startAgs(JaCaMoProject project) {
        RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices();
        for (AgentParameters ap : project.getAgents()) {
            for (int i = 0; i < ap.getNbInstances(); i++) 
                rt.startAgent(ap.getAgName());
        }
        //TODO: agents must focus when it is set
    }

    private void createAgs(JaCaMoProject project) throws Exception {
        RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices();
        for (AgentParameters ap : project.getAgents()) {
            for (int i = 0; i < ap.getNbInstances(); i++)
                rt.createAgent(ap.getAgName(), ap.asSource.toString(), ap.agClass.getClassName(),
                        ap.getAgArchClasses(), ap.getBBClass(), ap.getAsSetts(false, false), null);
        }
    }

    private void createOrganisation(JaCaMoProject project) throws CartagoException {
        /*TODO: Implement getPlatforms
        Moise m = null;
        for (Platform p : ((JaCaMoLauncher)JaCaMoLauncher.getJaCaMoRunner()).getPlatforms()) 
            if (p instanceof Moise) 
                m = (Moise) p;
        if (m == null) {
            m = new Moise();
            m.init(new String[] {});
        }
        m.setJcmProject(project);
        m.start();*/
    }

    private void createEnvironment(JaCaMoProject project) throws InterruptedException {
        
/*        // create workspaces and artifacts (if not exist)
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
*/
        /*TODO: Implement getPlatforms
        for (Platform p : ((JaCaMoLauncher)JaCaMoLauncher.getJaCaMoRunner()).getPlatforms()) {
            if (p instanceof Cartago) {
                Cartago c = (Cartago) p;
                c.setJcmProject(project);
                c.start();

                TimeUnit.SECONDS.sleep(1);               
            }
        }*/
    }
    
    public boolean artifactExists(String wksName, String artName) throws CartagoException {
        for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
            if (aid.getName().equals(artName)) return true;
        }
        return false;
    }

}
