package jacamo.web;

import java.awt.List;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.ICartagoController;


@Singleton
@Path("/oe")
public class RestImplOrg extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplOrg()).to(RestImplOrg.class);
    }

    protected Map<String, Map<String, String>> oePages = (Map<String, Map<String, String>>) new HashMap();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getOrgHtml() throws CartagoException {

        StringWriter so = new StringWriter();

        Map<String,String> pageMap = new HashMap<>();
        ArrayList<String> list = new ArrayList<String>(CartagoService.getNode().getWorkspaces());
        list.forEach(x -> {
            try {
                ICartagoController control = CartagoService.getController(x);
                ArtifactId[] arts = control.getCurrentArtifacts();
                for (ArtifactId aid: arts) {
                    if (aid.getArtifactType().equals("ora4mas.nopl.OrgBoard")) {
                        ArtifactInfo info = CartagoService.getController(x).getArtifactInfo(aid.getName());
                        System.out.println(aid.getArtifactType() + ":" + aid.getName());

                        pageMap.put(aid.getName(), "oe/"+aid.getName()+"/os");
                        oePages.put(aid.getName(), pageMap);

                        info.getObsProperties().forEach(y -> {
                            // get ora4mas.nopl.GroupBoard
                            if (y.toString().substring(0, 6).equals("group(")) {
                                pageMap.put(y.toString().substring(6, y.toString().indexOf(",")), "oe/"+aid.getName()+"/group/"+y.toString().substring(6, y.toString().indexOf(",")));
                                oePages.put(aid.getName(), pageMap);
                            }
                            // get ora4mas.nopl.SchemeBoard
                            if (y.toString().substring(0, 7).equals("scheme(")) {
                                pageMap.put(y.toString().substring(7, y.toString().indexOf(",")), "oe/"+aid.getName()+"/scheme/"+y.toString().substring(7, y.toString().indexOf(",")));
                                oePages.put(aid.getName(), pageMap);
                            }
                            //TODO norms
                        });
                    } 

                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });
        
        so.append("<html><head><title>Moise (list of organisational entities)</title><meta http-equiv=\"refresh\" content=\""
						+ 3 + "\" ></head><body>");

        for (String oeId : oePages.keySet()) {
            so.append("<font size=\"+2\"><p style='color: red; font-family: arial;'>organisation <b>" + oeId
                    + "</b></p></font>");

            Map<String, String> pages = oePages.get(oeId);
            StringWriter os = new StringWriter();
            StringWriter gr = new StringWriter();
            gr.append("<br/><scan style='color: red; font-family: arial;'>groups</scan> <br/>");
            StringWriter sch = new StringWriter();
            sch.append("<br/><scan style='color: red; font-family: arial;'>schemes</scan> <br/>");
            StringWriter nor = new StringWriter();
            nor.append("<br/><scan style='color: red; font-family: arial;'>norms</scan> <br/>");
            // show os
            // show groups
            // show schemes
            for (String id : pages.keySet()) {
                String addr = pages.get(id);
                String html = "<a href=\"" + addr + "\" target='cf' style=\"font-family: arial; text-decoration: none\">" + id + "</a><br/>";
                
                if (addr.endsWith("os"))
                    os.append(html);
                else if (addr.indexOf("/group") > 0)
                    gr.append("- " + html);
                else if (addr.indexOf("/scheme") > 0)
                    sch.append("- " + html);
                else
                    nor.append("- " + html);
                
            }
            so.append(os.toString());
            so.append(gr.toString());
            so.append(sch.toString());
            so.append(nor.toString());
        }

        so.append("<hr/>");
        so.append("Under development, stable interface <a href='http://localhost:3271/oe' target='lf'> here </a><br/><br/>");
        so.append(" by <a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a>");
        so.append("</body></html>");

        return so.toString();
    }
    
    @Path("/{oename}/group/{groupname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGrouptHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        StringWriter so = new StringWriter();
        ArrayList<String> list = new ArrayList<String>(CartagoService.getNode().getWorkspaces());
        list.forEach(x -> {
            try {
                ICartagoController control = CartagoService.getController(x);
                ArtifactId[] arts = control.getCurrentArtifacts();
                for (ArtifactId aid: arts) {
                    if ((aid.getArtifactType().equals("ora4mas.nopl.GroupBoard")) && (aid.getName().equals(groupName))){
                        ArtifactInfo info = CartagoService.getController(x).getArtifactInfo(aid.getName());

                        so.append("<html><head><title>"+groupName+"</title></head>Nbody>");
                            
                        so.append("<hr/> Properties" + info.getObsProperties() + "<br/><br/>");
                        so.append("<hr/> Observers" + info.getObservers() + "<br/><br/>");
                        so.append("<hr/> Operations" + info.getOperations() + "<br/><br/>");

                        so.append("</body></html>");
                        
                    }
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });
        return so.toString();
    }

    @Path("/{oename}/scheme/{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemeHtml(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {
        StringWriter so = new StringWriter();
        ArrayList<String> list = new ArrayList<String>(CartagoService.getNode().getWorkspaces());
        list.forEach(x -> {
            try {
                ICartagoController control = CartagoService.getController(x);
                ArtifactId[] arts = control.getCurrentArtifacts();
                for (ArtifactId aid: arts) {
                    if ((aid.getArtifactType().equals("ora4mas.nopl.SchemeBoard")) && (aid.getName().equals(schemeName))){
                        ArtifactInfo info = CartagoService.getController(x).getArtifactInfo(aid.getName());
                        System.out.println(aid.getArtifactType() + ":" + aid.getName());

                        so.append("<html><head><title>"+schemeName+"</title></head>Nbody>");
                            
                        so.append("<hr/> Properties" + info.getObsProperties() + "<br/><br/>");
                        so.append("<hr/> Observers" + info.getObservers() + "<br/><br/>");
                        so.append("<hr/> Operations" + info.getOperations() + "<br/><br/>");

                        so.append("</body></html>");
                        
                    }
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });
        return so.toString();
    }
}
