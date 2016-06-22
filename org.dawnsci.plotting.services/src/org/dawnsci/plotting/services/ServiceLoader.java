/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.services;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class ServiceLoader {

	private static IPaletteService pservice;
	private static ILoaderService lservice;

	public ServiceLoader() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	public static IPaletteService getPaletteService() {
		return pservice;
	}

	public static void setPaletteService(IPaletteService pservice) {
		ServiceLoader.pservice = pservice;
	}

	public static ILoaderService getLoaderService() {
		return lservice;
	}

	public static void setLoaderService(ILoaderService lservice) {
		ServiceLoader.lservice = lservice;
	}
}
