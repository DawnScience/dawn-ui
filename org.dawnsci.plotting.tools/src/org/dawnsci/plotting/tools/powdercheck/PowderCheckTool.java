package org.dawnsci.plotting.tools.powdercheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.dawb.common.ui.image.IconUtils;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.tools.powdercheck.PowderCheckJob.PowderCheckMode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.FFT;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

@SuppressWarnings("unused")
public class PowderCheckTool extends AbstractToolPage {
	
	
	private final static Logger logger = LoggerFactory.getLogger(PowderCheckTool.class);

	private static final double REL_TOL = 1e-10;
	private static final double ABS_TOL = 1e-10;
	private static final int MAX_EVAL = 100000;
	
	IPlottingSystem system;
	PowderCheckJob updatePlotJob;
	SashForm sashForm;

	private ITraceListener            traceListener;
	private CalibrantSelectedListener calListener;
	
	public PowderCheckTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		this.calListener = new CalibrantSelectedListener() {		
			@Override
			public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
				updatePlotJob.updateCalibrantLines();
			}
		};
		
		// TODO Listen to other things.
		this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void traceAdded(TraceEvent evt) {
				PowderCheckTool.this.update();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				PowderCheckTool.this.update();
			}

		};
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		sashForm = new SashForm(parent, SWT.VERTICAL);

		createActions();
		
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;

		system.createPlotPart(sashForm, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		system.getSelectedYAxis().setAxisAutoscaleTight(true);
		
		getPlottingSystem().addTraceListener(traceListener);
				
		Button test = new Button(sashForm, SWT.NONE);
		test.setText("Hello");
	
		sashForm.setMaximizedControl(system.getPlotComposite());

	}
	
	
	private void update() {

		IImageTrace im = getImageTrace();
		if (im == null) {
			cleanPlottingSystem();
			return;
		}
		
		final AbstractDataset ds = (AbstractDataset)im.getData();
		if (ds==null) return;
			
		final IMetaData       m  = ds.getMetadata();

		if (m == null || !(m instanceof IDiffractionMetadata)) {
			//TODO nicer error
			logger.error("No Diffraction Metadata");
			return;
		}
		
		if (updatePlotJob == null) {
			updatePlotJob= new PowderCheckJob(system);
		}
		
		updatePlotJob.cancel();
		updatePlotJob.setData(ds, (IDiffractionMetadata)m);
		updatePlotJob.schedule();
		
	}
	
	private void createActions() {
		
		final MenuAction modeSelect = new MenuAction("Select Check Mode");
		
		final Action fullImage = new Action("Full Image") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.FullImage);
				updatePlotJob.schedule();
			}
		};
		fullImage.setToolTipText("Integrate the entire image, showing lines at calibrant positions");
		
		modeSelect.add(fullImage);
		modeSelect.setSelectedAction(fullImage);
		
		final Action quad = new Action("Quadrants") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.Quadrants);
				updatePlotJob.schedule();
			}
		};
		
		quad.setToolTipText("Integrate the 4 quadrants, showing lines at calibrant positions");
		
		modeSelect.add(quad);
		
		final Action peakfit = new Action("Peak fit") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.PeakFit);
				updatePlotJob.schedule();
			}
		};
		peakfit.setToolTipText("Integrate the entire image, peak fit, and compare with calibrant positions");
		modeSelect.add(peakfit);
		
		getSite().getActionBars().getToolBarManager().add(modeSelect);
		getSite().getActionBars().getMenuManager().add(modeSelect);
		
	}
	
	private void cleanPlottingSystem(){
		if (system != null) {
			system.reset();
		}
	}
	
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		
		CalibrationFactory.addCalibrantSelectionListener(calListener);
		getPlottingSystem().addTraceListener(traceListener);
		boolean wasActive = isActive();
		super.activate();
		if (!wasActive) update();		
	}

	@Override
	public void deactivate() {
		
		if (updatePlotJob != null) updatePlotJob.cancel();
		
		CalibrationFactory.removeCalibrantSelectionListener(calListener);
		getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
		cleanPlottingSystem();
	}
	
	@Override
	public void dispose() {
		deactivate();
		if (system!=null) system.dispose();
		system = null;
		super.dispose();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system;
		} else if (clazz == IPlottingSystem.class) {
		    return system;
		} else {
			return super.getAdapter(clazz);
		}
	}
	
