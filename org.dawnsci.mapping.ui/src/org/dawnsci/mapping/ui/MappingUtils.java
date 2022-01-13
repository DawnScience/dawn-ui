package org.dawnsci.mapping.ui;

import java.util.List;

import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.XYImagePixelCache;

public class MappingUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(MappingUtils.class);

	public static double[] getGlobalRange(ILazyDataset... datasets) {
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(datasets[0]);
		double[] range = calculateRangeFromAxes(ax);
		
		for (int i = 1; i < datasets.length; i++) {
			double[] r = calculateRangeFromAxes(MetadataPlotUtils.getAxesFromMetadata(datasets[i]));
			range[0]  = r[0] < range[0] ? r[0] : range[0];
			range[1]  = r[1] > range[1] ? r[1] : range[1];
			range[2]  = r[2] < range[2] ? r[2] : range[2];
			range[3]  = r[3] > range[3] ? r[3] : range[3];
		}
		
		return range;
	}
	
	public static double[] getRange(IDataset map, boolean is2D) {
		
		ILazyDataset[] axis = null;
		if (is2D) {
			AxesMetadata m = map.getFirstMetadata(AxesMetadata.class);
			axis = m.getAxes();
		} else {
			AxesMetadata md = map.getFirstMetadata(AxesMetadata.class);
			axis = md.getAxis(0);
		}
		
		IDataset[] ax = getDatasetsFromLazy(axis);
		
		return calculateRangeFromAxes(ax);
		

	}
	
	private static IDataset[] getDatasetsFromLazy(ILazyDataset[] lazys) {
		IDataset[] out = new IDataset[lazys.length];
		
		for (int i = 0; i < lazys.length; i++) {
			try {
				out[i] = DatasetUtils.sliceAndConvertLazyDataset(lazys[i]);
			} catch (DatasetException e) {
				logger.error("Could not slice dataset",e);
			}
		}
		
		return out;
	}
	
	public static double[] calculateRangeFromAxes(IDataset[] axes) {
		double[] range = new double[4];
		int xs = axes[1].getSize();
		int ys = axes[0].getSize();
		
		if (xs == 1) {
			xs = 2;
		}
		
		if (ys == 1) {
			ys = 2;
		}
		
		double fMin = axes[1].min(true).doubleValue();
		double fMax = axes[1].max(true).doubleValue();
		
		range[0] = fMin;
		range[1] = fMax;
		double dx = ((range[1]-range[0])/(xs-1))/2;
		range[0] -= dx;
		range[1] += dx;
		
		double sMin = axes[0].min(true).doubleValue();
		double sMax = axes[0].max(true).doubleValue();
		
		range[2] = sMin;
		range[3] = sMax;
		
		//pad range for extra 0.5 pixel
		
		if (sMin == sMax) {
			range[2] -= dx;
			range[3] += dx;
		} else {
			double dy = ((sMax-sMin)/(ys-1))/2;
			range[2] -= dy;
			range[3] += dy;
		}
		
		return range;
	}
	
	public static int[] getIndicesFromCoOrds(ILazyDataset map, double x, double y, int xDim, int yDim){
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(map);
		
		IDataset xx = ax[xDim];
		IDataset yy = ax[yDim];
		
		double xMin = xx.min(true).doubleValue();
		double xMax = xx.max(true).doubleValue();
		
		double yMin = yy.min(true).doubleValue();
		double yMax = yy.max(true).doubleValue();
		
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
	
	public static int[] getIndicesFromCoOrds(ILazyDataset map, double x, double y){
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(map, false);
		IDataset xx = ax[1];
		IDataset yy = ax[0];
		
		double xMin = xx.min(true).doubleValue();
		double xMax = xx.max(true).doubleValue();
		
		double yMin = yy.min(true).doubleValue();
		double yMax = yy.max(true).doubleValue();
		
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
	
	
	public static IDataset[] remapData(IDataset flatMap, int[] shape, int scanDim){
		if (flatMap == null) return null;
		
		IDataset[] axes = MetadataPlotUtils.getAxesForDimension(flatMap, scanDim);
		
		Dataset y = DatasetUtils.convertToDataset(axes[0]);
		Dataset x = DatasetUtils.convertToDataset(axes[1]);
		
		double yMax = y.max(true).doubleValue();
		double yMin = y.min(true).doubleValue();
		
		double xMax = x.max(true).doubleValue();
		double xMin = x.min(true).doubleValue();
		
		if (shape == null) {
			shape = guessBestShape(x,y);
		}
		
		XYImagePixelCache cache = new XYImagePixelCache(x,y,new double[]{xMin,xMax},new double[]{yMin,yMax},shape[1],shape[0]);
		
		List<Dataset> data = PixelIntegration.integrate(flatMap, null, cache);
		data.get(0).setName(x.getName());
		data.get(2).setName(y.getName());
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			axm.addAxis(0, data.get(2));
			axm.addAxis(1, data.get(0));
		} catch (MetadataException e) {
			logger.error("Could not create axis metadata",e);
		}
		IDataset map = data.get(1);
		map.addMetadata(axm);
		IDataset lookup = data.get(3);
		
		return new IDataset[] {map,lookup};
	}
	
	/**
	 * Use fast and slow axes position to guess
	 * a sensible grid shape for remapping
	 * @param fast
	 * @param slow
	 * @return
	 */
	public static int[] guessBestShape(Dataset fast, Dataset slow) {
		
		int nPoints = fast.getSize();
		
		if (nPoints == 1) {
			return new int[] {1,1};
		}
		
		IndexIterator it = fast.getIterator();
		
		//First check for flattened grid scan by looking for constant
		//values in x or y
		int nXConst = 0;
		int nYConst = 0;

		it.hasNext();
		double last_x = fast.getElementDoubleAbs(it.index);
		double last_y = slow.getElementDoubleAbs(it.index);
		
		while (it.hasNext()) {
			double x = fast.getElementDoubleAbs(it.index);
			double y = slow.getElementDoubleAbs(it.index);
			
			if (x == last_x) {
				nXConst++;
			}
			
			if (y == last_y) {
				nYConst++;
			}
			
			if (x != last_x && y != last_y) {
				break;
			}
			
			last_x = x;
			last_y = y;
		}
		
		if (nXConst != 0) {
			double nY = nXConst + 1;
			return new int[] {(int)nY, (int)Math.ceil(nPoints/nY)};
		}
		
		if (nYConst != 0) {
			double nX = nYConst + 1;
			return new int[] {(int)Math.ceil(nPoints/nX), (int)nX};
		}
		
		//Not a grid, so do our best to guess
		//based on most likely some kind of square-ish scan
		
		double xrange = fast.peakToPeak(true).doubleValue();
		double yrange = slow.peakToPeak(true).doubleValue();
		
		double ratio = xrange/yrange;
		
		//square
		if (ratio == 1) {
			int side = (int)Math.ceil(Math.sqrt(nPoints));
			return new int[] {side,side};
		}
		
		int yOut = (int)Math.ceil(Math.sqrt(nPoints/ratio));
		if (yOut < 1) yOut = 1;
		int xOut = (int)Math.ceil(nPoints/yOut);
		
		if (xOut < 1) xOut = 1;

		//limit to 1000x1000 grid for speed
		if (xOut > 1000) xOut = 1000;
		if (yOut > 1000) yOut = 1000;
 		
		return new int[]{yOut,xOut};
	}
	
	public static void saveRegisteredImage(PlottableMapObject image, String path, INexusFileFactory fileFactory) {
		
		NexusFile nexus = fileFactory.newNexusFile(path);
		try {
			nexus.openToWrite(true);
			GroupNode group = nexus.getGroup("/entry", true);
			nexus.addAttribute(group, TreeFactory.createAttribute("NX_class","NXentry"));
			group = nexus.getGroup("/entry/registered_image", true);
			nexus.addAttribute(group, TreeFactory.createAttribute("NX_class","NXdata"));
			IDataset data = image.getMap().getSliceView();
			AxesMetadata axm = data.getFirstMetadata(AxesMetadata.class);
			data.setName("data");
			DataNode dNode = nexus.createData(group, data);
			nexus.addAttribute(dNode, TreeFactory.createAttribute("interpretation","rgb-image"));
			
			IDataset y = axm.getAxis(0)[0].getSlice().squeeze();
			y.setName(MetadataPlotUtils.removeSquareBrackets(y.getName()));
			nexus.createData(group, y);
			IDataset x = axm.getAxis(1)[0].getSlice().squeeze();
			x.setName(MetadataPlotUtils.removeSquareBrackets(x.getName()));
			nexus.createData(group, x);
			
			nexus.addAttribute(group, TreeFactory.createAttribute("signal","data"));
			nexus.addAttribute(group, TreeFactory.createAttribute("axes",new String[]{y.getName(),x.getName(), "."}));
		} catch (DatasetException de) {
			logger.error("Could not slice dataset", de);
		} catch (NexusException e) {
			logger.error("Error writing nexus file", e);
		} finally {
			if (nexus != null)
				try {
					nexus.close();
				} catch (NexusException e) {
					logger.error("Error closing file", e);
				}
		}
	}
	
	public  static int getSqueezedRank(long[] maxShape) {
		int r = 0;
		
		for (long i : maxShape) if (i != 1) r++;
		
		//no zero ranked map scans.
		if (r == 0) return 1;
		
		return r;
	}

	public static String getShortName(String name) {

		long count = name.chars().filter(ch -> ch == Node.SEPARATOR.charAt(0)).count();

		if (count < 2) {
			return name;
		} else {
			int lastIndexOf = name.lastIndexOf(Node.SEPARATOR);
			int last2 = name.lastIndexOf(Node.SEPARATOR,  lastIndexOf-1);
			return name.substring(last2+1, lastIndexOf);
		}
	}
}
