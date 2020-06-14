package jacamo.web;

import java.util.Optional;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import jacamo.rest.implementation.RestImplOrg;
import moise.os.OS;
import moise.os.ns.Norm;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgBoard;
import ora4mas.nopl.SchemeBoard;

@Singleton
@Path("/oe")
public class WebImplOrg extends RestImplOrg {

    @Override
    protected void configure() {
        bind(new WebImplOrg()).to(WebImplOrg.class);
    }

    /**
     * Disband all organisations.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response disbandAllOrganisations() {
        try {
            //precisa ter um agente dando comando
            //destruir atraves do orgboard, nao dos grupos...
            
            Optional<OrgBoard> ob = OrgBoard.getOrbBoards().stream().findFirst();
            OS os = OS.loadOSFromURI(ob.get().getOSFile());
            
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                System.out.println("group: " + gb.toString());
                gb.destroy();
            }
            for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
                System.out.println("scheme: " + sb.toString());
                sb.destroy();
            }
            for (Norm n : os.getNS().getNorms()) {
                System.out.println("norm: " + n.toString());
                os.getNS().removeNorms(n.getRole());
            }

            /*
            List<String> organisationalArtifacts = Arrays.asList("ora4mas.nopl.GroupBoard", "ora4mas.nopl.OrgBoard",
                  "ora4mas.nopl.SchemeBoard", "ora4mas.nopl.NormativeBoard", "ora4mas.light.LightOrgBoard",
                  "ora4mas.light.LightNormativeBoard", "ora4mas.light.LightGroupBoard",
                  "ora4mas.light.LightSchemeBoard");
            
            TranslEnv tEnv = new TranslEnv();
            for (String wrksName : tEnv.getWorkspaces()) {
                for (ArtifactId aid : CartagoService.getController(wrksName).getCurrentArtifacts()) {
                    if (organisationalArtifacts.contains(aid.getArtifactType())) {
                        System.out.println("disbanding : " + aid.getName());
                        //CartagoService.getController(wrksName).removeArtifact(aid.getName());
                    } else {
                        System.out.println("maintaining: " + aid.getName());
                    }
                }
            }
            */
            
            return Response.ok().entity("Organisations disbanded!").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }
}
