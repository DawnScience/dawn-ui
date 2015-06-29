package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FanoConfiguration extends BoxFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);

		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImage("icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		return content;
	}

	@Override
	protected String getBoxToolTip() {
		return "The box size must be odd numbers in the form: XxY";
	}

	private String getDescription() {
		return "Moves a box over the image and set each pixel value to the variance/mean for the box.";
	}

	@Override
	protected boolean hasHistoBounds() {
		return true;
	}
}
