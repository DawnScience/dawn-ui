package org.dawnsci.mapping.ui;

import java.util.List;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
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
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.XYImagePixelCache;

public class MappingUtils {

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
	
	private static double[] calculateRangeFromAxes(IDataset[] axes) {
		double[] range = new double[4];
		int xs = axes[1].getSize();
		int ys = axes[0].getSize();
		range[0] = axes[1].min().doubleValue();
		range[1] = axes[1].max().doubleValue();
		double dx = ((range[1]-range[0])/xs)/2;
		range[0] -= dx;
		range[1] += dx;
		
		range[2] = axes[0].min().doubleValue();
		range[3] = axes[0].max().doubleValue();
		double dy = ((range[3]-range[2])/ys)/2;
		range[2] -= dy;
		range[3] += dy;
		return range;
	}
	
	public static int[] getIndicesFromCoOrds(IDataset map, double x, double y){
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
	
	
	public static IDataset[] remapData(IDataset flatMap, int[] shape, int scanDim){
		if (flatMap == null) return null;
		IDataset[] axes = MetadataPlotUtils.getAxesForDimension(flatMap, scanDim);
		Dataset y = DatasetUtils.convertToDataset(axes[0]);
		Dataset x = DatasetUtils.convertToDataset(axes[1]);
		
		double yMax = y.max().doubleValue();
		double yMin = y.min().doubleValue();
		
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		
		if (shape == null) {
			shape = guessBestShapeShape(x,y);
		}
		
		XYImagePixelCache cache = new XYImagePixelCache(x,y,new double[]{xMin,xMax},new double[]{yMin,yMax},shape[0],shape[1]);
		
		List<Dataset> data = PixelIntegration.integrate(flatMap, null, cache);
		data.get(0).setName(x.getName());
		data.get(2).setName(y.getName());
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			axm.addAxis(0, data.get(2));
			axm.addAxis(1, data.get(0));
		} catch (MetadataException e) {
			e.printStackTrace();
		}
		IDataset map = data.get(1);
		map.addMetadata(axm);
		IDataset lookup = data.get(3);
		
		return new IDataset[] {map,lookup};
	}
	
	private static int[] guessBestShapeShape(Dataset xCoord, Dataset yCoord) {
		
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
	
	public static void saveRegisteredImage(AssociatedImage image, String path) {
		NexusFile nexus = LocalServiceManager.getNexusFactory().newNexusFile(path);
		try {
			nexus.openToWrite(true);
			GroupNode group = nexus.getGroup("/entry", true);
			nexus.addAttribute(group, new AttributeImpl("NX_class","NXentry"));
			group = nexus.getGroup("/entry/registered_image", true);
			nexus.addAttribute(group, new AttributeImpl("NX_class","NXdata"));
			IDataset data = image.getData().getSliceView();
			AxesMetadata axm = data.getFirstMetadata(AxesMetadata.class);
			if (data instanceof RGBDataset) {
				data = DatasetUtils.createDatasetFromCompoundDataset((RGBDataset)data, false);
				if (data.getShape().length == 3 && data.getShape()[2] == 3) {
					data = data.getTransposedView(new int[]{2,0,1}).getSlice();
				}
			}
			data.setName("data");
			DataNode dNode = nexus.createData(group, data);
			nexus.addAttribute(dNode, new AttributeImpl("interpretation","rgba-image"));
			
			IDataset y = axm.getAxis(0)[0].getSlice().squeeze();
			y.setName("y");
			nexus.createData(group, y);
			IDataset x = axm.getAxis(1)[0].getSlice().squeeze();
			x.setName("x");
			nexus.createData(group, x);
			
			nexus.addAttribute(group, new AttributeImpl("signal","data"));
			nexus.addAttribute(group, new AttributeImpl("axes",new String[]{".","y","x"}));
		} catch (DatasetException de) {
			de.printStackTrace();
		} catch (NexusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (nexus != null)
				try {
					nexus.close();
				} catch (NexusException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
