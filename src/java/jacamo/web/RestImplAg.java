package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jaca.CAgentArch;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEType;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.stdlib.print;
import jason.util.Config;
import ora4mas.nopl.GroupBoard;

@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

    Map<String, StringBuilder> agLog = new HashMap<>();
    
    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }
    
    public String designPage(String title, String selectedItem, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta http-equiv=\"Content-type\" name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append(              getAgentsMenu(selectedItem));
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-10 col-lg-10\" id=\"doc-content\">\n"); 
        so.append(                  mainContent);
        so.append("				</main>\n"); 
        so.append("			</div>\n"); 
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        so.append("<script src=\"/js/agent.js\"></script>\n");
        so.append("</html>\n");
        
        return so.toString();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentsHtml() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("<div id=\"getting-started\" class=\"card fluid\">\n"); 
        mainContent.append("  <h4 class=\"section double-padded\">getting started</h4>\n"); 
        mainContent.append("  <div class=\"section\">\n"); 
        mainContent.append("      <p>\n");
        mainContent.append("          <a href=\"http://jason.sourceforge.net/\" target=\"_blank\">Jason</a> is an interpreter for an extended version of AgentSpeak. It implements the operational semantics of that language, ");
        mainContent.append("          and provides a platform for the development of multi-agent systems, with many user-customisable features. Jason is ");
        mainContent.append("          available as <a href=\"https://github.com/jason-lang/jason\" target=\"_blank\">open-source</a>, and is distributed under GNU LGPL.\n");
        mainContent.append("      </p>\n"); 
        mainContent.append("      <br/>\n");
