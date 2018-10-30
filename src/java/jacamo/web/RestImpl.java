package jacamo.web;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
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

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

import jason.ReceiverNotFoundException;
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


@Singleton
@Path("/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    // HTML interface
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getRootHtml() {
        StringWriter so = new StringWriter();
        so.append("<html><head><title>Jason Mind Inspector -- Web View</title></head><body>");
        so.append("<iframe width=\"20%\" height=\"100%\" align=left src=\"/agents\" border=5 frameborder=0 ></iframe>");
        so.append("<iframe width=\"78%\" height=\"100%\" align=left src=\"/agent-mind/no_ag\" name=\"am\" border=5 frameborder=0></iframe>");
        so.append("</body></html>");
        return so.toString();
    }

    @Path("/agents")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentsHtml() {
        StringWriter so = new StringWriter();

        so.append("<html><head><title>Jason (list of agents)</title> <meta http-equiv=\"refresh\" content=\"3\"/> </head><body>");
        so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>Agents</p></font>");
        if (JCMRest.getZKHost() == null) {
            for (String a: BaseCentralisedMAS.getRunner().getAgs().keySet()) {
                so.append("- <a href=\"/agents/"+a+"/all\" target=\"am\" style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>");
            }
        } else {
            // get agents from ZK
            try {
                for (String a: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {
                    String url = new String(JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+a));
                    so.append("- <a href=\""+url+"/all\" target=\"am\" style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>");
                    Agent ag = getAgent(a);
                    if (ag != null) createAgLog(a, ag);                    
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        so.append("<br/><a href=\"/forms/new_agent\" target=\"am\" style=\"font-family: arial; text-decoration: none\">new agent</a>");                            
        so.append("<br/><a href=\"/services\" target=\"am\" style=\"font-family: arial; text-decoration: none\">DF</a><br/>");                            

        so.append("<hr/>by <a href=\"http://jason.sf.net\" target=\"_blank\">Jason</a>");
        so.append("</body></html>");        
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
    Map<String,Boolean> show = new HashMap<>();
    {
        show.put("bels", true);
        show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
        show.put("rules", false);
        show.put("evt", true);
        show.put("mb", true);
        show.put("int", true);
        show.put("int-details", false);
    }

    @Path("/agents/{agentname}/hide")
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
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/all'\" /></head>ok";
    }

    @Path("/agents/{agentname}/show")
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
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/all'\" /></head>ok";
    }

    static String helpMsg1 = "Example: +bel; !goal; .send(bob,tell,hello); +{+!goal <- .print(ok) });";

    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        return  "<html><head><title>new agent form</title></head>"+
                "<input type=\"text\" name=\"name\"  size=\"43\" id=\"inputcmd\" placeholder='enter the name of the agent' onkeydown=\"if (event.keyCode == 13) runCMD()\" />\n" + 
                "<script language=\"JavaScript\">\n" + 
                "    function runCMD() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.open(\"POST\", '/agents/'+document.getElementById('inputcmd').value, false); \n" +
                "        http.send();\n"+
                "        window.location.href = '/agents/'+document.getElementById('inputcmd').value+'/all';\n"+
                "    }\n" + 
                "</script>"+
                "</form></html>";
    }

    @Path("/agents/{agentname}")
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
            
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+name+"/all'\" /></head>ok for "+name;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }

    @Path("/agents/{agentname}/all")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentHtml(@PathParam("agentname") String agName) {
        StringWriter so = new StringWriter();
        so.append("<html><head><title>"+agName+"</title></head>");

        // REPL part
        so.append(
                "<input type=\"text\" name=\"c\"  size=\"70\" id=\"inputcmd\" placeholder=\""+helpMsg1+"\" onkeydown=\"if (event.keyCode == 13) runCMD()\" />" + 
                //"</form>"+
                //"<input type=\"submit\" onclick=\"runCMD();\" value=\"Submit\" />"+
                "<code><span id='display'></span></code>  <span id='plog'></span>"+
                "<pre><span id='log'></span></pre>"+
                
                "<script language=\"JavaScript\">\n" + 
                "    function runCMD() {\n" +
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
                "    showLog(); \n" +
                "</script>");
        
        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = new asl2html("/xml/agInspection.xsl");
            }
            for (String p: show.keySet())
                mindInspectorTransformerHTML.setParameter("show-"+p, show.get(p)+"");
            Agent ag = getAgent(agName);
            if (ag != null) {
                so.append( mindInspectorTransformerHTML.transform( ag.getAgState() )); // transform to HTML
            } else {
                // TODO: use the rest API to get the agent HTML in the remote host
            }
            
            so.append("<hr/><a href='plans'      style='font-family: arial; text-decoration: none'>list plans</a>, &nbsp;");
            so.append("<a href='load_plans_form' style='font-family: arial; text-decoration: none'>upload plans</a>, &nbsp;");
            so.append("<a href='kill' onclick='killAg()'     style='font-family: arial; text-decoration: none'>kill this agent</a>");
        } catch (Exception e) {
            e.printStackTrace();
            so.append("Agent "+agName+" does not exist or cannot be observed.");
        }

        so.append("</body></html>");
        return so.toString();
    }

    @Path("/agents/{agentname}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String killAgent(@PathParam("agentname") String agName) throws ReceiverNotFoundException {
        try {
            return "result of kill: "+BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent(agName,"web");
        } catch (Exception e) {
            return "Agent "+agName+" in unknown."+e.getMessage();
        }
    }

    @Path("/agents/{agentname}/load_plans_form")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getLoadPlansForm(@PathParam("agentname") String agName) {
        return  "<html><head><title>load plans for "+agName+"</title></head>"+
                "<form action=\"/agents/"+agName+"/plans\" method=\"post\" id=\"usrform\" enctype=\"multipart/form-data\">" +
                "Enter Jason code below:<br/><textarea name=\"plans\" form=\"usrform\" placeholder=\"optinally, write plans here\" rows=\"13\" cols=\"62\" ></textarea>" +
                "<br/>or upload a file: <input type=\"file\" name=\"file\">"+
                "<br/><input type=\"submit\" value=\"Upload it\">"+
                "</form></html>";
    }

    @Path("/agents/{agentname}/plans")
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
            } else {
                // TODO: use rest API to load in the remote agent
            }
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+agName+"/all'\" /></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }

    @Path("/agents/{agentname}/all")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Document getAgentXml(@PathParam("agentname") String agName) {
        try {
            Agent ag = getAgent(agName);
            if (ag != null)
                return ag.getAgState();
            else
                // TODO: use rest API to load in the remote agent
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    @Path("/agents/{agentname}/plans")
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
            } else {
                // TODO: use rest API to load in the remote agent               
            }
        } catch (Exception e) {
            e.printStackTrace();
            so.append("Agent "+agName+" does not exist or cannot be observed.");
        }
        return so.toString();
    }

    Map<String, StringBuilder> agLog = new HashMap<>();
    
    @Path("/agents/{agentname}/cmd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String runCmdPost(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        String r = execCmd(agName, cmd.trim());
        addAgLog(agName, "Command "+cmd+": "+r);
        return r;
    }

    @Path("/agents/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogOutput(@PathParam("agentname") String agName) {
        StringBuilder o = agLog.get(agName);
        if (o != null) {
            return o.toString();
        }
        return "";
    }
    
    @Path("/agents/{agentname}/log")
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
                // TODO: use rest API to load in the remote agent               
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
    
    @Path("/agents/{agentname}/mb")
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
    

    /** DF **/

    @Path("/services")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentHtml() {
        StringWriter so = new StringWriter();
        so.append("<html><head><title>Directory Facilitator State</title></head><body>");
        so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>Directory Facilitator State</p></font>");
                            
        so.append("<table border=\"0\" cellspacing=\"3\" cellpadding=\"6\" >");
        so.append("<tr style='background-color: #ece7e6; font-family: arial;'><td><b>Agent</b></td><td><b>Services</b></td></tr>");
        if (JCMRest.getZKHost() == null) {
            // get DF locally 
            Map<String, Set<String>> df = BaseCentralisedMAS.getRunner().getDF();
            for (String a: df.keySet()) {
                so.append("<tr style='font-family: arial;'><td>"+a+"</td>");
                for (String s: df.get(a)) {
                    so.append("<td>"+s+"<br/></td>");
                }
                so.append("</tr>");
                    
            }
        } else {
            // get DF from ZK
            try {
                for (String s: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+s)) {
                        so.append("<tr style='font-family: arial;'><td>"+a+"</td>");
                        so.append("<td>"+s+"<br/></td>");
                        so.append("</tr>");                 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        so.append("</table></body></html>");

        return so.toString();            
    }

}
