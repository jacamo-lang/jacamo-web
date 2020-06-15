package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tools.ant.filters.StringInputStream;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.gson.Gson;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jaca.CAgentArch;
import jacamo.infra.JaCaMoLauncher;
import jacamo.rest.implementation.RestImplAg;
import jacamo.rest.mediation.TranslAg;
import jacamo.rest.mediation.TranslEnv;
import jacamo.web.exception.SystemOverloadException;
import jacamo.web.exception.UnderstandabilityException;
import jacamo.web.exception.UsefulnessException;
import jason.asSemantics.Agent;
import jason.asSemantics.GoalListenerForMetaEvents;
import jason.asSyntax.Atom;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.RuntimeServicesFactory;
import jason.stdlib.print;

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

    TranslAg tAg = new TranslAg();
    Gson gson = new Gson();

    @Override
    protected void configure() {
        bind(new WebImplAg()).to(WebImplAg.class);
    }

    /**
     * Create an Agent. Produces PLAIN TEXT with HTTP response for this operation. If
     * an ASL file with the given name exists, it will launch an agent with existing
     * code. Otherwise, creates an agent that will start say 'Hi'.
     * 
     * @param agName name of the agent to be created
     * @return HTTP 201 Response (created) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    /*
    @Path("/{agentname}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Create an Agent.")
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgent(
            @PathParam("agentname") String agName, 
            @DefaultValue("false") @QueryParam("only_wp") boolean onlyWP, 
            Map<String,String> metaData,
            @Context UriInfo uriInfo) {
        try {
            String givenName = RuntimeServicesFactory.get().createAgent(agName, null, null, null, null, null, null);
            RuntimeServicesFactory.get().startAgent(givenName);
            // set some source for the agent
            Agent ag = tAg.getAgent(givenName);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("//Agent created automatically\n\n");
            stringBuilder.append("!start.\n\n");
            stringBuilder.append("+!start <- .print(\"Hi\").\n\n");
            stringBuilder.append("{ include(\"$jacamoJar/templates/common-cartago.asl\") }\n");
            stringBuilder.append("{ include(\"$jacamoJar/templates/common-moise.asl\") }\n");
            stringBuilder.append("// uncomment the include below to have an agent compliant with its organisation\n");
            stringBuilder.append("//{ include(\"$moiseJar/asl/org-obedient.asl\") }");
            ag.load(new StringInputStream( stringBuilder.toString()), "source-from-rest-api");
            ag.setASLSrc("no-inicial.asl");
            tAg.createAgLog(givenName, ag);

            return Response
                        .created(new URI(uriInfo.getBaseUri() + "agents/" + givenName))
                        .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }*/
    
    /**
     * Returns PLAIN TEXT of the context of an Jason agent code file (.asl). Besides
     * the asl filename it wants the agent's name for agent's refreshing commands.
     * 
     * @param agName      name of the agent
     * @param aslFileName name of the file (including .asl extension)
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/aslfiles/{aslfilename}")
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
     * Returns PLAIN TEXT of the context of an Jason agent code file (.asl). Besides
     * the asl filename it wants the agent's name for agent's refreshing commands.
     * 
     * @param agName      name of the agent
     * @param aslFileName name of the file (including .asl extension)
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/aslfiles")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getASLfiles(@PathParam("agentname") String agName) {

        // get agent's plans
        Agent ag = tAg.getAgent(agName);

        Set<String> files = new HashSet<>();
        try {
            PlanLibrary pl = ag.getPL();
            for (Plan plan : pl.getPlans()) {
                // skip plans from jar files (usually system's plans)
                if (!(plan.getSource().startsWith("jar:file") || plan.getSource().equals("kqmlPlans.asl")))
                    files.add(plan.getSource());
            }
            Gson json = new Gson();
            return Response.ok(json.toJson(files)).build();
            
        } catch (Exception e) {
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
    @Path("/{agentname}/aslfiles/{aslfilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response loadASLfileForm(@PathParam("agentname") String agName, @PathParam("aslfilename") String aslFileName,
            @FormDataParam("aslfile") InputStream uploadedInputStream) {
        try {
            Agent ag = tAg.getAgent(agName);
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

                return Response.ok().build();
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
     * @return HTTP 200 Response (ok status), 406 Not Acceptable, 503 Service Unavailable, 
     *         500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/aslfiles/{aslfilename}/parse")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response parseASLfileForm(@PathParam("agentname") String agName,
            @PathParam("aslfilename") String aslFileName, @FormDataParam("aslfile") InputStream uploadedInputStream) {
        
        String errorMsg = "Unknown exception";
        
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));
            while ((line = out.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            
            //Prevents the creation of more than one temp agent. Tries to get agent 'temp' for a few times.
            String TEMP_AGENT_NAME = "temp";
            String name = "";
            Agent ag = null;
            for (int i = 0; i < 3; i++) {
                synchronized(this) {
                    if (getAgent(TEMP_AGENT_NAME) == null) {
                        // make sure you only answer after the agent was completely deleted
                        name = BaseCentralisedMAS.getRunner().getRuntimeServices().createAgent(TEMP_AGENT_NAME, null, null, null, null, null, null);
                        if (name.equals(TEMP_AGENT_NAME)) {
                            ag = tAg.getAgent(TEMP_AGENT_NAME);
                            
                            //Creating temporary agent to check plans coherence
                            BaseCentralisedMAS.getRunner().getRuntimeServices().startAgent(name);
                            
                            as2j parser = new as2j(new ByteArrayInputStream(stringBuilder.toString().getBytes(Charset.forName("UTF-8"))));
                            parser.agent(ag);

                            lookForCollaborativeExceptions(ag);
                            
                            return Response.ok("Code looks correct.").build();
                        } else {
                            ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getRuntimeServices().killAgent(name, name, 0);
                            while (BaseCentralisedMAS.getRunner().getAg(name) != null)
                                ;
                            return Response.status(500, "Error [Unknown]: Error creating structure for parser.").build();
                        }
                    }
                }
                Thread.sleep(100);
            }
            throw new SystemOverloadException("Info: System overload when parsing...");

        } catch (SystemOverloadException e) {
            e.printStackTrace();

            return Response.status(503, e.getMessage()).build();
        } catch (jason.asSyntax.parser.TokenMgrError e) {
            e.printStackTrace();

            errorMsg = "Error: " + ((e.getMessage().length() >= 150) ? e.getMessage().substring(0, 150) + "..." : e.getMessage());
            return Response.status(406, errorMsg).build();
        } catch (UsefulnessException e) {
            e.printStackTrace();

            errorMsg = "Warning: " + ((e.getMessage().length() >= 150) ? e.getMessage().substring(0, 150) + "..." : e.getMessage());
            return Response.status(406, errorMsg).build();
        } catch (UnderstandabilityException e) {
            e.printStackTrace();

            errorMsg = "Warning: " + ((e.getMessage().length() >= 150) ? e.getMessage().substring(0, 150) + "..." : e.getMessage());
            return Response.status(406, errorMsg).build();
        } catch (ParseException e) {
            e.printStackTrace();

            errorMsg = "Error: " + ((e.getMessage().length() >= 150) ? e.getMessage().substring(0, 150) + "..." : e.getMessage());
            return Response.status(406, errorMsg).build();
        } catch (Exception e) {
            e.printStackTrace();

            errorMsg = "Error [Unknown]: " + ((e.getMessage().length() >= 150) ? e.getMessage().substring(0, 150) + "..." : e.getMessage());
            return Response.status(406, errorMsg).build();
        } finally {
            if (BaseCentralisedMAS.getRunner().getRuntimeServices().getAgentsNames().contains("temp")) {
                BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent("temp", "web", 0);
               
                // make sure you only answer after the agent was completely deleted
                while (BaseCentralisedMAS.getRunner().getAg("temp") != null)
                    ;
            }
        }
    }

    /**
     * Return a TEXT PLAIN of available internal action, external actions and
     * commands for the given agent Example:
     * "['.desire','.drop_desire','.drop_all_desires']"
     * 
     * @param agName Name of the agent
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/code")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCodeCompletionSuggestions(@PathParam("agentname") String agName) {
        Map<String,String> commands = new HashMap<>();
        try {
            // get internal actions
            getPlansSuggestions(agName, commands);
            // get internal actions
            getIASuggestions(commands);
            // get external actions
            getEASuggestions(agName, commands);

            Gson json = new Gson();
            Map<String,String> sortedCmds = new TreeMap<>(commands);
            return Response.ok(json.toJson(sortedCmds)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500, "Server Internal Error! Could not get code completion suggestions.").build();
    }
    
    public void getPlansSuggestions(String agName, Map<String, String> commands) {
        try {
            // get agent's plans
            Agent ag = tAg.getAgent(agName);
            if (ag != null) {
                PlanLibrary pl = ag.getPL();
                for (Plan plan : pl.getPlans()) {
                    
                    // do not add plans that comes from jar files (usually system's plans)
                    if (plan.getSource().startsWith("jar:file") || plan.getSource().equals("kqmlPlans.asl"))
                        continue;

                    // add namespace when it is not default
                    String ns = "";
                    if (!plan.getNS().equals(Literal.DefaultNS)) {
                        ns = plan.getNS().toString() + "::";
                    }

                    String terms = "";
                    if (plan.getTrigger().getLiteral().getArity() > 0) {
                        for (int i = 0; i < plan.getTrigger().getLiteral().getArity(); i++) {
                            if (i == 0)
                                terms = "(";
                            terms += plan.getTrigger().getLiteral().getTerm(i).toString();
                            if (i < plan.getTrigger().getLiteral().getArity() - 1)
                                terms += ", ";
                            else
                                terms += ")";
                        }
                    }

                    // when it is a goal or test goal, do not add operator
                    if ((plan.getTrigger().getType() == TEType.achieve)
                            || (plan.getTrigger().getType() == TEType.test)) {


                        commands.put(ns + plan.getTrigger().getType().toString()
                        + plan.getTrigger().getLiteral().getFunctor() + terms, "");

                    }
                    // when it is belief, do not add type which is anyway empty
                    else if (plan.getTrigger().getType() == TEType.belief) {
                        commands.put(ns + plan.getTrigger().getOperator().toString()
                                + plan.getTrigger().getLiteral().getFunctor() + terms, "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getEASuggestions(String agName, Map<String, String> commands) throws CartagoException {
        /*TODO: fix getCartagoArch is not exposed
        try {
            Agent ag = tAg.getAgent(agName);
            // get external actions (from focused artifacts)
            CAgentArch cartagoAgArch = tAg.getCartagoArch(ag);
            for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
                String wksName = wid.getName();
                for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {

                    // operations
                    ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

                    info.getObservers().forEach(y -> {
                        if (y.getAgentId().getAgentName().equals(agName)) {
                            info.getOperations().forEach(z -> {
                                String params = "";
                                for (int i = 0; i < z.getOp().getNumParameters(); i++) {
                                    if (i == 0) params = "(";
                                    params += "arg" + i;
                                    if (i == z.getOp().getNumParameters() - 1)
                                        params += ")";
                                    else
                                        params += ", ";
                                }

                                commands.put(z.getOp().getName() + params, "");
                            });
                        }
                    });

                }
            }
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        */
    }
    
    /**
     * Get list of internal actions for an agent
     * 
     * @return List of internal actions
     */
    
    public void getIASuggestions(Map<String,String> cmds) {
        /* TODO: fix fail on adding google guava
        try {
            ClassPath classPath = ClassPath.from(print.class.getClassLoader());
            Set<ClassInfo> allClasses = classPath.getTopLevelClassesRecursive("jason.stdlib");

            allClasses.forEach(a -> {
                try {
                    Class<?> c = a.load();
                    if (c.isAnnotationPresent(jason.stdlib.Manual.class)) {
                        // add full predicate provided by @Manual
                        jason.stdlib.Manual annotation = (jason.stdlib.Manual) c
                                .getAnnotation(jason.stdlib.Manual.class);
                        cmds.put(annotation.literal(), annotation.hint().replaceAll("\"", "`").replaceAll("'", "`"));
                    } else {
                        // add just the functor of the internal action
                        cmds.put("." + a.getSimpleName(), "");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    */
    }
    
    private void lookForCollaborativeExceptions(Agent ag) throws UnderstandabilityException, UsefulnessException {
        PlanLibrary pl = ag.getPL();
        for (Plan plan : pl.getPlans()) {
            
            // skip plans from jar files (usually system's plans)
            if (plan.getSource().startsWith("jar:file") || plan.getSource().equals("kqmlPlans.asl"))
                continue;

            //TODO: A .send can also be in the context of a plan
            
            PlanBody pb = plan.getBody();
            while (pb != null) {
                Term t = pb.getBodyTerm();
                //Check if the parsed code has any .send internal action
                if (t.isInternalAction() && ((InternalActionLiteral)t).getFunctor().equals(".send"))
                {
                    // If the performative is achieve, check if the recipient has a plan to execute
                    Term achieve = new Atom("achieve");
                    if (((InternalActionLiteral)t).getTerm(1).equals(achieve)) {
                        checkUnderstandability(t);
                    }
                    
                    // If the performative is achieve, check if the recipient use this info in anyway
                    Term tell = new Atom("tell");
                    if (((InternalActionLiteral)t).getTerm(1).equals(tell)) {
                        checkUsefulness(t);
                    }
                }
                pb = pb.getBodyNext();
            }
        }
    }

    private void checkUnderstandability(Term t) throws UnderstandabilityException {
        String recipientName = ((InternalActionLiteral)t).getTerm(0).toString();
        Agent recipient = tAg.getAgent(recipientName);
        Literal request = (Literal)((InternalActionLiteral)t).getTerm(2);
        PlanLibrary rpl = recipient.getPL();

        boolean recipientUnderstand = false;
        for (Plan rp : rpl.getPlans()) {
            // skip plans from jar files (usually system's plans)
            if (rp.getSource().startsWith("jar:file") || rp.getSource().equals("kqmlPlans.asl"))
                continue;

            //TODO: Check if the arity is also compatible
            if (rp.getTrigger().getLiteral().getFunctor().equals(request.getFunctor())) 
                recipientUnderstand = true;
        }
        if (!recipientUnderstand) 
            throw new UnderstandabilityException("Agent '" + recipientName + "' doesn't understand '"
                    + request.getFunctor() + "'");
    }
    
    private void checkUsefulness(Term t) throws UsefulnessException {
        String recipientName = ((InternalActionLiteral)t).getTerm(0).toString();
        Agent recipient = tAg.getAgent(recipientName);
        Literal request = (Literal)((InternalActionLiteral)t).getTerm(2);
        PlanLibrary rpl = recipient.getPL();

        boolean recipientUnderstand = false;
        
        for (Plan rp : rpl.getPlans()) {
            // skip plans from jar files (usually system's plans)
            if (rp.getSource().startsWith("jar:file") || rp.getSource().equals("kqmlPlans.asl"))
                continue;

            //TODO: Check if the arity is also compatible
            if (rp.getTrigger().getLiteral().getFunctor().equals(request.getFunctor())) 
                recipientUnderstand = true;
        }
        
        if (!recipientUnderstand) 
            throw new UsefulnessException("Agent '" + recipientName + "' doesn't use '"
                    + request.getFunctor() + "'");
    }
    
    /**
     * Kill all agents.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response killAllAgents() {
        try {
            ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getRuntimeServices().getAgentsNames().forEach(a -> {
                ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getRuntimeServices().killAgent(a, a, 0);

                // make sure you only answer after the agent was completely deleted
                while (((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getAg(a) != null)
                    ;

                // make sure agent's body was disposed
                TranslEnv tEnv = new TranslEnv();
                for (String wrksName : tEnv.getWorkspaces()) {
                    try {
                        for (ArtifactId aid : CartagoService.getController(wrksName).getCurrentArtifacts()) {
                            if (aid.getName().equals(a + "-body"))
                                CartagoService.getController(wrksName).removeArtifact(aid.getName());
                        }
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    }
                }
            });
            return Response.ok().entity("Agents deleted!").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }
}
