package org.dawnsci.isosurface.isogui;

import java.util.List;

import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class IncrementGroupSelectionListener extends SelectionAdapter{
	private final List<IFieldWidget> composites;
	private final int increment;

	public IncrementGroupSelectionListener(List<IFieldWidget> composites, int increment) {
		this.composites = composites;
		this.increment = increment;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		composites.forEach(widget -> widget.setValue(((int)widget.getValue()) + increment));
	}
}
