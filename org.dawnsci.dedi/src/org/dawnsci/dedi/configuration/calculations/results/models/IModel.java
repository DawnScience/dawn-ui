package org.dawnsci.dedi.configuration.calculations.results.models;

import java.beans.PropertyChangeListener;

public interface IModel {
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
