package org.dawnsci.plotting.api.trace;

import java.util.EventObject;

/**
 * Event fired to notify of the stack position changing.
 * @author fcp94556
 *
 */
public class StackPositionEvent extends EventObject {

	private int position;

	public StackPositionEvent(Object arg0, int position) {
		super(arg0);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8818838204678926655L;

}
