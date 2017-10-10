/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powdercheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.Stats;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.baseline.BaselineGeneration;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class PowderCheckJob extends Job {

	
	public enum PowderCheckMode {
		FullImage,Quadrants,PeakFit,Cake;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(PowderCheckJob.class);
	
	IPlottingSystem<?> system;
	Dataset dataset;
	IDiffractionMetadata metadata;
	List<PowderCheckResult> resultList;
	XAxis xAxis = XAxis.ANGLE;
	PowderCheckMode mode = PowderCheckMode.FullImage;

	public PowderCheckJob(IPlottingSystem<?> system) {
		super("Checking calibration...");
		this.system = system;
		resultList = new ArrayList<PowderCheckResult>();
	}

	public void setData(Dataset dataset, IDiffractionMetadata metadata) {
		this.dataset = dataset;
		this.metadata = metadata;
	}

	public void setCheckMode(PowderCheckMode mode) {
		this.mode = mode;
	}
	
	public void setAxisMode(XAxis xAxis) {
		this.xAxis = xAxis;
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
		system.setEnabled(false);
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
		case Cake:
			integrateCake(dataset, metadata, monitor);
		default:
			break;
		}
		system.setEnabled(true);
		return Status.OK_STATUS;
	}
	
	private IStatus integrateCake(Dataset data, IDiffractionMetadata md, IProgressMonitor monitor) {

		int[] shape = data.getShape();
		double[] farCorner = new double[]{0,0};
		double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
		if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
		if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];

		int maxDistance = (int)Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
		PixelSplittingIntegration2D npsi = new PixelSplittingIntegration2D(md, maxDistance,maxDistance);
		npsi.setAxisType(xAxis);

		List<Dataset> out = npsi.integrate(data);

		system.updatePlot2D(out.remove(1), out, monitor);
		setPlottingSystemAxes();
		
		return Status.OK_STATUS;
		
	}

	private IStatus integrateQuadrants(Dataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		
		int nBins = 36;
		NonPixelSplittingIntegration2D npsi = new NonPixelSplittingIntegration2D(md);
		npsi.setAxisType(xAxis);
		npsi.setAzimuthalRange(new double[]{-180,180});
		npsi.setNumberOfAzimuthalBins(nBins);
		List<Dataset> out = npsi.integrate(data);
		List<IDataset> ys = new ArrayList<IDataset>();
		
		Dataset result = out.get(1);
		SliceND slice = new SliceND(result.getShape());
		
		for (int i = 0; i < 36; i++) {
			slice.setSlice(0, i, i+1, 1);
			Dataset sv = result.getSliceView(slice);
			sv = sv.squeeze();
			sv.setName("Line: " + i*10 +" to " + (i*10+10));
			ys.add(sv);
		}
		
//		for (int i = -180; i <= 170; i+=10) {
//			npsi.setAzimuthalRange(new double[]{i, i+10});
//			List<Dataset> out = npsi.integrate(data);
//			out.get(1).setName("Line: " + i +" to " + (i+10));
//			system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
//		}

		system.updatePlot1D(out.get(0), ys, null);
		setPlottingSystemAxes();
		
		setPlottingSystemAxes();
		updateCalibrantLines();

		return Status.OK_STATUS;
	}

	private IStatus integrateFullSectorAndShowLines(Dataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		integrateFullSector(data, md, monitor);
		updateCalibrantLines();
		return Status.OK_STATUS;
	}
	
	private List<Dataset> integrateFullSector(Dataset data, IDiffractionMetadata md, IProgressMonitor monitor) {

		NonPixelSplittingIntegration npsi = new NonPixelSplittingIntegration(md);
		npsi.setAxisType(xAxis);
		
		List<Dataset> out = npsi.integrate(data);

		system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
		setPlottingSystemAxes();
		
		return out;
	}
	
	private IStatus integrateFullSectorPeakFit(Dataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
		
		List<Dataset> out =  integrateFullSector(data, md, monitor);
		
		Dataset baseline = BaselineGeneration.rollingBallBaseline(out.get(1), 10);
		
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

		return Status.OK_STATUS;
	}

	private static final int EDGE_PIXEL_NUMBER = 10;

	private List<PowderCheckResult> fitPeaksToTrace(final Dataset xIn, final Dataset yIn, Dataset baselineIn) {

		resultList.clear();
		
		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
		final double[] qVals = new double[spacings.size()];

		for (int i = 0 ; i < spacings.size(); i++) {
			if (xAxis == XAxis.ANGLE) qVals[i] = 2*Math.toDegrees(Math.asin((metadata.getDiffractionCrystalEnvironment().getWavelength() / (2*spacings.get(i).getDNano()*10))));
			else qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
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

		final Dataset x = xIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
		final Dataset y = yIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);
		y.setName("Fit");
		Dataset baseline = baselineIn.getSlice(new int[] {minXidx}, new int[] {maxXidx}, null);

		List<IPeak> peaks = Generic1DFitter.fitPeaks(x, y, Gaussian.class, count+10);
		
		List<PowderCheckResult> initResults = new ArrayList<PowderCheckResult>();
		
		CompositeFunction cf = new CompositeFunction();

		for (IPeak peak : peaks) cf.addFunction(peak);
		
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

		IOptimizer optimizer = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT);
		try {
			optimizer.optimize(new IDataset[]{x}, y, cfFinal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Dataset yfit = cfFinal.calculateValues(new IDataset[]{x});
		
		Dataset fit = Maths.add(yfit, baseline);
		fit.setName("Fit");
		Dataset residual = Maths.subtract(y,yfit);
		residual.setName("Residual");
		
		system.updatePlot1D(x, Arrays.asList(new IDataset[]{fit,residual}) , null);
		setPlottingSystemAxes();
		for (int i = 0; i < cfFinal.getNoOfFunctions(); i++) {
			resultList.add(new PowderCheckResult(cfFinal.getFunction(i), initResults.get(i).getCalibrantQValue()));
		}

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
		
		Dataset dd = DatasetFactory.createFromObject(minDif);
		
		double med = (Double)Stats.median(dd);
		double mad = (Double)Stats.median(Maths.abs(Maths.subtract(dd, med)));
		
		return mad*5;
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
	
	private void cleanPlottingSystem(){
		if (system != null) {
			system.reset();
		}
	}
	
	private void setPlottingSystemAxes(){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				IAxis selectedYAxis = system.getSelectedYAxis();
				selectedYAxis.setTitle("Intensity");
				
				IAxis selectedXAxis = system.getSelectedXAxis();
				
				if (xAxis == XAxis.Q) selectedXAxis.setTitle("Q");
				else selectedXAxis.setTitle("2 Theta");
			}
		});
	}
	
	public void updateCalibrantLines() {

		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
		final double[] qVals = new double[spacings.size()];

		for (int i = 0 ; i < spacings.size(); i++) {
			
			
			if (xAxis == XAxis.ANGLE) qVals[i] = 2*Math.toDegrees(Math.asin((metadata.getDiffractionCrystalEnvironment().getWavelength() / (2*spacings.get(i).getDNano()*10))));
			else qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
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

