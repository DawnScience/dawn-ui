package org.dawb.workbench.ui.diffraction.table;

import java.util.EventObject;

public class ImageDroppedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6998753742221406834L;
	public int x, y;

	public ImageDroppedEvent(Object source, int x, int y) {
		super(source);
		this.x = x;
		this.y = y;
	}
}
