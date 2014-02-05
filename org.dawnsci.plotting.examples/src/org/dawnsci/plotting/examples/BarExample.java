package org.dawnsci.plotting.examples;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.dawnsci.plotting.examples.util.BundleUtils;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
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
public class BarExample extends PlotExample {
	
	
	public void createPartControl(Composite parent) {
		try {
			// We create a basic plot
			system.createPlotPart(parent, "Bar Example", getViewSite().getActionBars(), PlotType.XY, this);

			// We read an image
			final File        loc     = new File(BundleUtils.getBundleLocation(Activator.PLUGIN_ID), getFileName());
			final IDataHolder allData = service.getData(loc.getAbsolutePath(), new IMonitor.Stub());
			// NOTE IMonitor is an alternative to IProgressMonitor which cannot be seen in the data layer.
			
			// We plot the data
			ILineTrace bar = system.createLineTrace(allData.getDataset(0).getName());
			bar.setData(null, allData.getDataset(0));
			bar.setTraceType(TraceType.HISTO);
			system.addTrace(bar);
		
			ILineTrace area = system.createLineTrace(allData.getDataset(10).getName());
			area.setData(null, allData.getDataset(10));
			area.setTraceType(TraceType.AREA);
			system.addTrace(area);
			
			system.autoscaleAxes();
			system.setTitle("Area and Bar Example");
			
		} catch (Throwable ne) {
			ne.printStackTrace(); // Or your favourite logging.
		}
    }
	
	protected String getFileName() {
		return "metalmix.mca";
	}


}
