package org.dawnsci.plotting.api.axis;

import java.util.EventListener;

public interface IClickListener extends EventListener {

	/**
	 * Called when a click happens in the plotting.
	 * 
	 * @param evt
	 */
	public void clickPerformed(ClickEvent evt);
	
	/**
	 * Called when a click happens in the plotting.
	 * (May also call clickPerformed(...) )
	 * 
	 * @param evt
	 */
	public void doubleClickPerformed(ClickEvent evt);

}
