package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import jacamo.project.JaCaMoProject;
import jacamo.project.parser.JaCaMoProjectParser;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServices;

@Singleton
@Path("/jcm")
public class WebImplJCM extends AbstractBinder {

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
            File f = new File("src/jcm/" + projectName);
            if (f.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(f));
                
                JaCaMoProjectParser parser = new JaCaMoProjectParser(in);
                JaCaMoProject project = new JaCaMoProject();

                project = parser.parse("src/jcm/" + projectName);

                // create agents
                RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices(); 
                for (AgentParameters ap: project.getAgents()) {
                    for (int i = 0; i < ap.getNbInstances(); i++ )
                        rt.createAgent(ap.getAgName(), ap.asSource.toString(), ap.agClass.getClassName(), ap.getAgArchClasses(), ap.getBBClass(), ap.getAsSetts(false, false), null);
                }
                
                // TODO: create workspaces, orgs, ...
                return Response.ok().entity("Project " + projectName + " lanched!").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

}
