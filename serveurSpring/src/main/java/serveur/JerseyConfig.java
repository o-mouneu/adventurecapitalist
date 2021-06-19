package serveur;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component 
@ApplicationPath("/adventureisis") 
public class JerseyConfig extends ResourceConfig { 
	public JerseyConfig() { 
		System.out.println("Jersey config");
		register(Webservice.class);
		register(CORSResponseFilter.class);
	}

}

