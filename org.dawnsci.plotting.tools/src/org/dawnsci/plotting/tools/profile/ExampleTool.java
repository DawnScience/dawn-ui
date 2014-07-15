package org.dawnsci.plotting.tools.profile;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ExampleTool extends AbstractToolPage {

	private Composite control;
	public ExampleTool() {
		// Create your listeners to the main plotting 
		// Perhaps create a plotting system here from the PlottingFactory which is your side plot.
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);
		
		// User interface shown in a page to the side of the plot.
		// ... For instance: a side plot, a Viewer part
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		// If you have a table or tree in your tool, set focus here.
	}

	@Override
	public void activate() {
		super.activate();
		// Now add any listeners to the plotting providing getPlottingSystem()!=null
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		// Now remove any listeners to the plotting providing getPlottingSystem()!=null
	}
	@Override
	public void dispose() {
		super.dispose();
        // Anything to kill off? This page is part of a view which is now disposed and will not be used again.
	}
}
