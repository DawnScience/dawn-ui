package org.dawnsci.plotting.api.views;

import java.io.Serializable;

/**
 * Interface used to mark a view part as being able to accept data.
 * @author fcp94556
 *
 */
public interface ISettablePlotView {

	/**
	 * Call this method to send data to the PlotView.
	 * 
	 * For instance if a peak fitting tool would like to 
	 * send the peaks back to the GuiBean to be sent to 
	 * the server, it may call setPlottedData(...) with a 
	 * list of IPeak objects to define the peaks.
	 * 
	 * @param data
	 * @param clazz
	 */
	public void updateData(Serializable data, Class<?> clazz);

	
}
