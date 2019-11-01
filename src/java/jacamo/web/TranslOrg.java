package jacamo.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import moise.os.Cardinality;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.ns.Norm;
import moise.os.ss.Compatibility;
import moise.os.ss.Link;
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
                List<Object> links = new ArrayList<>();
                group.put("links", links);
                for (Link l : gb.getSpec().getLinks()) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("type", l.getTypeStr());
                    link.put("isBiDir", l.isBiDir());
                    link.put("scope", l.getScope());
                    link.put("source", l.getSource().getId());
                    link.put("target", l.getTarget().getId());
                    links.add(link);
                }
                List<Object> compatibilities = new ArrayList<>();
                group.put("compatibilities", compatibilities);
                for (Compatibility c : gb.getSpec().getCompatibilities()) {
                    Map<String, Object> compatibility = new HashMap<>();
                    compatibility.put("isBiDir", c.isBiDir());
                    compatibility.put("scope", c.getScope());
                    compatibility.put("source", c.getSource().getId());
                    compatibility.put("target", c.getTarget().getId());
                    compatibilities.add(compatibility);
                }
                List<Object> players = new ArrayList<>();
                group.put("players", players);
                for (Player p : gb.getGrpState().getPlayers()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("agent", p.getAg());
                    player.put("role", p.getTarget());
                    players.add(player);
                }
                List<Object> responsibleFor = new ArrayList<>();
                group.put("responsibleFor", responsibleFor);
                for (String s : gb.getGrpState().getSchemesResponsibleFor()) {
                    Map<String, Object> schemeRF = new HashMap<>();
                    boolean wf = false;
                    SchemeBoard sb = findSchemeBoard(s);
                    if (sb != null)
                        wf = sb.isWellFormed();
                    schemeRF.put("isWellFormed", wf);
                    schemeRF.put("scheme", s);
                    responsibleFor.add(schemeRF);
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
                List<Object> goals = new ArrayList<>();
                addGoalsAInList(sb, sb.getSpec().getRoot(), null, goals);
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
                    String card = "";
                    if (! sb.getSpec().getMissionCardinality(m).equals(Cardinality.defaultValue)) {
                        card = sb.getSpec().getMissionCardinality(m).toStringFormat2();
                    }
                    mission.put("cardinality", card);
                    missions.add(mission);
                }

                List<Object> players = new ArrayList<>();
                scheme.put("players", players);
                for (Player p : sb.getSchState().getPlayers()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("agent", p.getAg());
                    player.put("mission", p.getTarget());
                    players.add(player);
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

    private SchemeBoard findSchemeBoard(String id) {
        for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
            if (sb.getArtId().equals(id))
                return sb;
        }
        return null;
    }

    /**
     * Add goals recursively in a list of string in a format sub-goal <- parent-goal
     * 
     * @param sb   scheme board where goals are assigned
     * @param g    usually the first given is the root goal
     * @param p    the parent of the given goal (null for root)
     * @param list a list to be recursively updated
     * @return list of strings
     */
    public List<Object> addGoalsAInList(SchemeBoard sb, Goal g, Goal p, List<Object> list) {
        Map<String, Object> goal = new HashMap<>();
        goal.put("goal", g.getId());
        if (p != null)
            goal.put("parent", p.getId());
        else
            goal.put("parent", "");
        if (g.hasPlan())
            goal.put("operation", g.getPlan().getOp());
        else
            goal.put("operation", "");
        goal.put("isSatisfied", sb.getSchState().isSatisfied(g));
        Term tSch = ASSyntax.createString(sb.getSchState().getId());
        Atom aGoal = new Atom(g.getId());
        goal.put("enabled", sb.getNormativeEngine().holds(ASSyntax.createLiteral("enabled", tSch, aGoal)));
        list.add(goal);
        if (g.hasPlan()) {
            for (Goal sg : g.getPlan().getSubGoals()) {
                addGoalsAInList(sb, sg, g, list);
            }
        }
        return list;
    }
}
