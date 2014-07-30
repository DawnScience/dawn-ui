package org.dawnsci.processing.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.IOperationService;

public class OperationDescriptor implements ISeriesItemDescriptor {

	private IOperation              operation;
	private final String            id;
	private final IOperationService service;
	
	// We never dispose these static images. They are small
	// in number and we just leave the VM to tidy them up...
	private static Map<String, Image>   icons;
	private static Map<String, Boolean> visible;
	
	public OperationDescriptor(String id, IOperationService service) {
		this.id      = id;
		this.service = service;
	}

	@Override
	public IOperation getSeriesObject() throws InstantiationException {
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
	public String getDescription() {
		try {
			return service.getDescription(id);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public Object getAdapter(Class clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((operation == null) ? 0 : operation.hashCode());
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
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
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
		return visible.get(id);
	}
	
	private static synchronized void read() {
		
		icons   = new HashMap<String, Image>(7);
		visible = new HashMap<String, Boolean>(7);
		
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.api.operation");
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
	        final IOperation op = ((OperationDescriptor)previous).getSeriesObject();
	        final IOperation ot = this.getSeriesObject();
	        
			return op.getOutputRank().isCompatibleWith(ot.getInputRank());
			
		} catch (Exception ne) {
			ne.printStackTrace();
			return false;
		}

	}
	
	public String toString() {
		return getName();
	}
}
