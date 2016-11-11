package org.dawnsci.mapping.ui.datamodel;

import java.awt.font.NumericShaper.Range;

import org.apache.commons.math3.ml.neuralnet.MapUtils;
import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReMappedData extends AbstractMapData {

	private IDataset lookup;
	private int[] shape;
	
	private ILazyDataset flatMap;
	
	private static final Logger logger = LoggerFactory.getLogger(ReMappedData.class);
	
	public ReMappedData(String name, ILazyDataset map, MappedDataBlock parent, String path) {
		super(name, (ILazyDataset)null, parent, path);
		flatMap = map;
		this.setRange(calculateRange(flatMap));
	}
	
	public ReMappedData(String name, IDatasetConnector map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}
	
	@Override
	protected double[] calculateRange(ILazyDataset map){
		if (map == null) return null;
		//FIXME for nd
		IDataset[] ax = MetadataPlotUtils.getAxesForDimension(map,parent.getMapDims().getxDim());
		double[] r = new double[4];
		r[0] = ax[1].min().doubleValue();
		r[1] = ax[1].max().doubleValue();
		r[2] = ax[0].min().doubleValue();
		r[3] = ax[0].max().doubleValue();
		return r;
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
		if (flatMap == null) return;
		IDataset fm = null;
		try {
		if (flatMap.getRank() == 1) {
			
				fm = DatasetUtils.sliceAndConvertLazyDataset(flatMap);
			
		} else {
			fm = flatMap.getSlice(parent.getMapDims().getMapSlice(flatMap));
		}
		} catch (DatasetException e) {
			logger.error("Error sliceing lazy dataset", e);
			return;
		}
		IDataset[] remapData = MappingUtils.remapData(fm, shape, 0);
		
		if (remapData == null) return;
		
		if (shape == null) {
			this.shape = remapData[0].getShape().clone();
		}
		
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
		int index = lookup.getInt(indices);
		if (index == -1) return null;
		if (parent.getLazy() instanceof IDatasetConnector) {
			((IDatasetConnector)parent.getLazy()).refreshShape();
		}
		return parent.getSpectrum(index);
	}


	@Override
	public boolean isLive() {
		return live;
	}

	public void replaceLiveDataset(ILazyDataset map) {
		live = false;
		disconnect();
		this.flatMap = map;
		setRange(calculateRange(flatMap));
	}
	
	public void update() {
		
		if (!live) return;
		if (!connected) {			
			try {
				connect();
			} catch (Exception e) {
				logger.debug("Could not connect",e);

			}
		}

		IDataset ma = LivePlottingUtils.getUpdatedLinearMap(baseMap, this.getParent(), this.toString());
		
		setRange(calculateRange(ma));
		flatMap = ma;
		updateRemappedData(null);
		
	}

	@Override
	public ILazyDataset getData() {
		return flatMap;
	}
}
