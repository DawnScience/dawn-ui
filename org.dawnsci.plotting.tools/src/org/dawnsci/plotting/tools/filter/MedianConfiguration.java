package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MedianConfiguration extends RadiusFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);

		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImageAndAddDisposeListener(info, "icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 3, 1));
		return content;
	}

	@Override
	protected String getRadiusToolTip() {
		return "Radius of the median blur function.";
	}

	private String getDescription() {
		return "Moves a box over the image and set each pixel value to the median for the box.";
	}
}
