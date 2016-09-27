package org.dawnsci.mapping.ui.datamodel;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.XYImagePixelCache;

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
		if (flatMap == null) return;
		IDataset[] axes = MetadataPlotUtils.getAxesForDimension(flatMap, 0);
		Dataset y = DatasetUtils.convertToDataset(axes[0]);
		Dataset x = DatasetUtils.convertToDataset(axes[1]);
		
		double yMax = y.max().doubleValue();
		double yMin = y.min().doubleValue();
		
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		
		
		
//		shape = new int[]{20,20};
//		double test = yMax - yMin;
		if (shape == null) {
			
			this.shape = shape = guessBestShapeShape(x,y);
			
//			double yStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(y.getSize(),Dataset.INT32),y,1)));
//			double xStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(x.getSize(),Dataset.INT32),x,1)));
//			
//			yStepMed = yStepMed == 0 ? 1 : yStepMed;
//			xStepMed = xStepMed == 0 ? 1 : xStepMed;
//			
//			int nBinsY = (int)(((yMax-yMin)/yStepMed));
//			int nBinsX = (int)(((xMax-xMin)/xStepMed));
//			
//			this.shape = shape = new int[]{nBinsX, nBinsY};
		}
		
		XYImagePixelCache cache = new XYImagePixelCache(x,y,new double[]{xMin,xMax},new double[]{yMin,yMax},shape[0],shape[1]);
		
		List<Dataset> data = PixelIntegration.integrate(flatMap, null, cache);
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			axm.addAxis(0, data.get(2));
			axm.addAxis(1, data.get(0));
		} catch (MetadataException e) {
			e.printStackTrace();
		}
		map = data.get(1);
		map.addMetadata(axm);
		lookup = data.get(3);
		
	}
	
	private int[] guessBestShapeShape(Dataset xCoord, Dataset yCoord) {
		
		IndexIterator it = xCoord.getIterator();
		
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		
		int nPoints = 0;
		
		while (it.hasNext()) {
			double x = xCoord.getElementDoubleAbs(it.index);
			double y = yCoord.getElementDoubleAbs(it.index);

			maxX = Math.max(x,maxX);
			maxY = Math.max(y,maxY);

			minX = Math.min(x,minX);
			minY = Math.min(y,minY);

			nPoints++;
		}
		
		double xrange = Math.abs(maxX - minX);
		double yrange = Math.abs(maxY -minY);
		
		double ratio = xrange/yrange;
		
		
		
		int yOut = (int)Math.ceil(Math.sqrt(nPoints/ratio));
		if (yOut < 1) yOut = 1;
		int xOut = (int)Math.ceil(nPoints/yOut);
		
		if (xOut < 1) xOut = 1;

		
		if (xOut > 1000) xOut = 1000;
		if (yOut > 1000) yOut = 1000;
 		
//		System.out.println(Arrays.toString(new int[]{xOut,yOut}));
		
		return new int[]{xOut,yOut};
//		double yStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(y.getSize(),Dataset.INT32),y,1)));
//		double xStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(x.getSize(),Dataset.INT32),x,1)));
//		
//		yStepMed = yStepMed == 0 ? 1 : yStepMed;
//		xStepMed = xStepMed == 0 ? 1 : xStepMed;
//		
//		int nBinsY = (int)(((yMax-yMin)/yStepMed));
//		int nBinsX = (int)(((xMax-xMin)/xStepMed));
//		
//		nBinsX = 10;
//		nBinsY = 10;
//		
//		return new int[]{nBinsX, nBinsY};
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
		
		((IDatasetConnector)ly).refreshShape();
		((IDatasetConnector)lx).refreshShape();
		
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

		int[] mapShape = ma.getShape();
		SliceND s = new SliceND(mapShape);
		int maxShape = Math.min(y.getShape()[0], y.getShape()[0]);
		maxShape = Math.min(maxShape, mapShape[0]);
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			axm.addAxis(0, y.getSlice(s));
			axm.addAxis(0, x.getSlice(s));
		} catch (MetadataException e) {
			logger.error("Could not create axes metdata", e);
		}
		
		s.setSlice(0, 0, y.getShape()[0], 1);
		IDataset fm = ma.getSlice(s);
		fm.setMetadata(axm);
		setRange(calculateRange(fm));
		flatMap = fm;
		updateRemappedData(null);
		
	}
}
