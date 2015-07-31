package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;



public class AssociatedImage implements MapObject {

	
	private String name;
	private RGBDataset image;

	public AssociatedImage(String name, RGBDataset image) {
		this.name = name;
		this.image = image;
	}
	
	public IDataset getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}

	
}
