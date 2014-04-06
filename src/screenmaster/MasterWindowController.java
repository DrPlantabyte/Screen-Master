/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

/**
 *
 * @author cybergnome
 */
public class MasterWindowController implements Initializable {
	
	
	@FXML private Region imgParent;
	@FXML private ImageView imgView;
	@FXML private ListView listView;
	@FXML private ChoiceBox zoomOptionsBox;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
		System.out.println(zoomOptionsBox.getValue().toString());
	}
	FileChooser fileChooser;
	@FXML private void openFiles(){
		List<File> chosenFiles = fileChooser.showOpenMultipleDialog(ScreenMaster.getInstance().getMainStage());
		if(chosenFiles == null || chosenFiles.size() <= 0) return;
		config().setProperty("lastImageDir", chosenFiles.get(chosenFiles.size()-1).getParent());
		for(File f : chosenFiles){
			addImage(f);
		}
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		zoomOptionsBox.getItems().addAll((Object[])ZoomMode.values());
		zoomOptionsBox.setValue(ZoomMode.values()[0]);
		
		fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("image files", "png","jpg","jpeg","gif"));//,"svg")); // SVG support comming soon
		// TODO: SVG support
		fileChooser.setInitialDirectory(new File(config().getProperty("lastImageDir", ".")));
		// TODO
	}	
	
	private Properties config(){
		return ScreenMaster.getInstance().getConfig();
	}

	private void addImage(File f) {
		String type = f.getName().substring(f.getName().lastIndexOf(".")).toLowerCase(Locale.US);
		if(type.equals("svg") == true){
			// TODO: SVG
		} else {
			try {
				// test
				ManagedImage i = new ManagedImage(new FileInputStream(f));
				i.setImage(imgView, imgParent, ZoomMode.lookUpByDisplayName(zoomOptionsBox.getValue().toString()));
				// TODO
			} catch (FileNotFoundException ex) {
				Logger.getLogger(MasterWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
