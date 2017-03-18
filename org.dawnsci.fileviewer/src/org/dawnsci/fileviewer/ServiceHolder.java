/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class ServiceHolder {

	private static ILoaderService loaderService;

	public ServiceHolder() {
		
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public static void setLoaderService(ILoaderService ls) {
		loaderService = ls;
	}

}
