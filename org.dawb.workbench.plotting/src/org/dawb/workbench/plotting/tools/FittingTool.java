package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FittingTool extends AbstractToolPage {

	private Composite composite;

	public FittingTool() {
		super();
	}

	@Override
	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Fitting...");
		
		getSite().getActionBars().getToolBarManager().add(new Action("Test") {});
	}

	@Override
	public void setFocus() {

	}

	@Override
	public Control getControl() {
		return composite;
	}

}
