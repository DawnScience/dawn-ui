package org.dawnsci.plotting.histogram.ui;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;

public class HistogramToolPage2 extends AbstractToolPage implements IToolPage {

	private FormToolkit toolkit;
	private ScrolledForm form;
	
	private HistogramWidget histogramWidget;

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
		createHistogramControl(form.getBody());

	}
	
	/*
	 * Create the histogram section 
	 */
	private void createHistogramControl(Composite comp) {
		Section section = toolkit.createSection(comp,
				Section.DESCRIPTION | Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		section.setText("Histogram Plot");
		section.setDescription("Histogram information for active plot view");

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(GridLayoutFactory.fillDefaults().create());
		sectionClient.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		
		try {
			histogramWidget = new HistogramWidget(sectionClient, getTitle(), null, null);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting systems!", e);
		}
		
		GridData create = GridDataFactory.fillDefaults().hint(0, 200).grab(true, true).create();
		histogramWidget.setLayoutData(create);

		toolkit.adapt(histogramWidget);
		section.setClient(sectionClient);
	}
	
	@Override
	public void activate() {
		super.activate();
		
		if (getPlottingSystem()!=null) {
			//getPlottingSystem().addTraceListener(traceListener);
			
			generateHistogram(getPaletteTrace()); 
		}
	}
	
	private void inputChanged (IPaletteTrace oldImage, IPaletteTrace newImage){
//	    if(oldImage != null) {
//	        removeListenerFrom(oldHistogramProvider);
//	    }
//	    if(newImage != null) {
//	       addListenerTo(newHistogramProvider);
//	    }
	}
	
	protected void addListenerTo(ImageHistogramProvider hProvider) {
//	    box.addListener(this);
//	    for (Iterator iterator = box.getBoxes().iterator(); iterator.hasNext();) {
//	       MovingBox aBox = (MovingBox) iterator.next();
//	       addListenerTo(aBox);
//	    }
	}

	
	private void generateHistogram(IPaletteTrace image) {
		if (image == null) return;
		
		ImageHistogramProvider h = new ImageHistogramProvider(image);
		histogramWidget.setInput(h);
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
