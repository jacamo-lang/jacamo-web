package jacamo.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.google.gson.Gson;

import cartago.CartagoException;
import jaca.CAgentArch;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;

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
     * Get root content, returning index.html from resources/html folder using
     * chache control and etags
     * 
     * @param request used to create etags
     * @return index.html content
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexHtml(@Context Request request) {
        return getHtml("index.html", request);
    }

    /**
     * Get html from resources/html folder using chache control and etags
     * 
     * @param file    to be retrieved
     * @param request used to create etags
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
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
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("/html/" + file).openStream()));
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
     * Get XML, JS and CSS resources from corresponding folders /resources/xml,...
     * uses cache control and etags
     * 
     * @param folder  xml, js or css
     * @param file    name of the file to be retrieved
     * @param request used to create etags
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
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
     * Generates whole MAS overview in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/overview")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOverviewJSON() {
        Gson gson = new Gson();
        Map<String, Object> overview = new HashMap<>();

        try {
            TranslOrg tOrg = new TranslOrg();
            TranslAg tAg = new TranslAg();
            TranslEnv tEnv = new TranslEnv();

            List<Object> organisations = new ArrayList<>();
            overview.put("organisations", organisations);
            tOrg.getOrganisations().forEach(o -> {
                organisations.add(tOrg.getSpecification(o));
            });

            List<Object> agents = new ArrayList<>();
            overview.put("agents", agents);
            tAg.getAgents().forEach(a -> {
                try {
                    agents.add(tAg.getAgentDetails(a));
                } catch (CartagoException e) {
                    e.printStackTrace();
                }
            });
            
            List<Object> workspaces = new ArrayList<>();
            overview.put("workspaces", workspaces);
            tEnv.getWorkspaces().forEach(w -> {
                try {
                    workspaces.add(tEnv.getWorkspace(w));
                } catch (CartagoException e) {
                    e.printStackTrace();
                }
            }); 
            
            return Response.ok(gson.toJson(overview)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }
}
