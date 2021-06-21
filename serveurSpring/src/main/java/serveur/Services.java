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
		System.out.println("Joueur " + username + ")");
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
			System.out.println("Produit introuvable");
			return false;
		}
		int quantiteAchetee = newProduct.getQuantite();
		int qtchange = quantiteAchetee - product.getQuantite();
		
		System.out.println("\tProduit " + product.getName() + " - qtChange: " + qtchange + "(timeleft: " + product.getTimeleft() + ")");
		
		if(qtchange > 0) {
			
			double coutBase = product.getCout(); double croissance = product.getCroissance(); 
			int productQuantity = product.getQuantite();
			double coutProduitN = coutBase*Math.pow(croissance, productQuantity);

			double coutTotal = coutBase * (( 1 - Math.pow(croissance, quantiteAchetee)) / (1 - croissance));
			coutTotal -= coutBase * (( 1 - Math.pow(croissance, productQuantity)) / (1 - croissance));
			
			System.out.println("\tCOUT :  u0 " + coutBase + " un+1(" + productQuantity + ") " + coutProduitN + " TOTAL : " + coutTotal );
			
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
	

	public Boolean updateCashupgrade(String username, PallierType cashupgrade) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allUpgrades = world.getUpgrades().getPallier();
		
		for(int p=0; p<allUpgrades.size(); p++) {
			if( cashupgrade.getName().equals( allUpgrades.get(p).getName() ) ) {
				if( allUpgrades.get(p).getSeuil() < world.getMoney() ) {
					world.setMoney( world.getMoney()-allUpgrades.get(p).getSeuil() );
					allUpgrades.get(p).setUnlocked(true);
					applyBonus(world, cashupgrade);
				} else {
					return false;
				}
			}
		}
		saveWorldToXml(username, world);
		
		return true;
	}
	
	public Boolean updateAngelupgrade(String username, PallierType angelupgrade) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allAngelupgrades = world.getAngelupgrades().getPallier();
		
		for(int p=0; p<allAngelupgrades.size(); p++) {
			if( angelupgrade.getName().equals( allAngelupgrades.get(p).getName() ) ) {
				if( allAngelupgrades.get(p).getSeuil() < world.getActiveangels() ) {
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
	
	public Boolean updateGlobalunlock(String username, PallierType unlock) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		List<PallierType> allUnlocks = world.getAllunlocks().getPallier();
		List<ProductType> allProducts = world.getProducts().getProduct();
		
		for(int p=0; p<allUnlocks.size(); p++) {
			if( unlock.getName().equals( allUnlocks.get(p).getName() ) ) {
				double seuilUnlock = allUnlocks.get(p).getSeuil();
				boolean isSeuilOk = true;
				for(int pro=0; pro < allProducts.size(); pro++) {
					isSeuilOk = (allProducts.get(pro).getQuantite()>=seuilUnlock)? true: false;
				}
				if( isSeuilOk ) {
					applyBonus(world, unlock);
					return true;
				}
				return false;
			}
		}
		
		saveWorldToXml(username, world);
		return false;
	}
	
	public Boolean updateProductunlock(String username, ProductType product, PallierType unlock) {
		
		World world = getWorld(username);
		
		// Trouver le manager et le produit associé
		ProductType worldProduct = findProductById(world, product.getId());
		
		if(worldProduct == null)
			return false;
		
		List<PallierType> productUnlocks = worldProduct.getPalliers().getPallier();
		for(int p=0; p<productUnlocks.size(); p++) {
			if( unlock.getName().equals(productUnlocks.get(p).getName()) ) {
				if( productUnlocks.get(p).getSeuil() >= unlock.getSeuil() ) {
					unlock.setUnlocked(true);
					applyBonus(world, unlock);
					return true;
				}
			}
		}
		
		saveWorldToXml(username, world);
		return false;
	}
	
	
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
					worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()*bonusVitesse );
					worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
				}
			} else {
				for(int p=0; p<worldProducts.size(); p++) {
					if( worldProducts.get(p).getId() == idCible) {
						worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()*bonusVitesse );
						worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
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
	
	
			
			
	public int updateScore(World world) {
		
		List<ProductType> productList = world.getProducts().getProduct();
		
		double nouveauxBenefices = 0;
		for(int p=0; p<productList.size(); p++) {
			
			PallierType currentManager = productList.get(p).getActiveManager();
					
			int nbCyclesProduction = updateProductTimeleft(world, productList.get(p));
			double bonusAnges =  1 + ( world.getActiveangels() * world.getAngelbonus()/100 );
			nouveauxBenefices += nbCyclesProduction * productList.get(p).getRevenu() * bonusAnges;
			System.out.println( "\tP  " + productList.get(p).getName() + " " + productList.get(p).getQuantite() + " (cycles: " + nbCyclesProduction + " = " + nouveauxBenefices + ")");

		}
		System.out.println("Argent = " + world.getMoney() + " + " + nouveauxBenefices + "$");
		world.setMoney(world.getMoney() + nouveauxBenefices);
		// Calcul bonus anges
		// Calcul bonus profits
		
		return 0;
		
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
			System.out.println("\t\tManaged");
			
			if( duree-timeleft > 0 ) {
				long dureeSansTimeleft = (duree-timeleft);
				nbCycleProduction = (int) (dureeSansTimeleft / (vitesse)) + 1;
				// Nouveau  Timeleft = dureeSansTimeleft - tempsPassePourLesXCycles
				worldProduct.setTimeleft( (long) (dureeSansTimeleft - (vitesse)*(nbCycleProduction-1)) );

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
	
	
	public void setMoney(String username, int qte) {
		World world = getWorld(username);
		world.setMoney(qte);
		saveWorldToXml(username, world);
	}
	
}
