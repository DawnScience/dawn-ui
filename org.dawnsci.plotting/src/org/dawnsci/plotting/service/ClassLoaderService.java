/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.service;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.vecmath.Vector3d;

import org.dawb.common.services.IClassLoaderService;
import org.dawb.workbench.jmx.UserPlotBean;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import com.thoughtworks.xstream.core.util.CompositeClassLoader;

/**
 * Defines the class loader that should be used for RMI communications.
 * The Loader will be able to load analysis classes which are communicated via RMI.
 * 
 * @author Matthew Gerring
 *
 */
public class ClassLoaderService extends AbstractServiceFactory implements IClassLoaderService {

	private ClassLoader originalLoader;
	
	static {
		System.out.println("Starting plotting class loader service");
	}
	
	public ClassLoaderService() {
		// Important do nothing here, OSGI may start the service more than once.
	}
	
	@Override
	public void setDataAnalysisClassLoaderActive(boolean active) {
		
	    ClassLoader classLoader;
		if (active) {
			originalLoader = Thread.currentThread().getContextClassLoader();
			classLoader = createClassLoader();
		} else {
			classLoader    = originalLoader;
			originalLoader = null;
		}
		
		if (classLoader==null) return;
		
		final ClassLoader finalCL = classLoader;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				Thread.currentThread().setContextClassLoader(finalCL);
				return null;
			}
		});

	}

	private ClassLoader createClassLoader() {
		final CompositeClassLoader loader = new CompositeClassLoader();
	    loader.add(org.eclipse.dawnsci.analysis.dataset.impl.Activator.class.getClassLoader());
	    loader.add(uk.ac.diamond.scisoft.analysis.Activator.class.getClassLoader());
		loader.add(Slice.class.getClassLoader());           // analysis.api
		loader.add(UserPlotBean.class.getClassLoader());    // workbench.jmx
		loader.add(Vector3d.class.getClassLoader());        // vecmath
		return loader;
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==IClassLoaderService.class) {
			return new ClassLoaderService(); // Important always new as has member data.
		} 
		return null;
	}

}
