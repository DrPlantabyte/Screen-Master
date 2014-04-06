/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package screenmaster.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cybergnome
 */
public class OSManager {
	private OSManager(String name){
		// singleton initialization
		APP_NAME = name;
		try {
			SYSTEM_PROPERTIES = System.getProperties();
			readEnv();
		} catch (IOException | SecurityException ex) {
			Logger.getLogger(APP_NAME).log(Level.SEVERE, null, ex);
			SYSTEM_PROPERTIES = new Properties();
		}
		try {
			if(SYSTEM_PROPERTIES.contains("app.home")){
				// alternative (user-specified) app home
				PROGRAM_DIRECTORY = Paths.get(SYSTEM_PROPERTIES.getProperty("app.home"));
			}
		} catch (SecurityException ex) {
			Logger.getLogger(APP_NAME).log(Level.WARNING, "Security manager prevented access to System.getProperties()", ex);
		}
	}
	
	private final String APP_NAME;
	/** The properties returned by <code> System.getProperties();</code> */
	private Properties SYSTEM_PROPERTIES = new Properties();
	/** This is the location where the files will be unpacked. */
	private Path PROGRAM_DIRECTORY = (new File(".")).toPath();
	
	public Path getProgramDirectory(){
		return PROGRAM_DIRECTORY;
	}
	/** JarFile instance pointing to the .jar file */
	private JarFile JAR_FILE = null;
	
	private static OSType OS = null;
	
	public OSType getOS(){
		return OS;
	}
	/** Lock used to prevent spawned threads from getting ahead of the initializer */
	private static final Lock initLock = new ReentrantLock();
	
	private static final Map<String,OSManager> appInstances = new HashMap<>();
	
	public static OSManager getInstance(String appName){
		// Thread-safe singleton init
		if(appInstances.get(appName) == null){
			initLock.lock();
			try{
				if(appInstances.get(appName) == null){
					appInstances.put(appName, new OSManager(appName));
				}
			} finally{
				initLock.unlock();
			}
		}
		return appInstances.get(appName);
	}
	
	/**
	 * Reads the environment variables and sets parameters based on host system data.
	 * @throws IOException Thrown if there was a problem reading or writing a file
	 */
	private void readEnv() throws IOException{
		
		switch (SYSTEM_PROPERTIES.getProperty("os.name")) {
			case ("Windows Vista"):
			case ("Windows 7"):
			case ("Windows 8"):
			case ("Windows NT"):
			case ("Windows XP"):
			case ("Windows 95"):
			case ("Windows 98"):
			case ("Windows 2000"):
				OS = OSType.WINDOWS;
				break;
			case ("Mac OS X"):
				OS = OSType.MAC_OSX;
				break;
			case ("Linux"):
			case ("Unix"):
			case ("FreeBSD"):
			case ("Digital Unix"):
				OS = OSType.LINUX;
				break;
			case ("Solaris"):
				OS = OSType.SOLARIS;
				break;
			default:
				// unknown/unsupported OS
				Logger.getLogger(APP_NAME).log(Level.SEVERE,
						"OS type '" + SYSTEM_PROPERTIES.getProperty("os.name")
						+ "' is not supported. Cannot load native LWJGL libraries.");
				break;
		}
		
		Path jarDir = null;
		try {
				JAR_FILE = getJarFile();
				jarDir = getJarDirectory();
				if(JAR_FILE == null){
					throw new FileNotFoundException("Code source .jar file was not found");
				}
			} catch (FileNotFoundException ex) {
				throw new IOException("Unable to access source .jar file. Code may not be running from a .jar file", ex);
			}
		
		
			
					switch(SYSTEM_PROPERTIES.getProperty("os.name")){
						case ("Windows Vista"):
						case ("Windows 7"):
						case ("Windows 8"):
						case ("Windows NT"):
							PROGRAM_DIRECTORY = Paths.get(SYSTEM_PROPERTIES.getProperty("user.home"),"AppData","Roaming",APP_NAME);
							break;
						case ("Windows XP"):
						case ("Windows 95"):
						case ("Windows 98"):
						case ("Windows 2000"):
							PROGRAM_DIRECTORY = Paths.get(SYSTEM_PROPERTIES.getProperty("user.home"),"Application Data",APP_NAME);
							break;
						case ("Mac OS X"):
							PROGRAM_DIRECTORY = Paths.get(SYSTEM_PROPERTIES.getProperty("user.home"),"Library","Application Support",APP_NAME);
							break;
						case ("Linux"):
						case ("Unix"):
						case ("FreeBSD"):
						case ("Digital Unix"):
						case ("Solaris"):
							PROGRAM_DIRECTORY = Paths.get(SYSTEM_PROPERTIES.getProperty("user.home"),"."+APP_NAME);
							break;
						default:
							// unknown/unsupported OS
							Logger.getLogger(APP_NAME).log(Level.WARNING, 
								"OS type '"+SYSTEM_PROPERTIES.getProperty("os.name")
										+"' is not yet supported. Program will install into local directory instead of user home.");
								PROGRAM_DIRECTORY = jarDir;
							break;
					}
					
		
		
		
	}
	
	public Logger getLogger(){
		return Logger.getLogger(APP_NAME);
	}
	public Logger getLogger(Class srcClass){
		if(srcClass == null){
			return getLogger();
		}
		return Logger.getLogger(APP_NAME+" "+srcClass.getName());
	}
	public Logger getLogger(Object src){
		if(src == null){
			return getLogger();
		}
		return getLogger(src.getClass());
	}
	/**
	 * Gets the location of the .jar file holding the code for this class. 
	 * @return A JarFile instance pointing to the code source, if it is a .jar 
	 * file. If not, then 
	 * @throws IOException Thrown if there was an error reading the .jar code 
	 * source.
	 * @throws FileNotFoundException Thrown if the code is not running from a 
	 * .jar file
	 * @throws SecurityException Thrown if the security manager prohibits access 
	 * to the code source.
	 */
	public JarFile getJarFile() throws IOException, FileNotFoundException{
		try {
			return new java.util.jar.JarFile(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
		} catch (URISyntaxException ex) {
			throw new IOException("Error parsing URI '"+this.getClass().getProtectionDomain().getCodeSource().getLocation().toString()+"'");
		}
	}
	/**
	 * Gets the filepath of this executable jar.
	 * @return A Path object pointing to the location of this executable.
	 */
	public Path getJarDirectory() throws FileNotFoundException, IOException{
		File f = null;
	//	try {
			f = new File(getJarFile().getName());
	//	} catch (URISyntaxException ex) {
	//		Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
	//	}
		if(f == null || f.exists() == false){
			throw new FileNotFoundException("Directory '" + f.toString() + "' does not exist!");
		}
		return f.getParentFile().toPath();
	}
	
	/** type of OS on host computer */
	public static enum OSType{
		WINDOWS,MAC_OSX,LINUX,SOLARIS
	}
}
