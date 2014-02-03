package org.dawnsci.plotting.examples;

import java.io.File;

import org.dawnsci.plotting.api.IPlottingService;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.examples.util.BundleUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * A basic view which plots image (2D) data.
 * 
 * This view uses the services available from plotting.api and 
 * analysis.io
 * 
 * @author fcp94556
 *
 */
public class ImageExample extends ViewPart {
	
	protected ILoaderService  service;
	protected IPlottingSystem system;

	public ImageExample() {
		
		// A service for loading data from any data file format.
		service = (ILoaderService)Activator.getService(ILoaderService.class);

		
		final IPlottingService pservice = (IPlottingService)Activator.getService(IPlottingService.class);
		try {
			this.system = pservice.createPlottingSystem();
		} catch (Exception ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
			
	}
	
	public void createPartControl(Composite parent) {
		try {
			// We create a basic plot
			system.createPlotPart(parent, "Image Example", getViewSite().getActionBars(), PlotType.IMAGE, this);

			// We read an image
			final File loc = new File(BundleUtils.getBundleLocation(Activator.PLUGIN_ID), "pow_M99S5_1_0001.cbf");
			final IDataset image = service.getDataset(loc.getAbsolutePath(), new IMonitor.Stub());
			// NOTE IMonitor is an alternative to IProgressMonitor which cannot be seen in the data layer.
			
			// We plot the image
			system.createPlot2D(image, null, new NullProgressMonitor());
			
		} catch (Throwable ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
    }
	
	@Override
	public void dispose() {
		super.dispose();
		system.dispose();
	}
	
	@Override
	public Object getAdapter(Class clazz) {
		if (system.getAdapter(clazz)!=null) return system.getAdapter(clazz);
		return super.getAdapter(clazz);
	}

	@Override
	public void setFocus() {
		system.setFocus();
	}

}
