package dynamic;

import cartago.*;
import jason.asSyntax.Atom;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.TelegramComponent;
import org.apache.camel.impl.DefaultCamelContext;

import camelartifact.ArtifactComponent;
import camelartifact.CamelArtifact;

@ARTIFACT_INFO(outports = { @OUTPORT(name = "out-1") })

public class Telegram extends CamelArtifact {

    static int n = 0;
    List<String> menu = new ArrayList<String>();
    
    @OPERATION
    public void startCamel() {
        String token = "700859698:AAGS06B9darPqxD3wrEoCnXB47xs9zWIa2A";
        String telegramURI = "telegram:bots/" + token + "?" + "chatId=" + "-274694619";
        final CamelContext camelContext = new DefaultCamelContext();

        // This simple application has only one component receiving messages from the route and producing operations
        camelContext.addComponent("artifact", new ArtifactComponent(this.getIncomingOpQueue(),this.getOutgoingOpQueue()));
        camelContext.addComponent("telegram", new TelegramComponent());
        /* Create the routes */
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from(telegramURI).process(new Processor() {
                        public void process(Exchange exchange) {
                            try {
                                String str = exchange.getIn().getBody(String.class).toLowerCase();
                                log("Content to process: " + str);

                                exchange.getIn().setHeader("ArtifactName", getId().toString());
                                List<Object> listObj = new ArrayList<Object>();
                                exchange.getIn().setHeader("OperationName", "generateSignal");
                                listObj.add(str);
                                exchange.getIn().setBody(listObj);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).to("artifact:cartago");

                    from("artifact:cartago").process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            try {
                                String str = exchange.getIn().getBody().toString();
                                exchange.getIn().setBody(str.replaceAll("\\[", "").replaceAll("\\]", ""));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).to(telegramURI);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start routing
        log("Starting camel... (context: "+camelContext+" route definitions: "+camelContext.getRouteDefinitions().toString()+") ");
        try {
            camelContext.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("Starting artifact...");
    }

    @OPERATION
    public void sendString(String msg) {
        List<Object> params  = new ArrayList<Object>();
        params.add(msg);
        sendMsg(getId().toString(),"telegram",params);
    }   

    @INTERNAL_OPERATION
    public void generateSignal(String eventName) {
        signal(eventName);
        log("signal: " + eventName);
    }
}
