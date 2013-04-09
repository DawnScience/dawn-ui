package org.dawnsci.plotting.api.region;

import java.util.EventObject;

public abstract class MouseEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2140030391287691594L;

	public MouseEvent(Object source) {
		super(source);
	}

	public abstract int getButton();

	public abstract int getX();
	
	public abstract int getY();

}
