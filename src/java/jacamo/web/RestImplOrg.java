package jacamo.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.ICartagoController;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jacamo.platform.EnvironmentWebInspector;
import jacamo.web.plugins.GoalNode;


@Singleton
@Path("/oe")
public class RestImplOrg extends AbstractBinder {
    
    int MAX_LENGTH = 30; // max length of strings when printed in graphs

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
                        //System.out.println(aid.getArtifactType() + ":" + aid.getName());

                        pageMap.put(aid.getName(), "oe/"+aid.getName()+"/os");
                        oePages.put(aid.getName(), pageMap);

                        info.getObsProperties().forEach(y -> {
                            //System.out.println("Prop: "+y.getName() + "- " + y.toString());
                            // add group, described on ora4mas.nopl.GroupBoard
                            if (y.toString().substring(0, 6).equals("group(")) {
                                pageMap.put(y.toString().substring(6, y.toString().indexOf(",")), "oe/" + aid.getName()
                                        + "/group/" + y.toString().substring(6, y.toString().indexOf(",")));
                                oePages.put(aid.getName(), pageMap);
                            }
                            // add scheme which is described on ora4mas.nopl.OrgBoard
                            if (y.toString().contains("[scheme_specification(")) {
                                int id_ini = y.toString().indexOf("[scheme_specification(")
                                        + "[scheme_specification(".length();
                                String name_scheme = y.toString().substring(id_ini, y.toString().indexOf(",", id_ini));
                                pageMap.put(name_scheme, "oe/" + aid.getName() + "/scheme/" + name_scheme);
                                System.out.println("### " + y.toString());
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
                
                if (addr.indexOf(oeId+"/group") > 0)
                    gr.append("- " + html);
                else if (addr.indexOf(oeId+"/scheme") > 0)
                    sch.append("- " + html);
                else if (addr.indexOf(oeId+"/norm") > 0) //TODO must have something to know that it is a norm ????
                    nor.append("- " + html);
                else {} // do nothing, it not belongs this organization
                
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
    
    @Path("/{oename}/group/{groupname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGrouptHtml(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            String img = "<img src='" + groupName + "/img.svg' /><br/>";
            
            ArtifactInfo info = CartagoService.getController(oeName).getArtifactInfo(groupName);
            
            StringBuilder out = new StringBuilder("<html>");
            out.append("<details><span style=\"color: red; font-family: arial\"><font size=\"+2\">");
            out.append("Inspection of artifact <b>"+info.getId().getName()+"</b> in organziation "+oeName+"</font></span>");
            out.append("<table border=0 cellspacing=3 cellpadding=6 style='font-family:verdana'>");
            for (ArtifactObsProperty op: info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl: op.getValues()) {
                    vls.append(v+vl);
                    v = ",";
                }
                out.append("<tr><td>"+op.getName()+"</td><td>"+vls+"</td></tr>");
            }
            out.append("</details></table>");
            out.append("</html>");
            
            return img+out.toString();
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }
    
    @Path("/{oename}/group/{groupname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getGroupImg(@PathParam("oename") String oeName, @PathParam("groupname") String groupName) {
        try {
            String dot = getGroupAsDot(oeName,groupName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }
    
    @Path("/{oename}/scheme/{schemename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemeHtml(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {

        try {
            String img = "<img src='" + schemeName + "/img.svg' /><br/>";
            
            // scheme is described on ora4mas.nopl.GroupBoard
            ArtifactInfo info = CartagoService.getController(oeName).getArtifactInfo(oeName);
            
            System.out.println("*** "+info.toString());
            
            StringBuilder out = new StringBuilder("<html>");
            out.append("<details><span style=\"color: red; font-family: arial\"><font size=\"+2\">");
            out.append("Inspection of artifact <b>"+info.getId().getName()+"</b> in organziation "+oeName+"</font></span>");
            out.append("<table border=0 cellspacing=3 cellpadding=6 style='font-family:verdana'>");
            for (ArtifactObsProperty op: info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl: op.getValues()) {
                    vls.append(v+vl);
                    v = ",";
                }
                out.append("<tr><td>"+op.getName()+"</td><td>"+vls+"</td></tr>");
            }
            out.append("</details></table>");
            out.append("</html>");
            
            return img+out.toString();
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
/*      
        StringWriter so = new StringWriter();
        ArrayList<String> list = new ArrayList<String>(CartagoService.getNode().getWorkspaces());
        list.forEach(x -> {
            try {
                ICartagoController control = CartagoService.getController(x);
                ArtifactId[] arts = control.getCurrentArtifacts();
                for (ArtifactId aid: arts) {
                    System.out.println("*** " + aid.getArtifactType() + ":" + aid.getName());
                    if ((aid.getArtifactType().equals("ora4mas.nopl.SchemeBoard")) && (aid.getName().equals(schemeName))){
                        ArtifactInfo info = CartagoService.getController(x).getArtifactInfo(aid.getName());
                        
                        System.out.println("### " + aid.getArtifactType() + ":" + aid.getName());

                        so.append("<html><head><title>"+schemeName+"</title></head><body>");
                            
                        so.append("<hr/> Properties:<br/>");
                        info.getObsProperties().forEach(y -> so.append(">>> " + y + " <br/>"));
                        so.append("<hr/> Observers: <br/>");
                        info.getObservers().forEach(y -> so.append(">>> " + y.getAgentId().getAgentName() + " <br/>"));
                        so.append("<hr/> Operations: <br/>");
                        info.getOperations().forEach(y -> so.append(">>> " + y.getOp().getName() + " <br/>"));
                        so.append("<hr/> OngoingOp: <br/>");
                        info.getOngoingOp().forEach(y -> so.append(">>> " + y.getName() + " <br/>"));
                        
                        so.append("</body></html>");
                        
                    }
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });
        return so.toString();
        */
    }

    @Path("/{oename}/scheme/{schemename}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getSchemeImg(@PathParam("oename") String oeName, @PathParam("schemename") String schemeName) {
        try {
            String dot = getOrgSpecificationAsDot(oeName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }
    
    //TODO: It is a copy and paste from artifacts, must be adapted to groups
    @SuppressWarnings("finally")
    protected String getGroupAsDot(String wksName, String artName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        ArtifactInfo info;
        try {
            String s1;
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t]\n");
            info = CartagoService.getController(wksName).getArtifactInfo(artName);
            s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                    : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
            sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                    + s1 + "|");
            s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
                    : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
            sb.append(s1 + "|");
            
            // observable properties
            info.getObsProperties().forEach(y -> {
                String s2 = (y.toString().length() <= MAX_LENGTH) ? y.toString()
                        : y.toString().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\t" + s2 + "\n");
            });
            sb.append("\t\t\t|");
            
            // operations
            info.getOperations().forEach(y -> {
                String s2 = (y.getOp().getName().length() <= MAX_LENGTH) ? y.getOp().getName()
                        : y.getOp().getName().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\t" + s2 + "\n");
            });   
            sb.append("\t\t\t\"\n");

            sb.append("\t\tshape = \"record\"\n");
            sb.append("\t];\n");

            // linked artifacts
            info.getLinkedArtifacts().forEach(y -> {
                // print node with defined shape
                String s2 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
                        : y.getName().substring(0, MAX_LENGTH) + "...";
                sb.append("\t\"" + y.getName() + "\" [ " + "\n\t\tlabel = \""
                        + s2 + "|");
                s2 = (y.getArtifactType().length() <= MAX_LENGTH) ? y.getArtifactType()
                        : y.getArtifactType().substring(0, MAX_LENGTH) + "...";
                sb.append(s2 + "\"\n");
                sb.append("\t\tURL = \"../" + y.getName() + "/img.svg\"\n");
                sb.append("\t\tshape = \"record\"\n");
                sb.append("\t];\n");

                // print arrow
                sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName()
                      + "\" [arrowhead=\"onormal\"];\n");
            });

            // observers
            info.getObservers().forEach(y -> {
                // do not print agents_body observation
                if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    // print node with defined shape
                    String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH) ? y.getAgentId().getAgentName()
                            : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \""
                            + s2 + "\"\n");
                    sb.append("\t\tURL = \"../../../agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                    sb.append("\t];\n");
                    
                    // print arrow
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName() 
                            + "\" [arrowhead=\"odot\"];\n");
                }
            });
            
            sb.append("}\n");
            graph = sb.toString();

            // for debug
            try (FileWriter fw = new FileWriter("graph.gv", false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                 out.print(graph);
                out.flush();
                out.close();
            } catch (Exception ex) {
            }
            
        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }

	private static String getOrgSpecificationAsDot(String oeName) {
		
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        StringBuilder sb = new StringBuilder();
		ArtifactInfo info;
		try {
			info = CartagoService.getController(oeName).getArtifactInfo(oeName);
			String spec = "";
	        for (ArtifactObsProperty op: info.getObsProperties())
	            if (op.getName().equals("specification")) spec = op.getValue().toString(); 

			List<GoalNode> tree = new ArrayList<GoalNode>();
			Stack<GoalNode> stackParent = new Stack<GoalNode>();
			stackParent.push(null);

			List<String> reservedWords = new ArrayList();
			reservedWords.add("os");
			reservedWords.add("authority");
			reservedWords.add("role");
			reservedWords.add("communication");
			reservedWords.add("soc");
			reservedWords.add("link");
			reservedWords.add("mission");
			reservedWords.add("scheme_specification");
			reservedWords.add("norm");
			reservedWords.add("goal");
			
			int lastIndex = 0; 
			for (int i = 0; i < spec.length(); i++)
		    {
		        char current = spec.charAt(i);
		        if (current == '(' || current == '[')
		        {
		        	String name = spec.substring(lastIndex, i);
		        	if (name.indexOf(",") > 0) name = name.substring(0,name.indexOf(","));
		        	name = name.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\(", "").replaceAll("\\)", "");
		        	System.out.println("nd: '"+name+"'");
		        	//if (name.length() > 0) 
		        	{
			        	if (stackParent.peek() != null) System.out.println("New node: '"+name+"' - parent: '"+stackParent.peek().getGoalName()+"'");
			        	GoalNode newNode = stackParent.peek();
			        	if (name.length() > 0 && !reservedWords.contains(name)) {
			        		newNode = new GoalNode(stackParent.peek(),name);
				        	tree.add(newNode);
			        	}
			        	lastIndex = i;
			        	stackParent.push(newNode);
		        	}
		        }
		        if (current == ')' || current == ']')
		        {
		        	stackParent.pop();
		        }
		    }
			
			sb.append("digraph G {");
			for (GoalNode or : tree) {
				if (or.getOperator().equals("parallel")) {
					sb.append("\t\"" + or.getGoalName() + "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
							+ "shape = \"diamond\" label = <<table border=\"0\" cellborder=\"0\">"
							+ "<tr><td align=\"center\"><font color=\"black\"><b>" 
							+ or.getGoalName() + "</b></font></td></tr>");
				} else {
					sb.append("\t\"" + or.getGoalName() + "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
							+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
							+ "<tr><td align=\"center\"><b>" 
							+ or.getGoalName() + "</b></td></tr>");
				}
				for (String s : or.getSkills())
					sb.append("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");
				sb.append("</table>> ];");
				if (or.getParent() != null)
					sb.append("\t\"" + or.getParent().getGoalName() + "\"->\"" + or.getGoalName() + "\";");
			}
	    		
			sb.append("}");
			graph = sb.toString();
		} catch (CartagoException e) {
			e.printStackTrace();
		}
		
		return graph;
	}
}
