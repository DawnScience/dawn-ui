package org.dawnsci.plotting.tools.history;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * TODO This class is actually generic, could be used elsewhere.
 * @author fcp94556
 *
 */
public class VariableNameLabelProvider extends ColumnLabelProvider {
	
	private Color BLUE;

	VariableNameLabelProvider() {
		BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	}

	@Override
	public String getText(Object ob) {
        return ((HistoryBean)ob).getVariable();
	}
	
	@Override
	public Color getForeground(Object ob) {
		return BLUE;
	}
}
