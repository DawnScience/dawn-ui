package org.dawnsci.dedi.configuration.preferences;

import java.util.Map.Entry;

import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * A {@link ComboWrapper} for a {@link Combo} that is read-only.
 */
public class ComboWrapperWithoutClearSelection extends ComboWrapper {
	public ComboWrapperWithoutClearSelection(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	public void setValue(Object value) {
		if (value == null) return;
		

		String textValue = itemMap != null ? getKeyForValue(value) : value.toString();
		if (textValue == null && itemMap != null && !itemMap.isEmpty())
			textValue = itemMap.keySet().iterator().next();
		if (textValue != null) {
			final int index = combo.indexOf(textValue);
			if (index < 0) {
				combo.clearSelection();
			} else {
				combo.select(index);
			}
		}
	}

	
	private String getKeyForValue(final Object value) {
		if (itemMap==null) return null;
		if (value  ==null) return null;
		for (Entry<String,?> entry : itemMap.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
