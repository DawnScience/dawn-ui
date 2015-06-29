package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DilateConfiguration extends BinaryFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);
		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImage("icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		return content;
	}

	private String getDescription() {
		return "Dilates an image according to a 8-neighborhood. If a pixel is connected "
				+ "to any other pixel then its output value will be one.";
	}
}
