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
import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class ServiceHolder {

	private static ILoaderService loaderService;

	private static INexusFileFactory nexusFactory;
	
	public ServiceHolder() {
		
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public void setLoaderService(ILoaderService ls) {
		loaderService = ls;
	}

	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}

	public void setNexusFactory(INexusFileFactory nf) {
		nexusFactory = nf;
	}
}
