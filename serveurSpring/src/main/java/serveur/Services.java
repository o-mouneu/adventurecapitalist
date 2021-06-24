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
		if( !doesPlayerExists(username) ) {
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
			
			JAXBContext cont = JAXBContext.newInstance(World.class);
			
			Marshaller m = cont.createMarshaller();
			m.marshal(world, fichierJoueur);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	boolean deleteFile( String username ) {
		File fichierJoueur = new File(worldPath + username + "-world.xml");
		
		if( !fichierJoueur.delete() ) {
			return false;
		}
		return true;
	}
	
	
	boolean doesPlayerExists( String username ) {
		File fichierJoueur = new File(worldPath + username + "-world.xml");
		
		if( !fichierJoueur.exists() ) {
			return false;
		}
		return true;
	}
	
	
	
	
	World getWorld(String username) {
		System.out.println("Joueur " + username );
		World world = readWorldFromXml(username);
		
		// mettre à jour score
		updateScore(world);
		world.setLastupdate(System.currentTimeMillis());

		
		saveWorldToXml(username, world);
		return world;
	}
	
	/*
	 * Achat d'un produit
	 * 
	 * Verifie si le produit peut être acheté
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateProduct(String  username, ProductType newProduct) {
		World world = getWorld(username);
		
		ProductType product = findProductById(world, newProduct.getId());
		
		if( product == null ) {
			System.out.println("Produit introuvable");
			return false;
		}
		int nouvelleQuantite = newProduct.getQuantite();
		int qtchange = nouvelleQuantite - product.getQuantite();
		
		System.out.println("\tProduit " + product.getName() + " - qtChange: " + qtchange + " (timeleft: " + product.getTimeleft() + ")");
		
		if(qtchange > 0) {
			

			double croissance = product.getCroissance(); 
			double coutProduitBase = product.getCout();

			//  S = u0 * ( (1-q^n+1) / (1-q))       qtChange = N+1  car u0 compte 
			double coutTotal = coutProduitBase * (( 1 - Math.pow(croissance, qtchange)) / (1 - croissance));
			
			System.out.println("\tCOUT :  u0 " + coutProduitBase + " un (n=" + qtchange + ") => TOTAL : " + coutTotal );
			
			if( world.getMoney() >= coutTotal ) {
				
			// achat du produit
				world.setMoney(world.getMoney() - coutTotal);
				product.setQuantite(nouvelleQuantite);
				// Un = U0 * q^n
				product.setCout(coutProduitBase*Math.pow(croissance, qtchange));

				System.out.println("\t" + qtchange + " produits achetés");
			} else {
				System.out.println("\tPas assez d'argent");
				return false;
			}
			
			
		} else {
			if ( product.getQuantite() > 0 ) {
				if (product.getTimeleft() == 0) {
					product.setTimeleft( product.getVitesse() );
					System.out.println("\tDébut de production");
				}
			}
		}

		saveWorldToXml(username, world);
		return true;
	}
	
	/*
	 * Achat d'un manager
	 * 
	 * Verifie si le manager peut être acheté
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateManager(String username, PallierType newManager) {
		

		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		PallierType manager = findManagerByName(world, newManager.getName());
		if(manager == null || manager.isUnlocked()) {
			System.out.println("Manager introuvable ou déjà acheté");
			return false;
		}
		ProductType product = findProductById(world, manager.getIdcible());
		if(product == null) {
			return false;
		}
		
		// Verifier si joueur assez d'argent
		if( world.getMoney() < manager.getSeuil() ) {
			System.out.println("Manager - pas assez d'argent");
			return false;
		}
		
		// Unlock manager
		manager.setUnlocked(true);
				
		// Marquer produit comme managé
		product.setManagerUnlocked(true);
		
		// Retirer cout à l'argent
		world.setMoney( world.getMoney()-manager.getSeuil() );
		applyBonus(world, manager);
		
		saveWorldToXml(username, world);
		return true;
	}
	
	/*
	 * Achat d'un cash upgrade
	 * 
	 * Verifie si l'upgrade peut etre achetée
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateCashupgrade(String username, PallierType cashupgrade) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allUpgrades = world.getUpgrades().getPallier();
		System.out.println("\tSearch : " + cashupgrade.getName());
		
		// Cherche upgrade dans liste upgrades
		for(int p=0; p<allUpgrades.size(); p++) {
			if( cashupgrade.getName().equals( allUpgrades.get(p).getName() ) ) {
				// Vérifier si assez d'argent
				if( allUpgrades.get(p).getSeuil() <= world.getMoney() ) {
					world.setMoney( world.getMoney()-allUpgrades.get(p).getSeuil() );
					allUpgrades.get(p).setUnlocked(true);
					System.out.println("Upgrade " + allUpgrades.get(p).getName() + " achetée ! (" + allUpgrades.get(p).getTyperatio()  + " " + allUpgrades.get(p).getRatio() + ")");

					applyBonus(world, cashupgrade);
				} else {
					System.out.println("\tPas assez d'argent");
					return false;
				}
			}
		}
		saveWorldToXml(username, world);
		
		return true;
	}
	
	/*
	 * Achat d'un angel upgrade
	 * 
	 * Verifie si l'upgrade peut etre achetée
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateAngelupgrade(String username, PallierType angelupgrade) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allAngelupgrades = world.getAngelupgrades().getPallier();
		
		for(int p=0; p<allAngelupgrades.size(); p++) {
			if( angelupgrade.getName().equals( allAngelupgrades.get(p).getName() ) ) {
				if( allAngelupgrades.get(p).getSeuil() <= world.getActiveangels() ) {
					world.setActiveangels( world.getActiveangels()-allAngelupgrades.get(p).getSeuil() );
					allAngelupgrades.get(p).setUnlocked(true);
					
					applyBonus(world, angelupgrade);
				} else {
					return false;
				}
			}
		}
		saveWorldToXml(username, world);
		
		return true;
	}
	
	
	/*
	 * Déblocage d'un unlock global
	 * 
	 * Verifie si l'unlock peut etre débloqué
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateGlobalunlock(String username, PallierType unlock) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allUnlocks = world.getAllunlocks().getPallier();
		List<ProductType> allProducts = world.getProducts().getProduct();
		
		for(int p=0; p<allUnlocks.size(); p++) {
			
			// Trouver l'unlock
			if( unlock.getName().equals( allUnlocks.get(p).getName() ) ) {
				double seuilUnlock = allUnlocks.get(p).getSeuil();
				boolean isSeuilOk = true;
				// Verifier si tous les produits ont atteint le seuil
				for(int pro=0; pro < allProducts.size(); pro++) {
					isSeuilOk = (allProducts.get(pro).getQuantite()>=seuilUnlock)? true: false;
				}
				// Appliquer le bonus
				if( isSeuilOk ) {
					allUnlocks.get(p).setUnlocked(true);
					applyBonus(world, unlock);
					return true;
				}
				return false;
			}
		}
		
		saveWorldToXml(username, world);
		return false;
	}
	
	/*
	 * Déblocage d'un unlock propre à un produit
	 * 
	 * Verifie si l'unlock peut etre débloqué
	 * Applique le bonus au produit
	 * 
	 * @return {Boolean} succès
	 */
	public Boolean updateProductunlock(String username, PallierType unlock) {
		
		World world = getWorld(username);
		
		// Trouver le produit associé
		ProductType worldProduct = findProductById(world, unlock.getIdcible());
		
		if(worldProduct == null)
			return false;
		
		List<PallierType> productUnlocks = worldProduct.getPalliers().getPallier();
		
		// Chercher l'unlock
		for(int p=0; p<productUnlocks.size(); p++) {
			if( unlock.getName().equals(productUnlocks.get(p).getName()) ) {
				
				// Verifier si quantite de produit dépasse seuil
				if( productUnlocks.get(p).getSeuil() >= unlock.getSeuil() ) {
					productUnlocks.get(p).setUnlocked(true);
					// Appliquer le bonus
					applyBonus(world, productUnlocks.get(p));
					return true;
				}
			}
		}
		
		saveWorldToXml(username, world);
		return false;
	}
	
	/*
	 * Applique bonus des upgrades, unlocks et managers	
	 */
	public void applyBonus(World world, PallierType pallier) {
		
		int bonusVitesse = 1;
		int bonusGain = 1;
		int bonusAnge = 0;
		
		int idCible = pallier.getIdcible();
		
		if( pallier.getTyperatio() == TyperatioType.ANGE ) {
			bonusAnge = (int) pallier.getRatio();
			world.setAngelbonus(bonusAnge);
		} else {
			if( pallier.getTyperatio() == TyperatioType.VITESSE ) {
				bonusVitesse = (int) pallier.getRatio();
			}
			if( pallier.getTyperatio() == TyperatioType.GAIN ) {
				bonusGain = (int) pallier.getRatio();
			}
			
			
			List<ProductType> worldProducts = world.getProducts().getProduct();

			if(idCible==0) {
				for(int p=0; p<worldProducts.size(); p++) {
					worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()/bonusVitesse );
					worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
					System.out.println("\tP: " + worldProducts.get(p).getName() + " " + pallier.getRatio() + " " +pallier.getTyperatio());
				}
			} else {
				for(int p=0; p<worldProducts.size(); p++) {
					if( worldProducts.get(p).getId() == idCible) {
						worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()/bonusVitesse );
						worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
						System.out.println("\tP: " + worldProducts.get(p).getName() + " " + pallier.getRatio() + " " +pallier.getTyperatio());
					}
				}
			}
			
			
		}
		
		
		
		
		
		
		
		
	}
	
