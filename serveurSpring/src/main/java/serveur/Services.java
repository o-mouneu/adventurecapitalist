package serveur;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import generated.PallierType;
import generated.ProductType;
import generated.World;


public class Services {

	World readWorldFromXml(String username) {
		//InputStream input = getClass().getClassLoader().getResourceAsStream(username + "-world.xml");
		
		File fichierJoueur = new File(username + "-world.xml");
		
		if( !fichierJoueur.exists() ) {
			fichierJoueur = new File("world.xml");
		}
		
		JAXBContext cont;
		try {
			cont = JAXBContext.newInstance(World.class);
			Unmarshaller u = cont.createUnmarshaller();
			World monde = (World) u.unmarshal(fichierJoueur);
			return monde;

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	void saveWorldToXml(String username, World world) {
		try {
			File fichierJoueur = new File(username + "-world.xml");
			/*OutputStream output = new FileOutputStream(fichierJoueur);
			output.write(65);    
			output.close(); */
			JAXBContext cont = JAXBContext.newInstance(World.class);
			
			Marshaller m = cont.createMarshaller();
			m.marshal(world, fichierJoueur);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	
	World getWorld(String username) {
		World world = readWorldFromXml(username);
		
		// mettre à jour score
		world
		world.setLastupdate(System.currentTimeMillis());
		
		saveWorldToXml(username, world);
		return world;
	}
	
	public Boolean updateProduct(String username, ProductType newProduct) {
		World world = getWorld(username);
		
		ProductType product = findProductById(world, newProduct.getId());
		
		if( product == null ) {
			return false;
		}
		
		int qtchange = newProduct.getQuantite() - product.getQuantite();
		
		if(qtchange > 0) {
			
		} else {
			
		}

		saveWorldToXml(username, world);
		return true;
	}
	
	public Boolean updateManager(String username, PallierType newManager) {
		
		World world = getWorld(username);
		
		PallierType manager = findManagerByName(world, newManager.getName());
		if(manager == null) {
			return false;
		}
		
		ProductType product = findProductById(world, manager.getIdcible());
		if(product == null) {
			return false;
		}
		
		
		
		saveWorldToXml(username, world);
		return true;
	}
	
	
	public ProductType findProductById(World world, int id) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		for(int p=0; p<productList.size(); p++) {
			if(productList.get(p).getId() == id) {
				return productList.get(p);
			}
		}
		return null;
				
	}
	
	public PallierType findManagerByName(World world, String name) {
		
		List<PallierType> managerList = world.getManagers().getPallier();
		
		for(int m=0; m<managerList.size(); m++) {
			if(managerList.get(m).getName() == name) {
				return managerList.get(m);
			}
		}
		return null;
				
	}
	
	public int calculateScore(World world) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		long tempsEcoule = world.getLastupdate();
		
		for(int p=0; p<productList.size(); p++) {
			
		}
		
		return 0;
		
	}
	
}
