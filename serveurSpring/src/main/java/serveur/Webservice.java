package serveur;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import generated.PallierType;
import generated.ProductType;

@Path("generic")
public class Webservice {

	
	Services services;
	
	public Webservice() {
		services = new Services();
	}
	
	@GET
	@Path("world")
	@Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
	public Response getWorld(@Context HttpServletRequest request) {
		String username = request.getHeader("X-user");
		return Response.ok(services.getWorld(username)).build();
	}
	
	
	
	@PUT
	@Path("product")
	@Produces(MediaType.APPLICATION_XML)
	public Response putProduct( ProductType p) {
		return null;
	}
	
	
	@PUT
	@Path("manager")
	@Produces(MediaType.APPLICATION_XML)
	public Response putManager( PallierType p) {
		return null;
	}
	
	
	@PUT
	@Path("upgrade")
	@Produces(MediaType.APPLICATION_XML)
	public Response putUpgrade() {
		return null;

	}
	
	@PUT
	@Path("angelupgrade")
	@Produces(MediaType.APPLICATION_XML)
	public Response putAngelupgrade() {
		return null;
	}
	
	
	@DELETE
	@Path("world")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteWorld() {
		return null;
	}
	
	
	
	
}
