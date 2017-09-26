package org.dawnsci.datavis.model;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotDataModifierStack implements IPlotDataModifier {

	private double value = 0;
	private double proportion = 0.2;
	private double[] xRange = null;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		
		AxesMetadata md = d.getFirstMetadata(AxesMetadata.class);
		
		if (xRange  != null) {
			if (md != null && md.getAxes()!= null && md.getAxes()[0] != null) {
				try {
					IDataset axis = md.getAxes()[0].getSlice();
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
					
				} catch (DatasetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		
		double delta = max-min;
		if (delta == 0) delta = 1;
		Dataset dataset = DatasetUtils.convertToDataset(d);
//		dataset = Maths.subtract(dataset, min).idivide(delta);
//		dataset.iadd(value*proportion);
		
		dataset = Maths.add(dataset,(value-min));
		double peakToPeak = dataset.peakToPeak(true).doubleValue();
		peakToPeak = (max+value-min)-value;
		value += peakToPeak*proportion;
		
//		dataset = Maths.subtract(dataset, min).idivide(delta);
//		dataset.iadd(value*proportion);
		
//		value++;
		
		
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
		return rank == 1;
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

}
