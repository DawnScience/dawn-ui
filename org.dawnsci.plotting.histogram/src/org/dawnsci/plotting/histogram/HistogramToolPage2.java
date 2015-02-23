package org.dawnsci.plotting.histogram;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class HistogramToolPage2 extends AbstractToolPage implements IToolPage {

	private FormToolkit toolkit;
	private ScrolledForm form;
	
	IPlottingSystem histogramPlot = null;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.reflow(true); // create view with no scrollbars reflowing at this point
		
		form.getBody().setLayout(GridLayoutFactory.fillDefaults().create());
		createHistogramControl();

	}

	public void createHistogramControl() {
		Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		section.setText("Histogram Plot");
		section.setDescription("");

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(GridLayoutFactory.fillDefaults().create());
		sectionClient.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());

		try {
			histogramPlot = PlottingFactory.createPlottingSystem();
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}

		//final IPageSite site = getSite();
		//IActionBars actionBars = (site != null) ? site.getActionBars() : null;
		histogramPlot.createPlotPart(sectionClient, getTitle(), null,
				PlotType.XY, null);
		GridData create = GridDataFactory.fillDefaults().hint(0, 200)
				.grab(true, true).create();
		histogramPlot.getPlotComposite().setLayoutData(create);
		toolkit.adapt(histogramPlot.getPlotComposite());

		section.setClient(sectionClient);
	}

	@Override
	public boolean isAlwaysSeparateView() {
		return true;
	}

	@Override
	public Control getControl() {
		return form;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

}
