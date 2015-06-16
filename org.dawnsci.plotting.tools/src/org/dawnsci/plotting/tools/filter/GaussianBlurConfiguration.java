package org.dawnsci.plotting.tools.filter;

import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GaussianBlurConfiguration extends BoxFilterConfiguration {

	private FloatDecorator sigmadeco;

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);

		Label label = new Label(content, SWT.NONE);
		label.setText("Sigma");
		label.setToolTipText("Gaussian distribution's sigma. If <= 0 then will be selected based on radius or using "
				+ "radius of the Gaussian blur function. If <= 0 then radius will be determined by sigma");

		final Text sigma = new Text(content, SWT.BORDER);
		sigma.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sigma.setText("0");
		filter.putConfiguration("sigma", new Double(0));
		sigma.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				double value = sigmadeco.getValue().doubleValue();
				filter.putConfiguration("sigma", value);
			}
		});

		sigmadeco = new FloatDecorator(sigma);
		sigmadeco.setMinimum(-10d);
		sigmadeco.setMaximum(50);

		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImage("icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		return content;
	}

	@Override
	protected String getBoxToolTip() {
		return "The box size must be in the form: XxY where X=Y";
	}

	private String getDescription() {
		return "Filter using the Gaussian distribution's sigma. If <= 0 then will be selected based on radius or using"
				+ "Radius of the Gaussian blur function. If <= 0 then radius will be determined by sigma.";
	}
}
