package jacamo.web;

import java.awt.List;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import cartago.ArtifactDescriptor;
import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.ICartagoController;
import cartago.WorkspaceId;
import ora4mas.nopl.ORA4MASConstants;
import ora4mas.nopl.OrgBoard;


@Singleton
@Path("/oe")
public class RestImplOrg<WorkspaceId> extends AbstractBinder {

    @Override
    protected void configure() {
        System.out.println("#0.2");
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
            ICartagoController control;
            try {
                control = CartagoService.getController(x);
                ArtifactId[] arts = control.getCurrentArtifacts();
                for (ArtifactId aid: arts) {
                    if (aid.getArtifactType().equals("ora4mas.nopl.OrgBoard")) {
                        ArtifactInfo info = CartagoService.getController(x).getArtifactInfo(aid.getName());
                        System.out.println(aid.getArtifactType() + ":" + aid.getName());

                        pageMap.put(aid.getName(), aid.getName()+"/os");
                        oePages.put(aid.getName(), pageMap);

                        // get ora4mas.nopl.GroupBoard
                        info.getObsProperties().forEach(y -> {
                            System.out.println("\n\n" + y);
                            if (y.toString().substring(0, 6).equals("group(")) {
                                pageMap.put(y.toString().substring(6, y.toString().indexOf(",")), aid.getName()+"/group/"+y.toString().substring(6, y.toString().indexOf(",")));
                                oePages.put(aid.getName(), pageMap);
                            }
                        });
                        // get ora4mas.nopl.SchemeBoard
                        //TODO
                    }
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });
        
        System.out.println("#0");
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
                String html = "<a href=\"" + addr
                        + "\" target=\"oe-frame\" style=\"font-family: arial; text-decoration: none\">" + id
                        + "</a><br/>";
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
    
}
