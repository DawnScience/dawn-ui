package org.dawnsci.mapping.ui.datamodel;

import java.util.List;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReMappedData extends AbstractMapData {

	private IDataset lookup;
	private int[] shape;
	
	private IDataset flatMap;
	
	private static final Logger logger = LoggerFactory.getLogger(ReMappedData.class);
	
	public ReMappedData(String name, IDataset map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}
	
	public ReMappedData(String name, IDatasetConnector map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}
	
	@Override
	protected double[] calculateRange(ILazyDataset map){
		IDataset[] ax = MetadataPlotUtils.getAxesForDimension(map,0);
		double[] r = new double[4];
		r[0] = ax[1].min().doubleValue();
		r[1] = ax[1].max().doubleValue();
		r[2] = ax[0].min().doubleValue();
		r[3] = ax[0].max().doubleValue();
		return r;
	}
	
	@Override
	public IDataset getData(){
		if (map == null) updateRemappedData(shape);
		
		return map;
	}
	
	private void updateRemappedData(int[] shape) {
		
		IDataset[] remapData = MappingUtils.remapData(flatMap, shape, 0);
		
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
	
	private int[] getIndices(double x, double y) {

		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(map);

		IDataset yy = ax[0];
		IDataset xx = ax[1];

		Dataset xd = Maths.subtract(xx, x);
		Dataset yd = Maths.subtract(yy, y);
		
		int xi = Maths.abs(xd).argMin();
		int yi = Maths.abs(yd).argMin();

		return new int[]{yi,xi};
	}
	
	@Override
	public IDataset getSpectrum(double x, double y) {
		int[] indices = getIndices(x, y);
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

	public void replaceLiveDataset(IDataset map) {
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
		
//		try{
//			baseMap.refreshShape();
//			ma = baseMap.getDataset().getSlice();
//		} catch (Exception e) {
//			//TODO log?
//		}
//		
//		if (ma == null) return;
//		
//		ma.setName(this.toString());
//		
//		if (parent.isTransposed()) ma = DatasetUtils.convertToDataset(ma).transpose();
//		
//		// TODO This check is probably not required
//		if ( baseMap instanceof ILazyDataset && ((ILazyDataset)baseMap).getSize() == 1) return;
//		
//		ILazyDataset ly = parent.getYAxis()[0];
//		ILazyDataset lx = parent.getXAxis()[0];
//	
//		
//		IDataset x;
//		IDataset y;
//		try {
//			x = lx.getSlice();
//			y = ly.getSlice();
//		} catch (DatasetException e) {
//			logger.debug("Could not slice",e);
//			return;
//		}
//		
//		if (y.getRank() == 2) {
//			SliceND s = new SliceND(y.getShape());
//			s.setSlice(1, 0, 1, 1);
//			y = y.getSlice(s);
//			if (y.getSize() == 1) {
//				y.setShape(new int[]{1});
//			} else {
//				y.squeeze();
//			}
//			
//		}
//		
//		if (x.getRank() == 2) {
//			SliceND s = new SliceND(x.getShape());
//			s.setSlice(0, 0, 1, 1);
//			x = x.getSlice(s);
//			if (x.getSize() == 1) {
//				x.setShape(new int[]{1});
//			} else {
//				x.squeeze();
//			}
//			
//		}
//
//		int[] mapShape = ma.getShape();
//		SliceND s = new SliceND(mapShape);
//		int maxShape = Math.min(y.getShape()[0], y.getShape()[0]);
//		maxShape = Math.min(maxShape, mapShape[0]);
//		
//		AxesMetadata axm = null;
//		try {
//			axm = MetadataFactory.createMetadata(AxesMetadata.class, 1);
//			axm.addAxis(0, y.getSlice(s));
//			axm.addAxis(0, x.getSlice(s));
//		} catch (MetadataException e) {
//			logger.error("Could not create axes metdata", e);
//		}
//		
//		s.setSlice(0, 0, y.getShape()[0], 1);
//		IDataset fm = ma.getSlice(s);
//		fm.setMetadata(axm);
		setRange(calculateRange(ma));
		flatMap = ma;
		updateRemappedData(null);
		
	}
}
