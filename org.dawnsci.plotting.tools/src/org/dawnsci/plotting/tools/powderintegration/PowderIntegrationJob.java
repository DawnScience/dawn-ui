package org.dawnsci.plotting.tools.powderintegration;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.function.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.dataset.function.NonPixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.dataset.function.PixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.dataset.function.PixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

public class PowderIntegrationJob extends Job {

	IPlottingSystem system;
	IDiffractionMetadata md;
	QSpace qSpace;
	AbstractPixelIntegration integrator;
	XAxis xAxis = XAxis.Q;
	IntegrationMode mode = IntegrationMode.NONSPLITTING;
	AbstractDataset data;
	AbstractDataset mask;
	IROI roi;
	int nBins;
	
	public enum IntegrationMode{NONSPLITTING,SPLITTING,SPLITTING2D}
	
	public PowderIntegrationJob(IDiffractionMetadata md, IPlottingSystem system) {
		super("Integration");
		this.md = md;
		this.system = system;
		this.qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
	}
	
	public void setData(AbstractDataset data, AbstractDataset mask, IROI roi) {
		this.data = data;
		this.mask = mask;
		this.roi = roi;
		this.nBins = AbstractPixelIntegration.calculateNumberOfBins(md.getDetector2DProperties().getBeamCentreCoords(),
				data.getShape());
	}
	
	public void setAxisType(XAxis xAxis) {
		if (this.xAxis != xAxis) {
			this.xAxis = xAxis;
			integrator = null;
		}
	}
	
	public XAxis getAxisType() {
		return xAxis;
	}
	
	public void setIntegrationMode(IntegrationMode mode) {
		if (this.mode != mode) {
			this.mode = mode;
			integrator = null;
		}
	}
	
	public IntegrationMode getIntegrationMode() {
		return mode;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (integrator == null) {
			updateIntegrator();
		}
		
		
		
		integrator.setMask(mask);
		integrator.setROI(roi);
		integrator.setAxisType(xAxis);
		system.setEnabled(false);
		final List<AbstractDataset> out = integrator.value(data);
		system.setEnabled(true);
		
		if (mode != IntegrationMode.SPLITTING2D) {
			if (system.is2D()) system.reset();
			out.get(1).setName("Intensity");

			system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					system.getSelectedXAxis().setTitle(out.get(0).getName());

				}
			});
		} else {
			out.get(1).setName("Intensity");
			system.createPlot2D(out.remove(1), out, null);
		}
		
		system.repaint();
		
		
		return Status.OK_STATUS;
	}
	
	public List<AbstractDataset> process(AbstractDataset data) {
		
		if (integrator == null) {
			updateIntegrator();
			integrator.setMask(mask);
			integrator.setROI(roi);
			integrator.setAxisType(xAxis);
		}
	
		return integrator.value(data);
		
	}
	
	private void updateIntegrator() {
		switch (mode) {
		case NONSPLITTING:
			integrator = new NonPixelSplittingIntegration(qSpace, nBins);
			break;
		case SPLITTING:
			integrator = new PixelSplittingIntegration(qSpace, nBins);
			break;
		case SPLITTING2D:
			integrator = new PixelSplittingIntegration2D(qSpace, nBins,nBins);
			break;
		}
	}

}
