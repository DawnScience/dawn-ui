package org.dawnsci.common.widgets.periodictable;

import java.util.EventObject;

import org.eclipse.swt.widgets.Button;

public class PeriodicTableButtonPressedEvent extends EventObject {

	private static final long serialVersionUID = -6699558656854129569L;
	private final int Z;
	private final String element;
	private final Button button;
	
	public PeriodicTableButtonPressedEvent(Object source, int Z, String element, Button button) {
		super(source);
		this.Z = Z;
		this.element = element;
		this.button = button;
	}

	public int getZ() {
		return Z;
	}
	
	public String getElement() {
		return element;
	}
	
	public Button getButton() {
		return button;
	}
}
