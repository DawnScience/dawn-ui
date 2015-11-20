/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceLoader {

	private static IPaletteService pService;
	private static ILoaderService lService;

	static {
		System.out.println("Starting plotting system services loader");
	}

	public ServiceLoader() {
		// do nothing, used for osgi loading
	}

	public static void setLoaderService(ILoaderService ls) {
		lService = ls;
	}

	public static ILoaderService getCommandService() {
		return lService;
	}

	public static void setPaletteService(IPaletteService ps) {
		pService = ps;
	}

	public static IPaletteService getPaletteService() {
		return pService;
	}
}
