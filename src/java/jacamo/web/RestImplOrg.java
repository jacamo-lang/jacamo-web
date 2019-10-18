package jacamo.web;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import moise.os.ss.Role;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgArt;

@Singleton
@Path("/oe")
public class RestImplOrg extends AbstractBinder {

    TranslOrg tOrg = new TranslOrg();
    
    @Override
    protected void configure() {
        bind(new RestImplOrg()).to(RestImplOrg.class);
    }

    /**
     * Get list of running organisations in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["testOrg","wkstest"]
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrganisationsJSON() {

        Gson gson = new Gson();
        try {
            return Response.ok(gson.toJson(tOrg.getOrganisations())).header("Access-Control-Allow-Origin", "*").build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get details of one organisation in JSON format, including groups, schemes and
     * norms.
     * 
     * @param oeName name of the organisation
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"norms":[{"mission":"scheme1.mission1","role":"role1","type":"obligation","norm":"norm2"},
     *         {"mission":"scheme1.mission2","role":"role2","type":"obligation","norm":"norm1"}],
     *         "organisation":"testOrg","groups":[{"roles":[{"role":"role1","cardinality":"1..1","superRoles":["soc"]},
     *         {"role":"soc","cardinality":"0..*","superRoles":[]},{"role":"role2","cardinality":"0..1","superRoles":["soc"]},
     *         {"role":"role3","cardinality":"0..*","superRoles":["role2"]}],"isWellFormed":true,"subGroups":[],"group":"group1"}],
     *         "schemes":[{"scheme":"scheme1","missions":[{"mission":"mission1","missionGoals":["goal2","goal4"]},
     *         {"mission":"mission2","missionGoals":["goal3"]}],"players":["marcos (
	 *         mission1 )"],"isWellFormed":true, "goals":["goal2 \u003c-
	 *         goal1","goal3 \u003c- goal1","goal4 \u003c- goal1"]}]}
     */
    @Path("/{oename}/os")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpecificationJSON(@PathParam("oename") String oeName) {
        try {
            Gson gson = new Gson();
            return Response.ok(gson.toJson(tOrg.getSpecification(oeName))).header("Access-Control-Allow-Origin", "*").build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Add a new role into an organisation/group
     * 
     * @param oeName name of the organisation
     * @param groupName name of the group
     * @param role name of the new role
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{oename}/group/{groupname}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response createNewRole(@PathParam("oename") String oeName, @PathParam("groupname") String groupName,
            @FormParam("role") String role) {
        try {
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName)) {
                    Role newRole = new Role(role, gb.getSpec().getSS());
                    newRole.addSuperRole("soc");
                    gb.getSpec().getSS().addRoleDef(newRole, true);
                    gb.getSpec().addRole(role);
                }
            }

            return Response.ok("Role created!").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    /**
     * Get group specifications in plain text format.
     * 
     * @param oeName name of the organization
     * @param groupName name of the group
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{oename}/group/{groupname}/{groupname}.npl")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGroupNpl(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            StringBuilder out = new StringBuilder();
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    out.append(((OrgArt) gb).getNPLSrc());
                }
            }
            
            return Response.ok(out.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return Response.status(500).build();
    }

    /**
     * Get group instance information in plain text format
     * 
     * @param oeName name of the organization
     * @param groupName name of the group
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{oename}/group/{groupname}/debug")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getGroupDebug(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            StringBuilder out = new StringBuilder();
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    out.append(((OrgArt) gb).getDebugText());
                }
            }
            
            return Response.ok(out.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

}
