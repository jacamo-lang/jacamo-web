package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
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

@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    TranslEnv tEnv = new TranslEnv();

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    /**
     * Get list of workspaces in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspacesJSON() {
        try {
            Gson gson = new Gson();

            return Response.ok().entity(gson.toJson(tEnv.getWorkspaces())).header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get details about a workspace, the artifacts that are situated on this
     * including their properties, operations, observers and linked artifacts
     * 
     * @param wrksName name of the workspace
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"workspace":"testwks","artifacts":{"a":{"artifact":"a","operations":["observeProperty","inc"],
     *         "linkedArtifacts":["b"],"type":"tools.Counter","properties":[{"count":10}],"observers":["marcos"]},
     *         "b":{"artifact":"b","operations":["observeProperty","inc"],"linkedArtifacts":[],"type":"tools.Counter",
     *         "properties":[{"count":2}],"observers":["marcos"]}}}
     */
    @Path("/{wrksname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceJSON(@PathParam("wrksname") String wrksName) {
        try {
            Gson gson = new Gson();

            Map<String, Object> workspace = new HashMap<String, Object>();
            try {
                Map<String, Object> artifacts = new HashMap<>();
                for (ArtifactId aid : CartagoService.getController(wrksName).getCurrentArtifacts()) {
                    ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(aid.getName());

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
                    info.getOperations().forEach(y -> {
                        operations.add(y.getOp().getName());
                    });

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
                    artifacts.put(aid.getName(), artifact);
                }

                workspace.put("workspace", wrksName);
                workspace.put("artifacts", artifacts);
            } catch (CartagoException e) {
                e.printStackTrace();
            }

            return Response.ok().entity(gson.toJson(workspace)).header("Access-Control-Allow-Origin", "*").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get details about an artifact including its properties, operations, observers
     * and linked artifacts
     * 
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"artifact":"a","operations":["observeProperty","inc"],"linkedArtifacts":["b"],
     *         "type":"tools.Counter","properties":[{"count":10}],"observers":["marcos"]}
     */
    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactJSON(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            Gson gson = new Gson();

            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);

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
            info.getOperations().forEach(y -> {
                operations.add(y.getOp().getName());
            });

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
            artifact.put("artifact", artName);
            artifact.put("type", info.getId().getArtifactType());
            artifact.put("properties", properties);
            artifact.put("operations", operations);
            artifact.put("observers", observers);
            artifact.put("linkedArtifacts", linkedArtifacts);

            return Response.ok().entity(gson.toJson(artifact)).header("Access-Control-Allow-Origin", "*").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

   /**
     * Get java file content of a file
     * 
     * @param wrksName     name of the workspace it belongs
     * @param javaFileName name of java file
     * @return file content
     */
    @Path("/{wrksname}/javafile/{javafilename}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLoadJavafileForm(@PathParam("wrksname") String wrksName,
            @PathParam("javafilename") String javaFileName) {

        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            String packageClass;
            if (javaFileName.endsWith(".java")) 
                packageClass = javaFileName.substring(0,javaFileName.length()-5);
            else
                packageClass = javaFileName;
            File f = new File("src/env/" + packageClass.replaceAll("\\.", "/") + ".java");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                URL u = RestImpl.class.getResource("../src/env/" + packageClass.replaceAll("\\.", "/") + ".java");
                if (u != null) {
                    in = new BufferedReader(new InputStreamReader(u.openStream()));
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("/* Artifact created automatically by jacamo-web */\n\n");

                    // Check whether there is a package
                    if (javaFileName.lastIndexOf(".") > 0) {
                        stringBuilder.append(
                                "package " + packageClass.substring(0, packageClass.lastIndexOf(".")) + ";\n\n");
                    }

                    stringBuilder.append("import cartago.*;\n\n");
                    stringBuilder.append("@ARTIFACT_INFO(outports = { @OUTPORT(name = \"out-1\") })\n\n");
                    stringBuilder.append("public class "
                            + packageClass.substring(packageClass.lastIndexOf(".") + 1, packageClass.length())
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

        return Response.status(500).build();
    }

    /**
     * Create/Update an artifact template by adding/replacing a file content in the
     * server
     * 
     * @param wrksName            workspace name
     * @param javaFileName        java file name
     * @param uploadedInputStream file content
     * @return Feedback of the operation
     */
    @Path("/{wrksname}/javafile/{javafilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response loadJavafileForm(@PathParam("wrksname") String wrksName,
            @PathParam("javafilename") String javaFileName,
            @FormDataParam("javafile") InputStream uploadedInputStream) {
        try {
            FileOutputStream outputFile;
            String packageClass;
            if (javaFileName.endsWith(".java")) 
                packageClass = javaFileName.substring(0,javaFileName.length()-5);
            else
                packageClass = javaFileName;
            File f = new File("src/env/" + packageClass.replaceAll("\\.", "/") + ".java");
            if (f.exists()) {
                outputFile = new FileOutputStream("src/env/" + packageClass.replaceAll("\\.", "/") + ".java", false);
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

            if (packageClass.substring(0, packageClass.indexOf(".")).equals("dynamic")) {
                return Response.ok("Saved! Next instances will use this new '" + javaFileName + "' template.").build();
            } else {
                return Response.ok("Saved! However, only templates in 'dynamic' package can be dynamically compiled.").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }
}
