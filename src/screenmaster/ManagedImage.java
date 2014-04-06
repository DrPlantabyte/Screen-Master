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
				defaultViewportArea = new Rectangle2D(0, 0,img.getWidth(),img.getHeight());
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
					double w = img.getHeight() * (parentAspectRatio);
					double h = img.getHeight();
					double m = 0.5*(img.getWidth() - w);
					defaultViewportArea = new Rectangle2D(m, 0,w,h);
					imgView.setViewport(defaultViewportArea);	
					imgView.setFitHeight(container.getHeight()); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(0);
				} else {
					// image is taller than container
					double w = img.getWidth();
					double h = img.getWidth() / parentAspectRatio;
					double m = 0.5*(img.getHeight() - h);
					defaultViewportArea = new Rectangle2D(0, m,w,h);
					imgView.setViewport(defaultViewportArea);	
					imgView.setFitHeight(0); // 0 means ignore fitting/ otherwise this is the dimension measurement
					imgView.setFitWidth(container.getWidth());
					
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
