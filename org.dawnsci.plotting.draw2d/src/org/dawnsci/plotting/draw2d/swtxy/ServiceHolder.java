package org.dawnsci.plotting.draw2d.swtxy;

import org.dawnsci.plotting.draw2d.Activator;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceHolder {

	public static IImageService getImageService() {
		return Activator.getService(IImageService.class);
	}

	public static IPaletteService getPaletteService() {
		return Activator.getService(IPaletteService.class);
	}
}
