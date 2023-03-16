package jacamo.web.mediation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jacamo.rest.mediation.TranslAg;
import jason.asSemantics.*;
import jason.JasonException;
import jason.asSemantics.Agent;
import jason.runtime.RuntimeServicesFactory;

public class TranslAgWeb extends TranslAg {

    /**
     * Create agent and corresponding asl file with the agName if possible, or agName_1, agName_2,...
     * 
     * @param agName
     * @return
     * @throws Exception
     * @throws JasonException
     */
    @Override
    public String createAgent(String agName) throws Exception, JasonException {
        String givenName = RuntimeServicesFactory.get().createAgent(agName, null, null, null, null, null, null);
        RuntimeServicesFactory.get().startAgent(givenName);
        // set some source for the agent
        Agent ag = getAgent(givenName);

        try {

            File f = new File("src/agt/" + givenName + ".asl");
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
        
        ag.parseAS(new FileInputStream("src/agt/" + givenName + ".asl"), givenName + ".asl");

        if (ag.getPL().hasMetaEventPlans())
            ag.getTS().addGoalListener(new GoalListenerForMetaEvents(ag.getTS()));

        ag.addInitialBelsInBB();
        ag.addInitialGoalsInTS();
        // ag.setASLSrc("no-inicial.asl");
        createAgLog(givenName, ag);
        return givenName;
    }
}

