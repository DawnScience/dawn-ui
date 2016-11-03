package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
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

public class MappedData extends AbstractMapData{

	private static final Logger logger = LoggerFactory.getLogger(MappedData.class);
	
	
	public MappedData(String name, IDataset map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}
	
	public MappedData(String name, IDatasetConnector map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}
	
	public void replaceLiveDataset(IDataset dataset) {
		live = false;
		disconnect();
		this.map = dataset;
		setRange(calculateRange(map));
	}
	
	protected double[] calculateRange(ILazyDataset map){
		
		if (map instanceof IDatasetConnector) return null;
		
		double[] range = MappingUtils.getGlobalRange(map);
		
		return range;
	}
	
	private int[] getIndices(double x, double y) {
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(map, false);
		
		IDataset xx = ax[1];
		IDataset yy = ax[0];
		
		double xMin = xx.min().doubleValue();
		double xMax = xx.max().doubleValue();
		
		double yMin = yy.min().doubleValue();
		double yMax = yy.max().doubleValue();
		
		double xd = ((xMax-xMin)/xx.getSize())/2;
		double yd = ((yMax-yMin)/yy.getSize())/2;
		
		if (xd == 0 && yd == 0) return null;
		
		yd = yd == 0 ? xd : yd;
		xd = xd == 0 ? yd : xd;
		
		if (x > xMax+xd || x < xMin-xd || y > yMax+yd || y < yMin-yd) return null;
		
		int xi = Maths.abs(Maths.subtract(xx, x)).argMin();
		int yi = Maths.abs(Maths.subtract(yy, y)).argMin();
		
		return new int[]{xi,yi};
	}
	
	public IDataset getSpectrum(double x, double y) {
		int[] indices = getIndices(x, y);
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
		return new MappedData(name, ds, parent, path);
	}

	
	public boolean isLive() {
		return live;
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

		IDataset ma = null;
		
		try{
			baseMap.refreshShape();
			ma = baseMap.getDataset().getSlice();
		} catch (Exception e) {
			//TODO log?
		}
		
		if (ma == null) return;
		
		ma.setName(this.toString());
		
		if (parent.isTransposed()) ma = DatasetUtils.convertToDataset(ma).transpose();
		
		// TODO This check is probably not required
		if ( baseMap instanceof ILazyDataset && ((ILazyDataset)baseMap).getSize() == 1) return;
		
		ILazyDataset ly = parent.getYAxis()[0];
		ILazyDataset lx = parent.getXAxis()[0];
		
		IDataset x;
		IDataset y;
		try {
			x = lx.getSlice();
			y = ly.getSlice();
		} catch (DatasetException e) {
			logger.debug("Could not slice",e);
			return;
		}
		
		if (y.getRank() == 2) {
			SliceND s = new SliceND(y.getShape());
			s.setSlice(1, 0, 1, 1);
			y = y.getSlice(s);
			if (y.getSize() == 1) {
				y.setShape(new int[]{1});
			} else {
				y.squeeze();
			}
			
		}
		
		if (x.getRank() == 2) {
			SliceND s = new SliceND(x.getShape());
			s.setSlice(0, 0, 1, 1);
			x = x.getSlice(s);
			if (x.getSize() == 1) {
				x.setShape(new int[]{1});
			} else {
				x.squeeze();
			}
			
		}
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			axm.addAxis(0, y);
			axm.addAxis(1, x);
		} catch (MetadataException e) {
			logger.error("Could not create axes metdata", e);
		}

		IDataset fm = null;
		int[] mapShape = ma.getShape();
		if (mapShape.length == 2) {
			SliceND s = new SliceND(mapShape);
			if (mapShape[0] > y.getShape()[0]) s.setSlice(0, 0, y.getShape()[0], 1);
			if (mapShape[1] > x.getShape()[0]) s.setSlice(1, 0, x.getShape()[0], 1);
			fm = ma.getSlice(s);
		} else {
			SliceND s = new SliceND(mapShape);
			int xDim = parent.getxDim();
			int yDim = parent.getyDim();
			int[] xyScanDims = parent.getMapDims().getNonXYScanDimensions();
			
			
			for (int i = 0; i < mapShape.length; i++) {
				if (i == yDim){
					if (mapShape[0] > y.getShape()[0]) s.setSlice(0, 0, y.getShape()[0], 1);
				} else if (i == xDim){
					if (mapShape[1] > x.getShape()[0]) s.setSlice(1, 0, x.getShape()[0], 1);
				} else {
					s.setSlice(i,mapShape[i]-1, mapShape[i], 1);
				}
			}
			
			fm = ma.getSlice(s).squeeze();
			
		}
		
		fm.setMetadata(axm);
		setRange(calculateRange(fm));
//		if (currentSlice == null) {
//		SliceND snd = build(fm);
//		}
		//TODO do something with the current slice
		map  =fm;
	}
}
