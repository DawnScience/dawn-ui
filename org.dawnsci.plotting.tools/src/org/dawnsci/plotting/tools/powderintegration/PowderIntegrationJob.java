/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderintegration;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration1D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils.IntegrationMode;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration2D;

public class PowderIntegrationJob extends Job {

	private final static Logger logger = LoggerFactory.getLogger(PowderIntegrationJob.class);
	
	private IPlottingSystem system;
	private IDiffractionMetadata diffractionMetadata;
	private MatchedIntegrator matchedIntegrator;
	private Dataset data;
	private Dataset mask;
	private MatchedCorrection matchedCorrection;
	private PowderIntegrationModel model;
	private PowderCorrectionModel correctionModel;
	private int nBins;
	
	public PowderIntegrationJob(IDiffractionMetadata diffractionMetadata, IPlottingSystem system) {
		super("Integration");
		this.diffractionMetadata = diffractionMetadata;
		this.system = system;
	}
	
	public void setData(Dataset data, Dataset mask,PowderIntegrationModel model, PowderCorrectionModel correctionModel) {
		this.data = data;
		this.mask = mask;
		this.nBins = AbstractPixelIntegration.calculateNumberOfBins(diffractionMetadata.getDetector2DProperties().getBeamCentreCoords(),
				data.getShape());
		this.model = model.clone();
		this.correctionModel = correctionModel.clone();
	}
	

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Dataset dl = data;
		Dataset ml = mask;
		PowderIntegrationModel modell = model;
		PowderCorrectionModel corModell = correctionModel;
		int nBinsl = nBins;
		
		if (matchedIntegrator != null && !matchedIntegrator.getModel().equals(modell) &&
				!isCompatibleIntegrator(matchedIntegrator.getIntegrator(), modell.getIntegrationMode())) {
			matchedIntegrator = null;
		}
		
		
		if (matchedIntegrator == null) {
			AbstractPixelIntegration i = createIntegrator(modell, diffractionMetadata, nBinsl);
			PowderIntegrationJob.this.matchedIntegrator = new MatchedIntegrator(modell, i);
			updateIntegratorFromModel(i, modell);
		}
		
		if (!matchedIntegrator.getModel().equals(modell)) {
			AbstractPixelIntegration i = matchedIntegrator.getIntegrator();
			updateIntegratorFromModel(i, modell);
			matchedIntegrator = new MatchedIntegrator(modell, i);
		}
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		matchedIntegrator.getIntegrator().setMask(ml);

		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		//all accept 2d no splitting should be fast
		if (modell.getIntegrationMode() == IntegrationMode.SPLITTING2D) system.setEnabled(false);

		if (matchedCorrection != null && !matchedCorrection.getModel().equals(corModell)){
			matchedCorrection = null;
		}
		
		if (matchedCorrection == null) {
			Dataset c = getCorrection(dl, corModell, diffractionMetadata);
			matchedCorrection = new MatchedCorrection(corModell, c);
		}
		
		if (matchedCorrection.getCorrection() != null) dl = Maths.multiply(dl, matchedCorrection.getCorrection());
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		
		final List<Dataset> out;
		
		try {
			out = matchedIntegrator.getIntegrator().integrate(dl);
		} catch (Exception e) {
			logger.error("Someones probably just toggling buttons to quickly, but if something looks wrong check here!");
			return Status.CANCEL_STATUS;
		}
		
		system.setEnabled(true);
		
