package jacamo.web;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ClientTest {

	public static void main(String[] args) {
		new ClientTest().t();
	}

	private void t() {
		try {
			String url = "http://10.0.1.2:3281/";
			
			Client client = ClientBuilder.newClient();
			Response response = client
					.target(url)
					.path("agents/bob/all")
					.request(MediaType.APPLICATION_XML)
					.get();

			System.out.println(response.toString() 
					+ "\n   "+response.getEntity() 
					+ "\n   "+response.readEntity(String.class));

			Message m = new Message("33","tell","jomi","bob","oi");				
			String r = client
					.target(url)
					.path("agents/bob/mb")
					.request(MediaType.APPLICATION_XML)
					.accept(MediaType.TEXT_PLAIN)
					.post(Entity.xml(m), String.class);

			System.out.println("Message sent result: "+r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
