package jacamo.web;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import jaca.CAgentArch;
import jacamo.infra.JaCaMoLauncher;
import jacamo.project.JaCaMoProject;
import jacamo.project.parser.ParseException;
import jacamo.rest.RestImpl;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.infra.centralised.BaseCentralisedMAS;

@Singleton
@Path("/")
public class WebImpl extends RestImpl {

    private CacheControl cc = new CacheControl();
    {
        cc.setMaxAge(20);
    } // in seconds

    @Override
    protected void configure() {
        bind(new WebImpl()).to(WebImpl.class);
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
    public Response getHtml(@PathParam("file") String file, @Context Request request) {
        try {
            /* Only images as png format is being supported */
            if (file.endsWith(".png")) {
                URL urlToResource = getClass().getResource("/html/" + file);
                BufferedImage image = ImageIO.read(urlToResource);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageData = baos.toByteArray();

                return Response.ok(new ByteArrayInputStream(imageData), "image/png").cacheControl(cc).build();
            } else {
                StringBuilder so = new StringBuilder();

                BufferedReader in = null;
                File f = new File("src/resources/html/" + file);
                if (f.exists()) {
                    in = new BufferedReader(new FileReader(f));
                } else {
                    in = new BufferedReader(
                            new InputStreamReader(WebImpl.class.getResource("/html/" + file).openStream()));
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

                return Response.ok(so.toString(), MediaType.TEXT_HTML).tag(etag).cacheControl(cc).build();
            }
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
                        new InputStreamReader(WebImpl.class.getResource("/" + folder + "/" + file).openStream()));
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
     * Get list of jcm files available to be launched in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/projects/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectsJSON() {
        return null;

    }

    /**
     * Launch a JCM project.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/projects/{projectname}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getProjectJSON(@PathParam("projectname") String projectName) {
        try {
            ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getRuntimeServices().getAgentsNames().forEach(a -> {
                ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).getRuntimeServices().killAgent(a, a, 0);
            });
            File f = new File("src/jcm/bob.jcm");
            if (f.exists()) {
                JaCaMoProject proj = new JaCaMoProject();

                proj.importProject("/src/jcm", f);

                ((JaCaMoLauncher) BaseCentralisedMAS.getRunner()).setProject(proj);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Path("/commit")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response commitChanges() {
        try {
            Git git = Git.open(new File(".git"));

            System.out.println("\n\n***** " + git.getRepository().getFullBranch());
            System.out.println("My repository: " + git.getRepository().toString());
            System.out.println("****: " + git.toString());
            
            RevCommit rev = git.commit().setAmend(true).setAll(true)
                    .setAuthor("cleberjamaral", "cleberjamaral@gmail.com")
                    .setMessage("Testing commit from jacamo-web").call();

            git.close();
            
            System.out.println(rev.getFullMessage());
            return Response.ok(rev.getFullMessage()).build();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

}
