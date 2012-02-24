package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FittingTool extends AbstractToolPage {

	public FittingTool() {
		super();
	}

	@Override
	public void createControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Fitting...");
		this.controlForPage = label;
	}

	@Override
	public void setFocus() {
		controlForPage.setFocus();
	}

}
