package org.dawnsci.common.widgets.decorator;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Text;

public class FloatDecorator extends BoundsDecorator {

	public FloatDecorator(Text text) {
		super(text, "[-0-9\\.∞]+", DecimalFormat.getNumberInstance());
	}

}
