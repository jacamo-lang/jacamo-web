package jacamo.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.ns.Norm;
import moise.os.ss.Role;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Player;

public class TranslOrg {

    /**
     * Get list of running organisations.
     * 
     * @return List of strings
     */
    public List<String> getOrganisations() {

        List<String> organisations = new ArrayList<>();
        for (OrgBoard ob : OrgBoard.getOrbBoards()) {
            organisations.add(ob.getOEId());
        }

        return organisations;
    }

    /**
     * Get details of one organisation in JSON format, including groups, schemes and
     * norms.
     * 
     * @param oeName name of the organisation
     * @return A map with organisation data
     */
    public Map<String, Object> getSpecification(String oeName) {
        Map<String, Object> org;
        OS os = null;
        org = new HashMap<>();
        org.put("organisation", oeName);

        List<Object> groups = new ArrayList<>();
        org.put("groups", groups);
        for (GroupBoard gb : GroupBoard.getGroupBoards()) {
            if (gb.getOEId().equals(oeName)) {
                os = gb.getSpec().getSS().getOS();
                Map<String, Object> group = new HashMap<>();
                groups.add(group);
                List<Object> roles = new ArrayList<>();
                group.put("group", gb.getSpec().toString());
                group.put("roles", roles);
                group.put("isWellFormed", gb.isWellFormed());
                for (Role r : gb.getSpec().getSS().getRolesDef()) {
                    Map<String, Object> role = new HashMap<>();
                    role.put("role", r.getId());
                    List<String> superRoles = new ArrayList<>();
                    role.put("superRoles", superRoles);
                    for (Role e : r.getSuperRoles())
                        superRoles.add(e.getId());
                    role.put("cardinality", gb.getSpec().getRoleCardinality(r).toStringFormat2());
                    roles.add(role);
                }
                List<String> subGroups = new ArrayList<>();
                group.put("subGroups", subGroups);
                for (ora4mas.nopl.oe.Group sgi : gb.getGrpState().getSubgroups()) {
                    if (sgi.getGrType().equals(gb.getSpec().getId())) {
                        subGroups.add(sgi.getId());
                    }
                }
            }
        }

        List<Object> schemes = new ArrayList<>();
        org.put("schemes", schemes);
        for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
            if (sb.getOEId().equals(oeName)) {
                os = sb.getSpec().getFS().getOS();

                Map<String, Object> scheme = new HashMap<>();
                schemes.add(scheme);
                List<String> goals = new ArrayList<>();
                addGoalsAInList(sb.getSpec().getRoot(), goals);
                scheme.put("scheme", sb.getArtId());
                scheme.put("isWellFormed", sb.isWellFormed());
                scheme.put("goals", goals);

                List<Object> missions = new ArrayList<>();
                scheme.put("missions", missions);
                // missions
                for (Mission m : sb.getSpec().getMissions()) {
                    Map<String, Object> mission = new HashMap<>();
                    mission.put("mission", m.getId());
                    List<String> missionGoals = new ArrayList<>();
                    mission.put("missionGoals", missionGoals);
                    for (Goal g : m.getGoals()) {
                        missionGoals.add(g.getId());
                    }
                    missions.add(mission);
                }

                List<String> players = new ArrayList<>();
                scheme.put("players", players);
                for (Player p : sb.getSchState().getPlayers()) {
                    players.add(p.getAg() + " ( " + p.getTarget() + " )");
                }
            }
        }

        List<Object> norms = new ArrayList<>();
        org.put("norms", norms);
        if (os == null) {
            for (OrgBoard ob : OrgBoard.getOrbBoards())
                if (ob.getOEId().equals(oeName))
                    os = OS.loadOSFromURI(ob.getOSFile());
        }
        for (Norm n : os.getNS().getNorms()) {
            Map<String, Object> norm = new HashMap<>();
            norms.add(norm);
            norm.put("norm", n.getId());
            norm.put("type", n.getType().name());
            norm.put("role", n.getRole().toString());
            norm.put("mission", n.getMission().toString());
        }

        return org;

    }

    /**
     * Add goals recursively in a list of string in a format sub-goal <- parent-goal
     * 
     * @param g    first goal (usually the root goal)
     * @param list a list to be recursively updated
     * @return list of strings
     */
    public List<String> addGoalsAInList(Goal g, List<String> list) {
        if (g.hasPlan()) {
            for (Goal sg : g.getPlan().getSubGoals()) {
                list.add(sg.getId() + " <- " + g.getId());
                addGoalsAInList(sg, list);
            }
        }
        return list;
    }
}