//	private void updateCalibrantLines() {
//		
//		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
//		final double[] qVals = new double[spacings.size()];
//		
//		for (int i = 0 ; i < spacings.size(); i++) {
//			qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
//		}
//
//		Display.getDefault().syncExec(new Runnable() {
//
//			@Override
//			public void run() {
//
//				if (system.getPlotComposite() == null) return;
//				
//				IAxis ax = system.getSelectedXAxis();
//
//				double low = ax.getLower();
//				double up = ax.getUpper();
//
//				for (IRegion r : system.getRegions()) system.removeRegion(r);
//				for (int i = 0; i < qVals.length; i++) {
//					if (qVals[i] < low || qVals[i] > up) continue;
//
//					try {
//						RectangularROI roi = new RectangularROI(qVals[i], 0, 1, 1, 0);
//						IRegion reg = system.getRegion("Q value: " + qVals[i]);
//						if (reg!=null) system.removeRegion(reg);
//						
//						final IRegion area = system.createRegion("Q value: " + qVals[i], RegionType.XAXIS_LINE);
//						area.setROI(roi);
//						area.setRegionColor(ColorConstants.gray);
//						area.setUserRegion(false);
//						system.addRegion(area);
//						area.setMobile(false);
//					} catch (Exception e) {
//						logger.error("Region is already there", e);
//					}
//
//				}
//			}
//		});
//	}
	
	@Override
	public Control getControl() {
		return sashForm;
		//if (system != null) return system.getPlotComposite();
		//return null;
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();

	}

