package jacamo.web;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestAppConfig extends ResourceConfig {
    public RestAppConfig() {
        registerInstances(new RestImpl());
        registerInstances(new RestImplAg());
        registerInstances(new RestImplDF());
        registerInstances(new RestImplEnv());
        registerInstances(new RestImplOrg());
        
        addProperties(new HashMap<String,Object>() {
            private static final long serialVersionUID = 1L;

        { put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }} );
    }
}
