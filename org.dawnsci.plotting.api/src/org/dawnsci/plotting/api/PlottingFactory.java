/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.api;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlottingFactory is the preferred way to get an AbstractPlottingSystem. Also
 * consider just using the IPlottingSystem interface as this is more likely to 
 * be persistent when declaring the type in the calling code.
 * 
 * 
 * 
 * @author fcp94556
 *
 */
public class PlottingFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(PlottingFactory.class);

	static {
		if (Boolean.getBoolean("org.dawnsci.plotting.jmx.active")) {
			try {
				PlottingFactory.startServer();
			} catch (Exception e) {
				logger.error("Cannot start server!", e);
			}
		}
	}
	
	/**
	 * This class has a public constructor so that the squish tests can get a references using
	 * the class loader. Really it should be private. 
	 * 
	 * In the squish tests there is a script called 'use_case_utils.py' with a def called getPlottingSystem(...)
	 * which requires this to be there.
	 * 
	 */
	public PlottingFactory() {
		
	}
	
	/**
	 * Reads the extension points for the plotting systems registered and returns
	 * a plotting system based on the users current preferences.
	 * 
	 * @return
	 */
	public static IPlottingSystem createPlottingSystem() throws Exception {
				
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,"org.dawb.workbench.ui");
		String plotType = store.getString("org.dawb.plotting.system.choice");
		if (plotType.isEmpty()) plotType = System.getProperty("org.dawb.plotting.system.choice");// For Geoff et. al. can override.
		if (plotType==null) plotType = "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem"; // That is usually around
		
        IPlottingSystem system = createPlottingSystem(plotType);
        if (system!=null) return system;
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.plottingClass");
        IPlottingSystem ifnotfound = (IPlottingSystem)systems[0].createExecutableExtension("class");
		store.setValue("org.dawb.plotting.system.choice", systems[0].getAttribute("id"));
		return ifnotfound;
		
	}
	
	/**
	 * Always returns the light weight plotter if one is available, otherwise null.
	 * 
	 * @return
	 */
	public static IPlottingSystem getLightWeightPlottingSystem() throws Exception {
				
		return  createPlottingSystem("org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");		
	}
	
	private static final IPlottingSystem createPlottingSystem(final String plottingSystemId) throws CoreException {
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.plottingClass");
        for (IConfigurationElement ia : systems) {
			if (ia.getAttribute("id").equals(plottingSystemId)) return (IPlottingSystem)ia.createExecutableExtension("class");
		}
		
        return null;
	}

	
	public static String[][] getPlottingPreferenceChoices() {
		
		final List<String[]> choices = new ArrayList<String[]>(7);
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.plottingClass");
        for (IConfigurationElement ia : systems) {
        	choices.add(new String[]{ia.getAttribute("visible_type"), ia.getAttribute("id")});
		}
        
        final String [][] ret = new String[choices.size()][];
        for (int i = 0; i < choices.size(); i++) {
        	ret[i] = choices.get(i);
		}
        return ret;
	}

	private static Map<String, IPlottingSystem> plottingSystems;
	
	public static void clear() {
		if (plottingSystems!=null) plottingSystems.clear();
	}
	
	/**
	 * Removes a plot system from the registered names.
	 * @param plotName
	 * @return the removed system
	 */
	public static IPlottingSystem removePlottingSystem(String plotName) {
		try {
			unregisterRemote(plotName);
		} catch (Exception e) {
			logger.error("Cannot unregister JMX plotting system!", e);
		}
		if (plottingSystems==null) return null;
		return plottingSystems.remove(plotName);
	}

	/**
	 * Registers a plotting system by name. NOTE if the name is already used this
	 * will overwrite the old one!
	 * 
	 * @param plotName
	 * @param abstractPlottingSystem
	 * @return the replaced system if any or null otherwise.
	 */
	public static IPlottingSystem registerPlottingSystem(final String                 plotName,
			                                             final IPlottingSystem abstractPlottingSystem) {
		
		
		try {
			registerRemote(plotName, abstractPlottingSystem);
		} catch (Exception e) {
			logger.error("Cannot register JMX plotting system!", e);
		}
		if (plottingSystems==null) plottingSystems = new HashMap<String, IPlottingSystem>(7);
		return plottingSystems.put(plotName, abstractPlottingSystem);
	}
	
	
	/**
	 * Call to regiser plotting system remotely.
	 */
	private static void registerRemote(final String plotName, final IPlottingSystem ps) throws Exception {

		if (!Boolean.getBoolean("org.dawnsci.plotting.jmx.active")) return;

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		ObjectName on = new ObjectName("remote.plotting.system/"+plotName+":type=RemotePlottingSystem");
		// Uniquely identify the MBeans and register them with the MBeanServer 
		try {
			if (mbs.getObjectInstance(on)!=null) {
				mbs.unregisterMBean(on);
			}
		} catch (Exception ignored) {
			// Throws exception not returns null, so ignore.
		}
		mbs.registerMBean(new ThreadSafePlottingSystem(ps), on);

	}
	
	/**
	 * Call to regiser plotting system remotely.
	 */
	private static void unregisterRemote(final String plotName) throws Exception {

		if (!Boolean.getBoolean("org.dawnsci.plotting.jmx.active")) return;

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		ObjectName on = new ObjectName(IPlottingSystem.class.getPackage().getName()+"/"+plotName+":type=RemotePlottingSystem");
		mbs.unregisterMBean(on);

	}
	

	
	private static void startServer() throws Exception {
		
		if (!Boolean.getBoolean("org.dawnsci.plotting.jmx.active")) return;

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// We force a new registry on the port and use this
		// for workflow processes started.
		try {
			LocateRegistry.createRegistry(8991);
		} catch (java.rmi.server.ExportException ne) {
			// If we are running in tango server mode, there may be a registry already existing.
			logger.debug("Found existing registry on "+8991);
		}
		
		String hostName = System.getProperty("org.dawnsci.plotting.jmx.host.name");
		if (hostName==null) hostName = InetAddress.getLocalHost().getHostName();
		if (hostName==null) hostName = InetAddress.getLocalHost().getHostAddress();
		if (hostName==null) hostName = "localhost";

		
		JMXServiceURL serverUrl     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+hostName+":8991/plottingservice");

		// Create an RMI connector and start it
		JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(serverUrl, null, mbs);
		cs.start();

		logger.debug("Plotting service started on "+serverUrl);

	}

	
	/**
	 * Get a plotting system by name. NOTE if more than one plotting system has the same name the
	 * last one registered with this name is returned.
	 * 
	 * NOTE an AbstractPlottingSystem is also a IToolPageSystem, you can get tool pages here.
	 * 
	 * @param plotName
	 * @return AbstractPlottingSystem or null
	 */
	public static IPlottingSystem getPlottingSystem(String plotName) {
		return getPlottingSystem(plotName, false);
	}
	
	/**
	 * Get a plotting system by name. NOTE if more than one plotting system has the same name the
	 * last one registered with this name is returned.
	 * 
	 * NOTE an AbstractPlottingSystem is also a IToolPageSystem, you can get tool pages here.

	 * @param plotName
	 * @param threadSafe - set if all the methods on the plotting system should be thread safe.
	 *                     Generally used for plotting systems on servers.
	 * @return
	 */
	public static IPlottingSystem getPlottingSystem(String plotName, boolean threadSafe) {
		if (plottingSystems==null) return null;
		IPlottingSystem ps = plottingSystems.get(plotName);
	    try {
			return threadSafe ? new ThreadSafePlottingSystem(ps) : ps;
		} catch (Exception e) {
			if (threadSafe) {
				logger.error("Cannot create thread safe system, will return UI thread one.", e);
			}
			return ps;
		}
	}

	/**
	 * Get a tool page system by name (normally a plotting system is also a toolPage system).
	 * @param plotName
	 * @return
	 */
	public static IToolPageSystem getToolSystem(String plotName) {
		if (plottingSystems==null) return null;
		return (IToolPageSystem)plottingSystems.get(plotName).getAdapter(IToolPageSystem.class);
	}

	/**
	 * Get all the registered plotting systems.
	 * @internal
	 * @return
	 */
	public static IPlottingSystem[] getPlottingSystems() {
		if (plottingSystems==null) return null;
		return plottingSystems.values().toArray(new IPlottingSystem[plottingSystems.size()]);
	}
	
	/**
	 * 
	 * @param system
	 * @return a new decorator which will filter the data being plotting by the system.
	 */
	public static IFilterDecorator createFilterDecorator(IPlottingSystem system) {
		return new FilterDecoratorImpl(system);
	}
}
