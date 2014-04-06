/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 *
 * @author cybergnome
 */
public class ManagedImage {
	private final Image img;
	private final String name;
	private final File src;
	
	public ManagedImage(File f) throws FileNotFoundException{
		InputStream imgStream = new FileInputStream(f);
		String name = f.getName();
		img = new javafx.scene.image.Image(imgStream);
		this.name = name;
		src = f;
	}
	
	public File getSrcFile(){
		return src;
	}
	
	public void setImage(ImageView imgView, Region container, ZoomMode zoomMode){
		double imgAspectRatio = (img.getWidth())/(img.getHeight());
		double parentAspectRatio = (container.getWidth())/(container.getHeight());
		imgView.setPreserveRatio(true);
		imgView.setImage(img);
		Rectangle2D defaultViewportArea;
		switch(zoomMode){
			case NOZOOM: // do not scale
				centerViewOn(0,0,imgView,container,zoomMode);
				imgView.setSmooth(false);
				imgView.setFitHeight(img.getHeight()); // 0 means ignore fitting/ otherwise this is the dimension measurement
				imgView.setFitWidth(img.getWidth());
				break;
			case FIT: // scale but don't crop
				imgView.setSmooth(true);
				double x = 0,y = 0;
				if(imgAspectRatio > parentAspectRatio){
					// image is wider than container
					y = (img.getHeight()/container.getHeight())*(container.getHeight() - container.getWidth() / imgAspectRatio)/2;
				} else {
					// image is taller than container
					x = (img.getWidth()/container.getWidth())*(container.getWidth() - container.getHeight() * imgAspectRatio)/2;
				}
				defaultViewportArea = new Rectangle2D(-x, -y,img.getWidth()+x,img.getHeight()+y);
				imgView.setViewport(defaultViewportArea);
				if(imgAspectRatio > parentAspectRatio){
					// image is wider than container
					imgView.setFitHeight(0); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(container.getWidth());
				} else {
					// image is taller than container
					imgView.setFitHeight(container.getHeight()); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(0);
				}
				break;
			case FILL: // scale and crop
				imgView.setSmooth(true);
				if(imgAspectRatio > parentAspectRatio){
					// image is wider than container
					double finalImgWidth = imgAspectRatio * container.getHeight();
					double finalImgHeight = container.getHeight();
					double dx = (img.getWidth()/container.getWidth())*(finalImgWidth - container.getWidth())/2;
					double dy = 0;
					defaultViewportArea = new Rectangle2D(dx, dy,img.getWidth(),img.getHeight());
					imgView.setViewport(defaultViewportArea);
					imgView.setFitHeight(finalImgHeight); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(finalImgWidth);
				} else {
					// image is taller than container
					double finalImgWidth = container.getWidth();
					double finalImgHeight = container.getWidth() / imgAspectRatio ;
					double dx = 0;
					double dy = (img.getHeight()/container.getHeight())*(finalImgHeight - container.getHeight())/2;
					defaultViewportArea = new Rectangle2D(dx, dy,img.getWidth(),img.getHeight());
					imgView.setViewport(defaultViewportArea);
					imgView.setFitHeight(finalImgHeight); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(finalImgWidth);
				}
				break;
		}
	}
	/**
	 * 
	 * @param dx Coordinate of center of view, where (0,0) is the center of the 
	 * image.
	 * @param dy Coordinate of center of view, where (0,0) is the center of the 
	 * image.
	 * @param imgView
	 * @param container 
	 * @param zoomMode 
	 */
	public void centerViewOn(double dx, double dy, ImageView imgView, Region container, ZoomMode zoomMode) {
		switch(zoomMode){
			case NOZOOM:
				double xOffset = (img.getWidth() - container.getWidth()) * 0.5;
				double yOffset = (img.getHeight() - container.getHeight()) * 0.5;
				Rectangle2D viewPortArea = new Rectangle2D(xOffset + dx, yOffset+dy,img.getWidth(),img.getHeight());
				imgView.setViewport(viewPortArea);
				break;
		}
	}
	
	@Override public String toString(){
		return name;
	}
}
