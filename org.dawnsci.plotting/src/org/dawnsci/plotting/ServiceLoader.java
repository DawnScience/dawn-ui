/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting;

import org.dawb.common.services.ISystemService;

public class ServiceLoader {

	private static ISystemService<?> sservice;

	public ServiceLoader() {
		//used to load osgi service
	}

	public static void setSystemService(ISystemService<?> s) {
		sservice = s;
	}

	public static ISystemService<?> getSystemService() {
		return sservice;
	}
 }