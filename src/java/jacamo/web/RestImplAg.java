package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

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
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.util.Config;
import jason.util.asl2html;
import ora4mas.nopl.GroupBoard;


@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentsHtml() {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>JaCamo-Rest - Agents</title>\n");
        //so.append("<meta http-equiv=\"refresh\" content=\"3\"/>");
        so.append("		<style>\n");
        so.append(getStyleCSS() + "\n");
        so.append("		</style>\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append(getAgentsMenu(""));                
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-9 col-lg-9\" id=\"doc-content\">\n"); 
        so.append("					<div id=\"getting-started\" class=\"card fluid\">\n"); 
        so.append("						<h2 class=\"section double-padded\">Getting started</h2>\n"); 
        so.append("						<div class=\"section\">\n"); 
        so.append("							<p>\n" +
                  "								If you have any agent running you can click on its name and watch its mind, check relation and more.<br/>\n" +
                  "								Using command text box you can send order to the agents, change plans, add and \n" +
                  "								remove beliefs, just using <a href=\"http://jason.sf.net\" target=\"_blank\">Jason</a>'s AgentSpeak sentences.</p>\n" + 
                  "							</p>\n" + 
                  "							<br/>\n");
        so.append("							<p>\n" +
                  "								You can <a href=\"/forms/new_agent\" target='mainframe'>create</a> a new agent and access the"
                  + "<a href=\"/services\" target='mainframe'>directory facilitator</a>.\n" + 
                  "							</p>\n" +
                  "							<br/>\n");
        so.append("						</div>\n");
        so.append("					</div>\n");
        so.append("				</main>\n"); 
        so.append("			</div>\n"); 
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        // copy to 'menucontent' the menu to show on drop down main page menu
//        so.append("   <script>\n");
//        so.append("       var pageContent = document.getElementById(\"nav-drawer\").innerHTML;\n");
//        so.append("       sessionStorage.setItem(\"menucontent\", pageContent);\n");
//        so.append("   </script>\n");
        so.append("</html>\n");
        return so.toString();
    }

    public String getAgentsMenu(String selectedAgent) {
        StringWriter so = new StringWriter();

        so.append("				<input id=\"doc-drawer-checkbox\" class=\"drawer\" value=\"on\" type=\"checkbox\">\n"); 
        so.append("				<nav class=\"col-xs-12 col-sm-12 col-md-3 col-lg-3\" id=\"nav-drawer\">\n");
        so.append("					<label for=\"doc-drawer-checkbox\" class=\"button drawer-close\"></label>\n"); 
        //so.append("                   <h3>Agents</h3>\n"); 

        if (JCMRest.getZKHost() == null) {
            for (String a : BaseCentralisedMAS.getRunner().getAgs().keySet()) {
                so.append("					<a href=\"/agents/" + a + "/mind\" id=\"link-to-" + a + "-mind\" target='mainframe'>" + a + "</a>\n");
                if (a.equals(selectedAgent)) {
                    so.append("					<a href=\"#overview\" id=\"link-to-overview\">.  Overview</a>\n");
                    so.append("					<a href=\"#mind\" id=\"link-to-mind\">.  Mind</a>\n");
                    so.append("					<a href=\"#uploadplans\" id=\"link-to-uploadplans\">.  Upload plans</a>\n");
                    so.append("					<a href='kill' onclick='killAg()'>.  kill this agent</a>\n");
                }
            }
        } else {
            // get agents from ZK
            try {
                for (String a : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {
                    String url = new String(JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId + "/" + a));
                    so.append("					<a href=\"" + url + "/mind\" id=\"link-to-" + a + "-mind\" target='mainframe'>" + a + "</a>\n");
                    if (a.equals(selectedAgent)) {
                        so.append("					<a href=\"#overview\" id=\"link-to-overview\">.  Overview</a>\n");
                        so.append("					<a href=\"#mind\" id=\"link-to-mind\">.  Mind</a>\n");
                        so.append("					<a href=\"#uploadplans\" id=\"link-to-uploadplans\">.  Upload plans</a>\n");
                        so.append("					<a href='kill' onclick='killAg()'>.  kill this agent</a>\n");
                    }
                    Agent ag = getAgent(a);
                    if (ag != null)
                        createAgLog(a, ag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }                
        so.append("				</nav>\n");
        return so.toString();
    }
    
    //TODO: @Path("css/style.css")???
    //@GET
    //@Produces(MediaType.TEXT_PLAIN)
    public String getStyleCSS() {
        StringBuilder so = new StringBuilder();
        Locale loc = new Locale("en", "US");
        try (Scanner scanner = new Scanner(new FileInputStream("src/resources/css/style.css"), "UTF-8")) {
            scanner.useLocale(loc);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                so.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    protected asl2html  mindInspectorTransformerHTML = null;
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
        StringWriter so = new StringWriter();
        
        so.append("<!DOCTYPE html>\n"); 
        so.append("<html lang=\"en\" target=\"mainframe\">\n"); 
        so.append("	<head>\n");
        so.append("		<title>JaCamo-Rest - Agents</title>\n");
        //so.append("<meta http-equiv=\"refresh\" content=\"3\"/>");
        so.append("		<style>"+getStyleCSS()+"</style>\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n");
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 

        so.append(getAgentsMenu(agName));  
        
        // agent's content
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-9 col-lg-9\" id=\"doc-content\">\n"); 

        // command box
        so.append("					<div id=\"command\" class=\"card fluid\">\n"); 
        so.append("						<div class=\"section\">\n");
        so.append("							<input style=\"width: 100%; margin: 0px;\" placeholder=\"Command... (e.g. +price(10.00), -+bestSupplier(bob), !goHome)\"\n"); 
        so.append("							type=\"text\" id=\"inputcmd\" onkeydown=\"if (event.keyCode == 13) runCMD()\">\n");
        so.append("						</div>\n"); 
        so.append("						<div class=\"section\">\n");
        //so.append("                           <code><span id='display'></span></code>");
        so.append("							<pre><span id='log'></span></pre>");
        so.append("							<span id='plog'></span>");
        so.append("						</div>\n"); 
        so.append("					</div>\n");
        
        // overview
        so.append("					<div id=\"overview\" class=\"card fluid\">\n"); 
        so.append("						<div class=\"section\">\n");
        so.append("							<center><img src='mind/img.svg'/></center><br/>\n");
        so.append("						</div>\n");
        so.append("					</div>\n");
        
        // details
        so.append("					<div id=\"mind\" class=\"card fluid\">\n" + 
                  "						<div class=\"section\">\n"); 
        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = new asl2html("/xml/agInspection.xsl");
            }
            for (String p : show.keySet())
                mindInspectorTransformerHTML.setParameter("show-" + p, show.get(p) + "");
            Agent ag = getAgent(agName);
            if (ag != null) {
                so.append(mindInspectorTransformerHTML.transform(ag.getAgState()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // transform to HTML
        
        // put plans on agent's mind section
        so.append("							<details>\n");
        so.append("								<summary style=\"text-align: left; color: blue; font-family: arial\">Agent's plans</summary>\n");
        so.append("								<embed src='plans/' width=\"100%\"/>\n");
        so.append("							</details>\n");

        so.append("						</div>\n"); 
        so.append("						<div class=\"section\">\n"); 
        if (show.get("annots")) {
            so.append("							<a href='hide?annots'     style='font-family: arial; text-decoration: none'>hide annotations</a>\n");              
        } else {
            so.append("							<a href='show?annots'     style='font-family: arial; text-decoration: none'>show annotations</a>\n");                              
        }
        so.append("						</div>\n"); 
        so.append("					</div>\n"); 
        
        so.append("					<div id=\"uploadplans\" class=\"card fluid\">\n");
        so.append("						<div class=\"section\">\n"); 
        so.append("							<embed src='load_plans_form/' width=\"100%\"/>\n");
        so.append("						</div>\n"); 
        so.append("					</div>\n"); 
        so.append("				</main>\n"); 
        so.append("			</div>\n");
        so.append("		</div>\n");
        so.append("		<script language=\"JavaScript\">\n");
        // function to run Jason commands
        so.append("    function runCMD() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.onreadystatechange = function() { \n" + 
                "          if (http.readyState == 4 && http.status == 200) {\n" +
                "                location.reload();\n" + 
                "                document.getElementById('display').innerHTML = \"  result: \" + http.responseText;\n" + 
                "        } }\n" + 
                "        http.open(\"POST\", \"cmd\", true); // true for asynchronous \n" +
                "        http.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");\n" +
                "        data = 'c='+encodeURIComponent(document.getElementById(\"inputcmd\").value); \n"+
                //"        document.getElementById('debug').innerHTML = data\n" + 
                "        http.send(data);\n"+
                "    }\n" + 
                "    function killAg() {\n" +
                "        h2 = new XMLHttpRequest();\n" + 
                "        h2.open('DELETE', '/agents/"+agName+"', false); \n" +
                "        h2.send(); \n" +
                "    }\n" + 
                "    function delLog() {\n" +
                "        h2 = new XMLHttpRequest();\n" + 
                "        h2.open('DELETE', 'log', false); \n" +
                "        h2.send(); \n" +
                "    }\n" + 
                "    function showLog() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.onreadystatechange = function() { \n" + 
                "          if (http.readyState == 4 && http.status == 200) {\n" +
                "                document.getElementById('log').innerHTML = http.responseText;\n" + 
                "                if (http.responseText.length > 1) {\n" + 
                "                      var btn = document.createElement(\"BUTTON\"); \n" + 
                "                      var t = document.createTextNode(\"clear log\");\n" + 
                "                      btn.appendChild(t); \n" +
                "                      btn.onclick = function() { delLog(); location.reload(); }; \n" +
                "                      document.getElementById('plog').appendChild(btn);  "+
                "                }\n" + 
                "        } }\n" + 
                "        http.open('GET', 'log', true); \n" +
                "        http.send();\n"+
                "    }\n" + 
                "    showLog(); \n");
        // copy to 'menucontent' the menu to show on drop down main page menu
//        so.append("           var pageContent = document.getElementById(\"nav-drawer\").innerHTML;\n"); 
//        so.append("           sessionStorage.setItem(\"menucontent\", pageContent);\n");
        so.append("		</script>\n");
        so.append("	</body>\n");
        so.append("</html>\n");
        return so.toString();
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
        return  "<html><head><title>load plans for "+agName+"</title></head>"+
                "<form action=\"/agents/"+agName+"/plans\" method=\"post\" id=\"usrform\" enctype=\"multipart/form-data\">" +
                "Enter Jason code below:<br/><textarea name=\"plans\" form=\"usrform\" placeholder=\"Write plans here...\" rows=\"5\" cols=\"62\"></textarea>" +
                "<br/>or upload a file: <input type=\"file\" name=\"file\"><br/><input type=\"submit\" value=\"Upload it\">"+
                "</form></html>";
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
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+agName+"/mind'\" /></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }

    @Path("/{agentname}/mind")
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

    Map<String, StringBuilder> agLog = new HashMap<>();
    
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

            // get workspaces the agent are in (including organizations)
            Set<String> workspacesIn = new HashSet<>();
            Agent ag = getAgent(agName);
            /*if (ag != null) {
                Iterator<Literal> i = ag.getBB().getPercepts();
                while (i.hasNext()) {
                    Literal belief = i.next();
                    if (belief.getFunctor().equals("joined")) {
                        workspacesIn.add(belief.getTerm(0).toString()); //belief.substring("joined(".length(), belief.indexOf(",")));
                    }
                }
            }*/


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
                sb.append("\t\tlabel=\"mind\"\n");
                sb.append("\t\tlabeljust=\"r\"\n");
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
                        sb.append("\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
                        sb.append("\n\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                        sb.append("\t];\n");
                        sb.append("\t\"" + gb.getArtId() + "\"->\"" + agName + "\" [arrowtail=none dir=back]\n");
                        gb.getGrpState().getPlayers().forEach(p -> {
                            if (p.getAg().equals(agName)) {
                                sb.append("\t\"" + p.getTarget() + "\" [ " + "\n\t\tlabel = \"" + p.getTarget() + "\"");
                                sb.append("\n\t\tshape=box style=\"filled,rounded\" fillcolor=white\n");
                                sb.append("\t];\n");
                                sb.append("\t\"" + p.getTarget() + "\"->\"" + gb.getArtId() + "\" [arrowtail=none dir=back label=\"plays\"]\n");
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
                                        sb.append("\t\t\tURL=\"[../../workspaces/" + wksName + "/" + info.getId().getName() + "/img.svg]\"\n");
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

            // for debug
            try (FileWriter fw = new FileWriter("graph.gv", false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                out.print(graph);
                out.flush();
                out.close();
            } catch (Exception ex) {
            }
        } finally {
            return graph;
        }
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
