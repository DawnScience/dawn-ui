/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.processing.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationCategory;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.Bundle;

public class OperationDescriptor implements ISeriesItemDescriptor {

	// We never dispose these static images. They are small
	// in number and we just leave the VM to tidy them up...
	private static Map<String, Image>   icons;
	private static Map<String, Boolean> visible;
	

	private IOperation<? extends IOperationModel, ? extends OperationData>              operation;
	private final String            id;
	private final IOperationService service;
	
	// Operations of the same id and service are required to be differentiated
	private final String            uniqueId;
	
	public OperationDescriptor(String id, IOperationService service) {
		this.id       = id;
		this.service  = service;
		this.uniqueId = UUID.randomUUID().toString()+"_"+System.currentTimeMillis();
	}
	
	public OperationDescriptor(IOperation<? extends IOperationModel, ? extends OperationData> operation, IOperationService service) {
		this.id       = operation.getId();
		this.operation= operation;
		this.service  = service;
		this.uniqueId = UUID.randomUUID().toString()+"_"+System.currentTimeMillis();
	}

	@Override
	public IOperation<? extends IOperationModel, ? extends OperationData> getSeriesObject() throws InstantiationException {
		if (operation==null) {
			try {
				operation = service.create(id);
			} catch (Exception e) {
				throw new InstantiationException(e.getMessage());
			}
		}
		return operation;
	}

	@Override
	public String getName() {
		try {
			return service.getName(id);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	@Override
	public String getLabel() {
		StringBuilder buf = new StringBuilder(getName());
		buf.append("   ");
		buf.append(getCategoryLabel());
		return buf.toString();
	}

	@Override
	public String getDescription() {
		try {
			return service.getDescription(id);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private OperationPropertySource modelSource;
	
	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == IPropertySource.class) {
			if (modelSource==null) modelSource = new OperationPropertySource(getModel());
			return modelSource;
		}
		return null;
	}

	public IOperationModel getModel() {
		
		if (operation!=null && operation.getModel()!=null) return operation.getModel();
		
        try {
			IOperationModel model = service.getModelClass(id).newInstance();
			IOperation op = getSeriesObject();
			op.setModel(model);
			return model;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationDescriptor other = (OperationDescriptor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}

	
	// Reads the declared operations from extension point, if they have not been already.
	public synchronized Image getImage() {
		
		if (icons==null) read();
		return icons.get(id);
	}

	public synchronized boolean isVisible() {
		if (visible==null) read();
		Boolean b = visible.get(id);
		return b!=null ? b : true;
	}
	
	private static synchronized void read() {
		
		icons   = new HashMap<String, Image>(7);
		visible = new HashMap<String, Boolean>(7);
		
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.operation");
		for (IConfigurationElement e : eles) {
			final String     identity = e.getAttribute("id");
				
			final String     vis = e.getAttribute("visible");
			boolean isVisible = vis==null ? true : Boolean.parseBoolean(vis);
			visible.put(identity, isVisible);

			final String icon = e.getAttribute("icon");
			if (icon !=null) {
				final String   cont  = e.getContributor().getName();
				final Bundle   bundle= Platform.getBundle(cont);
				final URL      entry = bundle.getEntry(icon);
				final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
				icons.put(identity, des.createImage());		
			}
			
		}
		
	}

	public String getId() {
		return id;
	}

	public boolean isCompatibleWith(Object previous) {
		
		try {
			if (previous == null) return true;
	        if (!getClass().isInstance(previous)) return false;
	        
	        // TODO Nice to do this without making the operations...
	        final IOperation<? extends IOperationModel, ? extends OperationData> op = ((OperationDescriptor)previous).getSeriesObject();
	        final IOperation<? extends IOperationModel, ? extends OperationData> ot = this.getSeriesObject();
	        
	        if (op.isPassUnmodifiedData()) {
	        	return op.getInputRank().isCompatibleWith(ot.getInputRank());
	        }
	        
			return op.getOutputRank().isCompatibleWith(ot.getInputRank());
			
		} catch (Exception ne) {
			ne.printStackTrace();
			return false;
		}

	}
	
	public String toString() {
		return getName();
	}

	public String getCategoryLabel() {
		
		final OperationCategory cat = service.getCategory(id);
		if (cat == null) return "";
        return "["+cat.getName()+"]";
	}
}
