package org.dawnsci.plotting.tools.preference.detector;

import org.dawnsci.common.richbeans.components.scalebox.NumberBox;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DiffractionDetectorComposite extends Composite  {

	private TextWrapper    detectorName;
	private NumberBox      xPixelSize;
	private NumberBox      yPixelSize;
	private NumberBox      numberOfPixelsX;
	private NumberBox      numberOfPixelsY;

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
		
		numberOfPixelsX = createRangeBox("number of pixels X", 0, 1000000, "pixels");
		numberOfPixelsX.setDecimalPlaces(0);
		numberOfPixelsX.setName("No of Pixels (X)");
		
		numberOfPixelsY = createRangeBox("number of pixels Y", 0, 1000000, "pixels");
		numberOfPixelsY.setDecimalPlaces(0);
		numberOfPixelsY.setName("No of Pixels (Y)");

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
	
	public NumberBox getNumberOfPixelsX() {
		return numberOfPixelsX;
	}
	
	public NumberBox getNumberOfPixelsY() {
		return numberOfPixelsY;
	}
	
}
