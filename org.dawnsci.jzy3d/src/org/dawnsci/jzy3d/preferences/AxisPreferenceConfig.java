package org.dawnsci.jzy3d.preferences;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jzy3d.chart.Chart;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DateTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DefaultDecimalTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.IntegerTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.PitchTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ScientificNotationTickRenderer;

/**
 * This will help to create the necessary widgets to configure an axis's
 * properties.
 * 
 */
public class AxisPreferenceConfig {
	private Text titleText;
	private Label scaleFontLabel;
	private Font scaleFont;

	private Composite composite;
	private int index;
	private Button tickLabelButton;
	private Button axeLabelButton;
	private Combo axisTypeCombo;
	private Chart chart;
	private FloatSpinner minText;
	private FloatSpinner maxText;

	private double minimum;
	private double maximum;
	private int[] shape;
	
	private enum AxisType {
		DECIMAL(0, "Decimal"),
		INTEGER(1, "Integer"),
		DATE(2, "Date"),
		SI(3, "Scientific Notation"),
		PITCH(4, "Pitch"),
		FONT (5, "Font");
	
		private int value;
		private String name;

		AxisType(int value, String name) {
			this.setValue(value);
			this.setName(name);
		}

		public void setValue(int value) {
			this.value = value;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * Create an Axis Configuration Page for the config dialog.
	 *
	 * @param chart
	 *            chart
	 * @param i
	 *            index of axis
	 * @param shape
	 *            shape of data
	 */
	public AxisPreferenceConfig(Chart chart, int i, int[] shape) {
		this.chart = chart;
		this.index = i;
		this.shape = shape;
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
//					scaleFontLabel.setFont(scaleFont);
//					scaleFontLabel.setText("Scale Font: " + fontData.getName());
//					composite.getShell().layout(true, true);
				}
			}
		});
		
		axeLabelButton = new Button(composite, SWT.CHECK);
		PreferenceDialog.configCheckButton(axeLabelButton, "Show Title");

		tickLabelButton = new Button(composite, SWT.CHECK);
		PreferenceDialog.configCheckButton(tickLabelButton, "Show Tick");

		Label axisTypeLabel = new Label(composite, SWT.NONE);
		axisTypeLabel.setText("Select axis type:");
		axisTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		axisTypeCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		AxisType[] types = AxisType.values();
		for (int i = 0; i < types.length; i++) {
			axisTypeCombo.add(types[i].name);
		}

		// axis range
		Label rangeLabel = new Label(composite, SWT.NONE);
		rangeLabel.setText("Select Axis Range:");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		rangeLabel.setLayoutData(labelGd);

		Composite comp = new Composite(composite, SWT.NONE);
		comp.setLayout(new GridLayout(4, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		
		Label minLabel = new Label(comp, SWT.NONE);
		minLabel.setText("Min:");
		minLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		minText = new FloatSpinner(comp, SWT.BORDER);
		minText.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		Label maxLabel = new Label(comp, SWT.NONE);
		maxLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		maxLabel.setText("Max:");
		maxText = new FloatSpinner(comp, SWT.BORDER);
		maxText.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		minText.setFormat(9, 4);
		maxText.setFormat(9, 4);
		minText.setIncrement(1.0);
		maxText.setIncrement(1.0);

		initialize();
	}

	/**
	 * @return the composite
	 */
	public Composite getComposite() {
		return composite;
	}

	public void applyChanges() {
		IAxeLayout axeLayout = chart.getAxeLayout();
		
		BoundingBox3d bounds = chart.getView().getBounds();
		if (index == 0) { // x axis
			axeLayout.setXAxeLabel(titleText.getText());
			axeLayout.setXTickRenderer(getUpdatedRenderer());
			axeLayout.setXTickLabelDisplayed(tickLabelButton.getSelection());
			axeLayout.setXAxeLabelDisplayed(axeLabelButton.getSelection());
			bounds.setXmin((float) minText.getDouble());
			bounds.setXmax((float) maxText.getDouble());
		} else if (index == 1) { // y axis
			axeLayout.setYAxeLabel(titleText.getText());
			axeLayout.setYTickRenderer(getUpdatedRenderer());
			axeLayout.setYTickLabelDisplayed(tickLabelButton.getSelection());
			axeLayout.setYAxeLabelDisplayed(axeLabelButton.getSelection());
			bounds.setYmin((float) minText.getDouble());
			bounds.setYmax((float) maxText.getDouble());
		} else if (index == 2) { // z axis
			axeLayout.setZAxeLabel(titleText.getText());
			axeLayout.setZTickRenderer(getUpdatedRenderer());
			axeLayout.setZTickLabelDisplayed(tickLabelButton.getSelection());
			axeLayout.setZAxeLabelDisplayed(axeLabelButton.getSelection());
			bounds.setZmin((float) minText.getDouble());
			bounds.setZmax((float) maxText.getDouble());
		}

	}

	private void initialize() {
		IAxeLayout axeLayout = chart.getAxeLayout();
		BoundingBox3d bounds = chart.getView().getBounds();

		if (index == 0) {
			titleText.setText(axeLayout.getXAxeLabel());
			//tick type
			initializeRenderer(axeLayout.getXTickRenderer());
			
//			scaleFontLabel.setForeground(getaxis.getForegroundColor());
//			scaleFontLabel.setFont(scaleFont);
//			scaleFontLabel.setText("Scale Font: " + scaleFont.getFontData()[0].getName());
			tickLabelButton.setSelection(axeLayout.isXTickLabelDisplayed());
			axeLabelButton.setSelection(axeLayout.isXAxeLabelDisplayed());
			
			minText.setDouble(bounds.getXmin());
			maxText.setDouble(bounds.getXmax());

		} else if (index == 1) {
			titleText.setText(axeLayout.getYAxeLabel());
			initializeRenderer(axeLayout.getYTickRenderer());

			tickLabelButton.setSelection(axeLayout.isYTickLabelDisplayed());
			axeLabelButton.setSelection(axeLayout.isYAxeLabelDisplayed());
			
			minText.setDouble(bounds.getYmin());
			maxText.setDouble(bounds.getYmax());
			
		} else if (index == 2) {
			titleText.setText(axeLayout.getZAxeLabel());
			initializeRenderer(axeLayout.getZTickRenderer());

			tickLabelButton.setSelection(axeLayout.isZTickLabelDisplayed());
			axeLabelButton.setSelection(axeLayout.isZAxeLabelDisplayed());

			minText.setDouble(bounds.getZmin());
			maxText.setDouble(bounds.getZmax());
		}
		minimum = 0;
		maximum = shape[index];
		minText.setMinimum(minimum);
		minText.setMaximum(maximum);
		maxText.setMinimum(minimum);
		maxText.setMaximum(maximum);

	}

	private void initializeRenderer(ITickRenderer renderer) {
		if (renderer instanceof DefaultDecimalTickRenderer) {
			axisTypeCombo.select(AxisType.DECIMAL.value);
		} else if (renderer instanceof IntegerTickRenderer) {
			axisTypeCombo.select(AxisType.INTEGER.value);
		} else if (renderer instanceof DateTickRenderer) {
			axisTypeCombo.select(AxisType.DATE.value);
		} else if (renderer instanceof ScientificNotationTickRenderer) {
			axisTypeCombo.select(AxisType.SI.value);
		} else if (renderer instanceof PitchTickRenderer) {
			axisTypeCombo.select(AxisType.PITCH.value);
		}
	}

	private ITickRenderer getUpdatedRenderer() {
		ITickRenderer renderer = null;
		AxisType[] types = AxisType.values();
		for (int i = 0; i < types.length; i++) {
			if (axisTypeCombo.getSelectionIndex() == types[i].value) {
				switch (types[i]) {
				case DECIMAL:
					renderer = new DefaultDecimalTickRenderer();
					break;
				case INTEGER:
					renderer = new IntegerTickRenderer();
					break;
				case DATE:
					renderer = new DateTickRenderer();
					break;
				case SI:
					renderer = new ScientificNotationTickRenderer();
					break;
				case PITCH:
					renderer = new PitchTickRenderer();
					break;
				default:
					break;
				}
			}
		}
		return renderer;
	}
}