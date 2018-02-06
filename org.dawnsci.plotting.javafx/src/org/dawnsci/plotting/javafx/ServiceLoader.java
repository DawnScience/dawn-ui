package org.dawnsci.plotting.javafx;

import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceLoader {

	private static IPaletteService paletteService;

	// do not anything here: OSGi might call it more than once
	public ServiceLoader() {
		
	}

	public static IPaletteService getPaletteService() {
		return paletteService;
	}

	/**
	 * Injected by OSGi
	 * @param ps
	 */
	public void setPaletteService(IPaletteService ps) {
		paletteService = ps;
	}

	
}
