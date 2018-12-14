package jacamo.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import cartago.CartagoException;
import cartago.CartagoService;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import moise.os.OS;
import moise.xml.DOMUtils;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgArt;
import ora4mas.nopl.OrgBoard;
import ora4mas.nopl.NormativeBoard;
import ora4mas.nopl.SchemeBoard;

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
         mainContent.append("	<h4 class=\"section double-padded\">Getting started</h4>\n"); 
         mainContent.append("	<div class=\"section\">\n"); 
         mainContent.append("		<p>\n");
         mainContent.append("			<a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a> is an <a href=\"https://github.com/moise-lang/moise\" target=\"_blank\">open-source</a> organisational platform for MultiAgent Systems");
         mainContent.append(" 			based on notions like roles, groups, and missions. It enables an MAS to have an explicit specification of its organisation. ");     
         mainContent.append("		</p> ");
         mainContent.append("		<br/>\n");
         mainContent.append("	</div>\n");
         mainContent.append("</div>\n");
         
        return designPage("JaCamo-Rest - Organisation","",mainContent.toString());
    }

    private String getOrganisationMenu(String selectedOrganisation) {

        StringWriter so = new StringWriter();

        so.append("<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n");
        so.append("<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("	</br>\n");

        for (OrgBoard ob : OrgBoard.getOrbBoards()) {
            if (ob.getOEId().equals(selectedOrganisation)) {
                so.append("	<a href=\"/oe/" + ob.getOEId() + "/os#specification\"><h5>. " + ob.getOEId() + "</h5></a>\n");
                so.append("	<a href='/oe/" + ob.getOEId() + "/os#groups' target='mainframe'><h6>. Groups</h6></a>\n");
                so.append("	<a href='/oe/" + ob.getOEId() + "/os#schemes' target='mainframe'><h6>. Schemes</h6></a>\n");
                so.append("	<a href='/oe/" + ob.getOEId() + "/os#norms' target='mainframe'><h6>. Norms</h6></a>\n");
            } else {
                so.append("	<a href=\"/oe/" + ob.getOEId() + "/os#specification\"><h5>+ " + ob.getOEId() + "</h5></a>\n");
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
            mainContent.append("<div id=\"specification\" class=\"card fluid\">\n");
            mainContent.append("	<h2 class=\"section double-padded\">Specification</h2>\n");
            mainContent.append("	<div class=\"section\">\n");
            for (OrgBoard ob : OrgBoard.getOrbBoards()) {
                if (ob.getOEId().equals(oeName)) {
                    OS os = OS.loadOSFromURI(ob.getOSFile());
                    String osSpec = ob.specToStr(os,
                            DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
                    mainContent.append(osSpec);
                }
            }
            mainContent.append("	</div>\n");
            mainContent.append("</div>\n");

            mainContent.append("<div id=\"groups\" class=\"card fluid\">\n");
            mainContent.append("	<h2 class=\"section double-padded\">Groups</h2>\n");

            // add groups sub section
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName)) {
                    if (((OrgArt) gb).getStyleSheet() != null) {
                        mainContent.append("	<div class=\"section\">\n");
                        StringWriter so = new StringWriter();
                        ((OrgArt) gb).getStyleSheet().setParameter("show-oe-img", "true");
                        // TODO: links that comes from xsl specification are wrong!!!
                        ((OrgArt) gb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb))),
                                new StreamResult(so));
                        mainContent.append("		<center><img src='/oe/" + oeName + "/group/" + gb.getArtId()
                                + "/img.svg' /></center><br/>");
                        mainContent.append("	</div>\n");
                        mainContent.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) gb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb).getNormativeEngine())),
                            new StreamResult(so));
                    mainContent.append(so.toString());
                }
            }
            mainContent.append("</div>\n");
            
            mainContent.append("<div id=\"schemes\" class=\"card fluid\">\n");
            mainContent.append("	<h2 class=\"section double-padded\">Schemes</h2>\n");

            for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(oeName)) {
                    if (((OrgArt) sb).getStyleSheet() != null) {
                        StringWriter so = new StringWriter();
                        ((OrgArt) sb).getStyleSheet().setParameter("show-oe-img", "true");
                        ((OrgArt) sb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb))),
                                new StreamResult(so));
                        mainContent.append("	<center><img src='/oe/" + oeName + "/scheme/"
                                + sb.getArtId() + "/img.svg' /></center><br/>");
                        mainContent.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) sb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb).getNormativeEngine())),
                            new StreamResult(so));
                    mainContent.append(so.toString());
                }
            }
            mainContent.append("</div>\n");

            mainContent.append("<div id=\"norms\" class=\"card fluid\">\n");
            mainContent.append("	<h2 class=\"section double-padded\">Norms</h2>\n");

            for (NormativeBoard nb : NormativeBoard.getNormativeBoards()) {
                if (nb.getOEId().equals(oeName)) {
                    StringWriter so = new StringWriter();
                    ((OrgArt) nb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) nb).getNormativeEngine())),
                            new StreamResult(so));
                    mainContent.append(so.toString());
                }
            }
            mainContent.append("</div>\n");

            return designPage("JaCamo-Rest - Organisation: " + oeName, oeName, mainContent.toString());
        } catch (Exception | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    // this method is not being used anymore, should be kept for other rest requests?
    @Path("/{oename}/group/{groupname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGrouptHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            StringBuilder out = new StringBuilder();
            out.append("<html target=\"mainframe\"><head><title>Group: " + groupName + "</title></head><body>");
            String img = "<img src='" + groupName + "/img.svg' /><br/>";
            out.append("<details>");
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    if (((OrgArt) gb).getStyleSheet() != null) {
                        StringWriter so = new StringWriter();
                        ((OrgArt) gb).getStyleSheet().setParameter("show-oe-img", "true");
                        // TODO: links that comes from xsl specification are wrong!!!
                        ((OrgArt) gb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb))),
                                new StreamResult(so));
                        out.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) gb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb).getNormativeEngine())),
                            new StreamResult(so));
                    out.append(so.toString());
                }
            }
            out.append("</details>");
            out.append("<hr/><a href='" + groupName + "/" + groupName + ".npl'>NPL program</a>");
            out.append(" / <a href='" + groupName + "/debug'>debug page</a>");
            out.append("</body></html>");

            return img + out.toString();
        } catch (TransformerException | TransformerFactoryConfigurationError | IOException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    @Path("/{oename}/group/{groupname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getGroupImg(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    String dot = gb.getAsDot();
                    if (dot != null && !dot.isEmpty()) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        MutableGraph g = Parser.read(dot);
                        Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                        return Response.ok(out.toByteArray()).build();
                    }
                }
            }
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

    // this method is not being used anymore, should be kept for other rest requests?
    @Path("/{oename}/scheme/{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemeHtml(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {

        try {
            StringBuilder out = new StringBuilder();
            out.append("<html target=\"mainframe\"><head><title>Scheme: " + schemeName + "</title></head><body>");
            String img = "<img src='" + schemeName + "/img.svg' /><br/>";
            out.append("<details>");
            for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(oeName) && sb.getArtId().equals(schemeName)) {
                    if (((OrgArt) sb).getStyleSheet() != null) {
                        StringWriter so = new StringWriter();
                        ((OrgArt) sb).getStyleSheet().setParameter("show-oe-img", "true");
                        ((OrgArt) sb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb))),
                                new StreamResult(so));
                        out.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) sb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb).getNormativeEngine())),
                            new StreamResult(so));
                    out.append(so.toString());
                }
            }
            out.append("</details>");
            out.append("</body></html>");

            return img + out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    @Path("/{oename}/scheme/{schemename}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getSchemeImg(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {
        try {
            for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(oeName)) {
                    String dot = sb.getAsDot();
                    if (dot != null && !dot.isEmpty()) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        MutableGraph g = Parser.read(dot);
                        Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                        return Response.ok(out.toByteArray()).build();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }

    // this method is not being used anymore, should be kept for other rest requests?
    @Path("/{oename}/norm/{groupname}.{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNormHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName,
            @PathParam("schemename") String schemeName) {
        try {
            StringBuilder out = new StringBuilder();
            out.append("<html target=\"mainframe\"><head><title>Scheme: " + schemeName + "</title></head><body>");
            for (NormativeBoard nb : NormativeBoard.getNormativeBoards()) {
                if (nb.getOEId().equals(oeName) && nb.getArtId().equals(groupName + "." + schemeName)) {
                    StringWriter so = new StringWriter();
                    ((OrgArt) nb).getNSTransformer().transform(
                            new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) nb).getNormativeEngine())),
                            new StreamResult(so));
                    out.append(so.toString());
                }
            }
            out.append("</body></html>");

            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }
}
