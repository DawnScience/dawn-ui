package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.api.IMapFileController;

public class FileManagerSingleton {

	private static IMapFileController manager = null;
	
	protected FileManagerSingleton(){
		
	}
	
//	public static MappedFileManager getFileManager() {
//		if (manager == null) manager = new MappedFileManager();
//		return manager;
//	}
	
	
	public static void clearManager() {
		manager = null;
	}
	
}
