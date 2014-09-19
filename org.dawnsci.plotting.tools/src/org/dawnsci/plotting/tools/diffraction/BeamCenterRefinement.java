/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.diffraction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.Well19937a;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

/**
 * Class for optimising sector region position to increase base on 1D radial profile
 */
public class BeamCenterRefinement implements MultivariateFunction {

	private IProgressMonitor monitor;
	private ArrayList<IPeak> initPeaks;

	private Dataset dataset, mask;
	private SectorROI sroi;
	private XAxis axis;

	private int cmaesLambda = 15;
	private double[] cmaesInputSigma = new double[] { 3.0, 3.0 };
	private int cmaesMaxIterations = 10000;
	private int cmaesCheckFeasableCount = 10;
	private ConvergenceChecker<PointValuePair> cmaesChecker = new SimplePointChecker<PointValuePair>(1e-3, 1e-4);

	private static final Logger logger = LoggerFactory.getLogger(BeamCenterRefinement.class);

	public BeamCenterRefinement(Dataset dataset, Dataset mask, SectorROI sroi) {
		super();
		this.dataset = dataset;
		this.mask = mask;
		this.sroi = sroi;
		this.axis = XAxis.Q;	// TODO: set this values from Radial Profile and/or Peak Fitting tools 
	}

	/**
	 * Method for setting CMA-ES optimiser parameters.
	 * 
	 * @see <a
	 *      href="http://commons.apache.org/math/apidocs/org/apache/commons/math3/optimization/direct/CMAESOptimizer.html">Apache
	 *      Commons Math CMAESOptimiser class</a>
	 * @param cmaesLambda
	 *            Population size
	 * @param cmaesInputSigma
	 *            Initial search volume
	 * @param cmaesMaxIterations
	 *            Maximal number of iterations
	 * @param cmaesCheckFeasableCount
	 *            Number of times new random objective variables are generated when they are out of bounds
	 * @param cmaesChecker
	 *            Convergence checker
	 */
	public void configureOptimizer(Integer cmaesLambda, double[] cmaesInputSigma, Integer cmaesMaxIterations,
			Integer cmaesCheckFeasableCount, ConvergenceChecker<PointValuePair> cmaesChecker) {
		if (cmaesLambda != null)
			this.cmaesLambda = cmaesLambda;
		if (cmaesInputSigma != null)
			this.cmaesInputSigma = cmaesInputSigma;
		if (cmaesMaxIterations != null)
			this.cmaesMaxIterations = cmaesMaxIterations;
		if (cmaesCheckFeasableCount != null)
			this.cmaesCheckFeasableCount = cmaesCheckFeasableCount;
		if (cmaesChecker != null)
			this.cmaesChecker = cmaesChecker;
	}

	/**
	 * Method for setting initial positions of peaks on radial profiles that will be used in optimisation process.
	 * 
	 * @param initPeaks
	 *            List of peaks on radial profile
	 */
	public void setInitPeaks(List<IPeak> initPeaks) {
		this.initPeaks = new ArrayList<IPeak>(initPeaks);
	}

