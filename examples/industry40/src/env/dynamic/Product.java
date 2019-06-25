package dynamic;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import camelartifact.CamelArtifact;
import camelartifact.ArtifactComponent;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.ObsProperty;

public class Product extends CamelArtifact {

	void init() {

		final CamelContext camelContext = new DefaultCamelContext();

		// This simple application has only one component receiving messages from the route and producing operations
		camelContext.addComponent("artifact", new ArtifactComponent(this.getIncomingOpQueue(),this.getOutgoingOpQueue()));

		defineObsProperty("position", 0);

		try {
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {

					from("timer:test?period=1000")
					.process(new Processor() {
						public void process(Exchange exchange) throws Exception {
							exchange.getIn().setHeader("ArtifactName", "product");
							exchange.getIn().setHeader("OperationName", "updatePosition");

							ObsProperty prop = getObsProperty("position");
                            List<Object> listObj = new ArrayList<Object>();
                            listObj.add(prop.intValue()+1);
							exchange.getIn().setBody(listObj);

					}})
					//.to("mqtt:camelArtifact?host=tcp://broker.mqttdashboard.com:1883&publishTopicName=paams19pos")
					//.to("log:MQTTLogger1?level=info");

					//from("mqtt:camelArtifact?host=tcp://broker.mqttdashboard.com:1883&subscribeTopicName=paams19pos")
					//	.process(new Processor() {
					//		public void process(Exchange exchange) throws Exception {
					//			exchange.getIn().setHeader("ArtifactName", "product");
					//			exchange.getIn().setHeader("OperationName", "updatePosition");
					//}})
					.to("artifact:cartago")
					.to("log:MQTTLogger3?level=info");
					
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			camelContext.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@INTERNAL_OPERATION
	void updatePosition(int pos) {
		System.out.println("updatePosition to:"+pos);

		ObsProperty prop = getObsProperty("position");
		prop.updateValue(pos);
	}
}
