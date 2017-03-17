/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Vector3d;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.Vector3dutil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

public class FittingUtils {

	private static Logger logger = LoggerFactory.getLogger(FittingUtils.class);
	
	
	
	public static Add getInitialPeaks(Dataset xDataSet, Dataset yDataSet, Integer nPeaks, Class<? extends IPeak> peakClass) {
		
		Add initialPeaks = new Add();
		
		//Set variables for peak finding and fitting
		//-this has to be a list of composite functions unless we change Generic1DFitter
		List<CompositeFunction> fittedPeaksAndBkgs;
		IOptimizer optimizer = getOptimizer();
		int smoothing = getSmoothing();
		//Check user variables are defined
		Class<? extends IPeak> peakFunction = peakClass;
		if (peakFunction == null) {
				peakFunction = getPeakClass();
		}
	
		//We need to find things that look like peaks in the data
		List<IdentifiedPeak> foundPeaks = Generic1DFitter.parseDataDerivative(xDataSet, yDataSet, smoothing);
		

		//How many peaks are we looking for (user specified, can be null)
		Integer nrPeaks = nPeaks;
		if (nrPeaks == null) {
			
			nrPeaks = foundPeaks.size();
			if (nrPeaks == null || nrPeaks == 0) {
				//In case no peaks were found
				logger.error("No peaks were found!");
				return null;
			}
		}
		
		//Fit the peaks we found
		fittedPeaksAndBkgs = Generic1DFitter.fitPeakFunctions(foundPeaks, xDataSet, yDataSet, peakFunction, optimizer, smoothing, nrPeaks,  0.0, false, false, null, true);
		
		if (fittedPeaksAndBkgs == null)
			return null;
		//Pick out the peak functions of the correct class & package into new composite function
		for (IOperator peakAndBkg : fittedPeaksAndBkgs) {
			for (IFunction partFunction : peakAndBkg.getFunctions()) {
				if (partFunction.getClass() == peakFunction) {
					initialPeaks.addFunction(partFunction);
				}
			}
		}
		
		return initialPeaks;
	}
	
	
	//Fit the peaks we found
	//TODO: place inside fitting utils
	//TODO: just default IPeak to gaussian 
	/**
	 * A variate that takes a already fittin set of found peaks
	 * 
	 * Number of peaks fitted based on the found peaks size	
	 * 
	 * @param foundPeaks
	 * @param xDataSet
	 * @param yDataSet
	 * @param peakClass
	 * @return
	 */
	public static Add getSeededPeakFit(List<IdentifiedPeak> foundPeaks, Dataset xDataSet, Dataset yDataSet, Class<? extends IPeak> peakClass) {
		
		Add initialPeaks = new Add();
		
		//Set variables for peak finding and fitting
		//-this has to be a list of composite functions unless we change Generic1DFitter
		List<CompositeFunction> fittedPeaksAndBkgs;
		
		//TODO: these are set before in the fitting utils section
		IOptimizer optimizer = getOptimizer();
		
		int smoothing = getSmoothing();
		
		//Check user variables are defined
		Class<? extends IPeak> peakFunction = peakClass;
		if (peakFunction == null) {
				peakFunction = getPeakClass();
		}
		
		//How many peaks are we looking for (user specified, can be null)
		Integer nrPeaks = foundPeaks.size() - 1;
		if (nrPeaks == null || nrPeaks == 0) {
			//In case no peaks were found
			logger.error("Trying to fit on no peaks found!");
			return null;
		}
		

		//Intial fit against the peaks against the foundPeaks
		fittedPeaksAndBkgs = Generic1DFitter.fitPeakFunctions(foundPeaks, xDataSet, yDataSet, peakFunction, optimizer, smoothing, nrPeaks,  0.0, false, false, null, true);
		
		if (fittedPeaksAndBkgs == null)
			return null;
		
		//Pick out the peak functions of the correct class & package into new composite function
		for (IOperator peakAndBkg : fittedPeaksAndBkgs) {
			for (IFunction partFunction : peakAndBkg.getFunctions()) {
				if (partFunction.getClass() == peakFunction) {
					initialPeaks.addFunction(partFunction);
				}
			}
		}
		
		return initialPeaks;
	} 
	
	
	
//	public static DataSet getTraceRemainder(DataSet xDataSet, DataSet yDataSet, Add, initialPeakFuncs) {
//		
//	}
	
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
			IdentifiedPeak iniPeak = new IdentifiedPeak(((Number)info.getX().mean()).doubleValue(),
					info.getX().min().doubleValue(), info.getX().max().doubleValue(),
					info.getX().peakToPeak().doubleValue()*info.getY().peakToPeak().doubleValue(),
					info.getY().max().doubleValue(),
					0, info.getX().getSize()-1,
					Arrays.asList(new Double[] {info.getX().min().doubleValue()+fwhmApprox,
							info.getX().max().doubleValue()-fwhmApprox}));
			iniPeak.setFWHM(fwhmApprox);
			
			info.setIdentifiedPeaks(Arrays.asList(new IdentifiedPeak[]{iniPeak}));
			
			Constructor<? extends IPeak> ctor = getPeakClass().getConstructor(IdentifiedPeak.class);
			IPeak localPeak = ctor.newInstance(iniPeak);
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
			Class<? extends IPeak> myPeak = getPeakClass();
			composites =  Generic1DFitter.fitPeakFunctions(info.getIdentifiedPeaks(), info.getX(), info.getY(), myPeak, optimizer, getSmoothing(), info.getNumPeaks(), 0.0, false, false,
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
				p.setX(DatasetUtils.convertToDataset(info.getSelectedTrace().getXData()));
				p.setY(DatasetUtils.convertToDataset(info.getSelectedTrace().getYData()));
				p.setDataTrace(info.getSelectedTrace());
				p.setQ(getQ(info, p));
			}
		return bean;
	}

	public static Vector3d getQ(FittedPeaksInfo info,FittedFunction p) {
		IRegion region = info.getPlottingSystem().getRegion(info.getSelectedTrace().getName());
		if (region == null)
			return null;

		LinearROI bounds = (LinearROI) region.getROI();
		double l=bounds.getLength();
		double [] pt = bounds.getPoint(p.getPosition()/l);
		
		IDiffractionMetadata dmeta = null;
		IDataset set = null;
		final Collection<ITrace> traces = info.getPlottingSystem().getTraces(IImageTrace.class);
		final IImageTrace trace = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
		if (trace!=null) {
			set = trace.getData();
			final IMetadata      meta = set.getMetadata();
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

	public static Class<? extends IPeak> getPeakClass() {
		String peakClassName = Activator.getPlottingPreferenceStore().getString(FittingConstants.PEAK_TYPE);
		Class<? extends IPeak> peakClass = null;
		try {
			peakClass = FunctionFactory.getPeakFunctionClass(peakClassName);
		} catch (Exception ne) {
			peakClassName = "Gaussian";
			Activator.getPlottingPreferenceStore().setValue(FittingConstants.PEAK_TYPE, peakClassName);
			try {
				peakClass = FunctionFactory.getPeakFunctionClass(peakClassName);
			} catch (Exception ne2){
				logger.error("Fallback Gaussian peak type was not found by FunctionFactory.");
			}
		}
		return peakClass;
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
