package org.dawnsci.plotting.preference.detector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

public class DiffractionDetectorComposite extends Composite  {

	private TextWrapper    detectorName;
	private NumberBox      xPixelSize;
	private NumberBox      yPixelSize;

	public DiffractionDetectorComposite(Composite par, int config) {
		super(par, config);
		
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Name");
		
		this.detectorName = new TextWrapper(this, SWT.BORDER);
		detectorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		detectorName.setTextLimit(64);
		
		xPixelSize = createRangeBox("x pixel size", 0, 10, "mm");
		xPixelSize.setDecimalPlaces(5);
		xPixelSize.setName("x pixel size");
		
		yPixelSize = createRangeBox("y pixel size", 0, 10, "mm");
		yPixelSize.setDecimalPlaces(5);
		yPixelSize.setName("y pixel size");

	}

	private NumberBox createRangeBox(String label, double lower, double upper, String unit) {
		
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);

		NumberBox rb = new ScaleBox(this, SWT.NONE);
		rb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rb.setMinimum(lower);
		rb.setMaximum(upper);
		if (unit!=null) rb.setUnit(unit);
		
		return rb;
	}
	


	public TextWrapper getDetectorName() {
		return detectorName;
	}

	public NumberBox getXPixelMM() {
		return xPixelSize;
	}
	
	public NumberBox getYPixelMM() {
		return yPixelSize;
	}
	
}
