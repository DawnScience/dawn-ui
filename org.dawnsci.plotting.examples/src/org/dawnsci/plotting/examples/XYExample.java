package org.dawnsci.plotting.examples;

import java.io.File;
import java.util.Arrays;

import org.dawnsci.plotting.api.IPlottingService;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.examples.util.BundleUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * A basic view which plots xy (1D) data.
 * 
 * This view uses the services available from plotting.api and 
 * analysis.io
 * 
 * @author fcp94556
 *
 */
public class XYExample extends PlotExample {
	
	
	public void createPartControl(Composite parent) {
		try {
			// We create a basic plot
			system.createPlotPart(parent, "XY Example", getViewSite().getActionBars(), PlotType.XY, this);

			// We read an image
			final File        loc     = new File(BundleUtils.getBundleLocation(Activator.PLUGIN_ID), getFileName());
			final IDataHolder allData = service.getData(loc.getAbsolutePath(), new IMonitor.Stub());
			// NOTE IMonitor is an alternative to IProgressMonitor which cannot be seen in the data layer.
			
			// We plot the data
			system.createPlot1D(null, Arrays.asList(allData.getDataset(0), allData.getDataset(1), allData.getDataset(2)), new NullProgressMonitor());
			system.setXFirst(false);
			
		} catch (Throwable ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
    }
	
	protected String getFileName() {
		return "metalmix.mca";
	}


}
