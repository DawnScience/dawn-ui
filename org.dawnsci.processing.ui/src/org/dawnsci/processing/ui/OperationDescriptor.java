package org.dawnsci.processing.ui;

import java.net.URL;

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

	private boolean readImage = false;
	private Image image;
	
	// Reads the declared operations from extension point, if they have not been already.
	public synchronized Image getImage() {
		
		if (readImage) return image;
		readImage = true;
		
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.api.operation");
		for (IConfigurationElement e : eles) {
			final String     identity = e.getAttribute("id");
			if (identity.equals(this.id)) {
				
				final String icon = e.getAttribute("icon");
				if (icon !=null) {
			    	final String   cont  = e.getContributor().getName();
			    	final Bundle   bundle= Platform.getBundle(cont);
			    	final URL      entry = bundle.getEntry(icon);
			    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
                    image = des.createImage();			
                    break;
				}
			}
		}
		return null;
	}
	
	private boolean isVisible = true;
	private boolean foundVisible;
	public boolean isVisible() {
		if (foundVisible) return isVisible;
		
		foundVisible = true;
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.api.operation");
		for (IConfigurationElement e : eles) {
			final String     identity = e.getAttribute("id");
			if (identity.equals(this.id)) {
				final String     vis = e.getAttribute("visible");
				if (vis==null) {
					isVisible = true;
				} else {
				    isVisible = Boolean.parseBoolean(vis);
				}
				break;
			}
		}
		return isVisible;

	}

	public void dispose() {
		if (image!=null) image.dispose();
	}

	public String getId() {
		return id;
	}
}