	private void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Method that calculates penalty function for a given sector region position. This function makes Gaussian profile
	 * fit to every peak included in optimisation process and calculates penalty function based on I/sigma values of the
	 * fitted peaks.
	 * 
	 * @param beamxy
	 *            Position of sector region origin
	 * @return Sum of log(1 + I/sigma) values for all selected peaks
	 */
	@Override
	public double value(final double[] beamxy) {

		if (monitor.isCanceled()) {
			return Double.NaN;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				((IDiffractionMetadata) dataset.getMetadata()).getDetector2DProperties().setBeamCentreCoords(
						beamxy);
			}
		});
		SectorROI tmpRoi = new SectorROI(sroi.getPointX(), sroi.getPointY(), sroi.getRadius(0), sroi.getRadius(1), sroi.getAngle(0),
				sroi.getAngle(1), 1.0, true, sroi.getSymmetry());
		QSpace qSpace = null;
		IMetadata metadata = dataset.getMetadata();
		if (metadata instanceof IDiffractionMetadata) {
			IDiffractionMetadata dm = (IDiffractionMetadata)metadata;
			DetectorProperties detprops = dm.getDetector2DProperties();
	    	DiffractionCrystalEnvironment diffexp = dm.getDiffractionCrystalEnvironment();
	    	if (detprops != null && diffexp != null) {
		    	qSpace = new QSpace(detprops, diffexp);
	    	}
		}
		Dataset[] intresult = ROIProfile.sector(dataset, mask, tmpRoi, true, false, false, qSpace, axis, false);
		Dataset axis = intresult[4];
		double error = 0.0;
		ArrayList<IPeak> peaks = new ArrayList<IPeak>(initPeaks.size());
		List<CompositeFunction> fittedGaussian = Generic1DFitter.fitPeakFunctions(axis, intresult[0], Gaussian.class, new GeneticAlg(0.0001),
				10, initPeaks.size());
		for (CompositeFunction peakFit : fittedGaussian) {
			try {
				IPeak fitPeak = new Gaussian(peakFit.getFunction(0).getParameters());
				peaks.add(fitPeak);
				error += Math.log(1.0 + fitPeak.getHeight() / fitPeak.getFWHM());
			} catch (Exception e) {
				logger.warn("Peak fitting failed during beam position optimisation", e);
				return Double.NaN;
			}

		}
		if (checkPeakOverlap(peaks)) {
			return Double.NaN;
		}
		logger.info("Error value for beam postion ({}, {}) is {}", new Object[] { beamxy[0], beamxy[1], error });
		return error;
	}

	private boolean checkPeakOverlap(ArrayList<IPeak> peaks) {
		if (peaks.size() < 2)
			return false;
		for (int i = 0; i < peaks.size() - 1; i++) {
			IPeak peak1 = peaks.get(i);
			for (int j = i + 1; j < peaks.size(); j++) {
				IPeak peak2 = peaks.get(j);
				double dist = Math.abs(peak2.getPosition() - peak1.getPosition());
				double fwhm1 = peak1.getFWHM();
				double fwhm2 = peak2.getFWHM();
				if (dist < (fwhm1 + fwhm2) / 2.0)
					return true;
			}
		}
		return false;
	}

	/**
	 * Run optimisation of sector region position in a separate job. 
	 * 
	 * @param startPosition Initial position of sector region
	 */
	public void optimize(final double[] startPosition) {
		final int cmaesLambda = this.cmaesLambda;
		final double[] cmaesInputSigma = this.cmaesInputSigma;
		final int cmaesMaxIterations = this.cmaesMaxIterations;
		final int cmaesCheckFeasableCount = this.cmaesCheckFeasableCount;
		final ConvergenceChecker<PointValuePair> cmaesChecker = this.cmaesChecker;
		final BeamCenterRefinement function = this;
		Job job = new Job("Beam Position Refinement") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				function.setInitPeaks(initPeaks);
				function.setMonitor(monitor);

				final double[] lB = new double [] {startPosition[0] - 20, startPosition[1] - 20};
				final double[] uB = new double [] {startPosition[0] + 20, startPosition[1] + 20};
				CMAESOptimizer beamPosOptimizer = new CMAESOptimizer(cmaesMaxIterations,
						0.0,
						true,
						0,
						cmaesCheckFeasableCount,
						new Well19937a(),
						false,
						cmaesChecker);
				final PointValuePair result = beamPosOptimizer.optimize(new MaxEval(cmaesMaxIterations),
						new ObjectiveFunction(function),
						GoalType.MAXIMIZE,
						new CMAESOptimizer.PopulationSize(cmaesLambda),
						new CMAESOptimizer.Sigma(cmaesInputSigma),
						new SimpleBounds(lB, uB),
						new InitialGuess(startPosition));
				
				final double[] newBeamPosition = result.getPoint();
				logger.info("Optimiser terminated at beam position ({}, {}) with the value {}", new Object[] { newBeamPosition[0], newBeamPosition[1], result.getValue() });
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						((IDiffractionMetadata) dataset.getMetadata()).getDetector2DProperties().setBeamCentreCoords(
								newBeamPosition);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}