		if (modell.getIntegrationMode() == IntegrationMode.NONSPLITTING || modell.getIntegrationMode() ==IntegrationMode.SPLITTING) {
			if (system.is2D()) system.reset();
			out.get(1).setName("Intensity");

			system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					system.getSelectedXAxis().setTitle(out.get(0).getName());
					system.getSelectedYAxis().setTitle(out.get(1).getName());
					system.setTitle("Integrated Intensity");

				}
			});
		} else {
			out.get(1).setName("Intensity");
			system.createPlot2D(out.remove(1), out, null);
		}
		
		system.repaint();
		
		return Status.OK_STATUS;
	}
	
	public List<Dataset> process(Dataset data) {
		
		if (matchedIntegrator == null) {
			AbstractPixelIntegration i = createIntegrator(model, diffractionMetadata, nBins);
			matchedIntegrator = new MatchedIntegrator(model, i);
		}
		
		if (matchedCorrection == null) {
			Dataset c = getCorrection(data, correctionModel, diffractionMetadata);
			matchedCorrection = new MatchedCorrection(correctionModel, c);
		}
		
		if (matchedCorrection.getCorrection() != null) data = Maths.multiply(data, matchedCorrection.getCorrection());
		
		List<Dataset> out = matchedIntegrator.getIntegrator().integrate(data);
		
		return out;
		
	}
	
	private static void updateIntegratorFromModel(AbstractPixelIntegration integrator, PowderIntegrationModel model) {
		
		integrator.setAxisType(model.getAxisType());

		if (model.getRadialRange() == null) integrator.setRadialRange(null);
		else integrator.setRadialRange(model.getRadialRange().clone());

		if (model.getAzimuthalRange() == null) integrator.setAzimuthalRange(null);
		else integrator.setAzimuthalRange(model.getAzimuthalRange().clone());

		integrator.setNumberOfBins(model.getNumberOfPrimaryBins());

		if (integrator instanceof AbstractPixelIntegration2D) {
			((AbstractPixelIntegration2D)integrator).setNumberOfAzimuthalBins(model.getNumberOfSecondaryBins());
		}

		if (integrator instanceof AbstractPixelIntegration1D) {
			((AbstractPixelIntegration1D)integrator).setAzimuthalIntegration(model.isAzimuthal());
		}
	}
	
	private static Dataset getCorrection(Dataset data, PowderCorrectionModel mod, IDiffractionMetadata diffractionMetadata) {

		if (mod == null) return null;
		
		if (!mod.isApplySolidAngleCorrection() &&
				!mod.isApplyPolarisationCorrection() &&
				!mod.isAppyDetectorTransmissionCorrection()) return null;

		Dataset correction = DatasetFactory.ones(data, Dataset.FLOAT32);

		if (!mod.isApplyPolarisationCorrection() &&
				!mod.isApplySolidAngleCorrection() &&
				!mod.isAppyDetectorTransmissionCorrection()) return data;


		//incase correction gets nulled while job is running

		Dataset tth = PixelIntegrationUtils.generate2ThetaArrayRadians(data.getShape(), diffractionMetadata);

		if (mod.isApplySolidAngleCorrection()) {
			PixelIntegrationUtils.solidAngleCorrection(correction,tth);
		}

		if (mod.isApplyPolarisationCorrection()) {
			Dataset az = PixelIntegrationUtils.generateAzimuthalArray(data.getShape(), diffractionMetadata, true);
			az.iadd(Math.toRadians(mod.getPolarisationAngularOffset()));
			PixelIntegrationUtils.polarisationCorrection(correction, tth, az, mod.getPolarisationFactor());
		}

		if (mod.isAppyDetectorTransmissionCorrection()) {
			PixelIntegrationUtils.detectorTranmissionCorrection(correction, tth, mod.getTransmittedFraction());
		}

		return correction;

	}
	
	private static AbstractPixelIntegration createIntegrator(PowderIntegrationModel model, IDiffractionMetadata diffractionMetadata, int nBins) {
		AbstractPixelIntegration in = null;
		
		switch (model.getIntegrationMode()) {
		case NONSPLITTING:
			in = new NonPixelSplittingIntegration(diffractionMetadata, nBins);
			break;
		case SPLITTING:
			in = new PixelSplittingIntegration(diffractionMetadata, nBins);
			break;
		case SPLITTING2D:
			in = new PixelSplittingIntegration2D(diffractionMetadata, nBins,nBins);
			break;
		case NONSPLITTING2D:
			in = new NonPixelSplittingIntegration2D(diffractionMetadata, nBins,nBins);
			break;
		}
		
		in.setAxisType(model.getAxisType());
		
		return in;
	}
	
	private static boolean isCompatibleIntegrator(AbstractPixelIntegration integrator, IntegrationMode mode) {
		
		if (integrator == null) return false;
		
		switch (mode) {
		case NONSPLITTING:
			return integrator instanceof NonPixelSplittingIntegration;
		case SPLITTING:
			return integrator instanceof PixelSplittingIntegration;
		case SPLITTING2D:
			return integrator instanceof PixelSplittingIntegration2D;
		case NONSPLITTING2D:
			return integrator instanceof NonPixelSplittingIntegration2D;
		}
		return false;
		
	}
	
	private class MatchedIntegrator {
		
		private final PowderIntegrationModel model;
		private final AbstractPixelIntegration integrator;
		
		public MatchedIntegrator(PowderIntegrationModel model,AbstractPixelIntegration integrator) {
			this.model = model;
			this.integrator = integrator;
		}

		public PowderIntegrationModel getModel() {
			return model;
		}

		public AbstractPixelIntegration getIntegrator() {
			return integrator;
		}
	}
	
	private class MatchedCorrection {
		
		private final PowderCorrectionModel model;
		private final Dataset correction;
		
		public MatchedCorrection(PowderCorrectionModel model, Dataset correction) {
			this.model = model;
			this.correction = correction;
		}

		public PowderCorrectionModel getModel() {
			return model;
		}

		public Dataset getCorrection() {
			return correction;
		}
	}

}
