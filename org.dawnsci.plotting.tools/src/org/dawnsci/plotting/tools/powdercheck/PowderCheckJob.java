package org.dawnsci.plotting.tools.powdercheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

public class PowderCheckJob extends Job {

	
	public enum PowderCheckMode {
		FullImage,Quadrants,PeakFit;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(PowderCheckJob.class);
	private static final double REL_TOL = 1e-10;
	private static final double ABS_TOL = 1e-10;
	private static final int MAX_EVAL = 100000;
	
	IPlottingSystem system;
	AbstractDataset dataset;
	IDiffractionMetadata metadata;
	List<PowderCheckResult> resultList;
	
	PowderCheckMode mode = PowderCheckMode.FullImage;

	public PowderCheckJob(IPlottingSystem system) {
		super("Checking calibration...");
		this.system = system;
		resultList = new ArrayList<PowderCheckResult>();
	}

	public void setData(AbstractDataset dataset, IDiffractionMetadata metadata) {
		this.dataset = dataset;
		this.metadata = metadata;
	}

	public void setCheckMode(PowderCheckMode mode) {
		this.mode = mode;
	}
	
	public List<PowderCheckResult> getResultsList() {
		List<PowderCheckResult> out = new ArrayList<PowderCheckResult>(this.resultList.size());
		out.addAll(this.resultList);
		return out;
	}
	

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (system.getPlotComposite()==null) return Status.CANCEL_STATUS;
		cleanPlottingSystem();
		
		switch (mode) {
		case FullImage:
			integrateFullSectorAndShowLines(dataset, metadata, monitor);
			break;
		case PeakFit:
			integrateFullSectorPeakFit(dataset, metadata, monitor);
			break;
		case Quadrants:
			integrateQuadrants(dataset,metadata, monitor);
			break;
		default:
			break;
		}
		
		return Status.OK_STATUS;
	}

	private IStatus integrateQuadrants(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
		double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
		int[] shape = data.getShape();

		double[] farCorner = new double[]{0,0};
		double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
		if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
		if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
		double maxDistance = Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
		SectorROI sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, Math.PI/4 - Math.PI/8, Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
		AbstractDataset[] profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);

		ArrayList<IDataset> y = new ArrayList<IDataset> ();
		profile[0].setName("Bottom right");
		y.add(profile[0]);
		if (system == null) {
			logger.error("Plotting system is null");
			return Status.CANCEL_STATUS;
		}

		List<ITrace> traces = system.updatePlot1D(profile[4], y, null);
		//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkBlue);
		y.remove(0);

		final AbstractDataset reflection = profile[2];
		final AbstractDataset axref = profile[6];
		reflection.setName("Top left");
		y.add(reflection);
		traces = system.updatePlot1D(axref, y, null);
		//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightBlue);
		y.remove(0);

		if (monitor.isCanceled()) return Status.CANCEL_STATUS;

		sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, 3*Math.PI/4 - Math.PI/8, 3*Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
		profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);

		profile[0].setName("Bottom left");
		y.add(profile[0]);
		traces = system.updatePlot1D(profile[4], y, null);
		//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkGreen);
		y.remove(0);

		final AbstractDataset reflection2 = profile[2];
		final AbstractDataset axref2 = profile[6];
		reflection2.setName("Top right");
		y.add(reflection2);
		traces = system.updatePlot1D(axref2, y, null);
		//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightGreen);
		updateCalibrantLines();

		return Status.OK_STATUS;
	}

	private IStatus integrateFullSectorAndShowLines(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		integrateFullSector(data, md, monitor);
		updateCalibrantLines();
		return Status.OK_STATUS;
	}
	
	private List<AbstractDataset> integrateFullSector(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
		double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
		int[] shape = data.getShape();
		double[] farCorner = new double[]{0,0};
		double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
		if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
		if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];

		int maxDistance = (int)Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
		NonPixelSplittingIntegration npsi = new NonPixelSplittingIntegration(qSpace, maxDistance);

		List<AbstractDataset> out = npsi.value(data);

		system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
		
		return out;
	}
	
	private IStatus integrateFullSectorPeakFit(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
//		QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
//		double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
//		int[] shape = data.getShape();
//		double[] farCorner = new double[]{0,0};
//		double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
//		if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
//		if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
//
//		int maxDistance = (int)Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
//		NonPixelSplittingIntegration npsi = new NonPixelSplittingIntegration(qSpace, maxDistance);
//
//		List<AbstractDataset> out = npsi.value(data);
//
//		system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
		
		List<AbstractDataset> out =  integrateFullSector(data, md, monitor);
		
		AbstractDataset baseline = rollingBallBaselineCorrection(out.get(1), 10);
		
		List<PowderCheckResult> result = fitPeaksToTrace(out.get(0),Maths.subtract(out.get(1), baseline), baseline);

		double maxRatio = 0;

		for (PowderCheckResult r : result) {
			double q = r.getCalibrantQValue();
			double qExp = r.getPeak().getParameter(0).getValue();
			double ratio = q/qExp;
			if (ratio > 1) ratio = 1/ratio;

			ratio = 1-ratio;

			if (ratio > maxRatio) maxRatio = ratio;

		}

		logger.debug("Max ratio = " + maxRatio);

		return Status.OK_STATUS;
	}

	private static final int EDGE_PIXEL_NUMBER = 10;

	private List<PowderCheckResult> fitPeaksToTrace(final AbstractDataset xIn, final AbstractDataset yIn, AbstractDataset baselineIn) {

		resultList.clear();
		
		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
		final double[] qVals = new double[spacings.size()];

		for (int i = 0 ; i < spacings.size(); i++) {
			qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
		}

		double qMax = xIn.max().doubleValue();
		double qMin = xIn.min().doubleValue();

		List<Double> qList = new ArrayList<Double>();

		int count = 0;

		for (double q : qVals) {
			if (q > qMax || q < qMin) continue;
			count++;
			qList.add(q);
		}

		double minPeak = Collections.min(qList);
		double maxPeak = Collections.max(qList);

		int minXidx = ROISliceUtils.findPositionOfClosestValueInAxis(xIn, minPeak) - EDGE_PIXEL_NUMBER;
		int maxXidx = ROISliceUtils.findPositionOfClosestValueInAxis(xIn, maxPeak) + EDGE_PIXEL_NUMBER;

		int maxSize = xIn.getSize();

		minXidx = minXidx < 0 ? 0 : minXidx;
		maxXidx = maxXidx > maxSize-1 ? maxSize-1 : maxXidx;

		final AbstractDataset x = xIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
		final AbstractDataset y = yIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
		y.setName("Fit");
		AbstractDataset baseline = baselineIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);

		List<APeak> peaks = Generic1DFitter.fitPeaks(x, y, Gaussian.class, count+10);

		
		List<PowderCheckResult> initResults = new ArrayList<PowderCheckResult>();
		
		CompositeFunction cf = new CompositeFunction();

		for (APeak peak : peaks) cf.addFunction(peak);
		
		double limit = findMatchLimit(qList, cf);
		
		while (cf.getNoOfFunctions() != 0 && !qList.isEmpty()) findMatches(initResults, qList, cf, limit);
		
		final CompositeFunction cfFinal = compositeFunctionFromResults(initResults);
		
		double[] initParam = new double[cfFinal.getFunctions().length*3];

		{
			int i = 0;
			for (IFunction func : cfFinal.getFunctions()) {
				initParam[i++] = func.getParameter(0).getValue();
				initParam[i++] = func.getParameter(1).getValue();
				initParam[i++] = func.getParameter(2).getValue();
			}
		}

		final AbstractDataset yfit = AbstractDataset.zeros(x, AbstractDataset.FLOAT64);

		MultivariateOptimizer opt = new SimplexOptimizer(REL_TOL,ABS_TOL);

		MultivariateFunction fun = new MultivariateFunction() {

			@Override
			public double value(double[] arg0) {

				int j = 0;
				for (IFunction func : cfFinal.getFunctions()) {

					double[] p = func.getParameterValues();
					p[0] = arg0[j++];
					p[1] = arg0[j++];
					p[2] = arg0[j++];
					func.setParameterValues(p);
				}

				for (int i = 0 ; i < yfit.getSize() ; i++) {
					yfit.set(cfFinal.val(x.getDouble(i)), i);
				}

				double test = y.residual(yfit);

				return y.residual(yfit);
			}
		};

		PointValuePair result = opt.optimize(new InitialGuess(initParam), GoalType.MINIMIZE,
				new ObjectiveFunction(fun), new MaxEval(MAX_EVAL),
				new NelderMeadSimplex(initParam.length));	

		system.updatePlot1D(x, Arrays.asList(new IDataset[]{Maths.add(yfit, baseline)}), null);

		for (int i = 0; i < cfFinal.getNoOfFunctions(); i++) {
			resultList.add(new PowderCheckResult(cfFinal.getFunction(i), initResults.get(i).getCalibrantQValue()));
		}
		
		
		//while (cf.getNoOfFunctions() != 0 || !qList.isEmpty()) findMatches(resultList, qList, cf);

		return resultList;

	}
	
	private CompositeFunction compositeFunctionFromResults(List<PowderCheckResult> initialResults) {
		
		CompositeFunction cf = new CompositeFunction();
		
		for (PowderCheckResult r : initialResults) {
			cf.addFunction(r.getPeak());
		}
		
		return cf;
		
	}

	private double findMatchLimit(List<Double> qList, CompositeFunction cf) {
		
		double[] minDif = new double[cf.getNoOfFunctions()];
		
		for (int i = 0; i <  cf.getNoOfFunctions(); i++) {
			double minVal = Double.POSITIVE_INFINITY;
			for (int j = 0; j < qList.size(); j++) {
				double a = Math.abs(qList.get(j) - cf.getFunction(i).getParameter(0).getValue());

				if (a < minVal) {
					minVal = a;
					minDif[i] = a;
				}
			}
		}
		
		DoubleDataset dd = new DoubleDataset(minDif, new int[]{minDif.length});
		
		double med = (Double)Stats.median(dd);
		double mad = (Double)Stats.median(Maths.abs(Maths.subtract(dd, med)));
		
		return mad*10;
	}
	
	private void findMatches(List<PowderCheckResult> results, List<Double> qList, CompositeFunction cf, double limit) {

		double minVal = Double.POSITIVE_INFINITY;
		int minFuncIdx = 0;
		int minQIdx = 0;

		for (int i = 0; i <  cf.getNoOfFunctions(); i++) {
			for (int j = 0; j < qList.size(); j++) {
				double a = Math.abs(qList.get(j) - cf.getFunction(i).getParameter(0).getValue());

				if (a < minVal) {
					minVal = a;
					minFuncIdx = i;
					minQIdx = j;
				}
			}
		}

		if (minVal > limit) {
			qList.clear();
			return;
		}
		
		results.add(new PowderCheckResult(cf.getFunction(minFuncIdx), qList.get(minQIdx)));
		cf.removeFunction(minFuncIdx);
		qList.remove(minQIdx);
	}

	private AbstractDataset rollingBallBaselineCorrection(AbstractDataset y, int width) {

		AbstractDataset t1 = AbstractDataset.zeros(y);
		AbstractDataset t2 = AbstractDataset.zeros(y);

		for (int i = 0 ; i < y.getSize()-1; i++) {
			int start = (i-width) < 0 ? 0 : (i - width);
			int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
			double val = y.getSlice(new int[]{start}, new int[]{end}, null).min().doubleValue();
			t1.set(val, i);
		}

		for (int i = 0 ; i < y.getSize()-1; i++) {
			int start = (i-width) < 0 ? 0 : (i - width);
			int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
			double val = t1.getSlice(new int[]{start}, new int[]{end}, null).max().doubleValue();
			t2.set(val, i);
		}

		for (int i = 0 ; i < y.getSize()-1; i++) {
			int start = (i-width) < 0 ? 0 : (i - width);
			int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
			double val = (Double) t2.getSlice(new int[]{start}, new int[]{end}, null).mean();
			t1.set(val, i);
		}

		return t1;
	}
	
	private void cleanPlottingSystem(){
		if (system != null) {
			system.reset();
		}
	}
	
	public void updateCalibrantLines() {

		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
		final double[] qVals = new double[spacings.size()];

		for (int i = 0 ; i < spacings.size(); i++) {
			qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
		}

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {

				if (system.getPlotComposite() == null) return;

				IAxis ax = system.getSelectedXAxis();

				double low = ax.getLower();
				double up = ax.getUpper();

				for (IRegion r : system.getRegions()) system.removeRegion(r);
				for (int i = 0; i < qVals.length; i++) {
					if (qVals[i] < low || qVals[i] > up) continue;

					try {
						RectangularROI roi = new RectangularROI(qVals[i], 0, 1, 1, 0);
						IRegion reg = system.getRegion("Q value: " + qVals[i]);
						if (reg!=null) system.removeRegion(reg);
						
						final IRegion area = system.createRegion("Q value: " + qVals[i], RegionType.XAXIS_LINE);
						area.setROI(roi);
						area.setRegionColor(ColorConstants.gray);
						area.setUserRegion(false);
						system.addRegion(area);
						area.setMobile(false);
					} catch (Exception e) {
						logger.error("Region is already there", e);
					}

				}
			}
		});
	}
}

