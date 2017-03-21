package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReMappedData extends AbstractMapData {

	private IDataset lookup;
	private IDataset flatMap;
	private int[] shape;
	
	private static final Logger logger = LoggerFactory.getLogger(ReMappedData.class);
	
	public ReMappedData(String name, ILazyDataset map, MappedDataBlock parent, String path, boolean live) {
		super(name, map, parent, path, live);

	}
	
	@Override
	protected double[] calculateRange(ILazyDataset map){
		if (baseMap.getSize() == 0) return null;
		//FIXME for nd
		IDataset[] ax = MetadataPlotUtils.getAxesForDimension(map,parent.getMapDims().getxDim());
		return MappingUtils.calculateRangeFromAxes(ax);
	}
	
	@Override
	public IDataset getMap(){
		if (map == null) updateRemappedData(shape);
		
		Dataset d = null;
		try {
			d = DatasetUtils.sliceAndConvertLazyDataset(map);
		} catch (DatasetException e) {
			logger.error("Could not slice lazy dataset",e);
		}
		
		return d;
	}
	
	private void updateRemappedData(int[] shape) {
		IDataset fm = flatMap;
		if (fm == null) {
			
			if (baseMap.getSize() == 1) return;
			
			try {
				if (baseMap.getRank() == 1) {

					fm = DatasetUtils.sliceAndConvertLazyDataset(baseMap);

				} else {
					fm = baseMap.getSlice(parent.getMapDims().getMapSlice(baseMap));
					fm.squeeze();
				}
			} catch (DatasetException e) {
				logger.error("Error sliceing lazy dataset", e);
				return;
			}
		}
		
		MapScanDimensions mapDims = oParent.getMapDims();
		
		fm = LivePlottingUtils.cropNanValuesFromAxes(fm,!mapDims.isRemappingRequired());
		
		IDataset[] remapData = MappingUtils.remapData(fm, shape, 0);
		
		if (remapData == null) return;
		
		if (shape == null) {
			this.shape = remapData[0].getShape().clone();
		}
		
		setRange(MappingUtils.getRange(remapData[0], true));
		
		map = remapData[0];
		lookup = remapData[1];
		
	}
	
	public int[] getShape() {
		return shape;
	}
	
	public void setShape(int[] shape){
		this.shape = shape;
		map = null;
		updateRemappedData(shape);
	}
	
	
	@Override
	public IDataset getSpectrum(double x, double y) {
		int[] indices = MappingUtils.getIndicesFromCoOrds(map, x, y);
		int index = -1;
		try {
			index = lookup.getInt(new int[]{indices[1],indices[0]});
		} catch (Exception e) {
			logger.debug("click outside bounds");
		}
		if (index == -1) return null;
		if (parent.getLazy() instanceof IDynamicDataset) {
			((IDynamicDataset)parent.getLazy()).refreshShape();
		}
		return parent.getSpectrum(index);
	}

	@Override
	public boolean isLive() {
		return live;
	}

	
	public void update() {
		flatMap = updateMap();
		updateRemappedData(null);
	}
	
	@Override
	public ILazyDataset getData() {
		return baseMap;
	}
}
