package org.dawnsci.plotting.tools.powderintegration;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.function.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.dataset.function.NonPixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

public class PowderIntegrationJob extends Job {

	IPlottingSystem system;
	IDiffractionMetadata md;
	QSpace qSpace;
	NonPixelSplittingIntegration integrator;
	AbstractDataset data;
	AbstractDataset mask;
	IROI roi;
	int nBins;
	
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

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (integrator == null) integrator = new NonPixelSplittingIntegration(qSpace, nBins);
		integrator.setMask(mask);
		integrator.setROI(roi);
		integrator.setAxisType(XAxis.Q);
		
		List<AbstractDataset> out = integrator.value(data);
		
		out.get(0).setName("Q");
		out.get(1).setName("Intensity");

		system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
		system.repaint();
		return Status.OK_STATUS;
	}
	
	public List<AbstractDataset> process(AbstractDataset data) {
		
		if (integrator == null) {
			integrator = new NonPixelSplittingIntegration(qSpace, nBins);
			integrator.setMask(mask);
			integrator.setROI(roi);
			integrator.setAxisType(XAxis.Q);
		}
	
		return integrator.value(data);
		
	}

}
