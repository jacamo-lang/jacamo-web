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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import jacamo.rest.RestImplEnv;

@Singleton
@Path("/workspaces")
public class WebImplEnv extends RestImplEnv {

    @Override
    protected void configure() {
        bind(new WebImplEnv()).to(WebImplEnv.class);
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
                URL u = WebImplEnv.class.getResource("../src/env/" + packageClass.replaceAll("\\.", "/") + ".java");
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
