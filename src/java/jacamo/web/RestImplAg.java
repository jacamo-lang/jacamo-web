package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
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

        so.append("<html><head><title>Jason (list of agents)</title> <meta http-equiv=\"refresh\" content=\"3\"/> </head><body>");
        //so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>Agents</p></font>");
        if (JCMRest.getZKHost() == null) {
            for (String a: BaseCentralisedMAS.getRunner().getAgs().keySet()) {
                so.append("<a href=\"/agents/"+a+"/mind\" target='cf' style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>");
            }
        } else {
            // get agents from ZK
            try {
                for (String a: JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {
                    String url = new String(JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+a));
                    so.append("<a href=\""+url+"/mind\" target='cf' style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>");
                    Agent ag = getAgent(a);
                    if (ag != null) createAgLog(a, ag);                    
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        so.append("<br/><a href=\"/forms/new_agent\" target='cf' style=\"font-family: arial; text-decoration: none\">new agent</a>");                            
        so.append("<br/><a href=\"/services\" target='cf' style=\"font-family: arial; text-decoration: none\">directory facilitator</a><br/>");                            

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
        so.append("<img src='mind/img.svg'/><br/><details>");

        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = new asl2html("/xml/agInspection.xsl");
            }
            for (String p: show.keySet())
                mindInspectorTransformerHTML.setParameter("show-"+p, show.get(p)+"");
            Agent ag = getAgent(agName);
            if (ag != null) {
                so.append( mindInspectorTransformerHTML.transform( ag.getAgState() )); // transform to HTML
            }
            
            so.append("</details><hr/><a href='plans'      style='font-family: arial; text-decoration: none'>list plans</a>, &nbsp;");
            so.append("<a href='load_plans_form' style='font-family: arial; text-decoration: none'>upload plans</a>, &nbsp;");
            so.append("<a href='kill' onclick='killAg()'     style='font-family: arial; text-decoration: none'>kill this agent</a>, &nbsp;");
            if (show.get("annots")) {
                so.append("<a href='hide?annots'     style='font-family: arial; text-decoration: none'>hide annotations</a>");              
            } else {
                so.append("<a href='show?annots'     style='font-family: arial; text-decoration: none'>show annotations</a>");                              
            }
        } catch (Exception e) {
            e.printStackTrace();
            so.append("Agent "+agName+" does not exist or cannot be observed.");
        }

        so.append("</body></html>");
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
                "Enter Jason code below:<br/><textarea name=\"plans\" form=\"usrform\" placeholder=\"optinally, write plans here\" rows=\"13\" cols=\"62\" ></textarea>" +
                "<br/>or upload a file: <input type=\"file\" name=\"file\">"+
                "<br/><input type=\"submit\" value=\"Upload it\">"+
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
            Agent ag = getAgent(agName);
            Set<String> workspacesIn = new HashSet<String>();
            if (ag != null) {
                Iterator<?> i = ag.getBB().getPercepts();
                while (i.hasNext()) {
                    String belief = i.next().toString();
                    if (belief.startsWith("joined")) {
                        workspacesIn.add(belief.substring("joined(".length(), belief.indexOf(",")));
                    }
                }
            }

            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
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
                                sb.append("\n\t\tshape=box style=rounded\n");
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
                sb.append("\t\tshape = \"ellipse\"\n");
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
            /*
             * // Reference the toggle link var xa = document.getElementById('expAll');
             * 
             * // Register link on click event xa.addEventListener('click', function(e) {
             * 
             * // Toggle the two classes that represent "state" determined when link is
             * clicked e.target.classList.toggle('exp'); e.target.classList.toggle('col');
             * 
             * // Collect all <details> into a NodeList var details =
             * document.querySelectorAll('details');
             * 
             * // Convert NodeList into an array then iterate throught it... var D =
             * Array.from(details);
             * 
             * // Start a for loop at 6 instead of 0 Now 0 - 5 details are excluded for (let
             * i = 6; i < D.length; i++) {
             * 
             * // If the link has the class .exp... make each <detail>'s open attribute true
             * if (e.target.classList.contains('exp')) { D[i].open = true; // Otherwise make
             * it false } else { D[i].open = false; }
             * 
             * }
             */

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
}
