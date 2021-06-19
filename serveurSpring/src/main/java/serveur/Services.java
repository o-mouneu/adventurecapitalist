package serveur;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import generated.PallierType;
import generated.PalliersType;
import generated.ProductType;
import generated.TyperatioType;
import generated.World;


public class Services {
	
	String worldPath = "./data/worlds/";

	World readWorldFromXml(String username) {
		
		File fichierJoueur = new File(worldPath + username + "-world.xml");
		boolean playerExists = true;
		
		if( !fichierJoueur.exists() ) {
			fichierJoueur = new File( worldPath + "world.xml");
			playerExists = false;
		}
		
		JAXBContext cont;
		try {
			cont = JAXBContext.newInstance(World.class);
			Unmarshaller u = cont.createUnmarshaller();
			World world = (World) u.unmarshal(fichierJoueur);
			
			// definir le temps de debut du monde
			if( !playerExists ) {
				world.setLastupdate( System.currentTimeMillis() );
			}
			
			return world;

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
		updateScore(world);
		world.setLastupdate(System.currentTimeMillis());

		
		saveWorldToXml(username, world);
		return world;
	}
	

	public Boolean updateProduct(String  username, ProductType newProduct) {
		World world = getWorld(username);
		
		ProductType product = findProductById(world, newProduct.getId());
		
		if( product == null ) {
			return false;
		}
		
		int qtchange = newProduct.getQuantite() - product.getQuantite();
		
		System.out.println("updateProduct " + product.getName() + " - qtChange: " + qtchange);
		
		if(qtchange > 0) {
			
			double coutBase = product.getCout(); double croissance = product.getCroissance();
			double coutTotal = (coutBase*Math.pow(croissance,croissance)) / (1 - croissance);
			if( world.getMoney() < coutTotal ) {
				System.out.println("\tPas assez d'argent");
				return false;
			} else {
				world.setMoney(world.getMoney() - coutTotal);
				product.setQuantite(qtchange);
				product.setTimeleft(0);
				System.out.println("\t" + qtchange + " produits achetés");
			}
		} else {
			if (product.getTimeleft() == 0) {
				product.setTimeleft( product.getVitesse() );
				System.out.println("\tDébut de production");
			}
		}

		saveWorldToXml(username, world);
		return true;
	}
	
	public boolean updateProductById(String  username, int idProduct, int quantiteAchetee) {
		World world = getWorld(username);
		
		ProductType product = findProductById(world, idProduct);
		
		if( product == null ) {
			System.out.println("Produit introuvable");
			return false;
		}
		
		int qtchange = quantiteAchetee - product.getQuantite();
		
		System.out.println("updateProduct " + product.getName() + " - qtChange: " + qtchange + "(timeleft: " + product.getTimeleft() + ")");
		
		if(qtchange > 0) {
			
			double coutBase = product.getCout(); double croissance = product.getCroissance(); 
			int productQuantity = product.getQuantite();
			double coutProduitN = coutBase*Math.pow(croissance, productQuantity);

			double coutTotal = coutBase * (( 1 - Math.pow(croissance, quantiteAchetee)) / (1 - croissance));
			coutTotal -= coutBase * (( 1 - Math.pow(croissance, productQuantity)) / (1 - croissance));
			
			System.out.println("COUT :  u0 " + coutBase + " un+1(" + productQuantity + ") " + coutProduitN + " TOTAL : " + coutTotal );
			
			if( world.getMoney() < coutTotal ) {
				System.out.println("\tPas assez d'argent");
				return false;
			} else {
			// achat du produit
				world.setMoney(world.getMoney() - coutTotal);
				product.setQuantite(quantiteAchetee);
				product.setTimeleft(0);
				System.out.println("\t" + qtchange + " produits achetés");
			}
			
			
		} else {
			if (product.getTimeleft() == 0) {
				product.setTimeleft( product.getVitesse() );
				System.out.println("\tDébut de production");
			}
		}

		saveWorldToXml(username, world);
		return true;
	}
	
	public Boolean updateManager(String username, PallierType newManager) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		PallierType manager = findManagerByName(world, newManager.getName());
		if(manager == null) {
			return false;
		}
		ProductType product = findProductById(world, manager.getIdcible());
		if(product == null) {
			return false;
		}
		
		// Verifier si joueur assez d'argent
		if( world.getMoney() < manager.getSeuil() ) {
			return false;
		}
		
		// Unlock manager
		manager.setUnlocked(true);
				
		// Unlock product manager
		List<PallierType> palliers = product.getPalliers().getPallier();
		
		for(int p=0; p<palliers.size(); p++) {
			if( palliers.get(p).getName() == newManager.getName() ){
				palliers.get(p).setUnlocked(true);
			}
		}
		// Marquer produit comme managé
		product.setManagerUnlocked(true);
		
		// Retirer cout à l'argent
		world.setMoney( world.getMoney()-manager.getSeuil() ); 
		saveWorldToXml(username, world);
		
		return true;
	}
	
	
/*
 * FINDERS
 */
	
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
	
	
			
			
	public int updateScore(World world) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		double nouveauxBenefices = 0;
		for(int p=0; p<productList.size(); p++) {
			PallierType currentManager = productList.get(p).getActiveManager();
			double avantageRevenu = 1;
			if( currentManager.getTyperatio() == TyperatioType.GAIN ) {
				avantageRevenu = currentManager.getRatio();
			}
			
			int nouveauxProduits = updateProductQuantity(world, productList.get(p));
			nouveauxBenefices += nouveauxProduits * (productList.get(p).getRevenu()*avantageRevenu) * (1 + world.getActiveangels() * world.getAngelbonus()/100);
			System.out.println( "\t P: " + productList.get(p).getName() + " " + productList.get(p).getQuantite() + " (+" + nouveauxProduits + ")");

		}
		System.out.println("calculateScore - money = " + world.getMoney() + " + " + nouveauxBenefices);
		world.setMoney(world.getMoney() + nouveauxBenefices);
		// Calcul bonus anges
		// Calcul bonus profits
		
