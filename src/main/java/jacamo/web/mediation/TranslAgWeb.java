package jacamo.web.mediation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonParser;

import jacamo.rest.mediation.TranslAg;
import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;
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
        ag.load(new FileInputStream("src/agt/" + givenName + ".asl"), givenName + ".asl");
        // ag.setASLSrc("no-inicial.asl");
        createAgLog(givenName, ag);
        return givenName;
    }
    
    /**
     * get deductions from a given rule (by a predicateIndicator)
     * 
     * @param agName
     * @param predicateIndicator (e.g. parent/2)
     * @return
     */
    public Object getDeductions(String agName, String predicateIndicator) {
        Agent ag = getAgent(agName);
        for (Literal l : ag.getBB()) {
            if (predicateIndicator.equals(l.getFunctor() + "/" + l.getArity())) {
                List<String> predicates = new ArrayList<>();
                try {
                    String terms = "";
                    if (l.getArity() > 0) {
                        for (int i = 0; i < l.getArity(); i++) {
                            if (i == 0)
                                terms = "(";
                            terms += l.getTerm(i).toString();
                            if (i < l.getArity() - 1)
                                terms += ", ";
                            else
                                terms += ")";
                        }
                    }
                    Unifier u;
                    u = execCmd(ag, ASSyntax.parsePlanBody(".findall("+l.getFunctor() + terms+","+l.getFunctor() + terms+",L)"));
                    String deductions = "";
                    for (VarTerm v : u) 
                        deductions += u.get(v).toString();
                    ListTerm lt = ListTermImpl.parseList(deductions);
                    for (Term li : lt) {
                        predicates.add(((Literal)li).toString());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (TokenMgrError e) {
                    e.printStackTrace();
                }
                return predicates;
            }
        }
        return null;
    }

}

