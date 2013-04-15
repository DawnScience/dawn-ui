package org.dawnsci.plotting.api.axis;

import java.util.EventListener;

/**
 * Interface listener for reporting data position 
 * when the mouse moves over the graph.
 * 
 * @author fcp94556
 *
 */
public interface IPositionListener extends EventListener{

	/**
	 * Notifies as the mouse is dragged over the plot.
	 * Please do not do heavy work in this callback!
	 * 
	 * @param evt
	 */
	void positionChanged(PositionEvent evt);
}
