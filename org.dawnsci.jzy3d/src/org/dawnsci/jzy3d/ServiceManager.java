package org.dawnsci.jzy3d;

import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceManager {

	private static IPaletteService paletteService;
	private static IImageService imageService;
	
	public static IPaletteService getPaletteService() {
		return paletteService;
	}

	public void setPaletteService(IPaletteService paletteService) {
		ServiceManager.paletteService = paletteService;
	}

	public static IImageService getImageService() {
		return imageService;
	}

	public void setImageService(IImageService imageService) {
		ServiceManager.imageService = imageService;
	}
	
	
}
