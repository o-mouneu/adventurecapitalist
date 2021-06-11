package serveur;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component 
@ApplicationPath("/adventureisis") 

public class JerseyConfig extends ResourceConfig { 
	public JerseyConfig() { 
		register(Webservice.class);
		register(CORSResponseFilter.class);
	}
}