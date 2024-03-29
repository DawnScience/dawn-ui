package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;



public class AssociatedImage implements PlottableMapObject {

	private String name;
	private String shortName;
	private String path;
	private Dataset image;
	private double[] range;
	private boolean plotted;

	public AssociatedImage(String name, Dataset image, String path) {
		this.name = name;
		this.image = image;
		this.range = calculateRange(image);
		this.path = path;
		this.shortName = MappingUtils.getShortName(name);
	}
	
	@Override
	public IDataset getMap() {
		return image;
	}
	
	@Override
	public String toString() {
		return shortName;
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
		return range.clone();
	}

	@Override
	public String getLongName() {
		return path + " : " + name;
	}

	@Override
	public boolean isLive() {
		return false;
	}

	@Override
	public void update() {
		
	}

	@Override
	public int getTransparency() {
		return 0;
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		return null;
	}

	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public boolean isPlotted() {
		return this.plotted;
	}
	
	@Override
	public void setPlotted(boolean plot) {
		this.plotted = plot;
	}

	@Override
	public void setColorRange(double[] range) {
		// RGB so do nothing
	}

	@Override
	public double[] getColorRange() {
		// RGB return null
		return null;
	}

	@Override
	public void setTransparency(int transparency) {
		// unsupported for now
		
	}
	
}
