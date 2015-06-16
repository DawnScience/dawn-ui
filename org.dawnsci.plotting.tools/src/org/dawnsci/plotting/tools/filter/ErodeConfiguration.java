package org.dawnsci.plotting.tools.filter;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ErodeConfiguration extends BoxFilterConfiguration {

	@Override
	public Composite createControl(Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		Label label = new Label(content, SWT.NONE);
		label.setText("Convert to binary");
		label.setToolTipText("");
		final Combo binaryCombo = new Combo(content, SWT.NONE);
		binaryCombo.setItems(new String[] {"Yes", "No"});
		binaryCombo.select(1);
		filter.putConfiguration("binary", false);
		binaryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = binaryCombo.getSelectionIndex();
				boolean isBinary = false;
				if (idx == 1)
					isBinary = false;
				else if (idx == 0)
					isBinary = true;
				filter.putConfiguration("binary", isBinary);
			}
		});

		Label info = new Label(content, SWT.WRAP);
		info.setImage(Activator.getImage("icons/info.png"));
		info.setText(getDescription());
		info.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		return content;
	}

	@Override
	protected String getBoxToolTip() {
		return "";
	}

	private String getDescription() {
		return "Erodes an image according to a 8-neighborhood. Unless a pixel is connected to all its neighbors its value is set to zero.";
	}
}
