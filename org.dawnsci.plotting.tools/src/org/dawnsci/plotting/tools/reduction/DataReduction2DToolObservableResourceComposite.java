package org.dawnsci.plotting.tools.reduction;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.eclipse.swt.widgets.Composite;

/** mix of uk.ac.gda.client.ResourceComposite and org.dawnsci.ede.rcp.ObservableResourceComposite
 * 
 * @author awf63395
 *
 */
abstract class DataReduction2DToolObservableResourceComposite extends Composite {
	
	public DataReduction2DToolObservableResourceComposite(Composite parent, int style) {
		super(parent, style);
		this.addDisposeListener(e -> disposeResource());
	}
	
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
	
	protected abstract void disposeResource();
}
