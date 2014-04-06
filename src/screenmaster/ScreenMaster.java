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
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
	private ImageView imgView = null;
	private BorderPane imgParent = null;
	
	public Stage getMainStage(){
		return mainStage;
	}
	
	public ImageView getDisplayImageView(){
		return imgView;
	}
	public Region getDisplayImageViewContainer(){
		return imgParent;
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
		stage.setTitle("Control Window");
		Parent displayRoot = createDisplayWindow();
		Parent mainRoot = FXMLLoader.load(getClass().getResource("MasterWindow.fxml"));
		
		Scene mainScene = new Scene(mainRoot);
		Scene displayScene = new Scene(displayRoot);
		
		stage.setScene(mainScene);
		
		final Stage secondStage = new Stage();
		secondStage.setTitle("Display Window");
		secondStage.setScene(displayScene);
		secondStage.setX(0);
		secondStage.show();
		stage.show();
		stage.setOnCloseRequest((EventHandler)(Event e)-> Platform.exit());
		
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	private Parent createDisplayWindow() {
		AnchorPane p = new AnchorPane();
		p.setPrefSize(1024, 768);
		BorderPane imgParent = new BorderPane();
		p.getChildren().add(imgParent);
		AnchorPane.setBottomAnchor(imgParent, 0d);
		AnchorPane.setTopAnchor(imgParent, 0d);
		AnchorPane.setLeftAnchor(imgParent, 0d);
		AnchorPane.setRightAnchor(imgParent, 0d);
		ImageView imgView = new ImageView();
		imgView.setPickOnBounds(true);
		imgView.setPreserveRatio(true);
		imgParent.setCenter(imgView);
		this.imgView = imgView;
		this.imgParent = imgParent;
		return p;
	}
	
}
