package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.jface.viewers.Viewer;

public class FileManagerSingleton {

	private static MappedFileManager manager = null;
	
	protected FileManagerSingleton(){
		
	}
	
	public static MappedFileManager getFileManager() {
		return manager;
	}
	
	public static void initialiseManager(MapPlotManager plotManager, MappedDataArea mappedDataArea, Viewer viewer) {
		if (manager == null) manager = new MappedFileManager();
		manager.init(plotManager, mappedDataArea, viewer);
	}
	
	public static void clearManager() {
		manager = null;
	}
	
}
