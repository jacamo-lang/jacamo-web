package jacamo.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import jaca.CAgentArch;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

public class TranslAg {

    /**
     * Get list of existing agents Example: ["ag1","ag2"]
     * 
     * @return Set of agents;
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getAgents() {
        return BaseCentralisedMAS.getRunner().getAgs().keySet();
    }

    /**
     * Get agent information (namespaces, roles, missions and workspaces)
     * 
     * @param agName name of the agent
     * @return A Map with agent information
     * @throws CartagoException
     * 
     */
    public Map<String, Object> getAgentDetails(@PathParam("agentname") String agName) throws CartagoException {

        Agent ag = getAgent(agName);

        // get workspaces the agent are in (including organisations)
        List<String> workspacesIn = new ArrayList<>();
        CAgentArch cartagoAgArch = getCartagoArch(ag);

        for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
            workspacesIn.add(wid.getName());
        }
        List<String> nameSpaces = new ArrayList<>();
        ag.getBB().getNameSpaces().forEach(x -> {
            nameSpaces.add(x.toString());
        });

        // get groups and roles this agent plays
        List<Object> roles = new ArrayList<>();
        for (GroupBoard gb : GroupBoard.getGroupBoards()) {
            if (workspacesIn.contains(gb.getOEId())) {
                gb.getGrpState().getPlayers().forEach(p -> {
                    if (p.getAg().equals(agName)) {
                        Map<String, Object> groupRole = new HashMap<>();
                        groupRole.put("group", gb.getArtId());
                        groupRole.put("role", p.getTarget());
                        roles.add(groupRole);
                    }
                });

            }
        }

        // get schemed this agent belongs
        List<Object> missions = new ArrayList<>();
        for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
            schb.getSchState().getPlayers().forEach(p -> {
                if (p.getAg().equals(agName)) {
                    Map<String, Object> schemeMission = new HashMap<>();
                    schemeMission.put("scheme", schb.getArtId());
                    schemeMission.put("mission", p.getTarget());
                    List<Object> responsibles = new ArrayList<>();
                    schemeMission.put("responsibles", responsibles);
                    for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
                        responsibles.add(gb.getId());
                    }
                    missions.add(schemeMission);
                }
            });
        }

        // TODO: unify the list of 'system' artifacts with RestImplEnv
        List<Object> workspaces = new ArrayList<>();
        workspacesIn.forEach(wksName -> {
            Map<String, Object> workspace = new HashMap<>();
            workspace.put("workspace", wksName);
            List<Object> artifacts = new ArrayList<>();
            try {
                for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                    ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
                    info.getObservers().forEach(y -> {
                        if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
                                || (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
                                || (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
                                || (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
                                || (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
                            ; // do not print system artifacts
                        } else {
                            if (y.getAgentId().getAgentName().equals(agName)) {
                                // Build returning object
                                Map<String, Object> artifact = new HashMap<String, Object>();
                                artifact.put("artifact", info.getId().getName());
                                artifact.put("type", info.getId().getArtifactType());
                                artifacts.add(artifact);
                            }
                        }
                    });
                }
                workspace.put("artifacts", artifacts);
                workspaces.add(workspace);
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });

        Map<String, Object> agent = new HashMap<>();
        agent.put("agent", agName);
        agent.put("namespaces", nameSpaces);
        agent.put("roles", roles);
        agent.put("missions", missions);
        agent.put("workspaces", workspaces);

        return agent;
    }

    /**
     * Return agent object by agent's name
     * 
     * @param agName name of the agent
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
}
