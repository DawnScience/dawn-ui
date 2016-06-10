package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;

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
