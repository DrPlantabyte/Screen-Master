/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import screenmaster.util.OSManager;

/**
 *
 * @author cybergnome
 */
public class ScreenMaster extends Application {
	
	private static ScreenMaster instance = null;
	
	public static ScreenMaster getInstance(){return  instance;}
	
	private Stage mainStage = null;
	
	public Stage getMainStage(){
		return mainStage;
	}
		
	private final Properties config = new Properties();
	private Path configFile = null;
	
	public OSManager getOSManager(){
		return OSManager.getInstance("ScreenMaster");
	}
	
	@Override public void init(){
		instance = this;
		configFile = Paths.get(getOSManager().getProgramDirectory().toString(),"config.properties");
		if(Files.exists(configFile) == false){
			try {
				Files.createDirectories(getOSManager().getProgramDirectory());
				Files.createFile(configFile);
			} catch (IOException ex) {
				getOSManager().getLogger(this).log(Level.WARNING, "Unable to create program directory", ex);
				configFile = null;
			}
		} else {
			try {
				config.load(new FileReader(configFile.toFile()));
			} catch (IOException ex) {
				getOSManager().getLogger(this).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public Properties getConfig(){
		return config;
	}
	
	@Override public void stop(){
		if(configFile != null){
			try {
				config.store(new FileWriter(configFile.toFile()),"Screen Master data");
			} catch (IOException ex) {
				getOSManager().getLogger(this).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		mainStage = stage;
		Parent root = FXMLLoader.load(getClass().getResource("MasterWindow.fxml"));
		
		Scene scene = new Scene(root);
		
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
}
