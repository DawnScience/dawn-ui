package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.region.RegionBounds;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.IAnalysisMonitor;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

public class FittingUtils {

	
	/**
	 * This method runs a fit on data passed in. It reads preferences to 
	 * determine which algorithm etc. the user prefers. 
	 * 
	 * It can take a while to run. Callers of these utils should protect the
	 * call by running it from a Job. This method is UI thread safe and may be called
	 * in any thread.
	 * 
	 * The x and y data may have been sliced if the user has completed a selection choice
	 * which limits the peak fit range.
	 * 
	 * The RegionBounds returns are not computed in y. Instead the peak fitter provides the
	 * y values as zero. So XAXIS regions should be created, then Y data is not required.
	 * 
	 * The method also computes x,y abstract data set pairs which are plot fragments for the
	 * function plotted over the peak region. These can be plotted to show the user the
	 * fitted function, which they may wish to adjust.
	 * 
	 * @param x
	 * @param y
	 * @param monitor
	 * @return
	 */
	public static FittedPeaksBean getFittedPeaks(final AbstractDataset  x, 
			                                     final AbstractDataset  y,
			                                     final IProgressMonitor monitor) {
				
		final IOptimizer optimizer = getOptimizer();
		final List<APeak> peaks =  Generic1DFitter.fitPeaks(x, y, getFunction(), optimizer, getSmoothing(), getPeaksRequired(), 0.0, false, false, new IAnalysisMonitor() {
			@Override
			public boolean hasBeenCancelled() {
				return monitor.isCanceled(); // We always use the monitor.isCancelled() the fitting can take a while
				                             // and should always allow stopping.
			}
		});
		
		if (peaks==null || peaks.isEmpty()) return null;
		
		final List<RegionBounds>      regions   = new ArrayList<RegionBounds>(peaks.size());
		final List<AbstractDataset[]> functions = new ArrayList<AbstractDataset[]>(peaks.size());
		
		for (APeak peak : peaks) {
			
			if (monitor.isCanceled()) return null;
			
			RegionBounds bounds = new RegionBounds();
			bounds.setP1(new double[]{peak.getPosition()-(peak.getFWHM()/2d), 0});
			bounds.setP2(new double[]{peak.getPosition()+(peak.getFWHM()/2d), 0});
			bounds.setCentre(new double[]{peak.getPosition(), 0});
			regions.add(bounds);
			
			final AbstractDataset[] pf = getPeakFunction(x, y, peak);
			functions.add(pf);
		}
		
		final FittedPeaksBean bean = new FittedPeaksBean();
		bean.setPeakBounds(regions);
		bean.setPeakFunctions(peaks);
		bean.setFunctionData(functions);
		bean.setOptimizer(optimizer);
		return bean;
	}
	
	private static int getPeaksRequired() {
		return 4;
	}

	/**
	 * 
	 * @param x
	 * @param peak
	 * @return
	 */
	private static AbstractDataset[] getPeakFunction(AbstractDataset x, AbstractDataset y, APeak peak) {

		double min = peak.getPosition() - (peak.getFWHM()/2d);
		double max = peak.getPosition() + (peak.getFWHM()/2d);

		final AbstractDataset[] a = xintersection(x,y,min,max);
		x=a[0];
		y=a[1];
		
		CompositeFunction function = new CompositeFunction();
		Offset os = new Offset(y.min().doubleValue(), y.max().doubleValue());
		function.addFunction(peak);
		function.addFunction(os);

		return new AbstractDataset[]{x,function.makeDataset(x)};
		
	}


	private static int getSmoothing() {
		return 1;
	}

	private static IOptimizer getOptimizer() {
		return new GeneticAlg(0.01d);
	}

	private static APeak getFunction() {
		return new Gaussian(1, 1, 1, 1);
	}

	/**
	 * Slices x and y using the x as the reference.
	 * @param x
	 * @param y - may be null
	 * @param startValue
	 * @param endValue
	 * @return x and y sliced to the startValue and endValue
	 */
	public static AbstractDataset[] xintersection(AbstractDataset x,
			                                      AbstractDataset y, 
			                                      final double startValue, 
			                                      final double endValue) {
		
		List<Double> cross = DatasetUtils.crossings(x, startValue);		
		final int    start = cross==null || cross.isEmpty() 
				           ? 0
				           : (int)Math.floor(cross.get(0)); // Lower value
		
		cross = DatasetUtils.crossings(x, endValue);		
		final int    stop  =  cross==null || cross.isEmpty() 
				           ? x.getSize()-1
				           : (int)Math.ceil(cross.get(cross.size()-1)); // Upper value
		
		x = x.getSlice(new int[] { start }, new int[] { stop }, null);
		if (y!=null) y = y.getSlice(new int[] { start }, new int[] { stop }, null);		
		
		return (y!=null) ? new AbstractDataset[]{x,y} : new AbstractDataset[]{x};
	}
}
