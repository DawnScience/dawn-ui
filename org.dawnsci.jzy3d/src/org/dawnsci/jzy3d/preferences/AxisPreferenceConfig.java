package org.dawnsci.jzy3d.preferences;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;

/**
 * This will help to create the necessary widgets to configure an axis's
 * properties.
 * 
 */
public class AxisPreferenceConfig {
	private Text titleText;
	private Label scaleFontLabel;
	private Font scaleFont;
	private Label titleFontLabel;
	private Font titleFont;
	private ColorSelector tickColorSelector;

	private Button dateEnabledButton;
	private Button autoFormat;
	private Label formatLabel;
	private Text formatText;

	private Composite composite;
	private int index;
	private IAxeLayout axeLayout;

	/**
	 * Create an Axis Configuration Page for the config dialog.
	 *
	 * @param axeLayout
	 *            axeLayout
	 * @param i
	 *            index of axis
	 */
	public AxisPreferenceConfig(IAxeLayout axeLayout, int i) {
		this.axeLayout = axeLayout;
		this.index = i;
	}

	public void createComposite(final Composite composite) {
		this.composite = composite;
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(3, false));
		GridData gd;
		GridData labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);

		final Label titleLabel = new Label(composite, 0);
		titleLabel.setText("Title: ");
		titleLabel.setLayoutData(labelGd);

		titleText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		titleText.setLayoutData(gd);

		titleFontLabel = new Label(composite, 0);
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		titleFontLabel.setLayoutData(labelGd);

		final Button titleFontButton = new Button(composite, SWT.PUSH);
		titleFontButton.setText("Change...");
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
		titleFontButton.setLayoutData(gd);
		titleFontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog fontDialog = new FontDialog(composite.getShell());
				if (titleFont != null)
					fontDialog.setFontList(titleFont.getFontData());
				FontData fontData = fontDialog.open();
				if (fontData != null) {
//					titleFont = XYGraphMediaFactory.getInstance().getFont(fontData);
//					titleFontLabel.setFont(titleFont);
//					titleFontLabel.setText("Title Font: " + fontData.getName());
//					composite.getShell().layout(true, true);
				}
			}
		});

		scaleFontLabel = new Label(composite, 0);
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		scaleFontLabel.setLayoutData(labelGd);

		final Button scaleFontButton = new Button(composite, SWT.PUSH);
		scaleFontButton.setText("Change...");
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
		scaleFontButton.setLayoutData(gd);
		scaleFontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog fontDialog = new FontDialog(composite.getShell());
				if (scaleFont != null)
					fontDialog.setFontList(scaleFont.getFontData());
				FontData fontData = fontDialog.open();
				if (fontData != null) {
//					scaleFont = XYGraphMediaFactory.getInstance().getFont(fontData);
					scaleFontLabel.setFont(scaleFont);
					scaleFontLabel.setText("Scale Font: " + fontData.getName());
					composite.getShell().layout(true, true);
				}
			}
		});

		final Label colorLabel = new Label(composite, 0);
		colorLabel.setText("Tick Color:");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		colorLabel.setLayoutData(labelGd);

		tickColorSelector = new ColorSelector(composite);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
		tickColorSelector.getButton().setLayoutData(gd);
		tickColorSelector.addListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
//				scaleFontLabel.setForeground(XYGraphMediaFactory.getInstance().getColor(axisColorSelector.getColorValue()));
//				titleFontLabel.setForeground(XYGraphMediaFactory.getInstance().getColor(axisColorSelector.getColorValue()));
			}
		});

//		dateEnabledButton = new Button(composite, SWT.CHECK);
//		configCheckButton(dateEnabledButton, "Time Format Enabled");
//
//		autoFormat = new Button(composite, SWT.CHECK);
//		configCheckButton(autoFormat, "Auto Format");
//
//		formatLabel = new Label(composite, 0);
//		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
//		formatLabel.setLayoutData(labelGd);
//		formatText = new Text(composite, SWT.BORDER | SWT.MULTI);
//		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
//		gd.minimumHeight = 40;
//		formatText.setLayoutData(gd);
//
//		dateEnabledButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				boolean saveDateEnabled = axis.isDateEnabled();
//				boolean saveAutoFormat = axis.isAutoFormat();
//				axis.setDateEnabled(dateEnabledButton.getSelection());
//				axis.setAutoFormat(true);
//				formatLabel.setText(dateEnabledButton.getSelection() ? "Time Format: " : "Numeric Format: ");
//				formatText.setText(axis.getFormatPattern());
//				axis.setDateEnabled(saveDateEnabled);
//				axis.setAutoFormat(saveAutoFormat);
//				composite.getShell().layout(true, true);
//			}
//		});

