package org.dawnsci.mapping.ui;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
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
}
