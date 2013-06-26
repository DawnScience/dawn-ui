package org.dawnsci.common.widgets.decorator;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Text;

public class IntegerDecorator extends BoundsDecorator {

	public IntegerDecorator(Text text) {
		super(text, "[-0-9∞]+", DecimalFormat.getIntegerInstance());
	}

}
