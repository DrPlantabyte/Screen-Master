/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster;

/**
 *
 * @author cybergnome
 */
public enum ZoomMode {
	NOZOOM("1:1 (no zoom)"),
	FIT("zoom to fit"),
	FILL("zoom to fill");
	public final String displayName;
	ZoomMode(String label){
		displayName = label;
	}
	@Override public String toString(){
		return displayName;
	}
	public static ZoomMode lookUpByDisplayName(String displayName){
		for(ZoomMode m : ZoomMode.values()){
			if(m.displayName.equals(displayName)){
				return m;
			}
		}
		return null;
	}
}
