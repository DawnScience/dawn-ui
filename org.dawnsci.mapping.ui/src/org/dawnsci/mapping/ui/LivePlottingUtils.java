package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivePlottingUtils {

	private static final Logger logger = LoggerFactory.getLogger(LivePlottingUtils.class);
	
	
	public static IDataset getUpdatedMap(IDynamicDataset baseMap, MappedDataBlock parent, String name){


		try{
			baseMap.refreshShape();

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

			y = cropNanValues(y);
			x = cropNanValues(x);

			AxesMetadata axm = null;
			try {
				axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
				axm.addAxis(0, y);
				axm.addAxis(1, x);
			} catch (MetadataException | IllegalArgumentException e) {
				logger.error("Could not create axes metdata", e);
			}

			IDataset fm = null;
			int[] mapShape = baseMap.getDataset().getShape();
			//may need to only squeeze fastest
			int[] squeezedShape = ShapeUtils.squeezeShape(mapShape, false);

			if (squeezedShape.length == 2) {
				SliceND s = new SliceND(mapShape);
				if (mapShape[0] > y.getShape()[0]) s.setSlice(0, 0, y.getShape()[0], 1);
				if (mapShape[1] > x.getShape()[0]) s.setSlice(1, 0, x.getShape()[0], 1);
				fm = baseMap.getDataset().getSlice(s);
				fm.squeeze();
			} else {

				MapScanDimensions mapDims = parent.getMapDims();

				SliceND s = new SliceND(mapShape);
				int xDim = parent.getxDim();
				int yDim = parent.getyDim();

				for (int i = 0; i < mapShape.length; i++) {
					if (i == yDim){
						if (mapShape[i] > y.getShape()[0]) s.setSlice(i, 0, y.getShape()[0], 1);
					} else if (i == xDim){
						if (mapShape[i] > x.getShape()[0]) s.setSlice(i, 0, x.getShape()[0], 1);
					} else {
						s.setSlice(i,mapShape[i]-1, mapShape[i], 1);
						mapDims.updateNonXYScanSlice(i, mapShape[i]-1);
					}
				}

				fm = baseMap.getDataset().getSlice(s).squeeze();
				if (fm.getRank() != 2) return null;

			}

			fm.setMetadata(axm);
			fm.setName(name);
			//take a slice to cut larger axes
			return fm.getSlice();
		} catch (Exception e) {
			logger.info("could not slice map",e);
			//TODO log?
		}

		return null;
	}
	
	private static IDataset cropNanValues(IDataset ax) {
		Dataset x = DatasetUtils.convertToDataset(ax);
		int i = 0;
		boolean found = false;
		for (; i < x.getSize(); i++) {
			double val = x.getElementDoubleAbs(i);
			if (Double.isNaN(val)) {
				found = true;
				break;
			}
		}
		
		if (!found) return ax;
		
		SliceND s = new SliceND(ax.getShape());
		s.setSlice(0, 0, i, 1);
		
		return ax.getSlice(s);
	}
	
	public static IDataset cropNanValuesFromAxes(IDataset map, boolean is2D) {
		SliceND slice = new SliceND(map.getShape());
		
		AxesMetadata axes = map.getFirstMetadata(AxesMetadata.class);
		
		IDataset y = null;
		IDataset x = null;
		ILazyDataset[] laa = null;
		
		if (is2D) {
			laa = axes.getAxes();
		}else {
			laa = axes.getAxis(0);
		}
			
			
		try {
			y = laa[0].getSlice();
			x = laa[1].getSlice();
		} catch (DatasetException e) {
			return null;
		}
		
		
		int firstNanY = findFirstNan(y);
		int firstNanX = findFirstNan(x);
		
		if (is2D) {
			slice.setSlice(0, 0, firstNanY, 1);
			slice.setSlice(1, 0, firstNanX, 1);
		} else {
			slice.setSlice(0, 0, Math.min(firstNanY, firstNanX), 1);
		}
		
		
		if (slice.isAll()) return map;
		
		return map.getSlice(slice);

	}
	
	private static int findFirstNan(IDataset ax) {
		
		DoubleDataset cast = DatasetUtils.cast(DoubleDataset.class, ax);
		cast.squeeze();
		for (int i = 0; i < cast.getSize(); i++){
			if (Double.isNaN(cast.getAbs(i))) {
				return i;
			}
		}
		
		return cast.getSize();
		
		
	}
	
	
	
	public static IDataset getUpdatedLinearMap(IDynamicDataset baseMap, MappedDataBlock parent, String name) {
		
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

