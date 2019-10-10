package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexHtml() throws ReceiverNotFoundException {
        return getRootHtml("/index.html");
    }
    
    @Path("/{resourcepathfile}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRootHtml(@PathParam("resourcepathfile") String resourcepathfile) throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/html/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/html/" + resourcepathfile).openStream()));
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
    private CacheControl cc = new CacheControl();
    {
        cc.setMaxAge(20);
    } // in seconds

    @Path("/css/{resourcepathfile}")
    @GET
    @Produces("text/css")
    public Response getStyleCSS(@PathParam("resourcepathfile") String resourcepathfile)
            throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/css/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/css/" + resourcepathfile).openStream()));
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
    public Response getResource(@PathParam("resourcepathfile") String resourcepathfile)
            throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/js/" + resourcepathfile).openStream()));
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
    public Response getAceResource(@PathParam("resourcepathfile") String resourcepathfile)
            throws ReceiverNotFoundException {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/js/ace/" + resourcepathfile);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/js/ace/" + resourcepathfile).openStream()));
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
                return (CAgentArch) arch;
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

                    // set to avoid to print roles and group twice if more than one agent is playing
                    // the same or in same group
                    // Set<String> groups = new HashSet<>();

                    for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                        // group
                        sb.append("\t\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
                        sb.append("\n\t\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                        sb.append("\t\t];\n");
                        gb.getGrpState().getPlayers().forEach(p -> {
                            orglinks.append("\t\"" + gb.getArtId() + "\"->\"" + p.getAg()
									+ "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
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
                            sb.append("\t\t{rank=same " + gb.getId() + " " + schb.getArtId() + "};\n");
                        }
                        schb.getSchState().getPlayers().forEach(p -> {
                            orglinks.append("\t\"" + schb.getArtId() + "\"->\"" + p.getAg()
									+ "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
                        });
                    }

                    // sb.append("\t\t{rank=same " + groups.toString().replaceAll("\\[",
                    // "").replaceAll("\\]", "").replaceAll(",", "") + "};\n");

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
                                ArtifactInfo info = CartagoService.getController(wksName)
                                        .getArtifactInfo(aid.getName());
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
                                        sb.append("\t\t\t\"" + wksName + "_" + info.getId().getName() + "\" [ "
                                                + "\n\t\t\tlabel=\"" + str1 + "\"\n");

                                        sb.append("\t\t\t\tshape=record style=filled fillcolor=white\n");
                                        sb.append("\t\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName()
												+ "\"\n");
                                        sb.append("\t\t\t\ttarget=\"mainframe\"\n");
                                        sb.append("\t\t\t];\n");

                                        wksartifacts.add(wksName + "_" + info.getId().getName());

                                        envlinks.append("\t\t\"" + y.getAgentId().getAgentName() + "\"->\"" + wksName
												+ "_" + info.getId().getName() + "\" [arrowhead=odot]\n");
                                    }
                                });
                            }

                            // put artifacts of same wks at same line
                            sb.append("{rank=same " + wksartifacts.toString().replaceAll("\\[", "")
                                    .replaceAll("\\]", "").replaceAll(",", "") + "};\n");

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
