package jacamo.web;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
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
        
        // copy to 'menucontent' the menu to show on drop down main page menu
        so.append("	<script language=\"JavaScript\">\n");
        so.append("		var buttonClose = \"<label for='doc-drawer-checkbox' class='button drawer-close'></label>\";\n"); 
        so.append("		var pageContent = document.getElementById(\"nav-drawer-frame\").innerHTML;\n"); 
        so.append("		var fullMenu = `${buttonClose} ${pageContent}`;\n");
        so.append("		sessionStorage.setItem(\"menucontent\", fullMenu);\n");
        // function to run Jason commands
        so.append("		function runCMD() {\n" +
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
                "        h2.open('DELETE', '/agents/"+selectedItem+"', false); \n" +
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
        so.append("    function newAg() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.open(\"POST\", '/agents/'+document.getElementById('createAgent').value, false); \n" +
                "        http.send();\n"+
                "        window.location.href = '/agents/'+document.getElementById('createAgent').value+'/mind';\n"+
                "	}\n"); 
        so.append("// Expand/collapse function\n" + 
                "var xa = document.getElementById('expAll');\n" + 
                "xa.addEventListener('click', function(e) {\n" + 
                "  e.target.classList.toggle('exp');\n" + 
                "  e.target.classList.toggle('col');\n" + 
                "  var details = document.querySelectorAll('details');\n" + 
                "  Array.from(details).forEach(function(obj, idx) {\n" + 
                "    if (e.target.classList.contains('exp')) {\n" + 
                "      obj.open = true;\n" + 
                "    } else {\n" + 
                "      obj.open = false;\n" + 
                "    }\n" + 
                "  });\n" + 
                "}, false);\n");
        so.append("	</script>\n");
        so.append("</html>\n");
        
        return so.toString();
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentsHtml() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("<div id=\"getting-started\" class=\"card fluid\">\n"); 
        mainContent.append("	<h2 class=\"section double-padded\">Getting started</h2>\n"); 
        mainContent.append("	<div class=\"section\">\n"); 
        mainContent.append("		<p>\n");
        mainContent.append("			<a href=\"http://jason.sourceforge.net/\" target=\"_blank\">Jason</a> is an interpreter for an extended version of AgentSpeak. It implements the operational semantics of that language, ");
        mainContent.append("			and provides a platform for the development of multi-agent systems, with many user-customisable features. Jason is ");
        mainContent.append("			available as <a href=\"https://github.com/jason-lang/jason\" target=\"_blank\">open-source</a>, and is distributed under GNU LGPL.\n");
        mainContent.append("		</p>\n"); 
        mainContent.append("		<br/>\n");
        mainContent.append("		<p>\n");
        mainContent.append("			Using command text box you can send orders to the agents, change plans, add and \n");
        mainContent.append("			remove beliefs and so on, just using <a href=\"http://jason.sf.net\" target=\"_blank\">Jason</a>'s AgentSpeak sentences.</p>\n"); 
        mainContent.append("		</p>\n"); 
        mainContent.append("		<br/>\n");
        mainContent.append("		<p>\n");
        mainContent.append("			You can access the <a href=\"/services\" target='mainframe'>directory facilitator</a> to check which registered services the agents are providing.");
        mainContent.append("			You can also <a href=\"/agents/forms/new_agent\" target='mainframe'>create</a> a new agent and kill some existing one.\n"); 
        mainContent.append("			<mark class=\"do\">Attention:</mark> killing agents may cause data loss. Make sure you don't need the data or the agent is using persistent belief base.\n"); 
        mainContent.append("		</p>\n");
        mainContent.append("		<br/>\n");
        mainContent.append("	</div>\n");
        mainContent.append("</div>\n");

        return designPage("JaCamo-Rest - Agents","",mainContent.toString());
    }

    public String getAgentsMenu(String selectedAgent) {
        StringWriter so = new StringWriter();

        so.append("<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n"); 
        so.append("<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("	</br>\n"); 

        if (JCMRest.getZKHost() == null) {
            for (String a : BaseCentralisedMAS.getRunner().getAgs().keySet()) {
                so.append("	<a href=\"/agents/" + a + "/mind\" id=\"link-to-" + a + "-mind\" target='mainframe'>" + a + "</a>\n");
                if (a.equals(selectedAgent)) {
                    so.append("	<a href=\"/agents/" + a + "/mind#overview\" id=\"link-to-overview\" target='mainframe'>.  Overview</a>\n");
                    so.append("	<a href=\"/agents/" + a + "/mind#mind\" id=\"link-to-mind\" target='mainframe'>.  Mind</a>\n");
                    so.append("	<a href=\"/agents/" + a + "/mind#uploadplans\" id=\"link-to-uploadplans\" target='mainframe'>.  Upload plans</a>\n");
                    so.append("	<a href='kill' onclick='killAg()'>.  kill this agent</a>\n");
                }
            }
        } else {
            // get agents from ZK
            try {
                for (String a : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {
                    String url = new String(JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId + "/" + a));
                    so.append("	<a href=\"" + url + "/mind\" id=\"link-to-" + a + "-mind\" target='mainframe'>" + a + "</a>\n");
                    if (a.equals(selectedAgent)) {
                        so.append("	<a href=\"/agents/" + a + "/mind#overview\" id=\"link-to-overview\" target='mainframe'>.  Overview</a>\n");
                        so.append("	<a href=\"/agents/" + a + "/mind#mind\" id=\"link-to-mind\" target='mainframe'>.  Mind</a>\n");
                        so.append("	<a href=\"/agents/" + a + "/mind#uploadplans\" id=\"link-to-uploadplans\" target='mainframe'>.  Upload plans</a>\n");
                        so.append("	<a href='kill' onclick='killAg()'>.  kill this agent</a>\n");
                    }
                    Agent ag = getAgent(a);
                    if (ag != null)
                        createAgLog(a, ag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }                
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

    @Path("/{agentname}/collapse")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setHide(@PathParam("agentname") String agName) {

        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/mind'\" /></head>ok";
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
        mainContent.append("    <div class=\"section\">\n");
        mainContent.append("        <input style=\"width: 100%; margin: 0px;\" placeholder=\"Command (" + helpMsg1 + ") ...\"\n"); 
        mainContent.append("        type=\"text\" id=\"inputcmd\" onkeydown=\"if (event.keyCode == 13) runCMD()\">\n");
        mainContent.append("    </div>\n");
        // if log is not empty
        if (!getLogOutput(agName).equals("")) {
            mainContent.append("    <div class=\"section\">\n");
            mainContent.append("      <pre><span id='log'></span></pre>");
            mainContent.append("      <span id='plog'></span>");
            mainContent.append("    </div>\n"); 
        }
        mainContent.append("</div>\n");
        
        // overview
        mainContent.append("<div id=\"overview\" class=\"card fluid\">\n"); 
        mainContent.append("    <div class=\"section\">\n");
        mainContent.append("        <center><img src='mind/img.svg'/></center><br/>\n");
        mainContent.append("    </div>\n");
        mainContent.append("</div>\n");
        
        
        // details
        mainContent.append("<div id=\"mind\" class=\"card fluid\">\n");
        mainContent.append("    <div class=\"section\">\n"); 
        mainContent.append("        <a href='#/' id='expAll' class='exp'>Expand/Collapse </a>\n"); 

        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = new asl2html("/xml/agInspection.xsl");
            }
            for (String p : show.keySet())
                mindInspectorTransformerHTML.setParameter("show-" + p, show.get(p) + "");
            Agent ag = getAgent(agName);
            if (ag != null) {
                mainContent.append(mindInspectorTransformerHTML.transform(ag.getAgState()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // transform to HTML
        
        // put plans on agent's mind section
        mainContent.append("        <details>\n");
        mainContent.append("            <summary style=\"text-align: left; color: blue; font-family: arial\">Agent's plans</summary>\n");
        mainContent.append("            <embed src='plans' width=\"100%\"/>\n");
        mainContent.append("        </details>\n");

        mainContent.append("    </div>\n"); 
        mainContent.append("    <div class=\"section\">\n"); 
        if (show.get("annots")) {
            mainContent.append("        <a href='hide?annots'     style='font-family: arial; text-decoration: none'>hide annotations</a>\n");              
        } else {
            mainContent.append("        <a href='show?annots'     style='font-family: arial; text-decoration: none'>show annotations</a>\n");                              
        }
        mainContent.append("    </div>\n"); 
        mainContent.append("</div>\n"); 
        
        // upload plans
        mainContent.append("<div id=\"uploadplans\" class=\"card fluid\">\n");
        mainContent.append("    <div class=\"section\">\n"); 
        mainContent.append("        <embed src='load_plans_form/' width=\"100%\" height=\"180px\"/>\n");
        mainContent.append("    </div>\n"); 
        mainContent.append("</div>\n"); 
        
        return designPage("JaCamo-Rest - Agents: " + agName,agName,mainContent.toString());
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
        return  "<html><head><title>load plans for "+agName+"</title><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\"></head>"+
                "<form action=\"/agents/"+agName+"/plans\" method=\"post\" id=\"usrform\" enctype=\"multipart/form-data\">" +
                "<textarea name=\"plans\" form=\"usrform\" placeholder=\"Write Jason plans here...\" style=\"width:99%; overflow: auto;\"></textarea>" +
                "<br/>or upload a file: <input type=\"file\" name=\"file\"><input type=\"submit\" value=\"Upload\"></form></html>";
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

    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("<div id=\"create-agent\" class=\"card fluid\">\n"); 
        mainContent.append("	<h2 class=\"section double-padded\">Create agent</h2>\n"); 
        mainContent.append("	<div class=\"section\">\n"); 
        mainContent.append("		<p>\n");
        mainContent.append("			<input style=\"width: 100%; margin: 0px;\" placeholder=\"enter agent's name ...\"\n"); 
        mainContent.append("			type=\"text\" id=\"createAgent\" onkeydown=\"if (event.keyCode == 13) newAg()\">\n");
        mainContent.append("		</p>\n");
        mainContent.append("		<br/>\n");
        mainContent.append("	</div>\n");
        mainContent.append("</div>\n");

        return designPage("JaCamo-Rest - Agents: new agent","",mainContent.toString());
    }

    protected String getAgAsDot(String agName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        try {

            StringBuilder sb = new StringBuilder();

            // get workspaces the agent are in (including organizations)
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
                                        sb.append("\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName() + "/img.svg\"\n");
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
