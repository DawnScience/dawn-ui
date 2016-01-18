package org.dawnsci.mapping.ui.datamodel;

public interface ILiveData {

	/**
	 * Connect to remote datasets
	 */
	public boolean connect();
	
	/**
	 * Disconnect to remote datasets
	 */
	public boolean disconnect();
	
}
