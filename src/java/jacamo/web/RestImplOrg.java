package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import cartago.CartagoException;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.ns.Norm;
import moise.os.ns.NS.OpTypes;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.NormativeBoard;
import ora4mas.nopl.OrgArt;
import ora4mas.nopl.OrgBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Player;

@Singleton
@Path("/oe")
public class RestImplOrg extends AbstractBinder {

    int MAX_LENGTH = 30; // max length of strings when printed in graphs

    @Override
    protected void configure() {
        bind(new RestImplOrg()).to(RestImplOrg.class);
    }

    public String designPage(String title, String selectedItem, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html><html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        so.append("	</head>\n");
        so.append("	<body>\n");
        so.append("		<div id=\"root\">\n");
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n");
        so.append(              getOrganisationMenu(selectedItem));
        so.append("				<main class=\"col-xs-12 col-sm-12 col-md-10 col-lg-10\" id=\"doc-content\">\n");
        so.append(                  mainContent);
        so.append("				</main>\n");
        so.append("			</div>\n");
        so.append("		</div>\n");
        so.append("	</body>\n");
        // copy to 'menucontent' the menu to show on drop down main page menu
        so.append("	<script>\n");
        so.append("		var buttonClose = \"<label for='doc-drawer-checkbox' class='button drawer-close'></label>\";\n");
        so.append("		var pageContent = document.getElementById(\"nav-drawer-frame\").innerHTML;\n");
        so.append("		var fullMenu = `${buttonClose} ${pageContent}`;\n");
        so.append("		sessionStorage.setItem(\"menucontent\", fullMenu);\n");
        so.append("	</script>\n");
        so.append("</html>\n");
        return so.toString();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getOrgHtml() throws CartagoException {

         StringWriter mainContent = new StringWriter();
         mainContent.append("<div id=\"getting-started\" class=\"card fluid\">\n"); 
         mainContent.append("	<h4 class=\"section double-padded\">getting started</h4>\n"); 
         mainContent.append("	<div class=\"section\">\n"); 
         mainContent.append("		<p>\n");
         mainContent.append("			<a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a> is an <a href=\"https://github.com/moise-lang/moise\" target=\"_blank\">open-source</a> organisational platform for MultiAgent Systems");
         mainContent.append(" 			based on notions like roles, groups, and missions. It enables an MAS to have an explicit specification of its organisation. ");     
         mainContent.append("		</p> ");
         mainContent.append("		<br/>\n");
         mainContent.append("	</div>\n");
         mainContent.append("</div>\n");
         
        return designPage("JaCaMo-web - Organisation","",mainContent.toString());
    }

    private String getOrganisationMenu(String selectedOrganisation) {

        StringWriter so = new StringWriter();

        so.append("<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n");
        so.append("<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("	</br>\n");

        for (OrgBoard ob : OrgBoard.getOrbBoards()) {
            if (ob.getOEId().equals(selectedOrganisation)) {
                so.append("	<a href=\"/oe/" + ob.getOEId() + "/os#specification\"><h5><b>" + ob.getOEId() + "</b></h5></a>\n");
            } else {
                so.append("	<a href=\"/oe/" + ob.getOEId() + "/os#specification\"><h5>" + ob.getOEId() + "</h5></a>\n");
            }
        }

        so.append("</nav>\n");

        return so.toString();
    }

    @Path("/{oename}/os")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSpecificationHtml(@PathParam("oename") String oeName) {
        try {
            StringBuilder mainContent = new StringBuilder();
            mainContent.append("<div id=\"groups\" class=\"card fluid\">\n");

            // add groups sub section
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName)) {
                    if (((OrgArt) gb).getStyleSheet() != null) {
                        mainContent.append("    <div class=\"section\">\n");
                        StringWriter so = new StringWriter();
                        // TODO: links that comes from xsl specification are wrong!!!
                        mainContent.append("        <center><img src='/oe/" + oeName + "/os/img.svg' /></center><br/>");
                        if (show.get("groups")) 
                            mainContent.append("<a href='hide?groups'>hide groups</a>&#160;&#160;&#160;");
                        else
                            mainContent.append("<a href='show?groups'>show groups</a>&#160;&#160;&#160;");
                        
                        if (show.get("schemes")) 
                            mainContent.append("<a href='hide?schemes'>hide schemes</a>&#160;&#160;&#160;");
                        else
                            mainContent.append("<a href='show?schemes'>show schemes</a>&#160;&#160;&#160;");
                        
                        if (show.get("norms")) 
                            mainContent.append("<a href='hide?norms'>hide norms</a>");
                        else
                            mainContent.append("<a href='show?norms'>show norms</a>");

                        mainContent.append("    </div>\n");
                        mainContent.append(so.toString());
                    }
                }
            }
            mainContent.append("</div>\n");

            return designPage("JaCaMo-web - Organisation: " + oeName, oeName, mainContent.toString());
        } catch (Exception | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    Map<String,Boolean> show = new HashMap<>();
    {
        // by default show only groups
        show.put("groups", true);
        show.put("schemes", false);
        show.put("norms", false);
    }
    
    @Path("/{oename}/hide")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setHide(@PathParam("oename") String oeName,
            @QueryParam("groups") String groups,
            @QueryParam("schemes") String schemes,
            @QueryParam("norms") String norms) {
        if (groups != null) show.put("groups",false);
        if (schemes != null) show.put("schemes",false);
        if (norms != null) show.put("norms",false);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/oe/"+oeName+"/os'\" /></head>ok";
    }

    @Path("/{oename}/show")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setShow(@PathParam("oename") String oeName,
            @QueryParam("groups") String groups,
            @QueryParam("schemes") String schemes,
            @QueryParam("norms") String norms) {
        if (groups != null) show.put("groups",true);
        if (schemes != null) show.put("schemes",true);
        if (norms != null) show.put("norms",true);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/oe/"+oeName+"/os'\" /></head>ok";
    }
    
    
    @Path("/{oename}/os/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getOSImg(@PathParam("oename") String oeName) {
        try {

            //for (OrgBoard ob : OrgBoard.getOrbBoards()) {
            //    if (ob.getOEId().equals(oeName)) {
            //        OS os = OS.loadOSFromURI(ob.getOSFile());
                    String dot = getOSAsDot(oeName, show.get("groups"), show.get("schemes"), show.get("norms"));
                    if (dot != null && !dot.isEmpty()) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Graphviz
                            .fromGraph(Parser.read(dot))
                            .render(Format.SVG)
                            .toOutputStream(out);
                        return Response.ok(out.toByteArray()).build();
                    }
            //    }
            //}

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }

    @Path("/{oename}/group/{groupname}/{groupname}.npl")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGroupNpl(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    StringBuilder out = new StringBuilder();
                    out.append("<html target=\"mainframe\"><head><title>debug: " + groupName + "</title></head><body>");
                    out.append("<pre>");
                    out.append(((OrgArt) gb).getNPLSrc());
                    out.append("</pre>");
                    out.append("</body></html>");
                    return out.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    @Path("/{oename}/group/{groupname}/debug")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGroupDebug(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    // TODO: develop debug function
                    StringBuilder out = new StringBuilder();
                    out.append("<html target=\"mainframe\"><head><title>debug: " + groupName + "</title></head><body>");
                    out.append("<pre>");
                    out.append(((OrgArt) gb).getDebugText());
                    out.append("</pre>");
                    out.append("</body></html>");
                    return out.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    protected String getOSAsDot(String oeName, boolean showSS, boolean showFS, boolean showNS) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            os2dot t = new os2dot();
            
            StringWriter so = new StringWriter();
            so.append("digraph "+oeName+" {\n");
            so.append("    rankdir=BT;\n");
            so.append("    compound=true;\n\n");

            OS os = null;
            // groups
            if (showSS) {
                for (GroupBoard gb: GroupBoard.getGroupBoards()) {
                    if (gb.getOEId().equals(oeName)) {
                        os = gb.getSpec().getSS().getOS();

                        so.append( t.transformRolesDef(gb.getSpec().getSS()));
                        so.append( t.transform(gb.getSpec(), gb) );
                    }
                }
            }
            
            Set<String> done = new HashSet<>();

            // schemes
            if (showFS) {
                for (SchemeBoard sb: SchemeBoard.getSchemeBoards()) {
                    if (sb.getOEId().equals(oeName)) {
                        os = sb.getSpec().getFS().getOS();

                        so.append("        "+sb.getArtId()+ t.getSchemeInstanceStyle(sb.getArtId(), sb.isWellFormed())+";\n");
                        so.append( t.transform( sb.getSpec().getRoot(), 0, sb));    
                        //so.append("        "+sb.getArtId()+ " -> "+sb.getSpec().getRoot()+" [arrowhead=open];\n");
                        
                        // missions
                        for (Mission m: sb.getSpec().getMissions()) {
                            so.append( t.transform(m, sb));
                            for (Goal g: m.getGoals()) {
                                so.append("        "+m.getId()+" -> "+g.getId()+" [arrowsize=0.5];\n");
                            }
                        }
                        for (Player p: sb.getSchState().getPlayers()) {
                            so.append("        "+p.getAg()+ "["+t.getAgStyle()+"];\n");
                            so.append("        "+p.getAg()+" -> "+p.getTarget()+" [arrowsize=0.5];\n");
                        }                    
                    }                
                }
            }
            
            if (showNS) {
                if (os == null) {
                    for (OrgBoard ob : OrgBoard.getOrbBoards())
                        if (ob.getOEId().equals(oeName))
                            os = OS.loadOSFromURI(ob.getOSFile());                  
                }
                for (Norm n: os.getNS().getNorms()) {
                    String e = n.getRole().toString()+n.getMission();
                    if (!done.contains(e)) {
                        done.add(e);

                        String s = "bold";
                        if (n.getType() == OpTypes.permission)
                            s = "filled";
                        String cond = "";

                        if (!showSS)
                            so.append( t.transform(n.getRole()));
                        if (!showFS)
                            so.append( t.transform(n.getMission()));

                        so.append("        "+n.getRole()+" -> "+n.getMission().getId()+" [arrowhead=inv,style="+s+",label=\""+cond+"\"];\n"); // decorate=true,
                    }
                }
            }

            so.append("}\n");                   
            
            graph = so.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // debug
        try (FileWriter fw = new FileWriter("graph.gv", false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter printout = new PrintWriter(bw)) {
            printout.print(graph);
            printout.flush();
            printout.close();
        } catch (Exception ex) {
        }
        return graph;
    }

}

