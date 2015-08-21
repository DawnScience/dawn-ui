package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class MappedData implements MapObject{

	private String name;
	protected IDataset map;
	protected MappedDataBlock oParent;
	protected MappedDataBlock parent;
	private int transparency = -1;
	
	public MappedData(String name, IDataset map, MappedDataBlock parent) {
		this.name = name;
		this.map = map;
		this.oParent = this.parent = parent;
	}
	
	public IDataset getMap(){
		return map;
	}
	
	private int[] getIndices(double x, double y) {
		
		IDataset[] ax = MappingUtils.getAxesFromMetadata(map);
		
		IDataset xx = ax[1];
		IDataset yy = ax[0];
		
		int xi = Maths.abs(Maths.subtract(xx, x)).argMin();
		int yi = Maths.abs(Maths.subtract(yy, y)).argMin();
		
		return new int[]{xi,yi};
	}
	
	public IDataset getSpectrum(double x, double y) {
		int[] indices = getIndices(x, y);
		return parent.getSpectrum(indices[0], indices[1]);
	}
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent);
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

	public int getTransparency() {
		return transparency;
	}

	public void setTransparency(int transparency) {
		this.transparency = transparency;
	}

	public MappedDataBlock getParent() {
		return parent;
	}

	public void setParent(MappedDataBlock parent) {
		this.parent = parent;
	}

	public void resetParent() {
		parent = oParent;
	}
	
	
}
