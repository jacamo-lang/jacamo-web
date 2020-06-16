package jacamo.web.config;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import jacamo.rest.implementation.RestImplDF;
import jacamo.web.implementation.WebImpl;
import jacamo.web.implementation.WebImplAg;
import jacamo.web.implementation.WebImplEnv;
import jacamo.web.implementation.WebImplJCM;
import jacamo.web.implementation.WebImplOrg;

@ApplicationPath("/")
public class WebAppConfig extends ResourceConfig {
    public WebAppConfig() {
        // Registering resource classes
        registerClasses(WebImpl.class, WebImplAg.class, WebImplEnv.class, WebImplOrg.class, WebImplJCM.class, RestImplDF.class);
        
        // gzip compression
        registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);
        
        addProperties(new HashMap<String,Object>() {
            private static final long serialVersionUID = 1L;

        { put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }} );
    }
}
