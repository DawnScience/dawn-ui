package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
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
		if (map == null && !live) updateRemappedData(shape);
		
		if (map == null) return null;
		
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
			
			if (baseMap.getSize() == 1 & live) return;
		
			try {
				if (baseMap.getSize() == 1) {
					map = baseMap.getSlice();
					lookup = DatasetFactory.zeros(1);
					return;
				}
				
				
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
		
		if (fm.getFirstMetadata(AxesMetadata.class) == null) {
			logger.error("Map requires have axes metadata");
			return;
		}
		
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
//		map = null;
		updateRemappedData(shape);
	}
	
	
	@Override
	public IDataset getSpectrum(double x, double y) {
		
		int index = -1;
		
		if (baseMap.getSize() == 1) {
			index = 0;
		} else {
			int[] indices = MappingUtils.getIndicesFromCoOrds(getMap(), x, y);
			
			try {
				index = lookup.getInt(new int[]{indices[1],indices[0]});
			} catch (Exception e) {
				logger.debug("click outside bounds");
			}
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
		synchronized (getLock()) {
			flatMap = updateMap();
			if (flatMap == null) {
				return;
			}
			updateRemappedData(null);
		}
	}
	
	@Override
	public ILazyDataset getData() {
		return baseMap;
	}

	@Override
	protected IDataset sanitizeRank(IDataset data, MapScanDimensions dims) {
		if (data.getRank() == 1) {
			return data;
		}
		
		//should be same for x and y
		int dim = dims.getxDim();
		
		AxesMetadata ax = data.getFirstMetadata(AxesMetadata.class);
		
		if (ax == null) return null;
		
		ILazyDataset[] axes = ax.getAxis(dim);
		
		int[] oShape = data.getShape();
		
		int[] shape = new int[] {oShape[dim]};
		
		IDataset view = data.getSliceView();
		view.clearMetadata(null);
		view.setShape(shape);
		
		buildSuffix(new SliceND(data.getShape()), ax);
		
		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			
			for (ILazyDataset l : axes) {
				md.addAxis(0, l.getSliceView().squeezeEnds());
			}
			
			view.setMetadata(md);
			
			return view;
			
		} catch (Exception e) {
			logger.error("Could not create axes metadata",e);
			return null;
		}
	}
}
