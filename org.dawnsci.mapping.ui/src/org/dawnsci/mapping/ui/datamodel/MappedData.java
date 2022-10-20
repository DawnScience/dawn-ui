package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MappedData extends AbstractMapData{

	private static final Logger logger = LoggerFactory.getLogger(MappedData.class);
	
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
			logger.error("Could not slice dataset", e);
		}
		return s;
	}
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path, false);
	}

	public void update() {
		if (live) {
			synchronized (getLock()) {
				cachedMap = updateMap();
			}
		}
	}

	@Override
	public ILazyDataset getData() {
		return baseMap;
	}

	@Override
	protected Dataset sanitizeRank(Dataset data, MapScanDimensions dims) {
		if (data.getRank() == 2) {
			return data;
		}
		
		int xd = dims.getxDim();
		int yd = dims.getyDim();
		
		//deal with transpose elsewhere
		if (xd < yd) {
			int tmp = xd;
			xd = yd;
			yd = tmp;
		}
		
		AxesMetadata ax = data.getFirstMetadata(AxesMetadata.class);
		
		if (ax == null) return null;
		
		ILazyDataset[] axx = ax.getAxis(xd);
		ILazyDataset[] axy = ax.getAxis(yd);
		
		int[] oShape = data.getShape();
		
		int[] shape = new int[] {oShape[yd], oShape[xd]};
		
		Dataset view = DatasetUtils.convertToDataset(data.getSliceView());
		view.clearMetadata(null);
		view.setShape(shape);
		
		buildSuffix(new SliceND(data.getShape()), ax);
		
		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			
			for (ILazyDataset l : axx) {
				md.addAxis(1, l.getSliceView().squeezeEnds());
			}
			
			for (ILazyDataset l : axy) {
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
