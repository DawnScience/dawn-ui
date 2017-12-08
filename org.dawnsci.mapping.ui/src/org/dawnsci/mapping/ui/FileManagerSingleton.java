package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MappedFileManager;

public class FileManagerSingleton {

	private static MappedFileManager manager = null;
	
	protected FileManagerSingleton(){
		
	}
	
	public static MappedFileManager getFileManager() {
		if (manager == null) manager = new MappedFileManager();
		return manager;
	}
	
	
	public static void clearManager() {
		manager = null;
	}
	
}
