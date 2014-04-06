/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
	@FXML private ComboBox zoomOptionsBox;
	
	private final ObservableList<ManagedImage> imageList = FXCollections.observableArrayList();
	
	
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
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("image files", 
				"*.png","*.jpg","*.jpeg","*.gif","*.PNG","*.JPG","*.JPEG","*.GIF"));//,"svg","SVG")); // SVG support comming soon
		// TODO: SVG support
		fileChooser.setInitialDirectory(new File(config().getProperty("lastImageDir", ".")));
		
		listView.setItems(imageList);
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// TODO: listView.setCellFactory(...);
		
		ChangeListener resizeListener = new ChangeListener(){
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				viewResize();
			}
		};
		imgParent.heightProperty().addListener(resizeListener); // method overloading makes using a lambda here tricky
		imgParent.widthProperty().addListener(resizeListener);
		ScreenMaster sm = ScreenMaster.getInstance();
		sm.getDisplayImageViewContainer().heightProperty().addListener(resizeListener);
		sm.getDisplayImageViewContainer().widthProperty().addListener(resizeListener);
		// TODO: Save/load image sets
		loadLastSession();
	}	
	
	@FXML private void deleteSelectedItems(javafx.event.Event e){
		ArrayList targets = new ArrayList();
		targets.addAll(listView.getSelectionModel().getSelectedItems());
		for(Object o : targets){
			imageList.remove(o);
		}
	}
	
	@FXML private void listViewClick(MouseEvent e){
		if(e.getSource() == listView && e.getClickCount() == 2){
			ManagedImage target = (ManagedImage)listView.getSelectionModel().getSelectedItem();
			setImage(target);
		}
	}
	@FXML private void zoomModeChange(javafx.event.Event e){
		ZoomMode zm = (ZoomMode)zoomOptionsBox.getValue();
		changeZoomMode(zm);
	}
	private Point2D dragStart = null;
	private Point2D currentCenter = new Point2D(0,0);
	@FXML private void moveView(MouseEvent e){
		if(dragStart != null){
			double dx = e.getScreenX()-dragStart.getX();
			double dy = e.getScreenY()-dragStart.getY();
			currentCenter = currentCenter.add(-dx, -dy);
			dragStart = dragStart.add(dx,dy);
			centerViewOn(currentCenter);
		}
	}
	@FXML private void moveStart(MouseEvent e){
		dragStart = new Point2D(e.getScreenX(), e.getScreenY());
	}
	
	@FXML private void moveEnd(MouseEvent e){
		dragStart = null;
	}
	@FXML private void recenter(Event e){
		currentCenter = new Point2D(0,0);
		centerViewOn(currentCenter);
	}
	
	@FXML private void viewResize(){
		changeZoomMode((ZoomMode)zoomOptionsBox.getValue());
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
				ManagedImage mi = new ManagedImage(f);
				imageList.add(mi);
			} catch (IOException ex) {
				Logger.getLogger(MasterWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		saveFileList();
	}

	private void saveFileList() {
		StringBuilder sb = new StringBuilder();
		for(ManagedImage m : imageList){
			sb.append(m.getSrcFile().toPath()).append(File.pathSeparator);
		}
		config().setProperty("lastSession", sb.toString());
	}
	
	private void loadLastSession(){
		if(config().containsKey("lastSession")){
			String[] filePaths = config().getProperty("lastSession").split(File.pathSeparator);
			for(String path : filePaths){
				File f = new File(path);
				if(f.exists()){
					addImage(f);
				}
			}
		}
	}

	private ManagedImage currentImage = null;
	private void setImage(ManagedImage image) {
		currentImage = image;
		image.setImage(imgView, imgParent, (ZoomMode)zoomOptionsBox.getValue());
		// set other window's image too
		image.setImage(
				ScreenMaster.getInstance().getDisplayImageView(), 
				ScreenMaster.getInstance().getDisplayImageViewContainer(), 
				(ZoomMode)zoomOptionsBox.getValue());
	}
	
	private void changeZoomMode(ZoomMode zm){
		if(currentImage == null) return;
		currentImage.setImage(imgView, imgParent, zm);
		currentImage.setImage(
				ScreenMaster.getInstance().getDisplayImageView(), 
				ScreenMaster.getInstance().getDisplayImageViewContainer(), 
				zm);
		currentCenter = new Point2D(0,0);
		centerViewOn(currentCenter);
	}

	private void centerViewOn(Point2D deltaCenter) {
		if(currentImage == null) return;
		currentImage.centerViewOn(deltaCenter.getX(), deltaCenter.getY(), imgView, imgParent,(ZoomMode)zoomOptionsBox.getValue());
		currentImage.centerViewOn(deltaCenter.getX(), deltaCenter.getY(), ScreenMaster.getInstance().getDisplayImageView(), ScreenMaster.getInstance().getDisplayImageViewContainer(),(ZoomMode)zoomOptionsBox.getValue());
	}
}
