package serveur;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import generated.PallierType;
import generated.ProductType;
import generated.World;

@RestController
@Path("generic")
public class Webservice {

	
	Services services;
	
	final int querySuccess = 200;
	final int queryImpossible = 202;
	
	
	public Webservice() {
		System.out.println("Web services");
		services = new Services();	
	}
	
	@GET
	@Path("world")
	@Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
	public Response getWorld(@Context HttpServletRequest request) {
		String username = request.getHeader("X-user");
		return Response.ok(services.getWorld(username)).build();
	}
	


	
	
	
	/*
	 * Achat d'un produit ou lancement manuel de sa production 
	 */
	@PUT
	@Path("product")
	@Produces(MediaType.APPLICATION_XML)
	public Response putProduct(@Context HttpServletRequest request, ProductType p) {
		String username = request.getHeader("X-user");
		
		if( p == null) {
			return Response.status(queryImpossible).build();
		}
		if(	!services.updateProduct(username, p) ) {
			return Response.status(queryImpossible).build();
		}
		
		return Response.ok().build();
		
	}
	
	
	
	/*
	 * Set money
	 */
	@PUT
	@Path("money")
	public Response setMoney(@Context HttpServletRequest request, @QueryParam("qte") int qte) {
		String username = request.getHeader("X-user");
		
	    services.setMoney(username, qte);
		
		return Response.ok().build();
	}
	
	
	
	
	/*
	 *  Achat du manager du produit
	 *  @PARAM : {pallierType}
	 */
	@PUT
	@Path("manager")
	@Produces(MediaType.APPLICATION_XML)
	public Response putManager(@Context HttpServletRequest request, PallierType p) {
		String username = request.getHeader("X-user");
		
		if(	!services.updateManager(username, p) ) {
			return Response.status(queryImpossible).entity("Achat manager refusé ou manager inexistant").build();
		}
		return Response.ok().build();
	}
	

	/*
	 * Achat d'un cash upgrade
	 * @PARAM : {pallierType}
	 */
	@PUT
	@Path("upgrade")
	@Produces(MediaType.APPLICATION_XML)
	public Response putUpgrade(@Context HttpServletRequest request, PallierType p) {
		String username = request.getHeader("X-user");

		if(	!services.updateCashupgrade(username, p) ) {
			return Response.status(queryImpossible).entity("Cash upgrade").build();
		}
		
		return Response.ok().build();
	}
	
	/*
	 * Achat d'un angel upgrade
	 * @PARAM : {pallierType}
	 */
	@PUT
	@Path("angelupgrade")
	@Produces(MediaType.APPLICATION_XML)
	public Response putAngelupgrade(@Context HttpServletRequest request, PallierType p) {
		String username = request.getHeader("X-user");

		if(	!services.updateAngelupgrade(username, p) ) {
			return Response.status(queryImpossible).entity("Angel upgrade").build();
		}
		
		return Response.ok().build();
	}
	
	/*
	 * Reset le monde
	 * Calcul du nombre d'anges
	 */
	@DELETE
	@Path("world")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteWorld(@Context HttpServletRequest request) {
		String username = request.getHeader("X-user");

		if(	!services.resetWorld(username) ) {
			return Response.status(queryImpossible).entity("Delete world " + username).build();
		}
		
		return Response.ok().build();
	}
	
	
	
}
