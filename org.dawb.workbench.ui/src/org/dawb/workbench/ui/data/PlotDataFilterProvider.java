/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.filter.IFilterDecorator;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.dawnsci.plotting.api.filter.UniqueNamedPlottingFilter;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage filters defined by the user.
 * 
 * These filters have a pydev interpreter which runs the user defined filter.
 * 
 * @author fcp94556
 *
 */
final class PlotDataFilterProvider implements IResourceChangeListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PlotDataFilterProvider.class);
	
	private IFilterDecorator             decorator;
	private Map<String, IPlottingFilter> filters;
	private Set<IResource>               filterFiles;

	public PlotDataFilterProvider(IPlottingSystem system) {
		this.decorator = PlottingFactory.createFilterDecorator(system);
		decorator.setActive(false);
		filters     = new HashMap<String, IPlottingFilter>();
		filterFiles = new HashSet<IResource>();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * 
	 * @param ob
	 * @throws Exception with user readable message describing why filter is not valid.
	 */
	public void createFilter(ITransferableDataObject ob) throws Exception {
		
		if (ob.getFilterPath()==null) throw new Exception("There must be a filter path to a python script in order for a filter to be applied!");
		
		final IResource scriptFile = ResourcesPlugin.getWorkspace().getRoot().findMember(ob.getFilterPath());
		if (scriptFile==null) throw new Exception("The script '"+ob.getFilterPath()+"' cannot be found!");
		
		PythonPlottingFilter filter = new PythonPlottingFilter(ob.getName(), scriptFile);
		filters.put(ob.getName(), filter);
		filterFiles.add(scriptFile);
		decorator.addFilter(filter);
		decorator.setActive(true);
		
	}
	
	public void deleteFilter(ITransferableDataObject ob) {
				
		final IResource scriptFile = ResourcesPlugin.getWorkspace().getRoot().findMember(ob.getFilterPath());
		if (scriptFile==null) return;
		filterFiles.remove(scriptFile);
		
		IPlottingFilter filter = filters.remove(ob.getName());
		if (filter==null) return;

		filter.setActive(false);
		decorator.removeFilter(filter); // Does a dispose and stops the service.
	}

	
	/**
	 * A named filter able to deal with 1D and 2D data.
	 * 
	 * @author fcp94556
	 *
	 */
	public class PythonPlottingFilter extends UniqueNamedPlottingFilter {

		private IResource                     file;
		private IPythonFilter                 pythonProxy;
		private AnalysisRpcPythonPyDevService service;
		
		public PythonPlottingFilter(String name, IResource file) throws Exception {	
			super(name);			
			this.file       = file;		
            createService();
		}

		
		private void createService() throws Exception {
			
			if (service!=null) service.stop();
			
	        // TODO use constructor with true which offers to configure a python interpreter if none exists.
			this.service = new AnalysisRpcPythonPyDevService(file.getProject());
			service.addHandlers("execfile('''" + file.getLocation().toPortableString() + "''')", new String[]{"filter1D", "filter2D"});
					
			// TODO A constructor to allow debugging to connect.
			this.pythonProxy = service.getClient().newProxyInstance(IPythonFilter.class);
		}
		
		@Override
		protected IDataset[] filter(IDataset x,    IDataset y) {
			try {
				if (pythonProxy==null) createService();
				return pythonProxy.filter1D(x, y);
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 1D filter, wrong implementation of method.");
				return new IDataset[]{x,y};
			}
		}
		
		@Override
		protected Object[] filter(IDataset image,    List<IDataset> axes) {
			try {
				if (pythonProxy==null) createService();
				IDataset xaxis =  axes!=null&&axes.size()==2 ? axes.get(0): null;
				IDataset yaxis =  axes!=null&&axes.size()==2 ? axes.get(1): null;
				IDataset[] data = pythonProxy.filter2D(image, xaxis, yaxis);
				
				return new Object[]{data[0], Arrays.asList(data[0], data[1])};
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 1D filter, wrong implementation of method.");
				return new Object[]{image,axes};
			}
		}
		
		public void dispose() {
			stop();
		}

		public boolean isScript(IResource changed) {
			return file.equals(changed);
		}

		public void stop()  {
			service.stop();
			service     = null;
			pythonProxy = null;
		}

	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
	
		IResourceDelta delta = event.getDelta();
		if (delta==null) return;
		
		try {
			delta.accept(new ResourceVistor());
		} catch (CoreException e) {
			logger.error("Cannot process resource change!", e);
		}
	}
	private class ResourceVistor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			
			IResource changed = delta.getResource();
			if (!filterFiles.contains(changed)) return true; // Continue checking
			
			final Collection<IPlottingFilter> fs = filters.values();
			for (IPlottingFilter ipf : fs) {
				PythonPlottingFilter ppf = (PythonPlottingFilter)ipf;
				if (ppf.isScript(changed)) {
					try {
						ppf.stop();
					} catch (Exception e) {
						logger.error("Cannot connect to python file '"+changed+"'");
					}
				}
			}
			
			return false; // No need to do more visits
		}
		
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		filters.clear();
		filterFiles.clear();
	}
}
