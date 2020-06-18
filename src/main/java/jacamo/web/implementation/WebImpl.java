package jacamo.web.implementation;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.google.gson.Gson;

import jaca.CAgentArch;
import jacamo.rest.implementation.RestImpl;
import jacamo.web.JCMWeb;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;

@Singleton
@Path("/")
public class WebImpl extends RestImpl {

    Gson gson = new Gson();

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
     * Stage all modified and deleted files committing then adding in comments the given message
     *
     * @param message to add in git comments
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/commit")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response commitChanges(String message, @QueryParam("email") String email) {
        try {
            File rep = getGitRepository();
            if (rep == null)
                return Response.status(500, "Repository not found!").build();

            Git git = Git.open(rep);

            System.out.println("Staging modified and deleted files to commit: " + git.getRepository().toString()
                    + ". With message: " + message);

            RevCommit rev = git.commit().setAll(true).setAuthor("", email).setMessage(message).call();

            git.close();

            return Response.ok(rev.toString()).build();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    /**
     * Stage all modified and deleted files committing then adding in comments the given message
     *
     * @param message to add in git comments
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/push")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response pushChanges(@QueryParam("username") String username, @QueryParam("password") String password) {
        try {
            File rep = getGitRepository();
            if (rep == null)
                return Response.status(500, "Repository not found!").build();

            Git git = Git.open(rep);
            PushCommand pushCommand = git.push();

            byte[] decodedPassword = Base64.getDecoder().decode(password);

            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, new String(decodedPassword)));
            //pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));

            pushCommand.call();
            git.close();

            return Response.ok(pushCommand.toString()).build();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    /**
     * Return git status of jacamo-web repository
     *
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         JSON example: {"removed":[],"added":[],"missing":[],"modified":["src/main/java/jacamo/web/WebImpl.java"],"untracked":["bob.bb"],"changed":[]}
     *
     *         More information about jgit: https://github.com/eclipse/jgit/tree/master/org.eclipse.jgit.test/tst/org/eclipse/jgit
     */
    @Path("/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response gitStatus() {
        try {
            File rep = getGitRepository();
            if (rep == null)
                return Response.status(500, "Repository not found!").build();

            Git git = Git.open(rep);
            Status stat = git.status().call();

            Map<String, Object> s = new HashMap<>();
            s.put("added", stat.getAdded());
            s.put("changed", stat.getAdded());
            s.put("missing", stat.getChanged());
            s.put("modified", stat.getModified());
            s.put("removed", stat.getRemoved());
            s.put("untracked", stat.getUntracked());
            return Response.ok(gson.toJson(s)).build();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    private File getGitRepository() throws IOException {
        String relPath = "";
        File rep = new File(relPath+".git");

        //count deepth
        int deepth = 0;
        for (int i = 0; i < rep.getAbsolutePath().length(); i++) {
            if (rep.getAbsolutePath().charAt(i) == '/') deepth++;
        }

        while (!rep.exists()) {
            //Searching for git repository in rep.getCanonicalPath()
            relPath = "../"+relPath;
            rep = new File(relPath+".git");
            deepth--;

            //Reached lowest level
            if (deepth <= 0) {
                return null;
            }
        }
        return rep;
    }

    /**
     * Return ACE editor files. It is used because Jason grammar may be available only locally.
     *
     * @param resourcepathfile the requested ACE file from its flat directory
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * @throws ReceiverNotFoundException
     */
    @Path("/js/ace/{resourcepathfile}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
            return Response.ok(so.toString()).cacheControl(cc).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }


    Map<String, Object> lockedFiles = new HashMap<>();
    /**
     * Lock an arbitrary resource. Used to check and inform user in multiple sessions
     * that they are competing by same resources which brings potential conflicts.
     * This method has an opposite end-point called unlock
     *
     * @param resource an arbitrary name of a resource given by the client
     * @param username give by the client
     * @return HTTP 200 Response (ok status)
     *         (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         a JSON containing resources and identification of users
     *         output example: {file1: ['user1', 'user2'], file1: ['user3', 'user4']}
     */
    @SuppressWarnings("unchecked")
    @Path("/lock/{resource}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response lockAFile(@PathParam("resource") String resource, @QueryParam("username") String username) {

        List<String> s;
        if (lockedFiles.containsKey(resource)) {
            s = (List<String>)lockedFiles.get(resource);
        } else {
            s = new ArrayList<>();
            lockedFiles.put(resource, s);
        }
        s.add(username);

        return Response.ok(gson.toJson(lockedFiles)).build();
    }

    /**
     * Unlock an arbitrary resource. Used to check and inform user in multiple sessions
     * that they are competing by same resources which brings potential conflicts.
     * This method has an opposite end-point called lock
     *
     * @param resource an arbitrary name of a resource given by the client
     * @param username give by the client
     * @return HTTP 200 Response (ok status)
     *         (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         a JSON containing resources and identification of users
     *         output example: {file1: ['user1', 'user2'], file1: ['user3', 'user4']}
     */
    @SuppressWarnings("unchecked")
    @Path("/unlock/{resource}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response unlockAFile(@PathParam("resource") String resource, @QueryParam("username") String username) {

        List<String> s;
        if (lockedFiles.containsKey(resource)) {
            s = (ArrayList<String>)lockedFiles.get(resource);
            s.remove(username);
            if (s.size() == 0) lockedFiles.remove(resource);
        }

        return Response.ok(gson.toJson(lockedFiles)).build();
    }

}
