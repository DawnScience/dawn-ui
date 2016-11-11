package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivePlottingUtils {

	private static final Logger logger = LoggerFactory.getLogger(LivePlottingUtils.class);
	
	
	public static IDataset getUpdatedMap(IDatasetConnector baseMap, MappedDataBlock parent, String name){
		
		IDataset ma = null;
		
		try{
			baseMap.refreshShape();
			ma = baseMap.getDataset().getSlice();
		} catch (Exception e) {
			//TODO log?
		}
		
		if (ma == null) return null;
		
		ma.setName(name);
		
		if (parent.isTransposed()) ma = DatasetUtils.convertToDataset(ma).transpose();
		
		// TODO This check is probably not required
		if ( baseMap instanceof ILazyDataset && ((ILazyDataset)baseMap).getSize() == 1) return null;
		
		ILazyDataset ly = parent.getYAxis()[0];
		ILazyDataset lx = parent.getXAxis()[0];
		
		IDataset x;
		IDataset y;
		try {
			x = lx.getSlice();
			y = ly.getSlice();
		} catch (DatasetException e) {
			logger.debug("Could not slice",e);
			return null;
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
			
			MapScanDimensions mapDims = parent.getMapDims();
			
			SliceND s = new SliceND(mapShape);
			int xDim = parent.getxDim();
			int yDim = parent.getyDim();
			
			for (int i = 0; i < mapShape.length; i++) {
				if (i == yDim){
					if (mapShape[0] > y.getShape()[0]) s.setSlice(0, 0, y.getShape()[0], 1);
				} else if (i == xDim){
					if (mapShape[1] > x.getShape()[0]) s.setSlice(1, 0, x.getShape()[0], 1);
				} else {
					s.setSlice(i,mapShape[i]-1, mapShape[i], 1);
					mapDims.updateNonXYScanSlice(i, mapShape[i]-1);
				}
			}
			
			fm = ma.getSlice(s).squeeze();
			if (fm.getRank() != 2) return null;
			
		}
		
		fm.setMetadata(axm);
		
		return fm;
	}
	
	
	public static IDataset getUpdatedLinearMap(IDatasetConnector baseMap, MappedDataBlock parent, String name) {
		
		IDataset map = null;
		
		try{
			baseMap.refreshShape();
			map = baseMap.getDataset().getSlice();
		} catch (Exception e) {
			//TODO log?
		}
		
		if (map == null) return null;
		
		map.setName(name);
		
		if (parent.isTransposed()) map = DatasetUtils.convertToDataset(map).transpose();
		
		// TODO This check is probably not required
		if ( baseMap instanceof ILazyDataset && ((ILazyDataset)baseMap).getSize() == 1) return null;
		
		ILazyDataset ly = parent.getYAxis()[0];
		ILazyDataset lx = parent.getXAxis()[0];
	
		
		IDataset x;
		IDataset y;
		try {
			x = lx.getSlice();
			y = ly.getSlice();
		} catch (DatasetException e) {
			logger.debug("Could not slice",e);
			return null;
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

		int[] mapShape = map.getShape();
		
		
		
		if (mapShape.length != 1) {
			MapScanDimensions mapDims = parent.getMapDims();
			SliceND s = new SliceND(map.getShape());
			int xDim = parent.getxDim();

			for (int i = 0; i < mapShape.length; i++) {
				if (i != xDim){
					s.setSlice(i,mapShape[i]-1, mapShape[i], 1);
					mapDims.updateNonXYScanSlice(i, mapShape[i]-1);
				}
			}

			map = map.getSlice(s).squeeze();
			if (map.getRank() != 1) return null;
			mapShape = map.getShape();
		}
		
		IDataset fm = null;

		SliceND s = new SliceND(mapShape);
		int maxShape = Math.min(y.getShape()[0], x.getShape()[0]);
		maxShape = Math.min(maxShape, mapShape[0]);
		s.setSlice(0, 0, y.getShape()[0], 1);
		fm = map.getSlice(s);
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			axm.addAxis(0, y.getSlice(s));
			axm.addAxis(0, x.getSlice(s));
		} catch (MetadataException e) {
			logger.error("Could not create axes metdata", e);
		}


		fm.setMetadata(axm);
		
		return fm;
	}
	
}