//	private class UpdatePlotJob extends Job {
//		
//		AbstractDataset dataset;
//		IDiffractionMetadata metadata;
//
//		public UpdatePlotJob() {
//			super("Integrate image and plot");
//			// TODO Auto-generated constructor stub
//		}
//		
//		private void setData(AbstractDataset ds, IDiffractionMetadata md) {
//			dataset = ds;
//			metadata = md;
//		}
//
//		@Override
//		protected IStatus run(IProgressMonitor monitor) {
//			if (system.getPlotComposite()==null) return Status.CANCEL_STATUS;
//			cleanPlottingSystem();
//			return integrateQuadrants(dataset,metadata, monitor);
//
//		}
//		
//		private IStatus integrateQuadrants(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
//			QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
//			double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
//			int[] shape = data.getShape();
//			
//			double[] farCorner = new double[]{0,0};
//			double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
//			if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
//			if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
//			double maxDistance = Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
//			SectorROI sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, Math.PI/4 - Math.PI/8, Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
//			AbstractDataset[] profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);
//			
//			ArrayList<IDataset> y = new ArrayList<IDataset> ();
//			profile[0].setName("Bottom right");
//			y.add(profile[0]);
//			if (system == null) {
//				logger.error("Plotting system is null");
//				return Status.CANCEL_STATUS;
//			}
//				
//			List<ITrace> traces = system.updatePlot1D(profile[4], y, null);
//			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkBlue);
//			y.remove(0);
//			
//			final AbstractDataset reflection = profile[2];
//			final AbstractDataset axref = profile[6];
//			reflection.setName("Top left");
//			y.add(reflection);
//			traces = system.updatePlot1D(axref, y, null);
//			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightBlue);
//			y.remove(0);
//			
//			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
//			
//			sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, 3*Math.PI/4 - Math.PI/8, 3*Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
//			profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);
//			
//			profile[0].setName("Bottom left");
//			y.add(profile[0]);
//			traces = system.updatePlot1D(profile[4], y, null);
//			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkGreen);
//			y.remove(0);
//			
//			final AbstractDataset reflection2 = profile[2];
//			final AbstractDataset axref2 = profile[6];
//			reflection2.setName("Top right");
//			y.add(reflection2);
//			traces = system.updatePlot1D(axref2, y, null);
//			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightGreen);
//			updateCalibrantLines();
//			
//			return Status.OK_STATUS;
//		}
//		
//		private IStatus integrateFullSector(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
//			QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
//			double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
//			int[] shape = data.getShape();
//			double[] farCorner = new double[]{0,0};
//			double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
//			if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
//			if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
//			
//			int maxDistance = (int)Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
//			NonPixelSplittingIntegration npsi = new NonPixelSplittingIntegration(qSpace, maxDistance);
//			
//			List<AbstractDataset> out = npsi.value(data);
//			
//			AbstractDataset baseline = rollingBallBaselineCorrection(out.get(1), 10);
//			
//			system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
//			
//			List<PeakResult> result = fitPeaksToTrace(out.get(0),Maths.subtract(out.get(1), baseline), baseline);
//			
//			double maxRatio = 0;
//			
//			for (PeakResult r : result) {
//				double q = r.q;
//				double qExp = r.peak.getParameter(0).getValue();
//				double ratio = q/qExp;
//				if (ratio > 1) ratio = 1/ratio;
//				
//				ratio = 1-ratio;
//				
//				if (ratio > maxRatio) maxRatio = ratio;
//				
//			}
//			
//			logger.debug("Max ratio = " + maxRatio);
//			
//			return Status.OK_STATUS;
//		}
//		
//		private static final int EDGE_PIXEL_NUMBER = 10;
//		
//		private List<PeakResult> fitPeaksToTrace(final AbstractDataset xIn, final AbstractDataset yIn, AbstractDataset baselineIn) {
//			
//			List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
//			final double[] qVals = new double[spacings.size()];
//			
//			for (int i = 0 ; i < spacings.size(); i++) {
//				qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
//			}
//			
//			double qMax = xIn.max().doubleValue();
//			double qMin = xIn.min().doubleValue();
//			
//			List<Double> qList = new ArrayList<Double>();
//			
//			int count = 0;
//			
//			for (double q : qVals) {
//				if (q > qMax || q < qMin) continue;
//				count++;
//				qList.add(q);
//			}
//			
//			double minPeak = Collections.min(qList);
//			double maxPeak = Collections.max(qList);
//			
//			int minXidx = ROISliceUtils.findPositionOfClosestValueInAxis(xIn, minPeak) - EDGE_PIXEL_NUMBER;
//			int maxXidx = ROISliceUtils.findPositionOfClosestValueInAxis(xIn, maxPeak) + EDGE_PIXEL_NUMBER;
//			
//			int maxSize = xIn.getSize();
//			
//			minXidx = minXidx < 0 ? 0 : minXidx;
//			maxXidx = maxXidx > maxSize-1 ? maxSize-1 : maxXidx;
//			
//			final AbstractDataset x = xIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
//			final AbstractDataset y = yIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
//			y.setName("Fit");
//			AbstractDataset baseline = baselineIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
//			
//			
//			//DatasetUtils.f
//			
//			
//			
//			List<APeak> peaks = Generic1DFitter.fitPeaks(x, y, Gaussian.class, count);
//			
//			final CompositeFunction cf = new CompositeFunction();
//			
//			for (APeak peak : peaks) cf.addFunction(peak);
//			
//			
//			double[] initParam = new double[cf.getFunctions().length*3];
//			
//			{
//				int i = 0;
//				for (IFunction func : cf.getFunctions()) {
//					initParam[i++] = func.getParameter(0).getValue();
//					initParam[i++] = func.getParameter(1).getValue();
//					initParam[i++] = func.getParameter(2).getValue();
//				}
//			}
//			
//			final AbstractDataset yfit = AbstractDataset.zeros(x, AbstractDataset.FLOAT64);
//			
//			 MultivariateOptimizer opt = new SimplexOptimizer(REL_TOL,ABS_TOL);
//			    
//			    MultivariateFunction fun = new MultivariateFunction() {
//					
//					@Override
//					public double value(double[] arg0) {
//						
//						int j = 0;
//						for (IFunction func : cf.getFunctions()) {
//							
//							double[] p = func.getParameterValues();
//							p[0] = arg0[j++];
//							p[1] = arg0[j++];
//							p[2] = arg0[j++];
//							func.setParameterValues(p);
//						}
//						
//						for (int i = 0 ; i < yfit.getSize() ; i++) {
//							yfit.set(cf.val(x.getDouble(i)), i);
//						}
//						
//						double test = y.residual(yfit);
//						
//						return y.residual(yfit);
//					}
//				};
//			    
//				PointValuePair result = opt.optimize(new InitialGuess(initParam), GoalType.MINIMIZE,
//						new ObjectiveFunction(fun), new MaxEval(MAX_EVAL),
//						new NelderMeadSimplex(initParam.length));	
//				
//			
//			List<PeakResult> resultList = new ArrayList<PeakResult>();
//			
//			system.updatePlot1D(x, Arrays.asList(new IDataset[]{Maths.add(yfit, baseline)}), null);
//			
//			
//			while (cf.getNoOfFunctions() != 0 || !qList.isEmpty()) findMatches(resultList, qList, cf);
//			
//			return resultList;
//				
//		}
//		
//		private void findMatches(List<PeakResult> results, List<Double> qList, CompositeFunction cf) {
//			
//			double minVal = Double.POSITIVE_INFINITY;
//			int minFuncIdx = 0;
//			int minQIdx = 0;
//			
//			for (int i = 0; i <  cf.getNoOfFunctions(); i++) {
//				for (int j = 0; j < qList.size(); j++) {
//					double a = Math.abs(qList.get(j) - cf.getFunction(i).getParameter(0).getValue());
//					
//					if (a < minVal) {
//						minVal = a;
//						minFuncIdx = i;
//						minQIdx = j;
//					}
//				}
//			}
//			
//			results.add(new PeakResult(cf.getFunction(minFuncIdx), qList.get(minQIdx)));
//			cf.removeFunction(minFuncIdx);
//			qList.remove(minQIdx);
//		}
//		
//		private AbstractDataset rollingBallBaselineCorrection(AbstractDataset y, int width) {
//			
//			AbstractDataset t1 = AbstractDataset.zeros(y);
//			AbstractDataset t2 = AbstractDataset.zeros(y);
//			
//			for (int i = 0 ; i < y.getSize()-1; i++) {
//				int start = (i-width) < 0 ? 0 : (i - width);
//				int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
//				double val = y.getSlice(new int[]{start}, new int[]{end}, null).min().doubleValue();
//				t1.set(val, i);
//			}
//			
//			for (int i = 0 ; i < y.getSize()-1; i++) {
//				int start = (i-width) < 0 ? 0 : (i - width);
//				int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
//				double val = t1.getSlice(new int[]{start}, new int[]{end}, null).max().doubleValue();
//				t2.set(val, i);
//			}
//			
//			for (int i = 0 ; i < y.getSize()-1; i++) {
//				int start = (i-width) < 0 ? 0 : (i - width);
//				int end = (i+width) > (y.getSize()-1) ? (y.getSize()-1) : (i+width);
//				double val = (Double) t2.getSlice(new int[]{start}, new int[]{end}, null).mean();
//				t1.set(val, i);
//			}
//			
//			return t1;
//		}
//
//	}
//	
//	class PeakResult {
//		public IFunction peak;
//		public double q;
//		
//		public PeakResult(IFunction peak, double q) {
//			this.peak = peak;
//			this.q = q;
//		}
//	}
	
}
