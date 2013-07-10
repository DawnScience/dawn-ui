/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.tools.preference;

import java.text.DecimalFormat;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class InfoPixelPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.dawb.workbench.plotting.infoPixelPreferencePage";

	private Text pixelPositionFormat;
	private Text dataFormat;
	private Text qFormat;
	private Text thetaFormat;
	private Text resolutionFormat;

	public InfoPixelPreferencePage() {

	}

	/**
	 * @wbp.parser.constructor
	 */
	public InfoPixelPreferencePage(String title) {
		super(title);
	}

	public InfoPixelPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getPlottingPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);

		Group formatGrp = new Group(comp, SWT.NONE);
		formatGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		formatGrp.setLayout(new GridLayout(2, false));
		formatGrp.setText("Number Format");
		
		Label label = new Label(formatGrp, SWT.NONE);
		label.setText("Pixel Position Format");
		label.setToolTipText("Format for the pixel information numbers shown in the PixelInfo tool");

		pixelPositionFormat = new Text(formatGrp, SWT.BORDER);
		pixelPositionFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		pixelPositionFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		label = new Label(formatGrp, SWT.NONE);
		label.setText("Data Format");
		label.setToolTipText("Format for the data numbers shown in the PixelInfo tool");

		dataFormat = new Text(formatGrp, SWT.BORDER);
		dataFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		dataFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		label = new Label(formatGrp, SWT.NONE);
		label.setText("q X/Y/Z (1/\u00c5) Format");
		label.setToolTipText("Format for the q X/Y/Z information numbers shown in the PixelInfo tool");

		qFormat = new Text(formatGrp, SWT.BORDER);
		qFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		qFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		label = new Label(formatGrp, SWT.NONE);
		label.setText("2\u03b8 (\u00b0) Format");
		label.setToolTipText("Format for the 2\u03b8 information numbers shown in the PixelInfo tool");

		thetaFormat = new Text(formatGrp, SWT.BORDER);
		thetaFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		thetaFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		label = new Label(formatGrp, SWT.NONE);
		label.setText("Resolution (\u00c5) Format");
		label.setToolTipText("Format for the resolution information numbers shown in the PixelInfo tool");

		resolutionFormat = new Text(formatGrp, SWT.BORDER);
		resolutionFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		resolutionFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});
		
		// initialize
		initializePage();
		return comp;
	}

	private void initializePage() {
		pixelPositionFormat.setText(getPixelPosFormat());
		dataFormat.setText(getDataFormat());
		qFormat.setText(getQFormat());
		thetaFormat.setText(getThetaFormat());
		resolutionFormat.setText(getResolutionFormat());
	}
	
	protected void checkState(String format) {
		try {
			final DecimalFormat realForm = new DecimalFormat(format);
			String ok = realForm.format(1.0);
			if (ok==null || "".equals(ok)) throw new Exception();
		} catch (Throwable ne) {
			setErrorMessage("The entered format is incorrect");
			setValid(false);
			return;
		}
		setErrorMessage(null);
		setValid(true);
	}
	
	@Override
	public boolean performOk() {
		return storePreferences();
	}

	@Override
	protected void performDefaults() {
		pixelPositionFormat.setText(getDefaultPixelPosFormat());
		dataFormat.setText(getDefaultDataFormat());
		qFormat.setText(getDefaultQFormat());
		thetaFormat.setText(getDefaultThetaFormat());
		resolutionFormat.setText(getDefaultResolutionFormat());
	}


	private boolean storePreferences() {
		String format = pixelPositionFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setPixelPosFormat(format);

		format = dataFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setDataFormat(format);

		format = qFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setQFormat(qFormat.getText());

		format = thetaFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setThetaFormat(thetaFormat.getText());

		format = resolutionFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setResolutionFormat(resolutionFormat.getText());

		return true;
	}

	private String getDefaultPixelPosFormat() {
		return getPreferenceStore().getDefaultString(InfoPixelConstants.PIXEL_POS_FORMAT);
	}

	private String getPixelPosFormat() {
		return getPreferenceStore().getString(InfoPixelConstants.PIXEL_POS_FORMAT);
	}

	public void setPixelPosFormat(String format) {
		getPreferenceStore().setValue(InfoPixelConstants.PIXEL_POS_FORMAT, format);
	}

	private String getDefaultDataFormat() {
		return getPreferenceStore().getDefaultString(InfoPixelConstants.DATA_FORMAT);
	}

	private String getDataFormat() {
		return getPreferenceStore().getString(InfoPixelConstants.DATA_FORMAT);
	}

	public void setDataFormat(String format) {
		getPreferenceStore().setValue(InfoPixelConstants.DATA_FORMAT, format);
	}

	private String getDefaultQFormat() {
		return getPreferenceStore().getDefaultString(InfoPixelConstants.Q_FORMAT);
	}

	private String getQFormat() {
		return getPreferenceStore().getString(InfoPixelConstants.Q_FORMAT);
	}

	public void setQFormat(String format) {
		getPreferenceStore().setValue(InfoPixelConstants.Q_FORMAT, format);
	}

	private String getDefaultThetaFormat() {
		return getPreferenceStore().getDefaultString(InfoPixelConstants.THETA_FORMAT);
	}

	private String getThetaFormat() {
		return getPreferenceStore().getString(InfoPixelConstants.THETA_FORMAT);
	}

	public void setThetaFormat(String format) {
		getPreferenceStore().setValue(InfoPixelConstants.THETA_FORMAT, format);
	}

	private String getDefaultResolutionFormat() {
		return getPreferenceStore().getDefaultString(InfoPixelConstants.RESOLUTION_FORMAT);
	}

	private String getResolutionFormat() {
		return getPreferenceStore().getString(InfoPixelConstants.RESOLUTION_FORMAT);
	}

	public void setResolutionFormat(String format) {
		getPreferenceStore().setValue(InfoPixelConstants.RESOLUTION_FORMAT, format);
	}
}
