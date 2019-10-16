package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.DatatypeConverter;

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

    private CacheControl cc = new CacheControl();
    {
        cc.setMaxAge(20);
    } // in seconds

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    /**
     * Get root content, returning index.html from resources/html folder using chache control and etags
     * 
     * @param request used to create etags
     * @return index.html content
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexHtml(@Context Request request) {
        return getHtml("/index.html", request);
    }

    /**
     * Get html from resources/html folder using chache control and etags
     * 
     * @param file to be retrieved
     * @param request used to create etags
     * @return html content
     */
    @Path("/{file}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml(@PathParam("file") String file, @Context Request request) {
        try {
            StringBuilder so = new StringBuilder();

            BufferedReader in = null;
            File f = new File("src/resources/html/" + file);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(new InputStreamReader(
                        RestImpl.class.getResource("/html/" + file).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }

            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(so.toString().getBytes(StandardCharsets.UTF_8));
            String hex = DatatypeConverter.printHexBinary(hash);
            EntityTag etag = new EntityTag(hex);

            ResponseBuilder builder = request.evaluatePreconditions(etag);
            if (builder != null) {
                return builder.build();
            }

            return Response.ok(so.toString()).tag(etag).cacheControl(cc).build();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get graph of the whole Multi-Agent System
     * 
     * @deprecated this is a client stuff
     * @return rendered svg
     */
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

        return Response.status(500).build();
    }

    /**
     * Get XML, JS and CSS resources from corresponding folders /resources/xml,...
     * uses chache control and etags
     * 
     * @param folder xml, js or css
     * @param file name of the file to be retrieved
     * @param request used to create etags
     * @return file content marking the media type according to the given folder
     */
    @Path("{folder: (?:xml|js|css)}/{file}")
    @GET
    public Response getWebResource(@PathParam("folder") String folder, @PathParam("file") String file,
            @Context Request request) {
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/resources/" + folder + "/" + file);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/" + folder + "/" + file).openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line);
                line = in.readLine();
            }

            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(so.toString().getBytes(StandardCharsets.UTF_8));
            String hex = DatatypeConverter.printHexBinary(hash);
            EntityTag etag = new EntityTag(hex);

            ResponseBuilder builder = request.evaluatePreconditions(etag);
            if (builder != null) {
                return builder.build();
            }
 
            if (folder.equals("xml")) 
                return Response.ok(so.toString(), "text/xml").tag(etag).cacheControl(cc).build();
            else if (folder.equals("js")) 
                return Response.ok(so.toString(), "application/javascript").tag(etag).cacheControl(cc).build();
            else 
                return Response.ok(so.toString(), "text/css").tag(etag).cacheControl(cc).build();

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get agent object from a given name
     * 
     * @param agName agent name
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
     * Get agent's CArtAgO architecture
     * 
     * @param ag Agent object
     * @return agent's CArtAgO architecture
     */
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

    /**
     * Generates a dot for the whole MAS
     * 
     * @deprecated it is a client stuff
     * @return dot representation
     */
    protected String getMASAsDot() {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            int MAX_LENGTH = 35;
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
