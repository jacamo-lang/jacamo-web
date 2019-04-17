package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataParam;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jacamo.platform.EnvironmentWebInspector;
import jason.asSemantics.Agent;

@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    int MAX_LENGTH = 30; // max length of strings when printed in graphs
    
    static Set<String> hidenArts = new HashSet<>( Arrays.asList(new String[] {
            "node",
            "console",
            "blackboard",
            "workspace",
            "manrepo",
        }));

    public String designPage(String title, String selectedItem, String selectedSubItem, String mainContent) {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\" target=\"mainframe\">\n");
        so.append("	<head>\n");
        so.append("		<title>" + title + "</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        so.append("	</head>\n"); 
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<div class=\"row\" id=\"doc-wrapper\">\n"); 
        so.append(              getEnvironmentMenu(selectedItem, selectedSubItem));                
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
    public String getWorkspacesHtml() {
        StringWriter mainContent = new StringWriter();
        mainContent.append("<div id=\"getting-started\" class=\"card fluid\">\n"); 
        mainContent.append("	<h4 class=\"section double-padded\">getting started</h4>\n"); 
        mainContent.append("	<div class=\"section\">\n"); 
        mainContent.append("		<p>\n");
        mainContent.append("			<a href=\"http://cartago.sourceforge.net\" target=\"_blank\">CArtAgO</a> is an <a href=\"https://github.com/cartago-lang/cartago\"" +
                           " 			target=\"_blank\">open-source</a> Java-based Framework for Programming Environments in Agent-oriented Applications." +
                           " 			Notice that the menu does not show empty workspaces.");       
        mainContent.append("		</p> ");        
        mainContent.append("		<br/>\n");
        mainContent.append("	</div>\n");
        mainContent.append("</div>\n");
        
        return designPage("jacamo-web - environment", "", "", mainContent.toString());
    }

    private String getEnvironmentMenu(String selectedWorkspace, String selectedArtifact) {
        
        StringWriter so = new StringWriter();

        so.append("<input id=\"doc-drawer-checkbox-frame\" class=\"leftmenu\" value=\"on\" type=\"checkbox\">\n"); 
        so.append("<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer-frame\">\n");
        so.append("	</br>\n"); 

        for (String wname : CartagoService.getNode().getWorkspaces()) {
            try {
                StringWriter arts = new StringWriter();
                for (ArtifactId aid : CartagoService.getController(wname).getCurrentArtifacts()) {
                    if (hidenArts.contains(aid.getName()))
                        continue;
                    if (aid.getName().endsWith("-body") || aid.getArtifactType().endsWith(".OrgBoard")
                            || aid.getArtifactType().endsWith(".SchemeBoard")
                            || aid.getArtifactType().endsWith(".NormativeBoard")
                            || aid.getArtifactType().endsWith(".GroupBoard"))
                        continue;
                    if (aid.getName().equals(selectedArtifact))
                        arts.append("	<a href=\"/workspaces/" + wname + "/" + aid.getName() + "\" id=\"link-to-"
								+ wname + "-" + aid.getName() + "\" target='mainframe'><h6><b>" + aid.getName()
                                + "</b></h6></a>\n");
                    else
                        arts.append("	<a href=\"/workspaces/" + wname + "/" + aid.getName() + "\" id=\"link-to-"
								+ wname + "-" + aid.getName() + "\" target='mainframe'><h6>" + aid.getName()
                                + "</h6></a>\n");
                }
                // Do not print empty workspaces, it includes workspaces that have only
                // organisation's artifacts
                if (!arts.toString().equals("")) {
                    if (wname.equals(selectedWorkspace)) {
                        so.append("	<a href=\"/workspaces/" + wname + "/\" id=\"link-to-" + wname
								+ "\" target='mainframe'><h5><b>" + wname + "</b></h5></a>\n");
                        so.append(arts.toString());
                    } else {
                        so.append("	<a href=\"/workspaces/" + wname + "/\" id=\"link-to-" + wname
								+ "\" target='mainframe'><h5>" + wname + "</h5></a>\n");
                    }
                }
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        }
        so.append("<br/>");
        so.append("<br/>");
        so.append("<a href=\"/forms/new_artifact\" target='mainframe'>create artifact</a>\n"); 
        
        
        so.append("</nav>\n");
        
        return so.toString();
    }

    @Path("/{wrksname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getWorkspaceHtml(@PathParam("wrksname") String wrksName) {
        try {
            StringWriter mainContent = new StringWriter();
            
            // overview
            mainContent.append("<div id=\"overview\" class=\"card fluid\">\n"); 
            mainContent.append("	<div class=\"section\">\n");
            mainContent.append("        <center><object data=\"/workspaces/" + wrksName + "/img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");
            
            mainContent.append("	</div>\n");
            mainContent.append("</div>\n");
            mainContent.append("<div id=\"inspection\" class=\"card fluid\">\n");
            mainContent.append("	<div class=\"section\">\n"); 
            mainContent.append("		Workspace <b>" + wrksName + "</b>\n");
            mainContent.append("	</div>\n"); 
            mainContent.append("</div>\n"); 
            
            return designPage("jacamo-web - environment: " + wrksName, wrksName, "", mainContent.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }
    
    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getArtifactHtml(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);
            StringWriter mainContent = new StringWriter();
            
            // overview
            mainContent.append("<div id=\"overview\" class=\"card fluid\">\n"); 
            mainContent.append("	<div class=\"section\">\n");
            mainContent.append("        <center><object data=\"/workspaces/" + wrksName + "/" + artName + "/img.svg\" type=\"image/svg+xml\" style=\"max-width:100%;\"></object></center><br/>\n");
            
            mainContent.append("	</div>\n");
            mainContent.append("</div>\n");
            mainContent.append("<div id=\"inspection\" class=\"card fluid\">\n");
            mainContent.append("	<div class=\"section\">\n"); 
            
            mainContent.append("		Artifact <b>" + info.getId().getName() + "</b> in workspace <b>" + wrksName + "</b>");
            mainContent.append("		<a href=\"javafile/" + info.getId().getArtifactType() + "\">[edit]</a>\n");
            mainContent.append("		<br/>");
            mainContent.append("		<br/>");
            mainContent.append("		<table>");
            mainContent.append("        	<thead><tr><th>Description</th><th>Value</th></tr></thead>");
            mainContent.append("			<tbody>");
            for (ArtifactObsProperty op: info.getObsProperties()) {
                StringBuilder vls = new StringBuilder();
                String v = "";
                for (Object vl: op.getValues()) {
                    vls.append(v+vl);
                    v = ",";
                }
                mainContent.append("        <tr><td>"+op.getName()+"</td><td>"+vls+"</td></tr>");
            }
            mainContent.append("			</tbody>");
            mainContent.append("        </table>");
            mainContent.append("    </div>\n"); 
            mainContent.append("</div>\n"); 
            
            return designPage("jacamo-web - environment: " + wrksName, wrksName, artName, mainContent.toString());
        } catch (CartagoException e) {
            e.printStackTrace();
        }
        return "error"; // TODO: set response properly
    }

    @Path("/{wrksname}/{artname}")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public String createNewArtifact(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            String r = "nok";
            
            File f = new File("src/env/" + artName.replaceAll("\\.", "/") + ".java");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream outputFile = new FileOutputStream(f, false);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("/* Artifact created automatically by jacamo-web */\n\n");
                
                // Check whether there is a package
                if (artName.lastIndexOf(".") > 0) {
                    stringBuilder.append("package " + artName.substring(0, artName.lastIndexOf(".")) + ";\n\n");
                }
                
                stringBuilder.append("import cartago.*;\n\n");
                stringBuilder.append("@ARTIFACT_INFO(outports = { @OUTPORT(name = \"out-1\") })\n\n");
                stringBuilder.append("public class " + artName.substring(artName.lastIndexOf(".")+1,artName.length()) + " extends Artifact {\n");
                stringBuilder.append("\tvoid init(int initialValue) {\n");
                stringBuilder.append("\t}\n");
                stringBuilder.append("}\n");
                
                byte[] bytes = stringBuilder.toString().getBytes();
                outputFile.write(bytes);
                outputFile.close();
            }
            r = "<br/><center>Artifact template file created!<br/>Redirecting...</center>";
            
            return "<head><meta http-equiv=\"refresh\" content=\"1; URL='/workspaces/" + wrksName + "/javafile/" + artName +
                  "'\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\"></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error " + e.getMessage();
        }
    }
    
    
    EnvironmentWebInspector bend = new EnvironmentWebInspector();
    
    @Path("/{wrksname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getWrksImg(@PathParam("wrksname") String wrksName) {
        try {
            String dot = getWksAsDot(wrksName);
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
    
    @SuppressWarnings("finally")
    protected String getWksAsDot(String wksName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            sb.append("\tsubgraph cluster_0 {\n");
            sb.append("\t\tlabel=\"" + wksName + "\"\n");
            sb.append("\t\tlabeljust=\"r\"\n");
            sb.append("\t\tgraph[style=dashed]\n");           
            for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

                if ((info.getId().getArtifactType().equals("cartago.WorkspaceArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.tools.Console")) ||
                    (info.getId().getArtifactType().equals("cartago.ManRepoArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.tools.TupleSpace")) ||
                    (info.getId().getArtifactType().equals("cartago.NodeArtifact")) ||
                    (info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))) {
                    ; // do not print system artifacts
                } else {
                    String s1;
                    s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                            : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                    sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                            + s1 + ":\n");
                    s1 = (info.getId().getArtifactType().length() <= MAX_LENGTH) ? info.getId().getArtifactType()
                            : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
                    sb.append(s1 + "\"\n");
                    sb.append("\t\tshape=record style=filled fillcolor=white\n");
                    sb.append("\t\tURL = \"/workspaces/" + info.getId().getWorkspaceId().getName() + "/" +  
                            info.getId().getName() + "\"\n");
                    sb.append("\t\ttarget=\"mainframe\"\n");
                    sb.append("\t\t];\n");
                }
                info.getObservers().forEach(y -> {
                    // do not print agents_body observation
                    if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                        // print node with defined shape
                        String s2 = (y.getAgentId().getAgentName().length() <= MAX_LENGTH)
                                ? y.getAgentId().getAgentName()
                                : y.getAgentId().getAgentName().substring(0, MAX_LENGTH) + "...";
                        sb.append("\t\"" + y.getAgentId().getAgentName() + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
                        sb.append("\t\tURL = \"/agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                        sb.append("\t\t\ttarget=\"mainframe\"\n");
                        sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                        sb.append("\t];\n");

                        // print arrow
                        sb.append("\t\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName()
                                + "\" [arrowhead=\"odot\"];\n");
                    }
                });

                // linked artifacts
                info.getLinkedArtifacts().forEach(y -> {
                    // linked artifact node already exists if it belongs to this workspace
                    sb.append("\t\"" + info.getId().getName() + "\" -> \"" + y.getName()
                            + "\" [arrowhead=\"onormal\"];\n");
                });
                    
            }
            sb.append("\t}\n");
            sb.append("}\n");
            graph = sb.toString();

        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }

    @Path("/{wrksname}/{artname}/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getArtImg(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            String dot = getArtAsDot(wrksName,artName);
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
    
    @Path("/{wrksname}/javafile/{javafilename}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getLoadJavafileForm(@PathParam("wrksname") String wrksName, @PathParam("javafilename") String javaFileName) {
        
        StringBuilder so = new StringBuilder();
        try {
            BufferedReader in = null;
            File f = new File("src/env/" + javaFileName.replaceAll("\\.", "/") + ".java");
            if (f.exists()) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(
                        new InputStreamReader(RestImpl.class.getResource("../src/env/" + javaFileName.replaceAll("\\.", "/") + ".java").openStream()));
            }
            String line = in.readLine();
            while (line != null) {
                so.append(line + "\n");
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return  "<html lang=\"en\">\n" + 
                "<head>\n" + 
                "<title>JaCaMo - Editing: "+javaFileName+"</title>\n" + 
                "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n" +
                "</head>\n" + 
                "<body>\n" + 
                "   <form action=\"/workspaces/"+wrksName+"/javafile/"+javaFileName+"\" method=\"post\" id=\"usrform\" enctype=\"multipart/form-data\">" +
                "       <div>\n" + 
                "           <textarea name=\"javafile\" form=\"usrform\">" +
                                so.toString() +
                "           </textarea>\n" + 
                "       </div>\n" + 
                "   <div class=\"editor_footer\">\n" +
                "           Editing: " + javaFileName +
                "           <button type=\"submit\" onclick=\"location.href='/workspaces/" + wrksName + "/';\">Save</button>\n" +
                "           <button type=\"button\" onclick=\"location.href='/workspaces/" + wrksName + "/';\">Discard changes</button>\n" +
                "   </div>"+
                "   </form>\n" + 
                "<script src=\"http://ajaxorg.github.io/ace-builds/src/ace.js\"></script>\n" +
                "<script src=\"/js/ace/ace.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" + 
                "<script src=\"/js/ace/ext-language_tools.js\"></script>\n" +
                "<script src=\"/js/load_artifacts_form.js\"></script>\n" +
                "</body>\n" + 
                "</html>";
    }

    @Path("/{wrksname}/javafile/{javafilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String loadJavafileForm(@PathParam("wrksname") String wrksName,
            @PathParam("javafilename") String javaFileName,
            @FormDataParam("javafile") InputStream uploadedInputStream
            ) {
        try {
            String r = "nok";
            System.out.println("wrksName: " + wrksName);
            System.out.println("restAPI://" + javaFileName);
            System.out.println("uis: " + uploadedInputStream);

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            FileOutputStream outputFile = new FileOutputStream("src/env/" + javaFileName.replaceAll("\\.", "/") + ".java", false);
            BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));

            while ((line = out.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            byte[] bytes = stringBuilder.toString().getBytes();
            outputFile.write(bytes);
            outputFile.close();

            r = "<br/><center>Artifact saved! Next instances will use this new file!<br/>Redirecting...</center>";
            return "<head><meta http-equiv=\"refresh\" content=\"1; URL='/workspaces/" + wrksName +
				   "/'\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\"></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }
    
    @SuppressWarnings("finally")
    protected String getArtAsDot(String wksName, String artName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        ArtifactInfo info;
        try {
            String s1;
            StringBuilder sb = new StringBuilder();
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir = \"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");
            info = CartagoService.getController(wksName).getArtifactInfo(artName);
            s1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                    : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
            sb.append("\t\"" + info.getId().getName() + "\" [ " + "\n\t\tlabel = \""
                    + s1 + ":\n");
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

            sb.append("\t\tshape=record style=filled fillcolor=white\n");
            sb.append("\t];\n");

            // linked artifacts
            info.getLinkedArtifacts().forEach(y -> {
                // print node with defined shape
//                String s2 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
//                        : y.getName().substring(0, MAX_LENGTH) + "...";
//                sb.append("\t\"" + y.getName() + "\" [ " + "\n\t\tlabel = \""
//                        + s2 + "|");
//                s2 = (y.getArtifactType().length() <= MAX_LENGTH) ? y.getArtifactType()
//                        : y.getArtifactType().substring(0, MAX_LENGTH) + "...";
//                sb.append(s2 + "\"\n");
                
                
                String str1 = (y.getName().length() <= MAX_LENGTH) ? y.getName()
                        : y.getName().substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\t\"" + y.getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1
                        + " :\\n");
                
                str1 = (y.getArtifactType().length() <= MAX_LENGTH)
                        ? y.getArtifactType()
                        : y.getArtifactType().substring(0, MAX_LENGTH) + " ...";
                sb.append(str1 + "\"\n");
                
                
                sb.append("\t\tURL = \"/workspaces/" + y.getWorkspaceId().getName() + "/" +  
                			y.getName() + "\"\n");
                sb.append("\t\ttarget=\"mainframe\"\n");
                sb.append("\t\tshape=record style=filled fillcolor=white\n");
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
                    sb.append("\t\tURL = \"/agents/" + y.getAgentId().getAgentName() + "/mind\"\n");
                    sb.append("\t\t\ttarget=\"mainframe\"\n");
                    sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                    sb.append("\t];\n");
                    
                    // print arrow
                    sb.append("\t\"" + y.getAgentId().getAgentName() + "\" -> \"" + info.getId().getName() 
                            + "\" [arrowhead=\"odot\"];\n");
                }
            });
            
            sb.append("}\n");
            graph = sb.toString();
            
        } catch (CartagoException e) {
            e.printStackTrace();
        } finally {
            return graph;
        }
    }
}
