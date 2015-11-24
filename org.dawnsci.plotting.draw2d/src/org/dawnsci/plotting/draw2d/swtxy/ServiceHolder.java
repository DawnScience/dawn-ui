package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceHolder {

	protected static IMacroService mservice;
	private static IImageService iservice;
	private static IPaletteService pservice;

	public ServiceHolder() {
		// do nothing
	}

	public static void setMacroService(IMacroService s) {
		mservice = s;
	}
	public static IMacroService getMacroService() {
		return mservice;
	}

	public static void setImageService(IImageService is) {
		iservice = is;
	}

	public static IImageService getImageService() {
		return iservice;
	}

	public static void setPaletteService(IPaletteService ps) {
		pservice = ps;
	}

	public static IPaletteService getPaletteService() {
		return pservice;
	}
}
