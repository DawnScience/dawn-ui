package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;



public class AssociatedImage implements PlottableMapObject {

	
	private String name;
	private String path;
	private RGBDataset image;
	private double[] range;

	public AssociatedImage(String name, RGBDataset image, String path) {
		this.name = name;
		this.image = image;
		this.range = calculateRange(image);
		this.path = path;
	}
	
	public IDataset getData() {
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
	
	protected double[] calculateRange(IDataset map){
		return MappingUtils.getGlobalRange(map);
	}

	@Override
	public double[] getRange() {
		return range;
	}

	public String getLongName() {
		return path + " : " + name;
	}
	
}
