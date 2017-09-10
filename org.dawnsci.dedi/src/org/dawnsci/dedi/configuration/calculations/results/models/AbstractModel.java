package org.dawnsci.dedi.configuration.calculations.results.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractModel implements IModel {
	 protected PropertyChangeSupport propertyChangeSupport;

	    public AbstractModel() {
	        propertyChangeSupport = new PropertyChangeSupport(this);
	    }

	    
	    @Override
	    public void addPropertyChangeListener(PropertyChangeListener listener) {
	        propertyChangeSupport.addPropertyChangeListener(listener);
	    }

	    
	    @Override
	    public void removePropertyChangeListener(PropertyChangeListener listener) {
	        propertyChangeSupport.removePropertyChangeListener(listener);
	    }

	    
	    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	    }
}
