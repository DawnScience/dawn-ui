package org.dawnsci.plotting.examples;

import java.io.File;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.examples.util.BundleUtils;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

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
			system.setPlotType(PlotType.SURFACE); // Set to a 3D plot type.
			
			ISurfaceTrace surface = system.createSurfaceTrace("Example surface");
			surface.setData(image, null);
			// NOTE We are viewing a window of the data. The user can 
			// open a tool from the toolbar to move around this window.
			surface.setWindow(new RectangularROI(300,300,600,600,0));
			
			// Let's make it something colorful!
			final IPaletteService pservice = (IPaletteService)Activator.getService(IPaletteService.class);
			final PaletteData     pData    = pservice.getDirectPaletteData("NCD");
			surface.setPaletteData(pData); 
			surface.setMax(10);
			surface.setMin(0);
			
			system.addTrace(surface);
			
			
		} catch (Throwable ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
    }
	
	protected String getFileName() {
		return "pow_M99S5_1_0001.cbf";
	}
}
