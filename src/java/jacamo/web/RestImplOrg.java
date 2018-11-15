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
import cartago.OperationException;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import npl.parser.ParseException;
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

        StringWriter so = new StringWriter();
        
        so.append("<html><head><title>Moise (list of organisational entities)</title><meta http-equiv=\"refresh\" content=\""
                        + 3 + "\" ></head><body>");

        for (OrgBoard ob: OrgBoard.getOrbBoards()) {
            so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>organisation <b>" + ob.getOEId()
                    + "</b></p></font>");

            so.append("- <a href='/oe/"+ob.getOEId()+"/os' target='cf' style=\"font-family: arial; text-decoration: none\">specification</a><br/>");
            StringWriter os = new StringWriter();
            StringWriter gr = new StringWriter();
            gr.append("<br/><scan style='color: red; font-family: arial;'>groups</scan> <br/>");
            StringWriter sch = new StringWriter();
            sch.append("<br/><scan style='color: red; font-family: arial;'>schemes</scan> <br/>");
            StringWriter nor = new StringWriter();
            nor.append("<br/><scan style='color: red; font-family: arial;'>norms</scan> <br/>");

            for (GroupBoard gb: GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(ob.getOEId())) {
                    gr.append("- <a href='/oe/"+gb.getOEId()+"/group/"+gb.getArtId()+"' target='cf' style=\"font-family: arial; text-decoration: none\">"+gb.getArtId()+"</a><br/>");
                }
            }
            //TODO: why the schemes are not appearing????
            for (SchemeBoard sb: SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(ob.getOEId())) {
                    sch.append("- <a href='/oe/"+sb.getOEId()+"/scheme/"+sb.getArtId()+"' target='cf' style=\"font-family: arial; text-decoration: none\">"+sb.getArtId()+"</a><br/>");
                }
            }
            for (NormativeBoard nb: NormativeBoard.getNormativeBoards()) {
                if (nb.getOEId().equals(ob.getOEId())) {
                    nor.append("- <a href='/oe/"+nb.getOEId()+"/norm/"+nb.getArtId()+"' target='cf' style=\"font-family: arial; text-decoration: none\">"+nb.getArtId()+"</a><br/>");
                }
            }
            so.append(os.toString());
            so.append(gr.toString());
            so.append(sch.toString());
            so.append(nor.toString());
        }

        so.append("<hr/>");
        so.append("Under development, earlier interface <a href='http://localhost:3271/oe' target='lf'> here </a><br/><br/>");
        so.append(" by <a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a>");
        so.append("</body></html>");

        return so.toString();
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
            out.append("<html><head><title>Group: "+groupName+"</title></head><body>");
            String img = "<img src='" + groupName + "/img.svg' /><br/>";
            out.append("<details>");
            for (GroupBoard gb: GroupBoard.getGroupBoards()) {
                if (gb.getOEId().equals(oeName) && gb.getArtId().equals(groupName)) {
                    if (((OrgArt) gb).getStyleSheet() != null) {
                        StringWriter so = new StringWriter();
                        ((OrgArt) gb).getStyleSheet().setParameter("show-oe-img", "true");
                        //TODO: links that comes from xsl specification are wrong!!!
                        ((OrgArt) gb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb))),new StreamResult(so));
                        out.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) gb).getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) gb).getNormativeEngine())), new StreamResult(so));
                    out.append(so.toString());
                }
            }
            out.append("</details>");

            return img+out.toString();
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
            for (GroupBoard gb: GroupBoard.getGroupBoards()) {
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
    
    //TODO: develop functions for debug and npl
    
    @Path("/{oename}/scheme/{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemeHtml(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {

        try {
            StringBuilder out = new StringBuilder();
            out.append("<html><head><title>Scheme: "+schemeName+"</title></head><body>");
            String img = "<img src='" + schemeName + "/img.svg' /><br/>";
            out.append("<details>");
            for (SchemeBoard sb: SchemeBoard.getSchemeBoards()) {
                if (sb.getOEId().equals(oeName) && sb.getArtId().equals(schemeName)) {
                    if (((OrgArt) sb).getStyleSheet() != null) {
                        StringWriter so = new StringWriter();
                        ((OrgArt) sb).getStyleSheet().setParameter("show-oe-img", "true");
                        ((OrgArt) sb).getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb))),new StreamResult(so));
                        out.append(so.toString());
                    }
                    StringWriter so = new StringWriter();
                    ((OrgArt) sb).getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(((OrgArt) sb).getNormativeEngine())), new StreamResult(so));
                    out.append(so.toString());
                }
            }
            out.append("</details>");
            
            return img+out.toString();
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
            for (SchemeBoard sb: SchemeBoard.getSchemeBoards()) {
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
}