//        mainContent.append("      <p>\n");
//        mainContent.append("          Using command text box you can send orders to the agents, change plans, add and \n");
//        mainContent.append("          remove beliefs and so on, just using <a href=\"http://jason.sf.net\" target=\"_blank\">Jason</a>'s AgentSpeak sentences.</p>\n"); 
//        mainContent.append("        </p>\n"); 
//        mainContent.append("        <br/>\n");
//        mainContent.append("        <p>\n");
//        mainContent.append("            You can access the <a href=\"/services\" target='mainframe'>directory facilitator</a> to check which registered services the agents are providing.");
//        mainContent.append("            You can also <a href=\"/forms/new_agent\" target='_top'>create</a> a new agent and kill some existing one.\n"); 
//        mainContent.append("            <mark class=\"do\">Attention:</mark> killing agents may cause data loss.\n"); 
//        mainContent.append("      </p>\n");
//        mainContent.append("      <br/>\n");
        mainContent.append("  </div>\n");
        mainContent.append("</div>\n");
        // overview
        mainContent.append("<div id=\"overview\" class=\"card fluid\">\n"); 
        mainContent.append("  <h4 class=\"section double-padded\">overview</h4>\n"); 
        mainContent.append("    <div class=\"section\">\n");
        mainContent.append("        <center><object data=\"/agents/img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");
        mainContent.append("    </div>\n");
        mainContent.append("</div>\n");
        // just to avoid html error generated by javascript method
        mainContent.append("    <div style=\"display:none\">\n");
        mainContent.append("        <input id=\"inputcmd\">\n");
        mainContent.append("    </div>\n");
        mainContent.append("</div>\n");

        return designPage("JaCamo-web - Agents","",mainContent.toString());
    }

    public String getAgentsMenu(String selectedAgent) {
        StringWriter so = new StringWriter();

        so.append("<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n"); 
        so.append("<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("	<br/>\n"); 

        if (JCMRest.getZKHost() == null) {
            Collection<String> agents = new TreeSet<String>(BaseCentralisedMAS.getRunner().getAgs().keySet());
            for (String a : agents) {
                if (a.equals(selectedAgent)) {
                    so.append("	<a href=\"/agents/" + a + "/mind\" id=\"link-to-" + a + "\" target='mainframe'><h5>. " + a + "</h5></a>\n");
                } else {
                    so.append("	<a href=\"/agents/" + a + "/mind\" id=\"link-to-" + a + "\" target='mainframe'><h5>+ " + a + "</h5></a>\n");
                }
            }
        } else {
            // get agents from ZK
            try {
                Collection<String> agents = new TreeSet<String>(JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId));
                for (String a : agents) {
                    String url = new String(JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId + "/" + a));
                    if (a.equals(selectedAgent)) {
                        so.append("	<a href=\"/agents/" + a + "/mind\" id=\"link-to-" + a + "\" target='mainframe'><h5>. " + a + "</h5></a>\n");
                    } else {
                        so.append("	<a href=\"" + url + "/mind\" id=\"link-to-" + a + "\" target='mainframe'><h5>+ " + a + "</h5></a>\n");
                    }
                    Agent ag = getAgent(a);
                    if (ag != null)
                        createAgLog(a, ag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }                
        so.append("<br/>");
        so.append("<br/>");
        so.append("<a href=\"/services\" target='mainframe'>directory facilitator</a>\n");
        so.append("<a href=\"/forms/new_agent\" target='_top'>create agent</a>\n"); 

        so.append("</nav>\n");
        return so.toString();
    }
    
    private Agent getAgent(String agName) {
        CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
    }

    /** AGENT **/

    protected Transformer  mindInspectorTransformerHTML = null;
    protected int MAX_LENGTH = 35;
    Map<String,Boolean> show = new HashMap<>();
    {
        //show.put("bels", true);
        show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
        //show.put("rules", false);
        //show.put("evt", true);
        //show.put("mb", true);
        //show.put("int", true);
        //show.put("int-details", false);
    }

    @Path("/{agentname}/hide")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setHide(@PathParam("agentname") String agName,
            @QueryParam("bels") String bels,
            @QueryParam("rules") String rules,
            @QueryParam("int-details") String intd,
            @QueryParam("annots") String annots) {
        if (bels != null) show.put("bels",false);
        if (rules != null) show.put("rules",false);
        if (intd != null) show.put("int-details",false);
        if (annots != null) show.put("annots",false);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/mind'\" /></head>ok";
    }

    @Path("/{agentname}/show")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setShow(@PathParam("agentname") String agName,
            @QueryParam("bels") String bels,
            @QueryParam("rules") String rules,
            @QueryParam("int-details") String intd,
            @QueryParam("annots") String annots) {
        if (bels != null) show.put("bels",true);
        if (rules != null) show.put("rules",true);
        if (intd != null) show.put("int-details",true);
        if (annots != null) show.put("annots",true);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/mind'\" /></head>ok";
    }

    static String helpMsg1 = "Example: +bel; !goal; .send(bob,tell,hello); +{+!goal <- .print(ok) });";


    @Path("/{agentname}")
    @POST
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    public String createNewAgent(@PathParam("agentname") String agName) { //@FormParam("name") String agName) {
        try {
            String name = BaseCentralisedMAS.getRunner().getRuntimeServices().createAgent(agName, null, null, null, null, null, null);
            BaseCentralisedMAS.getRunner().getRuntimeServices().startAgent(name);
            // set some source for the agent
            Agent ag = getAgent(name);
            ag.setASLSrc("no-inicial.asl");
            createAgLog(agName, ag);
            
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+name+"/mind'\" /></head>ok for "+name;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }
    
    @Path("/{agentname}/mind")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentHtml(@PathParam("agentname") String agName) {
        StringWriter mainContent = new StringWriter();

        // command box
        mainContent.append("<div id=\"command\" class=\"card fluid\">\n"); 
        //mainContent.append("    <div class=\"section\">\n");
        mainContent.append("<form autocomplete=\"off\" action=\"\">\n");
        mainContent.append("    <div class=\"autocomplete\">\n");
        mainContent.append("        <input style=\"width: 100%; margin: 0px;\" placeholder=\"Command (" + helpMsg1 + ") ...\"\n"); 
        mainContent.append("        type=\"text\" id=\"inputcmd\" onkeydown=\"if (event.keyCode == 13) {runCMD();} else {updateCmdCodeCompletion();}\">\n");
        mainContent.append("    </div>\n");
        mainContent.append("</form>\n");
        
        // if log is not empty (the element must exists as hidden to avoid httpresponse error
        if (!getLogOutput(agName).equals("")) {
            mainContent.append("    <div class=\"section\" style=\"display:block\">\n");
            mainContent.append("      <pre><span id='log'>log</span></pre>");
            mainContent.append("      <span id='plog'></span>");
            mainContent.append("    </div>\n"); 
        } else {
            mainContent.append("    <div class=\"section\" style=\"display:none\">\n");
            mainContent.append("      <pre><span id='log'>log</span></pre>");
            mainContent.append("      <span id='plog'></span>");
            mainContent.append("    </div>\n"); 
        }
        mainContent.append("</div>\n");
        
        // overview
        mainContent.append("<div id=\"overview\" class=\"card fluid\">\n"); 
        mainContent.append("    <div class=\"section\">\n");
        mainContent.append("        <center><object data=\"/agents/"+ agName + "/mind/img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");
        mainContent.append("    </div>\n");
        mainContent.append("</div>\n");
        
        
        // details
        mainContent.append("<div id=\"\" class=\"card fluid\">\n");
        mainContent.append("    <div class=\"section\">\n"); 
        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = TransformerFactory.newInstance().newTransformer(
                        new StreamSource(this.getClass().getResource("/xml/agInspection.xsl").openStream()));//new asl2html("/xml/agInspection.xsl");
            }
            for (String p : show.keySet())
                mindInspectorTransformerHTML.setParameter("show-" + p, show.get(p) + "");
            Agent ag = getAgent(agName);
            if (ag != null) {
                StringWriter so = new StringWriter();
                mindInspectorTransformerHTML.transform(new DOMSource(ag.getAgState()), new StreamResult(so) );
                mainContent.append(so.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // transform to HTML
        mainContent.append("    </div>\n"); 
        mainContent.append("</div>\n"); 
        
        // upload plans
        mainContent.append("<div id=\"uploadplans\" class=\"card fluid\">\n");
        mainContent.append("    <div class=\"section\">\n"); 
        mainContent.append("        <embed src='/agents/" + agName + "/load_plans_form/' width=\"100%\" height=\"400px\"/>\n");
        mainContent.append("    </div>\n"); 
        mainContent.append("</div>\n"); 
        
        mainContent.append("<div id=\"killagent\" class=\"card fluid\">\n");
        mainContent.append("    <div class=\"section\">\n"); 
        mainContent.append("        <a href='#' onclick='killAg(\"" + agName + "\")'><h5>kill this agent</h5></a>\n");
        mainContent.append("    </div>\n"); 
        mainContent.append("</div>\n"); 
        
        
        return designPage("JaCaMo-web - Agents: " + agName,agName,mainContent.toString());
    }

    @Path("/{agentname}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String killAgent(@PathParam("agentname") String agName) throws ReceiverNotFoundException {
        try {
            return "result of kill: "+BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent(agName,"web");
        } catch (Exception e) {
            return "Agent "+agName+" in unknown."+e.getMessage();
        }
    }

    @Path("/{agentname}/load_plans_form")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getLoadPlansForm(@PathParam("agentname") String agName) {
        return  "<html><head><title>load plans for "+agName+"</title><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">" +
                "<meta http-equiv=\"Content-type\" content=\"text/html,charset=UTF-8\"></head>" +
                "<script src=\"/js/codemirror.js\"></script>\n" +
                "<link rel=\"stylesheet\" href=\"/css/codemirror.css\">\n" +
                "<script src=\"/js/show-hint.js\"></script>\n" +
                "<script src=\"/js/erlang.js\"></script>\n" +
                "<script src=\"/js/placeholder.js\"></script>\n" +
                "<style>.CodeMirror pre.CodeMirror-placeholder { color: #999; }</style> " +
                "<form action=\"/agents/"+agName+"/plans\" method=\"post\" id=\"usrform\" enctype=\"multipart/form-data\">" +
                "<textarea name=\"planstextarea\" id=\"planstextarea\" form=\"usrform\" style=\"width:99%; overflow: auto;\"" + 
                "placeholder=\"Write Jason plans (ctrl+space gives code hinting).\">" +
                "</textarea>" +
                "<br/>or upload a file: <input type=\"file\" name=\"file\"><input type=\"submit\" value=\"Upload\"></form>" + 
                "<script src=\"/js/load_plans_form.js\"></script>\n" +
                "</html>";
    }

    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String loadPlans(@PathParam("agentname") String agName,
            @DefaultValue("") @FormDataParam("plans") String plans,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
            ) {
        try {
            String r = "nok";
            Agent ag = getAgent(agName);
            if (ag != null) {
                ag.parseAS(new StringReader(plans), "RrestAPI");
                ag.load(uploadedInputStream, "restAPI://"+fileDetail.getFileName());
                r = "ok, code uploaded!";
            }
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+agName+"/mind/'\"/></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }

    public List<String> getIAlist() {
        List<String> l = new ArrayList<>();
        try {
            ClassPath classPath = ClassPath.from(print.class.getClassLoader());
            Set<ClassInfo> allClasses = classPath.getTopLevelClassesRecursive("jason.stdlib");
            
            allClasses.forEach(a -> {l.add(a.getSimpleName());});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return l;
    }

    @Path("/{agentname}/code")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCodeCompletionSuggestions(@PathParam("agentname") String agName,
            @DefaultValue("all") @QueryParam("label") String label) {
        List<String> so = new ArrayList<>();
        try {
            // get agent's plans
            Agent ag = getAgent(agName);
            if (ag != null) {
                PlanLibrary pl = ag.getPL();
                for (Plan plan : pl.getPlans()) {
                    
                    // do not add plans that comes from jar files (usually system's plans)
                    if (plan.getFile().startsWith("jar:file")) continue;
                    
                    // add namespace when it is not default
                    String ns = "";
                    if (!plan.getNS().toString().equals("default")) {
                        ns = plan.getNS().toString() + "::";
                    }

                    // prepare terms to show between parenthesis plan_functor(Term1, Term2,...) 
                    String terms = "";
                    if (plan.getTrigger().getLiteral().getArity() > 0) {
                        for (int i = 0; i < plan.getTrigger().getLiteral().getArity(); i++) {
                            if (i == 0) terms = "(";
                            terms += plan.getTrigger().getLiteral().getTerm(i).toString();
                            if (i < plan.getTrigger().getLiteral().getArity() - 1) 
                                terms += ", "; 
                            else
                                terms += ")";
                        }
                    }

                    // do not add operator when type is achieve
                    if (plan.getTrigger().getType() == TEType.achieve)
                        so.add("'" + ns + plan.getTrigger().getType().toString()
                                + plan.getTrigger().getLiteral().getFunctor() + terms + "'");
                    // when it is belief, do not add type which is anyway empty
                    else if (plan.getTrigger().getType() == TEType.belief)
                        so.add("'" + ns + plan.getTrigger().getOperator().toString()
                                + plan.getTrigger().getLiteral().getFunctor() + terms + "'");
                    

                    // TODO: do nothing when type is TEType.test?
                }
            }
            
            // get internal actions
            getIAlist().forEach(a -> so.add("'." + a + "'"));
            
            //TODO: get external actions (from focused artifacts)
            
            //TODO: get beliefs add options to remove and to update

        } catch (Exception e) {
            e.printStackTrace();
            so.add("No code completion suggestions for " + agName);
        }
        Collections.sort(so);
        return so.toString();
        //return  "['.desire','.drop_desire','.drop_all_desires']";
    }

    @Path("/{agentname}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Document getAgentXml(@PathParam("agentname") String agName) {
        try {
            Agent ag = getAgent(agName);
            if (ag != null)
                return ag.getAgState();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Path("/{agentname}/plans")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAgentPlansTxt(@PathParam("agentname") String agName,
            @DefaultValue("all") @QueryParam("label") String label) {
        StringWriter so = new StringWriter();
        try {
            Agent ag = getAgent(agName);
            if (ag != null) {
                PlanLibrary pl = ag.getPL();
                if (label.equals("all"))
                    so.append(pl.getAsTxt(false));
                else
                    so.append(pl.get(label).toASString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            so.append("Agent "+agName+" does not exist or cannot be observed.");
        }
        return so.toString();
    }
 

    @Path("/{agentname}/cmd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String runCmdPost(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        String r = execCmd(agName, cmd.trim());
        addAgLog(agName, "Command "+cmd+": "+r);
        return r;
    }

    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogOutput(@PathParam("agentname") String agName) {
        StringBuilder o = agLog.get(agName);
        if (o != null) {
            return o.toString();
        }
        return "";
    }
    
    @Path("/{agentname}/log")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String delLogOutput(@PathParam("agentname") String agName) {
        agLog.put(agName, new StringBuilder());
        return "ok";
    }

    String execCmd(String agName, String sCmd) {
        try {
            if (sCmd.endsWith("."))
                sCmd = sCmd.substring(0,sCmd.length()-1);
            PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
            Trigger  te   = ASSyntax.parseTrigger("+!run_repl_expr");
            Intention i   = new Intention();
            i.push(new IntendedMeans(
                       new Option(
                           new Plan(null,te,null,lCmd),
                           new Unifier()),
                       te));

            Agent ag = getAgent(agName);
            if (ag != null) {
                TransitionSystem ts = ag.getTS();
                ts.getC().addRunningIntention(i);
                ts.getUserAgArch().wake();
                createAgLog(agName, ag);
                return "included for execution";
            } else {
                return "not implemented";
            }
        } catch (Exception e) {
            return("Error parsing "+sCmd+"\n"+e);
        }
    }

    protected void createAgLog(String agName, Agent ag) {
        // adds a log for the agent
        if (agLog.get(agName) == null) {
            agLog.put(agName, new StringBuilder());
            ag.getTS().getLogger().addHandler( new StreamHandler() {
                @Override
                public void publish(LogRecord l) {
                    addAgLog(agName, l.getMessage());
                }
            });
        }       
    }
    
    protected void addAgLog(String agName, String msg) {
        StringBuilder o = agLog.get(agName);
        if (o == null) {
            o = new StringBuilder();
            agLog.put(agName, o);
        }
        o.append(msg+"\n");     
    }
    
    
    // XML interface
    @Path("/{agentname}/mb")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public String addAgMsg(Message m, @PathParam("agentname") String agName) {
        CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
        if (a != null) {
            a.receiveMsg(m.getAsJasonMsg());
            return "ok";
        } else {
            return "receiver not found";
        }
    }
    
    @Path("/{agentname}/mind/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getAgentImg(@PathParam("agentname") String agName) {
        try {
            String dot = getAgAsDot(agName);
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

    protected String getAgAsDot(String agName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        try {

            StringBuilder sb = new StringBuilder();

            // get workspaces the agent are in (including organisations)
            Set<String> workspacesIn = new HashSet<>();
            Agent ag = getAgent(agName);
            CAgentArch cartagoAgArch = getCartagoArch(ag);
            for (WorkspaceId wid: cartagoAgArch.getSession().getJoinedWorkspaces()) {
                // TODO: revise whether the Set is necessary
                workspacesIn.add(wid.getName());
            }
            
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir=\"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");

            {// beliefs will be placed on the left
                sb.append("\tsubgraph cluster_mind {\n");
                sb.append("\t\tstyle=rounded\n");
                ag.getBB().getNameSpaces().forEach(x -> {
                    sb.append("\t\t\"" + x + "\" [ " + "\n\t\t\tlabel = \"" + x + "\"");
                    sb.append("\n\t\t\tshape=\"cylinder\" style=filled pencolor=black fillcolor=cornsilk\n");
                    sb.append("\t\t];\n");
                });
                sb.append("\t};\n");
                // just to avoid put agent node into the cluster
                ag.getBB().getNameSpaces().forEach(x -> {
                    sb.append("\t\"" + agName + "\"->\"" + x + "\" [arrowhead=none constraint=false style=dotted]\n");
                });
            }

            { // groups and roles are also placed on the left
                for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                    if (workspacesIn.contains(gb.getOEId())) {
                        gb.getGrpState().getPlayers().forEach(p -> {
                            if (p.getAg().equals(agName)) {
                                // role
                                sb.append("\t\"" + p.getTarget() + "\" [ " + "\n\t\tlabel = \"" + p.getTarget() + "\"");
                                sb.append("\n\t\tshape=box style=\"filled,rounded\" fillcolor=white\n");
                                sb.append("\t];\n");
                                sb.append("\t\"" + p.getTarget() + "\"->\"" + agName + "\" [arrowtail=normal dir=back]\n");
                                // group
                                sb.append("\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
                                sb.append("\n\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                                sb.append("\t];\n");
                                sb.append("\t\"" + gb.getArtId() + "\"->\"" + p.getTarget() + "\" [arrowtail=odiamond dir=back]\n");
                            }
                        });
                    }
                }
            }

            {// agent will be placed on center
                String s1 = (agName.length() <= MAX_LENGTH) ? agName : agName.substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\"" + agName + "\" [ " + "\n\t\tlabel = \"" + s1 + "\"");
                sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                sb.append("\t];\n");
            }

            { // workspances and artifacts the agents is focused on
                workspacesIn.forEach(w -> {
                    String wksName = w.toString();
                    try {
                        for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                            ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
                            info.getObservers().forEach(y -> {
                                if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
                                    ; // do not print system artifacts
                                } else {
                                    if (y.getAgentId().getAgentName().equals(agName)) {
                                        // create a cluster for each artifact even at same wks of other artifacts?
                                        sb.append("\tsubgraph cluster_" + wksName + " {\n");
                                        sb.append("\t\tlabel=\"" + wksName + "\"\n");
                                        sb.append("\t\tlabeljust=\"r\"\n");
                                        sb.append("\t\tgraph[style=dashed]\n");
                                        String str1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                                                : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                                        sb.append("\t\t\"" + info.getId().getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1
                                                + " :\\n");
                                        str1 = (info.getId().getArtifactType().length() <= MAX_LENGTH)
                                                ? info.getId().getArtifactType()
                                                : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
                                        sb.append(str1 + "\"\n");

                                        sb.append("\t\t\tshape=record style=filled fillcolor=white\n");
                                        sb.append("\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName() + "\"\n");
                                        sb.append("\t\t\ttarget=\"mainframe\"\n");
                                        sb.append("\t\t];\n");

                                        sb.append("\t};\n");

                                        sb.append("\t\"" + agName + "\"->\"" + info.getId().getName()
                                                + "\" [arrowhead=odot]\n");
                                    }
                                }
                            });
                        }
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    }
                });
            }

            sb.append("}\n");
            graph = sb.toString();

        } catch (Exception ex) {
        }
        
        return graph;
    }

    @Path("/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getMASImg() {
        try {
            String dot = getMASAsDot();
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

    protected String getMASAsDot() {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        try {

            StringBuilder sb = new StringBuilder();
            Set<String> allwks = new HashSet<>();

            Collection<String> agents = null;
            if (JCMRest.getZKHost() == null) {
                agents = new TreeSet<String>(BaseCentralisedMAS.getRunner().getAgs().keySet());
            } else {
                // get agents from ZK
                agents = new TreeSet<String>(JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId));
            }

            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir=\"TB\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");

            {// organisational dimension
                sb.append("\tsubgraph cluster_org {\n");
                sb.append("\t\tlabel=\"organisation\"\n");
                sb.append("\t\tlabeljust=\"r\"\n");
                sb.append("\t\tpencolor=gray\n");
                sb.append("\t\tfontcolor=gray\n");
                
                StringBuilder orglinks = new StringBuilder();
                
                { // groups and roles are also placed on the left
                    
                     // set to avoid to print roles and group twice if more than one agent is playing the same or in same group
                    Set<String> roles = new HashSet<>(); 
                    Set<String> groups = new HashSet<>(); 
                    
                    for (String a : agents) {
                        // get workspaces the agent are in (including organizations)
                        Set<String> workspacesIn = new HashSet<>();
                        Agent ag = getAgent(a);
                        CAgentArch cartagoAgArch = getCartagoArch(ag);
                        for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
                            // TODO: revise whether the Set is necessary
                            workspacesIn.add(wid.getName());
                        }
                        for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                            if (workspacesIn.contains(gb.getOEId())) {
                                gb.getGrpState().getPlayers().forEach(p -> {
                                    if (p.getAg().equals(a)) {
                                        // role
                                        sb.append("\t\t\"" + p.getTarget() + "\" [ " + "\n\t\tlabel = \"" + p.getTarget()
												+ "\"");
                                        sb.append("\n\t\t\tshape=box style=\"filled,rounded\" fillcolor=white\n");
                                        sb.append("\t\t];\n");
                                        roles.add(p.getTarget());
                                        
                                        // group
                                        sb.append("\t\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId()
                        						+ "\"");
                                        sb.append("\n\t\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                                        sb.append("\t\t];\n");
                                        groups.add(p.getTarget());
                                        
                                        orglinks.append("\t\"" + p.getTarget() + "\"->\"" + a
                        						+ "\" [arrowtail=normal dir=back label=role]\n");
                                        // group
                                        orglinks.append("\t\"" + gb.getArtId() + "\"->\"" + p.getTarget()
                        						+ "\" [arrowtail=odiamond dir=back]\n");
                                    }
                                });
                            }
                        }
                        sb.append("\t\t{rank=same " + roles.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "") + "};\n");
                        sb.append("\t\t{rank=same " + groups.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "") + "};\n");
                        
                        // for optimization purpose
                        allwks.addAll(workspacesIn);
                    }
                }
                
                sb.append("\t};\n");
                
                // links out of the subgraph to ensure it is not put inside
                sb.append(orglinks);
            }

            {// agents dimension
                sb.append("\tsubgraph cluster_ag {\n");
                sb.append("\t\tlabel=\"agents\"\n");
                sb.append("\t\tlabeljust=\"r\"\n");
                sb.append("\t\tpencolor=gray\n");
                sb.append("\t\tfontcolor=gray\n");
             
                {// agent's mind
                    for (String a : agents) {
                        String s1 = (a.length() <= MAX_LENGTH) ? a : a.substring(0, MAX_LENGTH) + " ...";
                        sb.append("\t\t\"" + a + "\" [ ");
                        sb.append("\n\t\t\tlabel = \"" + s1 + "\"");
                        sb.append("\n\t\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                        sb.append("\t\t];\n");
                    }

                    sb.append("\t\t{rank=same "
                            + agents.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "")
                            + "};\n");

                    sb.append("\t};\n");
                }
            }

            {// environment dimension
                StringBuilder envlinks = new StringBuilder();

                sb.append("\tsubgraph cluster_env {\n");
                sb.append("\t\tlabel=\"environment\"\n");
                sb.append("\t\tlabeljust=\"r\"\n");
                sb.append("\t\tpencolor=gray\n");
                sb.append("\t\tfontcolor=gray\n");
                allwks.forEach(w -> {
                    Set<String> wksartifacts = new HashSet<>(); 
                    String wksName = w.toString();
                    try {
                        if (CartagoService.getController(wksName).getCurrentArtifacts() != null) {
                            sb.append("\t\tsubgraph cluster_" + wksName + " {\n");
                            sb.append("\t\t\tlabel=\"" + wksName + "\"\n");
                            sb.append("\t\t\tlabeljust=\"r\"\n");
                            sb.append("\t\t\tstyle=dashed\n");
                            sb.append("\t\t\tpencolor=gray40\n");
                            sb.append("\t\t\tfontcolor=gray40\n");
                            for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                                ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
                                info.getObservers().forEach(y -> {
                                    if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
                                            || (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
                                            || (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
                                            || (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
                                            || (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
                                        ; // do not print system artifacts
                                    } else {
                                        // create a cluster for each artifact even at same wks of other artifacts?
                                        String str1 = (info.getId().getName().length() <= MAX_LENGTH)
                                                ? info.getId().getName()
                                                : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                                        sb.append("\t\t\t\"" + info.getId().getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1
												+ "\"\n");

                                        sb.append("\t\t\t\tshape=record style=filled fillcolor=white\n");
                                        sb.append("\t\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName()
												+ "\"\n");
                                        sb.append("\t\t\t\ttarget=\"mainframe\"\n");
                                        sb.append("\t\t\t];\n");

                                        wksartifacts.add(info.getId().getName());
                                        
                                        envlinks.append(
                                                "\t\t\"" + y.getAgentId().getAgentName() + "\"->\"" + info.getId().getName() + "\" [arrowhead=odot]\n");
                                    }
                                });
                            }
                            
                            //put artifacts of same wks at same line
                            sb.append("{rank=same " + wksartifacts.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "") + "};\n");
                            
                            sb.append("\t\t};\n");
                        }
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    }
                });
                sb.append("\t};\n");
                
                sb.append(envlinks);
            }

            sb.append("}\n");
            graph = sb.toString();
            
            // debug
            try (FileWriter fw = new FileWriter("graph.gv", false); 
                    BufferedWriter bw = new BufferedWriter(fw); 
                    PrintWriter out = new PrintWriter(bw)) {    
                out.print(graph);   
                out.flush();    
                out.close();    
            } catch (Exception ex) {    
            }

        } catch (Exception ex) {
        }
        
        return graph;
    }

    protected CAgentArch getCartagoArch(Agent ag) {
        AgArch arch = ag.getTS().getUserAgArch().getFirstAgArch();
        while (arch != null) {
            if (arch instanceof CAgentArch) {
                return (CAgentArch)arch;
            }
            arch = arch.getNextAgArch();
        }
        return null;
    }
}
