package jacamo.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;

public class TranslEnv {

    /**
     * Get list of workspaces in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    public Collection<String> getWorkspaces() {
        return CartagoService.getNode().getWorkspaces();
    }

    /**
     * Get details about a workspace, the artifacts that are situated on this
     * including their properties, operations, observers and linked artifacts
     * 
     * @param wrksName name of the workspace
     * @return A map with workspace details
     * @throws CartagoException
     */
    public Map<String, Object> getWorkspace(String wrksName) throws CartagoException {

        Map<String, Object> workspace = new HashMap<String, Object>();

        Map<String, Object> artifacts = new HashMap<>();
        for (ArtifactId aid : CartagoService.getController(wrksName).getCurrentArtifacts()) {
            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(aid.getName());

            // Get artifact's properties
            Set<Object> properties = new HashSet<>();
            for (ArtifactObsProperty op : info.getObsProperties()) {
                for (Object vl : op.getValues()) {
                    Map<String, Object> property = new HashMap<String, Object>();
                    property.put(op.getName(), vl);
                    properties.add(property);
                }
            }

            // Get artifact's operations
            Set<String> operations = new HashSet<>();
            info.getOperations().forEach(y -> {
                operations.add(y.getOp().getName());
            });

            // Get agents that are observing the artifact
            Set<Object> observers = new HashSet<>();
            info.getObservers().forEach(y -> {
                // do not print agents_body observation
                if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    observers.add(y.getAgentId().getAgentName());
                }
            });

            // linked artifacts
            Set<Object> linkedArtifacts = new HashSet<>();
            info.getLinkedArtifacts().forEach(y -> {
                // linked artifact node already exists if it belongs to this workspace
                linkedArtifacts.add(y.getName());
            });

            // Build returning object
            Map<String, Object> artifact = new HashMap<String, Object>();
            artifact.put("artifact", aid.getName());
            artifact.put("type", info.getId().getArtifactType());
            artifact.put("properties", properties);
            artifact.put("operations", operations);
            artifact.put("observers", observers);
            artifact.put("linkedArtifacts", linkedArtifacts);
            artifacts.put(aid.getName(), artifact);
        }

        workspace.put("workspace", wrksName);
        workspace.put("artifacts", artifacts);

        return workspace;
    }

}