/*
 * FINDERS
 */
	
	public ProductType findProductById(World world, int id) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		for(int p=0; p<productList.size(); p++) {
			if( productList.get(p).getId() == id) {
				return productList.get(p);
			}
		}
		return null;
				
	}
	
	public PallierType findManagerByName(World world, String name) {
		List<PallierType> managerList = world.getManagers().getPallier();
		
		for(int m=0; m<managerList.size(); m++) {
			if( name.equals(managerList.get(m).getName()) ) {
				return managerList.get(m);
			}
		}
		return null;
				
	}
	
	
			
	/*
	 * Calcul argent gagné pendant la durée écoulée
	 */
	public void updateScore(World world) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		double nouveauxBenefices = 0;
		for(int p=0; p<productList.size(); p++) {
			
			PallierType currentManager = productList.get(p).getActiveManager();
					
			int nbCyclesProduction = updateProductTimeleft(world, productList.get(p));
			double bonusAnges =  world.getActiveangels() * world.getAngelbonus()/100;
			
			double benefProduit = nbCyclesProduction * productList.get(p).getRevenu() * productList.get(p).getQuantite() * (1+bonusAnges);
			nouveauxBenefices += benefProduit;
			
			System.out.println( "\tP  " + productList.get(p).getName() + " " + productList.get(p).getQuantite() + " (cycles: " + nbCyclesProduction + " = " + benefProduit + "$ )\tTL= " + productList.get(p).getTimeleft());

		}
		System.out.println("Argent = " + world.getMoney() + " + " + nouveauxBenefices + "$");
		world.setMoney(world.getMoney() + nouveauxBenefices);
		
			
	}
	
	/*
	 * Compte le nombre de cycles de production pendant la durée écoulée (LastUpdate - Maintenant)
	 * Calcul le nouveau timeleft
	 * 
	 * @return {nbCyclesDeProduction} int
	 */
	public int updateProductTimeleft(World world, ProductType worldProduct) {
		
		long timeleft = worldProduct.getTimeleft();
		long lastUpdate = world.getLastupdate();
		long timeCurrent = System.currentTimeMillis();

		int vitesse = worldProduct.getVitesse();
		long duree = timeCurrent - lastUpdate;
		
		int nbCycleProduction=0;
		
		
		// SI Produit a un Manager
		if( worldProduct.isManagerUnlocked() ) {
			System.out.println("\tMANAGED");
			System.out.println("\t\tDuree " + duree + "  -  TL = " + timeleft + "  -  vitesse : " + vitesse );
			if( duree-vitesse >= 0 & duree > 0) {
				long dureeSansTimeleft = (duree-timeleft);
				nbCycleProduction = (int) Math.floor(dureeSansTimeleft / (vitesse)) + 1;
				// Nouveau  Timeleft = dureeSansTimeleft - tempsPassePourLesXCycles
				worldProduct.setTimeleft( (long) (dureeSansTimeleft - (vitesse)*(nbCycleProduction-1)) );

			} else {
				// Le produit n'a pas fini d'être créé
				worldProduct.setTimeleft(timeleft-duree);
			}
			
		} else {
			
			// SI le produit est en cours de production
			if( timeleft > 0 ) {
								
				// Le produit a été créé durant le temps écoulé
				if( duree >= timeleft ) {
					nbCycleProduction = 1;
					worldProduct.setTimeleft(0);
					System.out.println("\t Production terminée");
				} else {
					worldProduct.setTimeleft(timeleft-duree);
					System.out.println("\t Recalcul timeleft");
				}	
			}
		}
		
		return nbCycleProduction;
	}
	
	
	/*
	 * Reset du monde
	 * Calcul du nombre de nouveaux anges actifs
	 * 
	 */
	public boolean resetWorld(String username) {
		World world = getWorld(username);
		
		if( !doesPlayerExists(username) ) {
			return false;
		}
		
		double activeAngels = world.getActiveangels();
		double totalAngels = world.getTotalangels();
		double newAngels = (150 * Math.sqrt(world.getMoney()/(Math.pow(10, 15)) - totalAngels) );
		
		deleteFile(username);
		
		World newWorld = getWorld(username);
		world.setActiveangels( activeAngels + newAngels );
		world.setTotalangels( totalAngels + newAngels );
		System.out.println("[RESET]  " + username + "\tTT Angels " + newWorld.getTotalangels() + "  -  ACTIVE Angels " + newWorld.getActiveangels());
		
		return true;
	}
	
	
	public void setMoney(String username, int qte) {
		World world = getWorld(username);
		world.setMoney(qte);
		saveWorldToXml(username, world);
	}
	
}
