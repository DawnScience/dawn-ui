package org.dawnsci.common.widgets.decorator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;

class ArrayDecorator extends BoundsDecorator {

	protected String delimiter;

	public ArrayDecorator(Text text, String numberPattern, NumberFormat numFormat, String delimiter) {
		super(text, "("+numberPattern+delimiter+"? *)+", numFormat);
		this.delimiter = delimiter;
	}

	@Override
	protected boolean check(String totalString, String delta) {

		final List<String> strings = getList(totalString, delimiter);
		for (String string : strings) {
			if (!super.check(string, delta)) return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param value
	 * @return v
	 */
	private static List<String> getList(final String value, final String delimiter) {
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final String[]    vals = value.split(delimiter);
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++) ret.add(vals[i].trim());
		return ret;
	}

}
