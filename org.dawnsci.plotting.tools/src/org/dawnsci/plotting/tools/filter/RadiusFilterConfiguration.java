package org.dawnsci.plotting.tools.filter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RadiusFilterConfiguration extends BoxFilterConfiguration {

	private Text radiusText;

	@Override
	public Composite createControl(Composite parent) {

		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(3, false));

		Label label = null;

		label = new Label(content, SWT.NONE);
		label.setText("Radius");
		label.setToolTipText(getRadiusToolTip());

		radiusText = new Text(content, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = 100;
		radiusText.setLayoutData(gridData);
		radiusText.setText("3");
		radiusText.setToolTipText(getRadiusToolTip());
		radiusText.addVerifyListener(new VerifyListener() {
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
		radiusText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String str = radiusText.getText();
				if (str.equals(""))
					return;
				int value = Integer.valueOf(radiusText.getText());
				setRadius(value);
			}
		});
		setRadius(Integer.valueOf(radiusText.getText()));

		Label pixelLabel = new Label(content, SWT.NONE);
		pixelLabel.setText("pixels");

		return content;
	}

	/**
	 * Tooltip of the radius Label and Text widget. To override if necessary
	 * 
	 * @return String
	 */
	protected String getRadiusToolTip() {
		return "Radius";
	}

	private void setRadius(int radius) {
		filter.putConfiguration("radius", radius);
	}

}
