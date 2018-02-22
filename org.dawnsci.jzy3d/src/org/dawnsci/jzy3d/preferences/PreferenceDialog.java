package org.dawnsci.jzy3d.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;

public class PreferenceDialog extends Dialog {

	private List<AxisPreferenceConfig> axisConfigPageList;
	private Chart chart;
	private Button tickLineButton;
	private ColorSelector gridColorSelector;
	private Button showAxisButton;

	public PreferenceDialog(Shell parentShell, Chart chart) {
		super(parentShell);
		this.chart = chart;
		axisConfigPageList = new ArrayList<AxisPreferenceConfig>();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		IAxeLayout axeLayout = chart.getAxeLayout();

		Composite container = (Composite) super.createDialogArea(parent);
		// axes config
		Group axisSelectGroup = new Group(container, SWT.NONE);
		axisSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		axisSelectGroup.setText("Select Axis");
		axisSelectGroup.setLayout(new GridLayout(1, false));
		Combo axisCombo = new Combo(axisSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		axisCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		IAxeLayout axesLayout = chart.getAxeLayout();
		axisCombo.add(axesLayout.getXAxeLabel() + "(X-Axis)");
		axisCombo.add(axesLayout.getYAxeLabel() + "(Y-Axis)");
		axisCombo.add(axesLayout.getZAxeLabel() + "(Z-Axis)");
		axisCombo.select(0);

		Composite axisConfigComposite = new Composite(axisSelectGroup, SWT.NONE);
		axisConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		StackLayout axisStackLayout = new StackLayout();
		axisConfigComposite.setLayout(axisStackLayout);
		for (int i = 0; i < 3; i ++) {
			Group axisConfigGroup = new Group(axisConfigComposite, SWT.NONE);
			axisConfigGroup.setText("Change Settings");
			axisConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			AxisPreferenceConfig axisConfigPage = new AxisPreferenceConfig(axeLayout, i);
			axisConfigPageList.add(axisConfigPage);
			axisConfigPage.createComposite(axisConfigGroup);
		}
		axisStackLayout.topControl = axisConfigPageList.get(0).getComposite();
		axisCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				axisStackLayout.topControl = axisConfigPageList.get(axisCombo.getSelectionIndex()).getComposite();
				axisConfigComposite.layout(true, true);
			}
		});
		
		//plot config
		Group plotConfigGroup = new Group(container, SWT.NONE);
		plotConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		plotConfigGroup.setText("Plot options");
		plotConfigGroup.setLayout(new GridLayout(1, false));
		
		tickLineButton = new Button(plotConfigGroup, SWT.CHECK);
		configCheckButton(tickLineButton, "Show Tick Line");
		tickLineButton.setSelection(axeLayout.isTickLineDisplayed());

		Label gridColorLabel = new Label(plotConfigGroup, 0);
		gridColorLabel.setText("Grid Color");
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridColorLabel.setLayoutData(gd);

		gridColorSelector = new ColorSelector(plotConfigGroup);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
		gridColorSelector.getButton().setLayoutData(gd);
		Color color = axeLayout.getMainColor();
		gridColorSelector.setColorValue(new RGB(getRGBInt(color.r), getRGBInt(color.g), getRGBInt(color.b)));

		showAxisButton = new Button(plotConfigGroup, SWT.CHECK);
		configCheckButton(showAxisButton, "Show box");
		showAxisButton.setSelection(axeLayout.isFaceDisplayed());

		return container;
	}

	/**
	 * 
	 * @param value
	 *            rgb value from 0.0 to 1.0
	 * @return int rgb value from 0 to 255
	 */
	public static int getRGBInt(float value) {
		int result = (int) (value * 255);
		return result;
	}

	private void configCheckButton(Button button, String text) {
		button.setText(text);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 2));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		Button applyButton = new Button(parent, SWT.PUSH);
		applyButton.setText("Apply");
		applyButton.setFont(JFaceResources.getDialogFont());
		applyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyChanges();
			}
		});
		setButtonLayoutData(applyButton);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		applyChanges();
		super.okPressed();
	}

	private void applyChanges() {
		for (AxisPreferenceConfig axisConfigPage : axisConfigPageList) {
			axisConfigPage.applyChanges();
		}
		IAxeLayout axeLayout = chart.getAxeLayout();
		axeLayout.setTickLineDisplayed(tickLineButton.getSelection());
		RGB color = gridColorSelector.getColorValue();
		axeLayout.setMainColor(new Color(color.red, color.green, color.blue));
		axeLayout.setFaceDisplayed(showAxisButton.getSelection());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure Graph Settings");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 750);
	}

}