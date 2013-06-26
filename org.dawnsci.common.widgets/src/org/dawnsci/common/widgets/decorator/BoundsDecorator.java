package org.dawnsci.common.widgets.decorator;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A regexp decorator which will only accept numbers and 
 * will color invalid bounds red.
 * 
 * @author fcp94556
 *
 */
class BoundsDecorator extends RegexDecorator {

    private Number       maximum;
    private Number       minimum;
    private NumberFormat numberFormat;

	public BoundsDecorator(Text text, String stringPattern, NumberFormat numFormat) {
		super(text, stringPattern);
		numberFormat = numFormat;
	}
	
	@Override
	protected boolean check(String totalString, String delta) {
		Number val = null;
		try {
			val = Double.parseDouble(totalString);
		} catch (Throwable ne) {
			return "".equals(delta) || delta==null || "-".equals(totalString); // Will not allow current value to proceed.
		}
		if (val==null) return false;
		
		checkBounds(val); // Colors red not unacceptable value.
		
		return true;
	}

	public Number getValue() {
		return Double.parseDouble(text.getText());
	}

	private void checkBounds(Number value) {
		if (!checkMinimum(value)) {
			setError(true, value+" is less than the minimum value of "+getMinimum());
			return;
		}
		if (!checkMaximum(value)) {
			setError(true, value+" is greater than the maximum value of "+getMinimum());
			return;
		}
		setError(false, createToolTipTextFromBounds(value));
	}


	private String createToolTipTextFromBounds(Number value) {

		if (noBounds()) return "Please enter a number.";
		
		final StringBuilder buf = new StringBuilder();

		if (minimum!=null) {
			if (Double.isInfinite(minimum.doubleValue())) {
				buf.append("-∞");
			} else {
				buf.append(numberFormat.format(minimum));
			}
			buf.append(" <= ");
		}
		
		buf.append(numberFormat.format(value));

		if (maximum!=null) {
			
			buf.append(" >= ");

			if (Double.isInfinite(maximum.doubleValue())) {
				buf.append("∞");
			} else {
				buf.append(numberFormat.format(maximum));
			}
		}
        return buf.toString();
	}

	private final boolean noBounds() {
		return minimum == null && maximum == null;
	}

	private void setError(boolean isError, String toolTip) {
		text.setToolTipText(toolTip);
		if (isError) {
			text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED)); 
		} else {
			text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK)); 
		}
	}

	/**
	 * 
	 * @param value
	 * @return false if max exceeded.
	 */
	private boolean checkMaximum(Number value) {
		if (getMaximum()==null) return true;
		return value.doubleValue() <= getMaximum().doubleValue();
	}

	/**
	 * 
	 * @param value
	 * @return false if min not reached.
	 */
	private boolean checkMinimum(Number value) {
		if (getMinimum()==null) return true;
		return value.doubleValue() >= getMinimum().doubleValue();
	}

	public Number getMaximum() {
		return maximum;
	}

	public void setMaximum(Number maximum) {
		this.maximum = maximum;
		if (text.getText()==null || "".equals(text.getText())) return;
		checkBounds(getValue());
	}

	public Number getMinimum() {
		return minimum;
	}

	public void setMinimum(Number minimum) {
		this.minimum = minimum;
		if (text.getText()==null || "".equals(text.getText())) return;
		checkBounds(getValue());
	}

	
	public static void main(String[] args) {
		
		Display display = new Display();
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		
		Composite composite = new Composite(shell, SWT.NORMAL);
		composite.setLayout(new GridLayout());
		
		Text integerText = new Text(composite, SWT.BORDER);
		integerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new IntegerDecorator(integerText);
		integerText.setText("10");
		
		integerText = new Text(composite, SWT.BORDER);
		integerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		IntegerDecorator id = new IntegerDecorator(integerText);
		integerText.setText("10");
		id.setMaximum(100);
		id.setMinimum(0);
		
		integerText = new Text(composite, SWT.BORDER);
		integerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		id = new IntegerDecorator(integerText);
		integerText.setText("1000");
		id.setMaximum(100);
		id.setMinimum(0);
		
		Text realText = new Text(composite, SWT.BORDER);
		realText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new FloatDecorator(realText);
		realText.setText("10.7");
		
		realText = new Text(composite, SWT.BORDER);
		realText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new FloatDecorator(realText);
		realText.setText("BAD");

		realText = new Text(composite, SWT.BORDER);
		realText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		FloatDecorator fd = new FloatDecorator(realText);
		realText.setText("10.00");
		fd.setMaximum(100);
		fd.setMinimum(0);
		
		realText = new Text(composite, SWT.BORDER);
		realText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fd = new FloatDecorator(realText);
		realText.setText("10000.00");
		fd.setMaximum(100);
		fd.setMinimum(0);

		Text none = new Text(composite, SWT.NORMAL);
		
		composite.pack();
		shell.pack();
		shell.open();
		while (!shell.isDisposed()){
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

}
