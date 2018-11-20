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

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getOrgHtml() throws CartagoException {

        StringBuilder out = new StringBuilder();

        out.append(
                "<html><head><title>Moise (list of organisational entities)</title><meta http-equiv=\"refresh\" content=\""
						+ 3 + "\" ></head><body>");

        for (OrgBoard ob : OrgBoard.getOrbBoards()) {
            out.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>organisation <b>" + ob.getOEId()
                    + "</b></p></font>");

            out.append("- <a href='/oe/" + ob.getOEId()
                    + "/os' target='cf' style=\"font-family: arial; text-decoration: none\">specification</a><br/>");
            StringWriter os = new StringWriter();
            StringWriter gr = new StringWriter();
            gr.append("<br/><scan style='color: red; font-family: arial;'>groups</scan> <br/>");
            StringWriter sch = new StringWriter();
            sch.append("<br/><scan style='color: red; font-family: arial;'>schemes</scan> <br/>");
            StringWriter nor = new StringWriter();
            nor.append("<br/><scan style='color: red; font-family: arial;'>norms</scan> <br/>");

            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(ob.getOEId())) {
                    gr.append("- <a href='/oe/" + gb.getOEId() + "/group/" + gb.getArtId()
                            + "' target='cf' style=\"font-family: arial; text-decoration: none\">" + gb.getArtId()
                            + "</a><br/>");
                }
            }
            // TODO: why the schemes are not appearing????
            for (SchemeBoard sb : SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(ob.getOEId())) {
                    sch.append("- <a href='/oe/" + sb.getOEId() + "/scheme/" + sb.getArtId()
                            + "' target='cf' style=\"font-family: arial; text-decoration: none\">" + sb.getArtId()
                            + "</a><br/>");
                }
            }
            for (NormativeBoard nb : NormativeBoard.getNormativeBoards()) {
                if (nb.getOEId().equals(ob.getOEId())) {
                    nor.append("- <a href='/oe/" + nb.getOEId() + "/norm/" + nb.getArtId()
                            + "' target='cf' style=\"font-family: arial; text-decoration: none\">" + nb.getArtId()
                            + "</a><br/>");
                }
            }
            out.append(os.toString());
            out.append(gr.toString());
            out.append(sch.toString());
            out.append(nor.toString());
        }

        out.append("<hr/>");
        out.append(
                "Under development, earlier interface <a href='http://localhost:3271/oe' target='lf'> here </a><br/><br/>");
        out.append(" by <a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a>");
        out.append("</body></html>");

        return out.toString();
    }

    @Path("/{oename}/os")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSpecificationHtml(@PathParam("oename") String oeName) {
        try {
            StringBuilder out = new StringBuilder();
            out.append("<html><head><title>" + oeName + "</title></head><body>");
            for (OrgBoard ob : OrgBoard.getOrbBoards()) {
                if (ob.getOEId().equals(oeName)) {
                    OS os = OS.loadOSFromURI(ob.getOSFile());
                    String osSpec = ob.specToStr(os,
                            DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
                    out.append(osSpec);
                }
            }
            out.append("</body></html>");

            return out.toString();
        } catch (Exception | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    @Path("/{oename}/group/{groupname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGrouptHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            StringBuilder out = new StringBuilder();
            out.append("<html><head><title>Group: " + groupName + "</title></head><body>");
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
            out.append("<hr/><a href='"+groupName+"/"+groupName+".npl'>NPL program</a>");
            out.append(" / <a href='"+ groupName +"/debug'>debug page</a>");
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
                    // TODO: develop npl function
                    StringBuilder out = new StringBuilder();
                    out.append("<html><head><title>debug: " + groupName + "</title></head><body>");
                    out.append("<pre>");
                    out.append(((OrgArt)gb).getNPLSrc());
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
                    out.append("<html><head><title>debug: " + groupName + "</title></head><body>");
                    out.append("<pre>");
                    out.append(((OrgArt)gb).getDebugText());
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

    @Path("/{oename}/scheme/{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemeHtml(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {

        try {
            StringBuilder out = new StringBuilder();
            out.append("<html><head><title>Scheme: " + schemeName + "</title></head><body>");
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

    @Path("/{oename}/norm/{groupname}.{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNormHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName,
            @PathParam("schemename") String schemeName) {
        try {
            StringBuilder out = new StringBuilder();
            out.append("<html><head><title>Scheme: " + schemeName + "</title></head><body>");
            for (NormativeBoard nb : NormativeBoard.getNormativeBoards()) {
                if (nb.getOEId().equals(oeName) && nb.getArtId().equals(groupName+"."+schemeName)) {
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
