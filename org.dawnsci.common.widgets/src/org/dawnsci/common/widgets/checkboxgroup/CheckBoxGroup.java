package org.dawnsci.common.widgets.checkboxgroup;

import org.eclipse.swt.widgets.Composite;

/**
 * Override the class with the same name from org.mihalis.opal in order to avoid the
 * rather silly methods activate and deactivate, thereby bringing it more in line with
 * SWT API...
 * @author awf63395
 *
 */
public class CheckBoxGroup extends org.mihalis.opal.checkBoxGroup.CheckBoxGroup {

	public CheckBoxGroup(Composite arg0, int arg1) {
		super(arg0, arg1);
	}
	
	public void setSelection(boolean selection) {
		if (selection) {
			activate();
		} else {
			deactivate();
		}
	}
	
	public boolean getSelection() {
		return isActivated();
	}
}
