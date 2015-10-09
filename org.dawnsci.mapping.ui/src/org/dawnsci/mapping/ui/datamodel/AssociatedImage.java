package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;



public class AssociatedImage implements MapObject {

	
	private String name;
	private RGBDataset image;
	private double[] range;

	public AssociatedImage(String name, RGBDataset image) {
		this.name = name;
		this.image = image;
		this.range = calculateRange(image);
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
	
	protected double[] calculateRange(IDataset map){
		IDataset[] ax = MappingUtils.getAxesFromMetadata(map);
		double[] r = new double[4];
		r[0] = ax[1].min().doubleValue();
		r[1] = ax[1].max().doubleValue();
		r[2] = ax[0].min().doubleValue();
		r[3] = ax[0].max().doubleValue();
		return r;
	}

	@Override
	public double[] getRange() {
		return range;
	}

	
}
