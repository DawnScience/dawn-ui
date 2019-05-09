package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GaussianBlurConfiguration extends RadiusFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		Composite content = super.createControl(parent);

		Label label = new Label(content, SWT.NONE);
		label.setText("Sigma");
		label.setToolTipText("Gaussian distribution's sigma. If <= 0 then will be selected based on radius.");

		final Text sigma = new Text(content, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = 100;
		sigma.setLayoutData(gridData);
		sigma.setText("0");
		sigma.setToolTipText("Sigma Gaussian distribution's sigma. If <= 0 then will be selected based on radius.");
		filter.putConfiguration("sigma", 0.);
		sigma.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
		sigma.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String str = sigma.getText();
				if (str.equals(""))
					return;
				double value = Double.valueOf(sigma.getText());
				filter.putConfiguration("sigma", value);
			}
		});

		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImage("icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 3, 1));
		return content;
	}

	@Override
	protected String getRadiusToolTip() {
		return "Radius of the Gaussian blur function. If <= 0 then radius will be determined by sigma.";
	}

	private String getDescription() {
		return "Filter using the Gaussian distribution's sigma. If <= 0 then will be selected based on radius or using"
				+ "Radius of the Gaussian blur function. If <= 0 then radius will be determined by sigma.";
	}
}
