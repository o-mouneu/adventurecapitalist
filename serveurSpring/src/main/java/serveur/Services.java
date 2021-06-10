package serveur;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import generated.World;


public class Services {

	World readWorldFromXml() {
		InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
		return null;
	}
	
	void saveWorldToXml(World world) {
		
		//OutputStream output = new FileOutputStream(file);
	}
	
	
	World getWorld() {
		return readWorldFromXml();
	}
	
}
