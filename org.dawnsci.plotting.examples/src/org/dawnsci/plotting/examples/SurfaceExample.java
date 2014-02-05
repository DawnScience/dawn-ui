package org.dawnsci.plotting.examples;

import java.io.File;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.examples.util.BundleUtils;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class SurfaceExample extends PlotExample {

	public void createPartControl(Composite parent) {
		try {
			// We create a basic plot
			system.createPlotPart(parent, "Image Example", getViewSite().getActionBars(), PlotType.IMAGE, this);

			// We read an image
			final File loc = new File(BundleUtils.getBundleLocation(Activator.PLUGIN_ID), getFileName());
			final IDataset image = service.getDataset(loc.getAbsolutePath(), new IMonitor.Stub());
			// NOTE IMonitor is an alternative to IProgressMonitor which cannot be seen in the data layer.
			
			// We plot the image
			ISurfaceTrace surface = system.createSurfaceTrace("Example surface");
			surface.setData(image, null);
			//surface.setWindow(new SurfacePlotROI());
			// TODO need ROIs in public interface...
			system.addTrace(surface);
			
		} catch (Throwable ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
    }
	
	protected String getFileName() {
		return "pow_M99S5_1_0001.cbf";
	}
}
