package org.dawnsci.plotting.api.axis;

import java.util.EventListener;

/**
 * It is a good idea to implement hasCode and equals so that removeAxisListener(...) works ok.
 * @author fcp94556
 *
 */
public interface IAxisListener extends EventListener{

	/**
	 * Called when axis changes range e.g. zoom.
	 * @param evt
	 */
	public void rangeChanged(AxisEvent evt);
	
	/**
	 * Called when axis needs to be revalidated on the redraw.
	 * @param evt
	 */
	public void revalidated(AxisEvent evt);

}
