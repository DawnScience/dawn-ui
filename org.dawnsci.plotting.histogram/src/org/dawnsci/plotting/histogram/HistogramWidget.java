package org.dawnsci.plotting.histogram;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchSite;

public class HistogramWidget extends Composite {

	IPlottingSystem histogramPlottingSystem = null;

	/**
	 * Create a new Histogram Widget
	 * 
	 * @param composite
	 *            parent composite to add widget to. Must not be
	 *            <code>null</code>
	 * @param title
	 *            Title string for plot
	 * @param plot
	 *            A plotting system to work with this widget. Can be
	 *            <code>null</code>, in which case a default plotting system
	 *            will be created
	 * @param site
	 *            the workbench site this widget sits in. This is used to set
	 *            commands and handlers. Can be <code>null</code>, in which case
	 *            handlers will not be added and a pop-up menu will not be
	 *            added.
	 * @throws Exception
	 *             throws an exception if plot is null and there is a failure to
	 *             create a default plotting system
	 */
	public HistogramWidget(final Composite composite, String title,
			IPlottingSystem plot, IWorkbenchSite site) throws Exception {
		super(composite, SWT.NONE);
		
		setLayout(new FillLayout());

		if (plot != null) {
			histogramPlottingSystem = plot;
		} else {
			histogramPlottingSystem = PlottingFactory.createPlottingSystem();
		}

//		 IActionBars actionBars = (site != null) ? site.getActionBars() :
//		 null;
		histogramPlottingSystem.createPlotPart(this, title, null,
				PlotType.XY, null);
	}

	/**
	 * Get the plotting system associated with this histogramplot
	 * 
	 * @return IPlottingSystem the plotting system associated with this histogram
	 */
	public IPlottingSystem getHistogramPlot() {
		return histogramPlottingSystem;
	}
}
