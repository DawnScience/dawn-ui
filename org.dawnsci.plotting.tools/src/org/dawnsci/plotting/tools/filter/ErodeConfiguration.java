package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ErodeConfiguration extends BinaryFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);
		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImageAndAddDisposeListener(info, "icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		return content;
	}

	private String getDescription() {
		return "Erodes an image according to a 8-neighborhood. Unless a pixel is connected to all its neighbors its value is set to zero or False";
	}
}
