package org.dawnsci.common.widgets.decorator;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Text;

public class PercentDecorator extends BoundsDecorator {

	public PercentDecorator(Text text) {
		super(text, "[-0-9\\.]+", DecimalFormat.getPercentInstance());
	}

}
