package jacamo.web;

import javax.inject.Singleton;
import javax.ws.rs.Path;

import jacamo.rest.RestImplOrg;

@Singleton
@Path("/oe")
public class WebImplOrg extends RestImplOrg {

    @Override
    protected void configure() {
        bind(new WebImplOrg()).to(WebImplOrg.class);
    }

}
