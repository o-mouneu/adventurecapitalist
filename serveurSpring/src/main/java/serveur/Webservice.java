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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import generated.PallierType;
import generated.ProductType;

@RestController
@Path("generic")
public class Webservice {

	
	Services services;
	
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
	
	// Achat d'un produit ou lancement manuel de sa production 
	@PUT
	@Path("product")
	@Produces(MediaType.APPLICATION_XML)
	public Response putProduct(@Context HttpServletRequest request, ProductType p) {
		String username = request.getHeader("X-user");
		//services.buyProduct(p);
		return null;
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
		return null;
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
		return null;

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
		return null;
	}
	
	/*
	 * Remettre à zéro le monde
	 */
	@DELETE
	@Path("world")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteWorld(@Context HttpServletRequest request) {
		String username = request.getHeader("X-user");
		return null;
	}
	
	
	
	
}
