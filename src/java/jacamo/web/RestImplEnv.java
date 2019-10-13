package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.gson.Gson;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jacamo.platform.EnvironmentWebInspector;

@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    int MAX_LENGTH = 30; // max length of strings when printed in graphs

    static Set<String> hidenArts = new HashSet<>(
            Arrays.asList(new String[] { "node", "console", "blackboard", "workspace", "manrepo", }));

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceHtml(@PathParam("wrksname") String wrksName) {
        try {
            Gson gson = new Gson();

            /**
             * Following the format suggested in the second example of
             * https://opensource.adobe.com/Spry/samples/data_region/JSONDataSetSample.html
             * 
             * Sample jsonifiedDF:
             * [{"agent":"ag1","services":["s1","s2"]},{"agent":"ag2","services":["s2","s3"]}]
             * Testing platform: http://json.parser.online.fr/
             */
            Set<Object> workspaces = new HashSet<Object>();
            for (String wname : CartagoService.getNode().getWorkspaces()) {
                Map<String, Object> workspace = new HashMap<String, Object>();
                try {
                    Set<Object> artifacts = new HashSet<Object>();
                    for (ArtifactId aid : CartagoService.getController(wname).getCurrentArtifacts()) {
                        // Skip system's artifacts
                        if (hidenArts.contains(aid.getName()))
                            continue;
                        if (aid.getName().endsWith("-body") || aid.getArtifactType().endsWith(".OrgBoard")
                                || aid.getArtifactType().endsWith(".SchemeBoard")
                                || aid.getArtifactType().endsWith(".NormativeBoard")
                                || aid.getArtifactType().endsWith(".GroupBoard"))
                            continue;

                        ArtifactInfo info = CartagoService.getController(wname).getArtifactInfo(aid.getName());

                        // Get artifact's properties
                        Set<Object> properties = new HashSet<>();
                        for (ArtifactObsProperty op : info.getObsProperties()) {
                            for (Object vl : op.getValues()) {
                                Map<String, Object> property = new HashMap<String, Object>();
                                property.put(op.getName(), vl);
                                properties.add(property);
                            }
                        }

                        // Get artifact's operations
                        Set<String> operations = new HashSet<>();
                        info.getOperations().forEach(y -> {operations.add(y.getOp().getName());});
                        
                        // Get agents that are observing the artifact
                        Set<Object> observers = new HashSet<>();
                        info.getObservers().forEach(y -> {
                            // do not print agents_body observation
                            if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                                observers.add(y.getAgentId().getAgentName());
                            }
                        });

                        // linked artifacts
                        Set<Object> linkedArtifacts = new HashSet<>();
                        info.getLinkedArtifacts().forEach(y -> {
                            // linked artifact node already exists if it belongs to this workspace
                            linkedArtifacts.add(y.getName());
                        });
                        
                        // Build returning object
                        Map<String, Object> artifact = new HashMap<String, Object>();
                        artifact.put("artifact", aid.getName());
                        artifact.put("type", info.getId().getArtifactType());
                        artifact.put("properties", properties);   
                        artifact.put("operations", operations); 
                        artifact.put("observers", observers);
                        artifact.put("linkedArtifacts", linkedArtifacts);
                        artifacts.add(artifact);
                    }

                    workspace.put(wname, artifacts);
                    
                    workspaces.add(workspace);
                } catch (CartagoException e) {
                    e.printStackTrace();
                }
            }

            return Response.ok().entity(gson.toJson(workspaces)).header("Access-Control-Allow-Origin", "*").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * 500 Internal Server Error -
         * https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
         */
        return Response.status(500).build();
    }

    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getArtifactHtml(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);
            StringWriter mainContent = new StringWriter();

            // overview
            mainContent.append("<div id=\"overview\" class=\"card fluid\">\n");
            mainContent.append("	<div class=\"section\">\n");
            mainContent.append("        <center><object data=\"/workspaces/" + wrksName + "/" + artName
					+ "/img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");

            mainContent.append("	</div>\n");
            mainContent.append("</div>\n");
            mainContent.append("<div id=\"inspection\" class=\"card fluid\">\n");
            mainContent.append("	<div class=\"section\">\n");

            mainContent.append(
                    "		Artifact <b>" + info.getId().getName() + "</b> in workspace <b>" + wrksName + "</b>");
            mainContent.append("		<a href=\"javafile/" + info.getId().getArtifactType() + "\">[edit]</a>\n");
            mainContent.append("		<br/>");
            mainContent.append("		<br/>");
            mainContent.append("		<table>");
            mainContent.append("        	<thead><tr><th>Description</th><th>Value</th></tr></thead>");
            mainContent.append("			<tbody>");
            for (ArtifactObsProperty op : info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl : op.getValues()) {
                    vls.append(v + vl);
                    v = ",";
                }
                mainContent.append("        <tr><td>" + op.getName() + "</td><td>" + vls + "</td></tr>");
            }
            mainContent.append("			</tbody>");
            mainContent.append("        </table>");
            mainContent.append("    </div>\n");
            mainContent.append("</div>\n");

            return "jacamo-web - environment: ";
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    EnvironmentWebInspector bend = new EnvironmentWebInspector();

    @Path("/{wrksname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getWrksImg(@PathParam("wrksname") String wrksName) {
        try {
            String dot = getWksAsDot(wrksName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);

                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 500 Internal Server Error - https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html */
        return Response.status(500).build();
    }

    @SuppressWarnings("finally")
    protected String getWksAsDot(String wksName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            sb.append("\tsubgraph cluster_0 {\n");
            sb.append("\t\tlabel=\"" + wksName + "\"\n");
            sb.append("\t\tlabeljust=\"r\"\n");
            sb.append("\t\tgraph[style=dashed]\n");
            for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

                if ((info.getId().getArtifactType().equals("cartago.WorkspaceArtifact"))
                        || (info.getId().getArtifactType().equals("cartago.tools.Console"))
                        || (info.getId().getArtifactType().equals("cartago.ManRepoArtifact"))
                        || (info.getId().getArtifactType().equals("cartago.tools.TupleSpace"))
                        || (info.getId().getArtifactType().equals("cartago.NodeArtifact"))
                        || (info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))) {
                    ; // do not print system artifacts
                } else {
                    String s1;
                    s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                            : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                    sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\n");
					s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
							: info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
					sb.append(s1 + "\"\n");
                    sb.append("\t\tshape=record style=filled fillcolor=white\n");
                    sb.append("\t\tURL = \"/workspaces/" + info.getId().getWorkspaceId().getName() + "/"
							+ info.getId().getName() + "\"\n");
                    sb.append("\t\ttarget=\"mainframe\"\n");
                    sb.append("\t\t];\n");
                }
                info.getObservers().forEach(y -> {
                    // do not print agents_body observation
                    if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                        // print node with defined shape
                        String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH)
                                ? y.getAgentId().getAgentName()
                                : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                        sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
                        sb.append("\t\tURL = \"/agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                        sb.append("\t\t\ttarget=\"mainframe\"\n");
                        sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                        sb.append("\t];\n");

                        // print arrow
                        sb.append("\t\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName()
								+ "\" [arrowhead=\"odot\"];\n");
                    }
                });

                // linked artifacts
                info.getLinkedArtifacts().forEach(y -> {
                    // linked artifact node already exists if it belongs to this workspace
                    sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName()
							+ "\" [arrowhead=\"onormal\"];\n");
                });

            }
            sb.append("\t}\n");
            sb.append("}\n");
            graph = sb.toString();

        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }

    @Path("/{wrksname}/{artname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getArtImg(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            String dot = getArtAsDot(wrksName, artName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);

                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* 500 Internal Server Error - https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html */
        return Response.status(500).build();
    }

    @Path("/{wrksname}/javafile/{javafilename}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLoadJavafileForm(@PathParam("wrksname") String wrksName,
            @PathParam("javafilename") String javaFileName) {

        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/env/" + javaFileName.replaceAll("\\.", "/") + ".java");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                URL u = RestImpl.class.getResource("../src/env/" + javaFileName.replaceAll("\\.", "/") + ".java");
                if (u != null) {
                    in = new BufferedReader(new InputStreamReader(u.openStream()));
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("/* Artifact created automatically by jacamo-web */\n\n");

                    // Check whether there is a package
                    if (javaFileName.lastIndexOf(".") > 0) {
                        stringBuilder.append(
                                "package " + javaFileName.substring(0, javaFileName.lastIndexOf(".")) + ";\n\n");
                    }

                    stringBuilder.append("import cartago.*;\n\n");
                    stringBuilder.append("@ARTIFACT_INFO(outports = { @OUTPORT(name = \"out-1\") })\n\n");
                    stringBuilder.append("public class "
                            + javaFileName.substring(javaFileName.lastIndexOf(".") + 1, javaFileName.length())
                            + " extends Artifact {\n");
                    stringBuilder.append("\tvoid init(int initialValue) {\n");
                    stringBuilder.append("\t}\n");
                    stringBuilder.append("}\n");

                    in = new BufferedReader(new StringReader(stringBuilder.toString()));
                }
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

        /* Error codes: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html */
        return Response.status(500).build();
    }

    @Path("/{wrksname}/javafile/{javafilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response loadJavafileForm(@PathParam("wrksname") String wrksName,
            @PathParam("javafilename") String javaFileName,
            @FormDataParam("javafile") InputStream uploadedInputStream) {
        try {
            String r = "nok";
            System.out.println("wrksName: " + wrksName);
            System.out.println("restAPI://" + javaFileName);
            System.out.println("uis: " + uploadedInputStream);

            FileOutputStream outputFile;
            File f = new File("src/env/" + javaFileName.replaceAll("\\.", "/") + ".java");
            if (f.exists()) {
                outputFile = new FileOutputStream("src/env/" + javaFileName.replaceAll("\\.", "/") + ".java", false);
            } else {
                f.getParentFile().mkdirs();
                f.createNewFile();
                outputFile = new FileOutputStream(f, false);
            }

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));

            while ((line = out.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            byte[] bytes = stringBuilder.toString().getBytes();
            outputFile.write(bytes);
            outputFile.close();

            r = "<html><head><meta http-equiv=\"refresh\" content=\"1; URL='/workspaces.html'\"/></head><body>"
                    + "<br/><center>Artifact saved! Next instances will use this new file!<br/>Redirecting...</center></body></html>";
            
            return Response.ok(r).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /*500 Internal Server Error - https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html*/
        return Response.status(500).build();
    }

    @SuppressWarnings("finally")
    protected String getArtAsDot(String wksName, String artName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        ArtifactInfo info;
        try {
            String s1;
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            info = CartagoService.getController(wksName).getArtifactInfo(artName);
            s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                    : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
            sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\n");
			s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
					: info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
			sb.append(s1 + "|");

			// observable properties
			info.getObsProperties().forEach(y -> {
				String s2 = (y.toString().length() <= MAX_LENGTH) ? y.toString()
						: y.toString().substring(0, MAX_LENGTH) + " ...";
				sb.append("\t\t\t" + s2 + "\n");
			});
			sb.append("\t\t\t|");

			// operations
			info.getOperations().forEach(y -> {
				String s2 = (y.getOp().getName().length() <= MAX_LENGTH) ? y.getOp().getName()
						: y.getOp().getName().substring(0, MAX_LENGTH) + " ...";
				sb.append("\t\t\t" + s2 + "\n");
			});
			sb.append("\t\t\t\"\n");

            sb.append("\t\tshape=record style=filled fillcolor=white\n");
            sb.append("\t];\n");

            // linked artifacts
            info.getLinkedArtifacts().forEach(y -> {
                // print node with defined shape
//                String s2 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
//                        : y.getName().substring(0, MAX_LENGTH) + "...";
//                sb.append("\t\"" + y.getName() + "\" [ " + "\n\t\tlabel = \""
//                        + s2 + "|");
//                s2 = (y.getArtifactType().length() <= MAX_LENGTH) ? y.getArtifactType()
//                        : y.getArtifactType().substring(0, MAX_LENGTH) + "...";
//                sb.append(s2 + "\"\n");

                String str1 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
                        : y.getName().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\"" + y.getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1 + " :\\n");

				str1 = (y.getArtifactType().length() <= MAX_LENGTH) ? y.getArtifactType()
						: y.getArtifactType().substring(0, MAX_LENGTH) + " ...";
				sb.append(str1 + "\"\n");

                sb.append("\t\tURL = \"/workspaces/" + y.getWorkspaceId().getName() + "/" + y.getName() + "\"\n");
                sb.append("\t\ttarget=\"mainframe\"\n");
                sb.append("\t\tshape=record style=filled fillcolor=white\n");
                sb.append("\t];\n");

                // print arrow
                sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName() + "\" [arrowhead=\"onormal\"];\n");
            });

            // observers
            info.getObservers().forEach(y -> {
                // do not print agents_body observation
                if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    // print node with defined shape
                    String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH) ? y.getAgentId().getAgentName()
                            : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
                    sb.append("\t\tURL = \"/agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                    sb.append("\t\t\ttarget=\"mainframe\"\n");
                    sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                    sb.append("\t];\n");

                    // print arrow
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName()
							+ "\" [arrowhead=\"odot\"];\n");
                }
            });

            sb.append("}\n");
            graph = sb.toString();

        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }
}
