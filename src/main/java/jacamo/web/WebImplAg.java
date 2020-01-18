package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.gson.Gson;

import jacamo.rest.RestImplAg;
import jason.asSemantics.Agent;
import jason.asSemantics.GoalListenerForMetaEvents;
import jason.asSyntax.parser.as2j;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;

/**
 * Agent's REST implementation class
 * 
 * @author Jomi Fred Hubner
 * @author Cleber Jorge Amaral
 *
 */
@Singleton
@Path("/agents")
public class WebImplAg extends RestImplAg { // TODO: replace by extends RestImplAg and move some code to jacamo-rest

    Gson gson = new Gson();

    @Override
    protected void configure() {
        bind(new WebImplAg()).to(WebImplAg.class);
    }

    /**
     * Return agent object by agent's name
     * 
     * @param agName name of the agent
     * @return Agent object
     */
    private Agent getAgent(String agName) {
        CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
    }

    /**
     * Returns PLAIN TEXT of the context of an Jason agent code file (.asl). Besides
     * the asl filename it wants the agent's name for agent's refreshing commands.
     * 
     * @param agName      name of the agent
     * @param aslFileName name of the file (including .asl extension)
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/aslfile/{aslfilename}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLoadASLfileForm(@PathParam("agentname") String agName,
            @PathParam("aslfilename") String aslFileName) {

        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/agt/" + aslFileName);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(WebImpl.class.getResource("../src/agt/" + aslFileName).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line + "\n");
                line = in.readLine();
            }
            return Response.ok(so.toString()).build();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Updates an Jason agent code file (.asl) refreshing given agent's execution
     * plans immediately. Current intentions are kept running with old code.
     * 
     * @param agName              name of the agent
     * @param aslFileName         name of the file (including .asl extension)
     * @param uploadedInputStream new content for the given asl file name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/aslfile/{aslfilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response loadASLfileForm(@PathParam("agentname") String agName, @PathParam("aslfilename") String aslFileName,
            @FormDataParam("aslfile") InputStream uploadedInputStream) {
        try {
            Agent ag = getAgent(agName);
            if (ag != null) {
                System.out.println("agName: " + agName);
                System.out.println("restAPI://" + aslFileName);
                System.out.println("uis: " + uploadedInputStream);

                //Save new code
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                FileOutputStream outputFile = new FileOutputStream("src/agt/" + aslFileName, false);
                BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));

                while ((line = out.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                byte[] bytes = stringBuilder.toString().getBytes();
                outputFile.write(bytes);
                outputFile.close();

                //Reload agent with this new code
                ag.getPL().clear();

                ag.parseAS(new FileInputStream("src/agt/" + aslFileName), aslFileName);
                if (ag.getPL().hasMetaEventPlans())
                    ag.getTS().addGoalListener(new GoalListenerForMetaEvents(ag.getTS()));

                ag.loadKqmlPlans();

                return Response.ok("Agent reloaded with updated file. Old intentions were not affected.").build();
            }

            return Response.status(500, "Internal Server Error! Agent'" + agName + " Does not exists!").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }
    
    
    
    /**
     * Parse an Jason agent code file (.asl) answering whether it has sintax errors
     * of not
     * 
     * @param agName              name of the agent
     * @param aslFileName         name of the file (including .asl extension)
     * @param uploadedInputStream new content for the given asl file name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/parseAslfile/{aslfilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response parseASLfileForm(@PathParam("agentname") String agName,
            @PathParam("aslfilename") String aslFileName, @FormDataParam("aslfile") InputStream uploadedInputStream) {
        System.out.println("xxx: " + agName);
        try {
            System.out.println("agent: " + agName);
            System.out.println("aslfile: " + aslFileName);

            // as2j parser = new as2j(uploadedInputStream);

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));
            while ((line = out.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            
            String name = BaseCentralisedMAS.getRunner().getRuntimeServices().createAgent("temp", null, null, null,
                    null, null, null);
            BaseCentralisedMAS.getRunner().getRuntimeServices().startAgent(name);
            Agent ag = getAgent("temp");
            
            File f = new File("src/agt/temp.asl");
            
            if (!f.exists()) f.createNewFile();

            FileOutputStream outputFile = new FileOutputStream(f, false);
            byte[] bytes = stringBuilder.toString().getBytes();
            outputFile.write(bytes);
            outputFile.close();

            //ag.parseAS(new FileInputStream("src/agt/temp.asl"), "temp.asl");
            
            as2j parser = new as2j(new FileInputStream("src/agt/temp.asl"));
            parser.agent(ag);

            //just print the content
            System.out.println("parsed: " + stringBuilder.toString());

            BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent("temp", "web", 0);
            return Response.ok("Code parsed without errors.").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("error on parsing");
        BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent("temp", "web", 0);
        return Response.status(500, "Internal Server Error or Error When Parding Jason Code!").build();
    }
}