//		autoFormat.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				formatText.setEnabled(!autoFormat.getSelection());
//				if (autoFormat.getSelection()) {
//					boolean saveDateEnabled = axis.isDateEnabled();
//					boolean saveAutoFormat = axis.isAutoFormat();
//					axis.setDateEnabled(dateEnabledButton.getSelection());
//					axis.setAutoFormat(autoFormat.getSelection());
//					formatText.setText(axis.getFormatPattern());
//					axis.setDateEnabled(saveDateEnabled);
//					axis.setAutoFormat(saveAutoFormat);
//				}
//			}
//		});

//		Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
//		sep.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));

		initialize();
	}

	/**
	 * @return the composite
	 */
	public Composite getComposite() {
		return composite;
	}

	public void applyChanges() {
		RGB color = tickColorSelector.getColorValue();
		if (index == 0) { // x axis
			axeLayout.setXAxeLabel(titleText.getText());
			axeLayout.setXTickColor(new Color(color.red, color.green, color.blue));
			
		} else if (index == 1) { // y axis
			axeLayout.setYAxeLabel(titleText.getText());
			axeLayout.setYTickColor(new Color(color.red, color.green, color.blue));

		} else if (index == 2) { // z axis
			axeLayout.setZAxeLabel(titleText.getText());
			axeLayout.setZTickColor(new Color(color.red, color.green, color.blue));

		}

//		axis.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(axisColorSelector.getColorValue()));
//		// must be set before autoScale as we update the maxOrAutoScaleThrText
//		// button as well
//		axis.setDateEnabled(dateEnabledButton.getSelection());
//		axis.setAutoFormat(autoFormat.getSelection());
//		if (!autoFormat.getSelection()) {
//			String saveFormat = axis.getFormatPattern();
//			try {
//				axis.setFormatPattern(formatText.getText());
//				axis.format(0);
//			} catch (Exception e) {
//				axis.setFormatPattern(saveFormat);
//				MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
//				mb.setMessage("Failed to set format due to incorrect format pattern: " + e.getMessage());
//				mb.setText("Format pattern error!");
//				mb.open();
//			}
//		}
		
	}

	private void initialize() {
		Color color = null;
		if (index == 0) {
			titleText.setText(axeLayout.getXAxeLabel());
			color = axeLayout.getXTickColor();
			tickColorSelector.setColorValue(new RGB(PreferenceDialog.getRGBInt(color.r), PreferenceDialog.getRGBInt(color.g), PreferenceDialog.getRGBInt(color.b)));

		} else if (index == 1) {
			titleText.setText(axeLayout.getYAxeLabel());
			color = axeLayout.getXTickColor();
			tickColorSelector.setColorValue(new RGB(PreferenceDialog.getRGBInt(color.r), PreferenceDialog.getRGBInt(color.g), PreferenceDialog.getRGBInt(color.b)));

		} else if (index == 2) {
			titleText.setText(axeLayout.getZAxeLabel());
			color = axeLayout.getXTickColor();
		}
		tickColorSelector.setColorValue(new RGB(PreferenceDialog.getRGBInt(color.r), PreferenceDialog.getRGBInt(color.g), PreferenceDialog.getRGBInt(color.b)));

//		scaleFontLabel.setForeground(axis.getForegroundColor());
//		scaleFontLabel.setFont(scaleFont);
//		scaleFontLabel.setText("Scale Font: " + scaleFont.getFontData()[0].getName());
//		titleFontLabel.setForeground(axis.getForegroundColor());
//		titleFontLabel.setFont(titleFont);
//		titleFontLabel.setText("Title Font: " + titleFont.getFontData()[0].getName());
//		dateEnabledButton.setSelection(axis.isDateEnabled());
//		autoFormat.setSelection(axis.isAutoFormat());
//		formatLabel.setText(dateEnabledButton.getSelection() ? "Time Format: " : "Numeric Format: ");
//		formatText.setText(axis.getFormatPattern());
		
		// formatLabel.setVisible(!autoFormat.getSelection());
//		formatText.setEnabled(!autoFormat.getSelection());


	}

}