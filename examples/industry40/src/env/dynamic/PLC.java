package dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import com.summit.camel.opc.Opcda2Component;

import camelartifact.CamelArtifact;
import camelartifact.ArtifactComponent;
import cartago.INTERNAL_OPERATION;
import cartago.ObsProperty;

public class PLC extends CamelArtifact {

    static Opcda2Component opcda2;
    static String containerId;
    
    static String domain = "ADMINISTRADOR";
    static String user = "Administrador";
    static String password = "administrador";
    static String clsid = "f8582cf2-88fb-11d0-b850-00c0f0104305";
                       //"{F8582CF2-88FB-11D0-B850-00C0F0104305}"
    static String host = "192.168.56.101";

    int contagem = 0;

    void init() {

        // final Random rand = new Random();
        final CamelContext camelContext = new DefaultCamelContext();

        // This simple application has only one component receiving messages from the route and producing operations
        camelContext.addComponent("artifact", new ArtifactComponent(this.getIncomingOpQueue(),this.getOutgoingOpQueue()));
        defineObsProperty("plcinfo", 0);


        /* Create the routes */
        try {
            camelContext.addRoutes(new RouteBuilder() {

                @Override
                public void configure() {
                    
                	System.out.println("\n\nReceiving opc messages...\n\n");
                    
                    //***********************************************************************************
                    //OPC-DA Tests step 1: Receiving a message from OPC-DA
                    //Matrikon simulation server is sending a unique message continuously without any asking proccess
                    //Bellow this project is testing camel artifact producer with this feature of matrikon server
                    //The expected response is like ...Bucket Brigade.ArrayOfString={value=[Ljava.lang.String;@14070c0}, 
                    //Bucket Brigade.Boolean={value=false}, Bucket Brigade.Int1={value=1}, Bucket Brigade.Int2={value=2}... 
                    String uriString = "opcda2:Matrikon.OPC.Simulation.1?delay=2000&host=" 
                            + host + "&clsId=" + clsid + "&username=" + user + "&password=" + password + "&domain=" 
                            + domain + "&diffOnly=false";
                    from(uriString).process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange.getIn().setHeader("ArtifactName", "plc");
                            exchange.getIn().setHeader("OperationName", "getIntTag");
                            Map<String, Map<String, Object>> body = exchange.getIn().getBody(Map.class);
                            List<Object> listObj = new ArrayList<Object>();
                            for (String tagName : body.keySet()) {
                                Object value = body.get(tagName).get("value");
                                //log("Tag received: " + tagName + " = " + value.toString());
                                //For this test we are looking for Bucket Brigade.Int1 tag. It is simulating
                                //a receiving process of a tag, so this tagname and tagvalue are being added
                                //in the object list to be processed be producer
                                if (tagName.equals("Bucket Brigade.Int1")){
                                    log("Adding tag" + tagName + " = " + value.toString() + " in the queue");
                                    listObj.add("Bucket Brigade.Int1");
                                    listObj.add(value);
                                }
                            }
                            exchange.getIn().setBody(listObj);
                        }
                    })
                    .to("artifact:cartago").to("log:OPCDALogger1?level=info");
                    //OPC-DA Tests step 2: Setting a tag
                    from("artifact:cartago").process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            log.trace("Processing sending msgs...");
                            //The expected format is something like: "Bucket Brigade.Int1={value=1}"
                            Map<String, Map<String, Object>> data = new TreeMap<String, Map<String, Object>>();
                            Map<String, Object> dataItems = new TreeMap<String, Object>();
                            List<Object> params  = exchange.getIn().getBody(List.class);
                            dataItems.put("value",params.get(1));
                            data.put(params.get(0).toString(),dataItems);
                            exchange.getIn().setBody(data);
                        }
                    }).to(uriString).to("log:OPCDALogger2?level=info");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start routing
        System.out.println("Starting camel...");
        try {
            camelContext.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Starting router...");
    }

    @INTERNAL_OPERATION
    void getIntTag(String str, int i) {
    	System.out.println("Router: getIntTag called!");
        if (str.equals("Bucket Brigade.Int1")) {
            ObsProperty prop = getObsProperty("plcinfo");
            prop.updateValue(i);
        }
    }
    
    @INTERNAL_OPERATION
    void setIntTag(String str, int i) {
    	System.out.println("Router: setIntTag called!");
        List<Object> params  = new ArrayList<Object>();
        params.add("Bucket Brigade.Int1");
        params.add(i);
        sendMsg("plc","setIntTag",params);
    }
}

           
