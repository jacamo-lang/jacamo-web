package jacamo.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import com.google.gson.Gson;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jaca.CAgentArch;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.GoalListenerForMetaEvents;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEType;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.stdlib.print;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

/**
 * Agent's REST implementation class
 * 
 * @author Jomi Fred Hubner
 * @author Cleber Jorge Amaral
 *
 */
@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

	Map<String, StringBuilder> agLog = new HashMap<>();
	Gson gson = new Gson();

	@Override
	protected void configure() {
		bind(new RestImplAg()).to(RestImplAg.class);
	}

	/**
	 * Produces JSON containing the list of existing agents Example: ["ag1","ag2"]
	 * 
	 * @return HTTP 200 Response (ok status)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentsJSON() {
		return Response.ok().entity(gson.toJson(BaseCentralisedMAS.getRunner().getAgs().keySet()))
				.header("Access-Control-Allow-Origin", "*").build();
	}

	/**
	 * Return agent object by agent's name
	 * 
	 * @param agName name of the agent
	 * @return Agent object
	 */
	private Agent getAgent(String agName) {
		CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
		if (cag != null)
			return cag.getTS().getAg();
		else
			return null;
	}

	/**
	 * Create an Agent. Produces PLAIN TEXT with HTTP response for this operation If
	 * an ASL file with the given name exists, it will launch an agent with existing
	 * code. Otherwise, creates an agent that will start say 'Hi'.
	 * 
	 * @param agName name of the agent to be created
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@POST
	@Path("/{agentname}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createNewAgent(@PathParam("agentname") String agName) {
		try {
			String name = BaseCentralisedMAS.getRunner().getRuntimeServices().createAgent(agName, null, null, null,
					null, null, null);
			BaseCentralisedMAS.getRunner().getRuntimeServices().startAgent(name);
			// set some source for the agent
			Agent ag = getAgent(name);

			try {

				File f = new File("src/agt/" + agName + ".asl");
				if (!f.exists()) {
					f.createNewFile();
					FileOutputStream outputFile = new FileOutputStream(f, false);
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("//Agent created automatically\n\n");
					stringBuilder.append("!start.\n\n");
					stringBuilder.append("+!start <- .print(\"Hi\").\n\n");
					stringBuilder.append("{ include(\"$jacamoJar/templates/common-cartago.asl\") }\n");
					stringBuilder.append("{ include(\"$jacamoJar/templates/common-moise.asl\") }\n");
					stringBuilder.append(
							"// uncomment the include below to have an agent compliant with its organisation\n");
					stringBuilder.append("//{ include(\"$moiseJar/asl/org-obedient.asl\") }");
					byte[] bytes = stringBuilder.toString().getBytes();
					outputFile.write(bytes);
					outputFile.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			ag.load(new FileInputStream("src/agt/" + agName + ".asl"), agName + ".asl");
			// ag.setASLSrc("no-inicial.asl");
			createAgLog(agName, ag);

			return Response.ok("Agent '" + name + "' has been created!").build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Kill an agent. Produces PLAIN TEXT with response for this operation.
	 * 
	 * @param agName agent's name to be killed
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 * @throws ReceiverNotFoundException
	 */
	@DELETE
	@Path("/{agentname}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response killAgent(@PathParam("agentname") String agName) throws ReceiverNotFoundException {
		try {
			boolean r = BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent(agName, "web", 0);

			return Response.ok("Result of kill: " + r).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Produces Agent's intentions statuses in JSON format. Example:
	 * {"idle":true,"nbIntentions":1,"intentions":[{"size":1,"finished":false,"id":161,"suspended":false}]}
	 * 
	 * @param agName agent's name
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentStatusJSON(@PathParam("agentname") String agName) {
		try {
			Agent ag = getAgent(agName);
			Circumstance c = ag.getTS().getC();

			Map<String, Object> props = new HashMap<>();

			props.put("idle", ag.getTS().canSleep());

			props.put("nbIntentions", c.getNbRunningIntentions() + c.getPendingIntentions().size());

			List<Map<String, Object>> ints = new ArrayList<>();
			Iterator<Intention> ii = c.getAllIntentions();
			while (ii.hasNext()) {
				Intention i = ii.next();
				Map<String, Object> iprops = new HashMap<>();
				iprops.put("id", i.getId());
				iprops.put("finished", i.isFinished());
				iprops.put("suspended", i.isSuspended());
				if (i.isSuspended()) {
					iprops.put("suspendedReason", i.getSuspendedReason());
				}
				iprops.put("size", i.size());
				ints.add(iprops);
			}
			props.put("intentions", ints);

			return Response.ok(gson.toJson(props)).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	@GET
	@Path("/{agentname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentDetails(@PathParam("agentname") String agName) {

		Agent ag = getAgent(agName);

		// get workspaces the agent are in (including organisations)
		List<String> workspacesIn = new ArrayList<>();
		CAgentArch cartagoAgArch = getCartagoArch(ag);
		try {
			for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
				workspacesIn.add(wid.getName());
			}
			List<String> nameSpaces = new ArrayList<>();
			ag.getBB().getNameSpaces().forEach(x -> {
				nameSpaces.add(x.toString());
			});

			// get groups and roles this agent plays
			List<Object> roles = new ArrayList<>();
			for (GroupBoard gb : GroupBoard.getGroupBoards()) {
				if (workspacesIn.contains(gb.getOEId())) {
					gb.getGrpState().getPlayers().forEach(p -> {
						if (p.getAg().equals(agName)) {
							Map<String, Object> groupRole = new HashMap<>();
							groupRole.put("group", gb.getArtId());
							groupRole.put("role", p.getTarget());
							roles.add(groupRole);
						}
					});

				}
			}

			// get schemed this agent belongs
			List<Object> missions = new ArrayList<>();
			for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
				schb.getSchState().getPlayers().forEach(p -> {
					if (p.getAg().equals(agName)) {
						Map<String, Object> schemeMission = new HashMap<>();
						schemeMission.put("scheme", schb.getArtId());
						schemeMission.put("mission", p.getTarget());
						List<Object> responsibles = new ArrayList<>();
						schemeMission.put("responsibles", responsibles);
						for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
							responsibles.add(gb.getId());
						}
						missions.add(schemeMission);
					}
				});
			}

			// TODO: unify the list of 'system' artifacts with RestImplEnv
			List<Object> workspaces = new ArrayList<>();
			workspacesIn.forEach(wksName -> {
				Map<String, Object> workspace = new HashMap<>();
				workspace.put("workspace", wksName);
				List<Object> artifacts = new ArrayList<>();
				try {
					for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
						ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
						info.getObservers().forEach(y -> {
							if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
									|| (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
									|| (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
									|| (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
									|| (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
								; // do not print system artifacts
							} else {
								if (y.getAgentId().getAgentName().equals(agName)) {
									// Build returning object
									Map<String, Object> artifact = new HashMap<String, Object>();
									artifact.put("artifact", info.getId().getName());
									artifact.put("type", info.getId().getArtifactType());
									artifacts.add(artifact);
								}
							}
						});
					}
					workspace.put("artifacts", artifacts);
					workspaces.add(workspace);
				} catch (CartagoException e) {
					e.printStackTrace();
				}
			});

			Map<String, Object> agent = new HashMap<>();
			agent.put("agent", agName);
			agent.put("namespaces", nameSpaces);
			agent.put("roles", roles);
			agent.put("missions", missions);
			agent.put("workspaces", workspaces);

			return Response.ok(gson.toJson(agent)).build();
		} catch (CartagoException e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Return XML of agent's mind content including belief base, intentions and
	 * plans. See Jason's agInspection.xsl file for processing this data.
	 * 
	 * @param agName name of the agent
	 * @return A XML Document
	 * @deprecated Agent's mind in JSON format is provided in /{agentname}
	 */
	@GET
	@Path("/{agentname}/mind")
	@Produces(MediaType.APPLICATION_XML)
	public Document getAgentMindXml(@PathParam("agentname") String agName) {
		try {
			Agent ag = getAgent(agName);
			if (ag != null)
				return ag.getAgState();
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return agent's Belief base in JSON format.
	 * 
	 * @param agName
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/mind/bb")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentBBJSON(@PathParam("agentname") String agName) {
		try {
			Agent ag = getAgent(agName);
			List<String> bbs = new ArrayList<>();
			for (Literal l : ag.getBB()) {
				bbs.add(l.toString());
			}

			return Response.ok(gson.toJson(bbs)).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(500).build();
	}

	/**
	 * Return agent's plans in TEXT PLAIN format
	 * 
	 * @param agName
	 * @param label
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/plans")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAgentPlansTxt(@PathParam("agentname") String agName,
			@DefaultValue("all") @QueryParam("label") String label) {
		StringWriter so = new StringWriter();
		try {
			Agent ag = getAgent(agName);
			if (ag != null) {
				PlanLibrary pl = ag.getPL();
				if (label.equals("all"))
					so.append(pl.getAsTxt(false));
				else
					so.append(pl.get(label).toASString());
			}

			return Response.ok(so.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response
				.status(500, "Internal Server Error! Agent '" + agName + "' does not exist or cannot be observed.")
				.build();
	}

	/**
	 * Returns PLAIN TEXT of the context of an Jason agent code file (.asl). Besides
	 * the asl filename it wants the agent's name for agent's refreshing commands.
	 * 
	 * @param agName      name of the agent
	 * @param aslFileName name of the file (including .asl extension)
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/aslfile/{aslfilename}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLoadASLfileForm(@PathParam("agentname") String agName,
			@PathParam("aslfilename") String aslFileName) {

		StringBuilder so = new StringBuilder();
		try {
			BufferedReader in = null;
			File f = new File("src/agt/" + aslFileName);
			if (f.exists()) {
				in = new BufferedReader(new FileReader(f));
			} else {
				in = new BufferedReader(
						new InputStreamReader(RestImpl.class.getResource("../src/agt/" + aslFileName).openStream()));
			}
			String line = in.readLine();
			while (line != null) {
				so.append(line + "\n");
				line = in.readLine();
			}
			return Response.ok(so.toString()).build();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	// TODO: Test again this function because there are open issues on updating
	// agent's code regarding rules, KQML default plans, etc.
	/**
	 * Updates an Jason agent code file (.asl) refreshing given agent's execution
	 * plans immediately. Current intentions are kept running with old code.
	 * 
	 * @param agName              name of the agent
	 * @param aslFileName         name of the file (including .asl extension)
	 * @param uploadedInputStream new content for the given asl file name
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@POST
	@Path("/{agentname}/aslfile/{aslfilename}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response loadASLfileForm(@PathParam("agentname") String agName, @PathParam("aslfilename") String aslFileName,
			@FormDataParam("aslfile") InputStream uploadedInputStream) {
		try {
			Agent ag = getAgent(agName);
			if (ag != null) {
				System.out.println("agName: " + agName);
				System.out.println("restAPI://" + aslFileName);
				System.out.println("uis: " + uploadedInputStream);

				StringBuilder stringBuilder = new StringBuilder();
				String line = null;

				FileOutputStream outputFile = new FileOutputStream("src/agt/" + aslFileName, false);
				BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));

				while ((line = out.readLine()) != null) {
					stringBuilder.append(line + "\n");
				}

				byte[] bytes = stringBuilder.toString().getBytes();
				outputFile.write(bytes);
				outputFile.close();

				ag.getPL().clear();
				// for (Plan p: new ArrayList<>(ag.getPL().getPlans())) {
				// if (p.getSource().equals(aslFileName)) {
				// ag.getPL().remove(p.getLabel());
				// }
				// }

				ag.parseAS(new FileInputStream("src/agt/" + aslFileName), aslFileName);
				if (ag.getPL().hasMetaEventPlans())
					ag.getTS().addGoalListener(new GoalListenerForMetaEvents(ag.getTS()));

				// ag.fixAgInIAandFunctions(ag); // used to fix agent reference in functions
				// used inside includes
				ag.loadKqmlPlans();

				return Response.ok("Agent reloaded with updated file. Old intentions were not affected.").build();
			}

			return Response.status(500, "Internal Server Error! Agent'" + agName + " Does not exists!").build();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Upload new plans to an agent. Plan maintained only in memory.
	 * 
	 * @param agName              name of the agent
	 * @param plans               plans to be uploaded
	 * @param uploadedInputStream <need revision>
	 * @param fileDetail          <need revision>
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@POST
	@Path("/{agentname}/plans")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response loadPlans(@PathParam("agentname") String agName,
			@DefaultValue("") @FormDataParam("plans") String plans,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		try {
			Agent ag = getAgent(agName);
			if (ag != null) {
				ag.parseAS(new StringReader(plans), "RrestAPI");

				System.out.println("agName: " + agName);
				System.out.println("plans: " + plans);
				System.out.println("restAPI://" + fileDetail.getFileName());
				System.out.println("uis: " + uploadedInputStream);

				ag.load(uploadedInputStream, "restAPI://" + fileDetail.getFileName());
			}

			return Response.ok("ok, code uploaded for agent '" + agName + "'!").build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Get list of internal actions for an agent
	 * 
	 * @return List of internal actions
	 */
	public List<Command> getIAlist() {
		List<Command> l = new ArrayList<>();
		try {
			ClassPath classPath = ClassPath.from(print.class.getClassLoader());
			Set<ClassInfo> allClasses = classPath.getTopLevelClassesRecursive("jason.stdlib");

			allClasses.forEach(a -> {
				try {
					Class<?> c = a.load();
					if (c.isAnnotationPresent(jason.stdlib.Manual.class)) {
						// add full predicate provided by @Manual
						jason.stdlib.Manual annotation = (jason.stdlib.Manual) c
								.getAnnotation(jason.stdlib.Manual.class);
						Command cmd = new Command("'" + annotation.literal() + "'",
								"'" + annotation.hint().replaceAll("\"", "`").replaceAll("'", "`") + "'");
						l.add(cmd);
						// l.add(annotation.literal());

						// System.out.printf("%nforma :%s", annotation.literal());
						// System.out.printf("%nhint :%s", annotation.hint());
						// Literal iaLiteral = ASSyntax.parseLiteral(annotation.literal());
						// for (int i=0; i<iaLiteral.getArity(); i++) {
						// System.out.println(" " + iaLiteral.getTerm(i) + ": " +
						// annotation.argsHint()[i] + " " + annotation.argsType()[i]);
						// }
					} else {
						// add just the functor of the internal action
						Command cmd = new Command("'." + a.getSimpleName() + "'", "''");
						l.add(cmd);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

	// TODO: Use JSON format
	/**
	 * Return a TEXT PLAIN of available internal action, external actions and
	 * commands for the given agent Example:
	 * "['.desire','.drop_desire','.drop_all_desires']"
	 * 
	 * @param agName Name of the agent
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/code")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getCodeCompletionSuggestions(@PathParam("agentname") String agName) {
		List<Command> commands = new ArrayList<>();
		try {
			// get agent's plans
			Agent ag = getAgent(agName);
			if (ag != null) {
				PlanLibrary pl = ag.getPL();
				for (Plan plan : pl.getPlans()) {

					// do not add plans that comes from jar files (usually system's plans)
					if (plan.getSource().startsWith("jar:file"))
						continue;

					// add namespace when it is not default
					String ns = "";
					if (!plan.getNS().equals(Literal.DefaultNS)) {
						ns = plan.getNS().toString() + "::";
					}

					String terms = "";
					if (plan.getTrigger().getLiteral().getArity() > 0) {
						for (int i = 0; i < plan.getTrigger().getLiteral().getArity(); i++) {
							if (i == 0)
								terms = "(";
							terms += plan.getTrigger().getLiteral().getTerm(i).toString();
							if (i < plan.getTrigger().getLiteral().getArity() - 1)
								terms += ", ";
							else
								terms += ")";
						}
					}

					// when it is a goal or test goal, do not add operator
					if ((plan.getTrigger().getType() == TEType.achieve)
							|| (plan.getTrigger().getType() == TEType.test)) {

						Command cmd = new Command("'" + ns + plan.getTrigger().getType().toString()
								+ plan.getTrigger().getLiteral().getFunctor() + terms + "'", "''");
						if (!commands.contains(cmd))
							commands.add(cmd);
					}
					// when it is belief, do not add type which is anyway empty
					else if (plan.getTrigger().getType() == TEType.belief) {
						Command cmd = new Command("'" + ns + plan.getTrigger().getOperator().toString()
								+ plan.getTrigger().getLiteral().getFunctor() + terms + "'", "''");
						if (!commands.contains(cmd))
							commands.add(cmd);
					}
				}
			}

			// get internal actions
			commands.addAll(getIAlist());

			// get external actions (from focused artifacts)
			List<String> workspacesIn = new ArrayList<>();
			CAgentArch cartagoAgArch = getCartagoArch(ag);
			for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces())
				workspacesIn.add(wid.getName());

			workspacesIn.forEach(w -> {
				String wksName = w.toString();
				try {
					for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {

						// operations
						ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());

						info.getObservers().forEach(y -> {
							if (y.getAgentId().getAgentName().equals(agName)) {
								info.getOperations().forEach(z -> {
									String params = "";
									for (int i = 0; i < z.getOp().getNumParameters(); i++) {
										if (i == 0)
											params = "(";
										params += "arg" + i;
										if (i == z.getOp().getNumParameters() - 1)
											params += ")";
										else
											params += ", ";
									}

									Command cmd = new Command("'" + z.getOp().getName() + params + "'", "''");
									if (!commands.contains(cmd))
										commands.add(cmd);
								});
							}
						});

					}
				} catch (CartagoException e) {
					e.printStackTrace();
				}
			});

			Collections.sort(commands, new SortByCommandName());
			Response.ok(commands.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			Command cmd = new Command("No code completion suggestions for " + agName, "");
			commands.add(cmd);
		}

		return Response.status(500, "Server Internal Error! Could not finish process for code completion suggestions: "
				+ commands.toString()).build();
	}

	/**
	 * Command is something and agent can do. It essentially has an name and
	 * instruction about its functionality.
	 * 
	 * @author cleber
	 */
	class Command {
		String name;
		String comment;

		Command(String name, String comment) {
			this.name = name;
			this.comment = comment;
		}

		public String getName() {
			return this.name;
		}

		public String getComment() {
			return this.comment;
		}

		public String toString() {
			List<String> command = new ArrayList<>();
			command.add(name);
			command.add(comment);
			return command.toString();
		}

		@Override
		public boolean equals(Object o) {
			return this.name.equals(((Command) o).getName());

		}
	}

	class SortByCommandName implements Comparator<Command> {
		@Override
		public int compare(Command arg0, Command arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	}

	/**
	 * Send a command to an agent. Produces a TEXT PLAIN output containing a status
	 * message
	 * 
	 * @param cmd    command expression
	 * @param agName agent name
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@POST
	@Path("/{agentname}/cmd")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response runCmdPost(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
		String r;
		try {
			r = execCmd(agName, cmd.trim());
			addAgLog(agName, "Command " + cmd + ": " + r);

			return Response.ok(r).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(500).build();
	}

	// TODO: Implement websockets to avoid pooling
	/**
	 * Get agent full log in a TEXT PLAIN format
	 * 
	 * @param agName agent name
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/log")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLogOutput(@PathParam("agentname") String agName) {
		try {
			StringBuilder o = agLog.get(agName);
			if (o != null) {
				return Response.ok(o.toString()).build();
			}
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(500).build();
	}

	/**
	 * Delete agent's log.
	 * 
	 * @param agName
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 * 
	 */
	@DELETE
	@Path("/{agentname}/log")
	@Produces(MediaType.TEXT_PLAIN)
	public Response delLogOutput(@PathParam("agentname") String agName) {
		try {
			agLog.put(agName, new StringBuilder());

			Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(500).build();
	}

	/**
	 * Send a command to an agent
	 * 
	 * @param agName name of the agent
	 * @param sCmd   command to be executed
	 * @return Status message
	 */
	String execCmd(String agName, String sCmd) {
		try {
			if (sCmd.endsWith("."))
				sCmd = sCmd.substring(0, sCmd.length() - 1);
			PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
			Trigger te = ASSyntax.parseTrigger("+!run_repl_expr");
			Intention i = new Intention();
			i.push(new IntendedMeans(new Option(new Plan(null, te, null, lCmd), new Unifier()), te));

			Agent ag = getAgent(agName);
			if (ag != null) {
				TransitionSystem ts = ag.getTS();
				ts.getC().addRunningIntention(i);
				ts.getUserAgArch().wake();
				createAgLog(agName, ag);
				return "included for execution";
			} else {
				return "not implemented";
			}
		} catch (Exception e) {
			return ("Error parsing " + sCmd + "\n" + e);
		}
	}

	/**
	 * Creates a log area for an agent
	 * 
	 * @param agName agent name
	 * @param ag     agent object
	 */
	protected void createAgLog(String agName, Agent ag) {
		// adds a log for the agent
		if (agLog.get(agName) == null) {
			agLog.put(agName, new StringBuilder());
			ag.getTS().getLogger().addHandler(new StreamHandler() {
				@Override
				public void publish(LogRecord l) {
					addAgLog(agName, l.getMessage());
				}
			});
		}
	}

	/**
	 * Add a message to the agent log.
	 * 
	 * @param agName agent name
	 * @param msg    message to be added
	 */
	protected void addAgLog(String agName, String msg) {
		StringBuilder o = agLog.get(agName);
		if (o == null) {
			o = new StringBuilder();
			agLog.put(agName, o);
		} else {
			o.append("\n");
		}
		String dt = new SimpleDateFormat("dd-MM-yy HH:mm:ss").format(new Date());
		o.append("[" + dt + "] " + msg);
	}

	/**
	 * Send a message to an agent. Consumes an XML containing the message.
	 * 
	 * @param m      Message
	 * @param agName Agent name
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@POST
	@Path("/{agentname}/mb")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response addAgMsg(Message m, @PathParam("agentname") String agName) {
		try {
			CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
			if (a != null) {
				a.receiveMsg(m.getAsJasonMsg());
				return Response.ok().build();
			} else {
				return Response.status(500, "Internal Server Error! Receiver '" + agName + "' not found").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(500).build();
	}

	/**
	 * Returns agent's diagram
	 * 
	 * @param agName agent name
	 * @deprecated This is interface dependent, client should get agent's info
	 *             drawing and rendering this data by itself.
	 * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
	 *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
	 */
	@GET
	@Path("/{agentname}/mind/img.svg")
	@Produces("image/svg+xml")
	public Response getAgentImg(@PathParam("agentname") String agName) {
		try {
			String dot = getAgAsDot(agName);
			if (dot != null && !dot.isEmpty()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				MutableGraph g = Parser.read(dot);
				Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);

				return Response.ok(out.toByteArray()).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(500).build();
	}

	/**
	 * Generates agent diagram
	 * 
	 * @deprecated Only used to render graphviz which is a deprecated method
	 * @param agName agent name
	 * @return Dot representation
	 */
	protected String getAgAsDot(String agName) {
		int MAX_LENGTH = 35;
		String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";

		try {

			StringBuilder sb = new StringBuilder();

			// get workspaces the agent are in (including organisations)
			List<String> workspacesIn = new ArrayList<>();
			Agent ag = getAgent(agName);
			CAgentArch cartagoAgArch = getCartagoArch(ag);
			for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
				workspacesIn.add(wid.getName());
			}

			sb.append("digraph G {\n");
			sb.append("\tgraph [\n");
			sb.append("\t\trankdir=\"LR\"\n");
			sb.append("\t\tbgcolor=\"transparent\"\n");
			sb.append("\t]\n");

			{// beliefs will be placed on the left
				sb.append("\tsubgraph cluster_mind {\n");
				sb.append("\t\tstyle=rounded\n");
				ag.getBB().getNameSpaces().forEach(x -> {
					sb.append("\t\t\"" + x + "\" [ " + "\n\t\t\tlabel = \"" + x + "\"");
					sb.append("\n\t\t\tshape=\"cylinder\" style=filled pencolor=black fillcolor=cornsilk\n");
					sb.append("\t\t];\n");
				});
				sb.append("\t};\n");
				// just to avoid put agent node into the cluster
				ag.getBB().getNameSpaces().forEach(x -> {
					sb.append("\t\"" + agName + "\"->\"" + x + "\" [arrowhead=none constraint=false style=dotted]\n");
				});
			}

			StringBuilder orglinks = new StringBuilder();

			{ // groups and roles are also placed on the left

				for (GroupBoard gb : GroupBoard.getGroupBoards()) {
					if (workspacesIn.contains(gb.getOEId())) {
						gb.getGrpState().getPlayers().forEach(p -> {
							if (p.getAg().equals(agName)) {
								// group and role (arrow)
								sb.append("\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
								sb.append("\n\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
								sb.append("\t];\n");
								// roles (arrows)
								orglinks.append("\t\"" + gb.getArtId() + "\"->\"" + agName
										+ "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
							}
						});
					}
				}

				for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
					schb.getSchState().getPlayers().forEach(p -> {
						if (p.getAg().equals(agName)) {
							// scheme
							sb.append(
									"\t\t\"" + schb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + schb.getArtId() + "\"");
							sb.append("\n\t\t\tshape=hexagon style=filled pencolor=black fillcolor=linen\n");
							sb.append("\t\t];\n");
							for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
								orglinks.append("\t\"" + gb.getId() + "\"->\"" + schb.getArtId()
										+ "\" [arrowtail=normal arrowhead=open label=\"responsible\nfor\"]\n");
								sb.append("\t\t{rank=same " + gb.getId() + " " + schb.getArtId() + "};\n");
							}
							orglinks.append("\t\"" + schb.getArtId() + "\"->\"" + p.getAg()
									+ "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
						}
					});
				}

				sb.append(orglinks);
			}

			{// agent will be placed on center
				String s1 = (agName.length() <= MAX_LENGTH) ? agName : agName.substring(0, MAX_LENGTH) + " ...";
				sb.append("\t\"" + agName + "\" [ " + "\n\t\tlabel = \"" + s1 + "\"");
				sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
				sb.append("\t];\n");
			}

			{ // workspances and artifacts the agents is focused on
				workspacesIn.forEach(w -> {
					String wksName = w.toString();
					try {
						for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
							ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
							info.getObservers().forEach(y -> {
								if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
										|| (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
										|| (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
										|| (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
										|| (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
									; // do not print system artifacts
								} else {
									if (y.getAgentId().getAgentName().equals(agName)) {
										// create a cluster for each artifact even at same wks of other artifacts?
										sb.append("\tsubgraph cluster_" + wksName + " {\n");
										sb.append("\t\tlabel=\"" + wksName + "\"\n");
										sb.append("\t\tlabeljust=\"r\"\n");
										sb.append("\t\tgraph[style=dashed]\n");
										String str1 = (info.getId().getName().length() <= MAX_LENGTH)
												? info.getId().getName()
												: info.getId().getName().substring(0, MAX_LENGTH) + " ...";
										// It is possible to have same artifact name in different workspaces
										sb.append("\t\t\"" + wksName + "_" + info.getId().getName() + "\" [ "
												+ "\n\t\t\tlabel=\"" + str1 + " :\\n");

										str1 = (info.getId().getArtifactType().length() <= MAX_LENGTH)
												? info.getId().getArtifactType()
												: info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
										sb.append(str1 + "\"\n");

										sb.append("\t\t\tshape=record style=filled fillcolor=white;\n");
										sb.append("\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName()
												+ "\";\n");

										sb.append("\t\t\tlabeltooltip=\"teste teste\";\n");
										sb.append("\t\t\theadlabel=\"teste2\";\n");

										sb.append("\t\t\ttarget=\"mainframe\";\n");
										sb.append("\t\t];\n");

										sb.append("\t};\n");

										sb.append("\t\"" + agName + "\"->\"" + wksName + "_" + info.getId().getName()
												+ "\" [arrowhead=odot]\n");
									}
								}
							});
						}
					} catch (CartagoException e) {
						e.printStackTrace();
					}
				});
			}

			sb.append("}\n");
			graph = sb.toString();

		} catch (Exception ex) {
		}

		return graph;
	}

	/**
	 * Get agent's CArtAgO architecture
	 * 
	 * @param ag Agent object
	 * @return agent's CArtAgO architecture
	 */
	protected CAgentArch getCartagoArch(Agent ag) {
		AgArch arch = ag.getTS().getUserAgArch().getFirstAgArch();
		while (arch != null) {
			if (arch instanceof CAgentArch) {
				return (CAgentArch) arch;
			}
			arch = arch.getNextAgArch();
		}
		return null;
	}
}
