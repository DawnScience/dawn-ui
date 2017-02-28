package org.dawnsci.datavis.view.parts;

import org.dawnsci.datavis.model.IFileController;

public class ServiceManager {
	
	private static IFileController fController;
	
	public static IFileController getFileController() {
		return fController;
	}

	public static void setFileController(IFileController controller) {
		ServiceManager.fController = controller;
	}
}
