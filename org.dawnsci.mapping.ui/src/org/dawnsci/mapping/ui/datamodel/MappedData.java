package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;


public class MappedData extends AbstractMapData{

	
	public MappedData(String name, ILazyDataset map, MappedDataBlock parent, String path, boolean live) {
		super(name, map, parent, path, live);
	}
	
	protected double[] calculateRange(ILazyDataset map){
		
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(map);
		
		MapScanDimensions mapDims = parent.getMapDims();
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		return MappingUtils.calculateRangeFromAxes(new IDataset[]{ax[yDim],ax[xDim]});

	}
	

	public IDataset getSpectrum(double x, double y) {
		int[] indices = MappingUtils.getIndicesFromCoOrds(baseMap, x, y, parent.getMapDims().getxDim(),parent.getMapDims().getyDim());
		if (indices == null) return null;
		ILazyDataset spectrum = parent.getSpectrum(indices[0], indices[1]);
		if (spectrum == null) return null;
		IDataset s = null;
		try {
			s = spectrum.getSlice();
		} catch (DatasetException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path, false);
	}

	
	public boolean isLive() {
		return live;
	}
	
	public void update() {
		if (live) map = updateMap();
	}

	@Override
	public ILazyDataset getData() {
		return baseMap;
	}
}
