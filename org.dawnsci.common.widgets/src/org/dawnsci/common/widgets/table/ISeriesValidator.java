package org.dawnsci.common.widgets.table;

import java.util.List;

public interface ISeriesValidator {

	/**
	 * 
	 * @param series
	 * @return null if series is valid or the message to show to the user
	 * if the series is valid.
	 */
	public String getErrorMessage(List<ISeriesItemDescriptor> series);
}
