package org.dawnsci.plotting.tools.reduction;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Copy of uk.ac.gda.beans.ObservableModel */
class DataReduction2DToolObservableModel {
	private final PropertyChangeSupport changeSupport =
			new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener
			listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener
			listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void clearListeners() {
		for(PropertyChangeListener object : changeSupport.getPropertyChangeListeners()) {
			changeSupport.removePropertyChangeListener(object);
		}
	}

	protected void firePropertyChange(String propertyName,
			Object oldValue,
			Object newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}
