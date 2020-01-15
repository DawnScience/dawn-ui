package org.dawnsci.datavis.model;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotDataModifierStack implements IPlotDataModifier {
	
	private static final Logger logger = LoggerFactory.getLogger(PlotDataModifierStack.class);

	private double value = 0;
	private double proportion = 0.2;
	private double[] xRange = null;
	private boolean normalise = false;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		
		AxesMetadata md = d.getFirstMetadata(AxesMetadata.class);
		
		if (xRange  != null) {

			IDataset axis = null;

			if (md != null && md.getAxes()!= null && md.getAxes()[0] != null) {
				try {
					axis = md.getAxes()[0].getSlice();
				} catch (DatasetException e) {
					logger.error("Could not slice axes",e);
				}
			}

			if (axis == null) {
				axis = DatasetFactory.createRange(d.getSize());
			}

			int tmpMin = ROISliceUtils.findPositionOfClosestValueInAxis(axis, xRange[0]);
			int tmpMax = ROISliceUtils.findPositionOfClosestValueInAxis(axis, xRange[1]);
			SliceND s = new SliceND(d.getShape());
			if (tmpMin > tmpMax) {
				int tmp = tmpMin;
				tmpMin = tmpMax;
				tmpMax = tmp;
			}
			s.setSlice(0, tmpMin, tmpMax, 1);

			IDataset crop = d.getSlice(s);

			min = crop.min(true).doubleValue();
			max = crop.max().doubleValue();

		} 

		Dataset dataset = DatasetUtils.convertToDataset(d);
		String name = dataset.getName();

		if (normalise) {
			dataset = Maths.subtract(dataset, min).idivide(max-min);
			max = 1;
			min = 0;
		}

		dataset = Maths.add(dataset,(value-min));
		dataset.setName(name);
		double peakToPeak = (max+value-min)-value;
		value += peakToPeak*proportion;

		if (md != null) {
			dataset.addMetadata(md);
		}

		return dataset;
	}

	@Override
	public void init() {
		value = 0;
	}

	@Override
	public String getName() {
		return "Stack";
	}

	@Override
	public boolean supportsRank(int rank) {
		return rank == 1 || rank == 0;
	}

	public double getProportion() {
		return proportion;
	}

	public void setProportion(double proportion) {
		this.proportion = proportion;
	}

	@Override
	public void configure(IPlottingSystem<?> system) {
		IAxis x = system.getSelectedXAxis();
		if (x == null) {
			xRange = null;
		}
		
		xRange = new double[2];
		xRange[0] = x.getLower();
		xRange[1] = x.getUpper();
		Arrays.sort(xRange);
		
	}

	public boolean isNormalise() {
		return normalise;
	}

	public void setNormalise(boolean normalise) {
		this.normalise = normalise;
	}

}
