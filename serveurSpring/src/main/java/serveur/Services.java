package serveur;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import generated.PallierType;
import generated.PalliersType;
import generated.ProductType;
import generated.World;


public class Services {
	
	String worldPath = "./data/worlds/";

	World readWorldFromXml(String username) {
		
		File fichierJoueur = new File(worldPath + username + "-world.xml");
		
		if( !fichierJoueur.exists() ) {
			fichierJoueur = new File( worldPath + "world.xml");
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
			File fichierJoueur = new File( worldPath + username + "-world.xml");
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
		calculateScore(world);
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
			double coutBase = product.getCout(); double croissance = product.getCroissance();
			double coutTotal = (coutBase*Math.pow(croissance,croissance)) / (1 - croissance);
			if( world.getMoney() < coutTotal ) {
				return false;
			} else {
				world.setMoney(world.getMoney() - coutTotal);
				product.setQuantite(qtchange);
				product.setTimeleft(System.currentTimeMillis());
				return true;
			}
		} else {
			if (product.getTimeleft() == 0) {
				product.setTimeleft( product.getVitesse() );
			}
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
		
		// Unlock manager
		manager.setUnlocked(true);
		
		ProductType product = findProductById(world, manager.getIdcible());
		if(product == null) {
			return false;
		}
		
		// Unlock product manager
		List<PallierType> palliers = product.getPalliers().getPallier();
		
		for(int p=0; p<palliers.size(); p++) {
			if( palliers.get(p).getName() == newManager.getName() ){
				palliers.get(p).setUnlocked(true);
			}
		}
		
		// Enlever cout
		world.setMoney( world.getMoney()-manager.getSeuil() ); 
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
	
	public boolean buyProduct(String username, ProductType product) {
		World world = getWorld(username);
		ProductType worldProduct = findProductById(world, product.getId());
		
		if( worldProduct != null ) {
			wo
		}
	}
			
			
			
	public int calculateScore(World world) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		double nouveauxBenefices = 0;
		for(int p=0; p<productList.size(); p++) {
			int nouveauxProduits = updateProduct(world, productList.get(p));
			nouveauxBenefices += nouveauxProduits * productList.get(p).getRevenu() * (1 + world.getActiveangels() * world.getAngelbonus()/100);
		}
		
		world.setMoney(world.getMoney() + nouveauxBenefices);
		// Calcul bonus anges
		// Calcul bonus profits
		return 0;
		
	}
	
	public int updateProduct(World world, ProductType produit) {
		
		long timeleft = produit.getTimeleft();
		long dernierMaj = world.getLastupdate();
		int vitesse = produit.getVitesse();
		
		long tempsActuel = System.currentTimeMillis();
		long duree = tempsActuel - dernierMaj;
		
		//System.out.println( productList.get(p).getName() + "  debut : " + tempsDebut + " - duree " + duree + " - vitesse " + vitesse);
		
		int nbNouveauxProduits = 0;
		
		// SI Produit a un Manager
		if( produit.isManagerUnlocked() ) {
			if( duree-timeleft > 0 ) {
				long nouvelleDuree = (duree-timeleft);
				nbNouveauxProduits = (int) (nouvelleDuree / vitesse) + 1;
				produit.setTimeleft( nouvelleDuree - vitesse*nbNouveauxProduits-1 );
			} else {
				// Le produit n'a pas fini d'être créé
				produit.setTimeleft(timeleft-duree);
			}
			
		} else {
			
			// SI le produit est en cours de production
			if( timeleft > 0 ) {
				// calculTimeleft => verifier si un produit créé pendant temps écoulé
				long calculTimeleft = duree-timeleft;
				// Le produit a été créé durant le temps écoulé
				if( calculTimeleft >= 0 ) {
					nbNouveauxProduits = 1;
				} else {
					produit.setTimeleft(timeleft-duree);
				}	
			}
		}
		
		produit.setQuantite( produit.getQuantite() + nbNouveauxProduits );
		return nbNouveauxProduits;
	}
	
}