		return 0;
		
	}
	
	public int updateProductQuantity(World world, ProductType worldProduct) {
		
		long timeleft = worldProduct.getTimeleft();
		long lastUpdate = world.getLastupdate();
		long timeCurrent = System.currentTimeMillis();

		int vitesse = worldProduct.getVitesse();
		long duree = timeCurrent - lastUpdate;
		
		int nbNouveauxProduits=0;
		
		
		// SI Produit a un Manager
		if( worldProduct.isManagerUnlocked() ) {
			System.out.println("\t\tManaged");
			List<PallierType> productManagers = worldProduct.getPalliers().getPallier();
			PallierType currentManager = worldProduct.getActiveManager();
			
			double avantageVitesse = 1;
			if( currentManager.getTyperatio() == TyperatioType.VITESSE ) {
				avantageVitesse = currentManager.getRatio();
			}
				
			if( duree-timeleft > 0 ) {
				long nouvelleDuree = (duree-timeleft);
				nbNouveauxProduits = (int) (nouvelleDuree / (vitesse/avantageVitesse)) + 1;
				worldProduct.setTimeleft( (long) (nouvelleDuree - (vitesse/avantageVitesse)*(nbNouveauxProduits-1)) );

			} else {
				// Le produit n'a pas fini d'être créé
				worldProduct.setTimeleft(timeleft-duree);
			}
			
		} else {
			
			// SI le produit est en cours de production
			if( timeleft > 0 ) {
				
				// calculTimeleft => verifier si un produit créé pendant temps écoulé
				long calculTimeleft = duree-timeleft;
				
				// Le produit a été créé durant le temps écoulé
				if( calculTimeleft >= 0 ) {
					nbNouveauxProduits = 1;
					worldProduct.setTimeleft(0);
					System.out.println("\t Production terminée");
				} else {
					worldProduct.setTimeleft(timeleft-duree);
					System.out.println("\t Recalcul timeleft");
				}	
			}
		}
		
		worldProduct.setQuantite( worldProduct.getQuantite() + nbNouveauxProduits);
		return nbNouveauxProduits;
	}
	
	public void setMoney(String username, int qte) {
		World world = getWorld(username);
		world.setMoney(qte);
	}
	
}
