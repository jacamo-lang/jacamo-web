package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

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
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

@Singleton
@Path("/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    // HTML interface
    
    public String designPage(String title, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n"); 
        so.append("<html lang=\"en\">\n"); 
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        so.append("     <script src=\"/js/root.js\"></script>\n");
        so.append("	</head>\n");
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<header class=\"row\">\n");
        // logo JaCaMo
        so.append("				<a class=\"logo col-xp-2 col-sm-2 col-md\" href=\"/\">JaCaMo</a>\n"); 
        // top menu - button agents
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/agents/\" target=\"mainframe\">\n" +
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<circle cx=\"12\" cy=\"12\" r=\"11\"/>\n" + 
                  "					</svg><span>&nbsp;Agents</span>\n" + 
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/workspaces/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" +  
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 24 1, 24 8, 0 8, 0 16, 24 16, 24 23, 0 23, 0 1, 24 1, 24 23, 0 23\"></polygon>\n" +
                  "					</svg><span>&nbsp;Environment</span>\n" +
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"/oe/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 10 1, 10 6, 24 6, 24 23, 0 23, 0 6, 10 6, 0 6, 0 1\"></polygon>\n" + 
                  "					</svg><span>&nbsp;Organisation</span>\n" + 
                  "				</a>\n");
        so.append("             <label for=\"doc-drawer-checkbox\" class=\"button drawer-toggle\" style=\"right: 10px; width: 50px; position: fixed;\"></label>\n");
        so.append("				<input id=\"doc-drawer-checkbox\" class=\"drawer\" value=\"on\" type=\"checkbox\">\n" + 
                  "				<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer\">\n" + 
                  "					<label for=\"doc-drawer-checkbox\" class=\"button drawer-close\"></label>\n" + 
                  "					<h3>Menu</h3>\n" + 
                  "					<a hef=\"agents/\">Agents</a>\n" + 
                  "					<a hef=\"workspaces/\">Environment</a>\n" + 
                  "					<a hef=\"oe/\">Organisation</a>\n" + 
                  "				</nav>\n");
        so.append("			</header>\n"); 
        so.append("			<div class=\"second-row\" id=\"full-content\">\n");
        so.append(                  mainContent);
        so.append("			</div>\n");
        so.append("		</div>\n"); 
        so.append("	</body>\n"); 
        so.append("</html>\n"); 

        return so.toString();
    }

    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getRootHtml() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("				<iframe id=\"mainframe\" name=\"mainframe\" src=\"/overview\" width=\"100%\" height=\"100%\"\n" + 
                           "					frameborder=0></iframe>\n"); 

        return designPage("jacamo-web", mainContent.toString());
    }

    @Path("/overview")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getOverviewHtml() {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>Overview</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta http-equiv=\"Content-type\" name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\" id=\"doc-content\">\n"); 
        so.append("        			<center><object data=\"img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");
        so.append("				</main>\n"); 
        so.append("			</div>\n"); 
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        so.append("<script src=\"/js/agent.js\"></script>\n");
        so.append("</html>\n");
        
        return so.toString();
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

    protected int MAX_LENGTH = 35;
    private CacheControl cc = new CacheControl();  { cc.setMaxAge(20); } // in seconds

    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>Overview</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta http-equiv=\"Content-type\" name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        so.append("     <script src=\"/js/root.js\"></script>\n");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\" id=\"doc-content\">\n"); 
        so.append("					<div id=\"create-agent\" class=\"card fluid\">\n"); 
        so.append("						<h4 class=\"section double-padded\">Create agent</h4>\n"); 
        so.append("						<div class=\"section\">\n"); 
        so.append("							<p>\n");
        so.append("								<input style=\"width: 100%; margin: 0px;\" placeholder=\"enter agent's name ...\" type=\"text\" id=\"createAgent\" onkeydown=\"if (event.keyCode == 13) newAg()\">\n");
        so.append("							</p>\n");
        so.append("							<br/>\n");
        so.append("						</div>\n");
        so.append("					</div>\n");
        so.append("				</main>\n"); 
        so.append("			</div>\n"); 
        so.append("		</div>\n"); 
        so.append("	</body>\n");
        so.append("<script src=\"/js/agent.js\"></script>\n");
        so.append("</html>\n");
        
        return so.toString();
    }

    @Path("/css/{resourcepathfile}")
    @GET
    @Produces("text/css")
    public Response getStyleCSS(@PathParam("resourcepathfile") String resourcepathfile) throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/css/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/css/" + resourcepathfile).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), "text/css").cacheControl(cc).build();
    }

    @Path("/js/{resourcepathfile}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getResource(@PathParam("resourcepathfile") String resourcepathfile) throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/js/" + resourcepathfile).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }

    @Path("/js/ace/{resourcepathfile}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getAceResource(@PathParam("resourcepathfile") String resourcepathfile) throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/ace/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f)); 
            } else {
                in = new BufferedReader(new InputStreamReader(RestImpl.class.getResource("/js/ace/" + resourcepathfile).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(so.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    }

    private Agent getAgent(String agName) {
        CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
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
                    //Set<String> groups = new HashSet<>(); 
                    
                    for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                        // group
                        sb.append("\t\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
                        sb.append("\n\t\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                        sb.append("\t\t];\n");
                        gb.getGrpState().getPlayers().forEach(p -> {
                            orglinks.append("\t\"" + gb.getArtId() + "\"->\"" + p.getAg()
                                    + "\" [arrowtail=normal dir=back label=\""+p.getTarget()+"\"]\n");
                        });
                    }

                    for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
                        // scheme
                        sb.append("\t\t\"" + schb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + schb.getArtId() + "\"");
                        sb.append("\n\t\t\tshape=hexagon style=filled pencolor=black fillcolor=linen\n");
                        sb.append("\t\t];\n");
                        for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
                            orglinks.append("\t\"" + gb.getId() + "\"->\"" + schb.getArtId()
                                    + "\" [arrowtail=normal arrowhead=open label=\"responsible\nfor\"]\n");
                            sb.append("\t\t{rank=same "+gb.getId()+" "+schb.getArtId()+"};\n");
                        }
                        schb.getSchState().getPlayers().forEach(p -> {
                            orglinks.append("\t\"" + schb.getArtId() + "\"->\"" + p.getAg()
                                    + "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
                        });
                    }
                    
                    //sb.append("\t\t{rank=same " + groups.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "") + "};\n");
                        
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
                        sb.append("\t\t\t\tURL = \"/agents/" + a + "/mind\"\n");
                        sb.append("\t\t\t\ttarget=\"mainframe\"\n");
                        sb.append("\t\t];\n");

                        // get workspaces the agent are in (including organizations)
                        Set<String> workspacesIn = new HashSet<>();
                        Agent ag = getAgent(a);
                        CAgentArch cartagoAgArch = getCartagoArch(ag);
                        for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
                            // TODO: revise whether the Set is necessary
                            workspacesIn.add(wid.getName());
                        }
                        allwks.addAll(workspacesIn);
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
                                                
                                        // It is possible to have same artifact name in different workspaces
                                        sb.append("\t\t\t\"" + wksName + "_" + info.getId().getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1
                                                + "\"\n");

                                        sb.append("\t\t\t\tshape=record style=filled fillcolor=white\n");
                                        sb.append("\t\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName()
                                                + "\"\n");
                                        sb.append("\t\t\t\ttarget=\"mainframe\"\n");
                                        sb.append("\t\t\t];\n");

                                        wksartifacts.add(wksName + "_" + info.getId().getName());
                                        
                                        envlinks.append(
                                                "\t\t\"" + y.getAgentId().getAgentName() + "\"->\"" + wksName + "_" + info.getId().getName() + "\" [arrowhead=odot]\n");
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

        } catch (Exception ex) {
        }
        
        return graph;
    }
}
