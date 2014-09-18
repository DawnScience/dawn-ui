package org.dawnsci.plotting.tools.fitting;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.Vector3dutil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
import uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetadata;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class FittingUtils {

	private static Logger logger = LoggerFactory.getLogger(FittingUtils.class);
	
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
	 * @return
	 */
	public static FittedFunctions getFittedPeaks(final FittedPeaksInfo info) throws Exception {
		
		List<CompositeFunction> composites=null;
		final IOptimizer optimizer = getOptimizer();
		if (info.getNumPeaks()==1) {
			double lowOffset = info.getY().min().doubleValue();
			double highOffset = (Double) info.getY().mean();
			Offset offset = new Offset(lowOffset, highOffset);
			double fwhmApprox = info.getX().peakToPeak().doubleValue()/2.0;
			IdentifiedPeak iniPeak = new IdentifiedPeak(((Number)info.getX().mean()).doubleValue(), info.getX().min().doubleValue(), info.getX().max().doubleValue(), info.getX().peakToPeak().doubleValue()*info.getY().max().doubleValue(), info.getY().max().doubleValue(), 0, info.getX().getSize()-1, Arrays.asList(new Double[] {info.getX().min().doubleValue()+fwhmApprox, info.getX().max().doubleValue()-fwhmApprox}));
			iniPeak.setFWHM(fwhmApprox);
			
			info.setIdentifiedPeaks(Arrays.asList(new IdentifiedPeak[]{iniPeak}));
			
			Constructor<? extends APeak> ctor = getPeakClass().getConstructor(IdentifiedPeak.class);
			APeak localPeak = ctor.newInstance(iniPeak);
			CompositeFunction comp = new CompositeFunction();
			comp.addFunction(localPeak);
			comp.addFunction(offset);
			optimizer.optimize(new Dataset[] { info.getX() }, info.getY(), comp);

			composites = new ArrayList<CompositeFunction>(1);
			composites.add(comp);
		
		} else {
			if (info.getIdentifiedPeaks()==null) {
				info.setIdentifiedPeaks(Generic1DFitter.parseDataDerivative(info.getX(), info.getY(), getSmoothing()));
			}
			composites =  Generic1DFitter.fitPeakFunctions(info.getIdentifiedPeaks(), info.getX(), info.getY(), getPeakClass(), optimizer, getSmoothing(), info.getNumPeaks(), 0.0, false, false,
					info.getMonitor());
		}
		
		if (composites==null || composites.isEmpty()) return null;
				
		final FittedFunctions bean = new FittedFunctions();
		for (CompositeFunction function : composites) {
			
			final IPeak peak = function.getPeak(0);
			if (info.getMonitor().isCancelled()) return null;
			double w = peak.getFWHM();
			final double position = peak.getPosition();
			RectangularROI bounds = new RectangularROI(position - w/2, 0, w, 0, 0);
			
			final Dataset[] pf = getPeakFunction(info.getX(), info.getY(), function);
			
			bean.addFittedPeak(new FittedFunction(function, bounds, pf));

		}
		
		bean.setOptimizer(optimizer);
		if (bean != null && info.getSelectedTrace()!=null)
			for (FittedFunction p : bean.getFunctionList()) {
				p.setX((Dataset)info.getSelectedTrace().getXData());
				p.setY((Dataset)info.getSelectedTrace().getYData());
				p.setDataTrace(info.getSelectedTrace());
				p.setQ(getQ(info, p));
			}
		return bean;
	}

	public static Vector3d getQ(FittedPeaksInfo info,FittedFunction p) {
		LinearROI bounds;
		try {
			
			bounds = (LinearROI)(info.getPlottingSystem().getRegion(info.getSelectedTrace().getName()).getROI());
		} catch (Throwable e1) {
			return null;
		}
		double l=bounds.getLength();
		double [] pt = bounds.getPoint(p.getPosition()/l);
		
		IDiffractionMetadata dmeta = null;
		IDataset set = null;
		final Collection<ITrace> traces = info.getPlottingSystem().getTraces(IImageTrace.class);
		final IImageTrace trace = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
		if (trace!=null) {
			set = trace.getData();
			final IMetadata      meta = ((Dataset)set).getMetadata();
			if (meta instanceof IDiffractionMetadata) {

				dmeta = (IDiffractionMetadata)meta;
			}
		}
		QSpace qSpace  = null;
		Vector3dutil vectorUtil= null;
		if (dmeta != null) {

			try {
				DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
				DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
				
				if (!(detector2dProperties == null)){
					qSpace = new QSpace(detector2dProperties,
							diffractionCrystalEnvironment,1);
									
					vectorUtil = new Vector3dutil(qSpace, pt[0], pt[1]);
					return new Vector3d(vectorUtil.getQx(),vectorUtil.getQy(),vectorUtil.getQz());
				}
			} catch (Exception e) {
				logger.error("Could not create a detector properties object from metadata", e);
			}
		}
		
		return new Vector3d(0,0,0);
	}

	public static List<IdentifiedPeak> getIdentifiedPeaks(final FittedFunctions      fittedPeaks,
			                                              final Dataset  x,
								                          final IProgressMonitor monitor) throws Exception {
		
		if (fittedPeaks==null) return null;
		
		final List<IdentifiedPeak> idpeaks = new ArrayList<IdentifiedPeak>(fittedPeaks.size());
		for (FittedFunction peak : fittedPeaks.getFunctionList()) {
			
			final IPeak       apeak = peak.getPeak();
			
			IdentifiedPeak idp = new IdentifiedPeak( apeak.getPosition(), 
					                                 apeak.getParameter(1).getLowerLimit(), 
					                                 apeak.getParameter(1).getUpperLimit(), 
					                                 apeak.getArea(), 
					                                 apeak.getHeight(), 
					                                 0, 
					                                 x.getSize()-1, 
					                                 null);
			idp.setFWHM(apeak.getFWHM());
			idpeaks.add(idp);
		}
		return idpeaks;
	}
	
	public static int getPeaksRequired() {
		return Activator.getPlottingPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
	}

	/**
	 * 
	 * @param x
	 * @param peak
	 * @return
	 */
	private static Dataset[] getPeakFunction(Dataset x, final Dataset y, CompositeFunction peak) {

//		double min = peak.getPosition() - (peak.getFWHM()); // Quite wide
//		double max = peak.getPosition() + (peak.getFWHM());
//
//		final Dataset[] a = xintersection(x,y,min,max);
//		x=a[0];
//		y=a[1];
		
//		CompositeFunction function = new CompositeFunction();
//		Offset os = new Offset(info.getY().min().doubleValue(), info.getY().max().doubleValue());
//		function.addFunction(peak);
//		function.addFunction(os);
		
		// We make an x dataset with n times the points of the real data to get a smooth
		// fitting function
		final int    factor = Activator.getPlottingPreferenceStore().getInt(FittingConstants.FIT_SMOOTH_FACTOR);
		final double xmin = x.min().doubleValue();
		final double xmax = x.max().doubleValue();
		final double step = (xmax-xmin)/(x.getSize()*factor);
		x = DatasetFactory.createRange(xmin, xmax, step, Dataset.FLOAT64);

		return new Dataset[]{x,peak.calculateValues(x)};
		
	}

    /**
     * TODO
     * @return
     */
	public static int getSmoothing() {
		return Activator.getPlottingPreferenceStore().getInt(FittingConstants.SMOOTHING);
	}

	public static IOptimizer getOptimizer() {
		return new GeneticAlg(getQuality());
	}

	/**
	 * TODO
	 * @return
	 */
	private static double getQuality() {
		return Activator.getPlottingPreferenceStore().getDouble(FittingConstants.QUALITY);
	}

	public static Class<? extends APeak> getPeakClass() {
		try {
			
			final String peakClass = Activator.getPlottingPreferenceStore().getString(FittingConstants.PEAK_TYPE);
			
			/**
			 * Could use reflection to save on objects, but there's only 4 of them.
			 */
			return getPeakOptions().get(peakClass);
			
		} catch (Exception ne) {
			logger.error("Cannot determine peak type required!", ne);
			Activator.getPlottingPreferenceStore().setValue(FittingConstants.PEAK_TYPE, Gaussian.class.getName());
		    return Gaussian.class;
		}
	}	
	
	public static Map<String, Class <? extends APeak>> getPeakOptions() {
		final Map<String, Class <? extends APeak>> opts = new LinkedHashMap<String, Class <? extends APeak>>(4);
		opts.put(Gaussian.class.getName(),    Gaussian.class);
		opts.put(Lorentzian.class.getName(),  Lorentzian.class);
		opts.put(PearsonVII.class.getName(),  PearsonVII.class);
		opts.put(PseudoVoigt.class.getName(), PseudoVoigt.class);
		return opts;
	}
	
	public static int getPolynomialOrderRequired() {
		return Activator.getPlottingPreferenceStore().getInt(FittingConstants.POLY_ORDER);
	}
	
	public static FittedFunctions getFittedPolynomial(final FittedPeaksInfo info) throws Exception {
		
		Polynomial poly = Fitter.polyFit(new Dataset[] {info.getX()}, info.getY(), 1e-8, getPolynomialOrderRequired());
		
		CompositeFunction function = new CompositeFunction();
		function.addFunction(poly);

		final FittedFunctions bean = new FittedFunctions();

		if (info.getMonitor().isCancelled()) return null;
		
		Double max = info.getX().max().doubleValue();
		Double min = info.getX().min().doubleValue();
		
		RectangularROI bounds = new RectangularROI(min,0,max-min,0,0);

		final Dataset[] pf = getPeakFunction(info.getX(), info.getY(), function);

		bean.addFittedPeak(new FittedFunction(function, bounds, pf));

		return bean;
	}
}
