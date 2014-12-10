package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.dawnsci.macro.api.IMacroService;

public class ServiceHolder {

	protected static IMacroService mservice;
	public static void setMacroService(IMacroService s) {
		mservice = s;
	}
	public static IMacroService getMacroService() {
		return mservice;
	}

}
