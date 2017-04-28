package org.dawnsci.common.widgets.statuscomposite;

import java.util.HashSet;

import org.eclipse.swt.widgets.Composite;

/** A composite with listeners for a status change
 *  
 *  Useful for dialogs whose Ok button should be active only when input data is valid.
 * 
 * @author awf63395
 *
 */
public class StatusComposite extends Composite {

	private final HashSet<IStatusCompositeChangedListener> listeners = new HashSet<>();

	public StatusComposite(Composite parent, int style) {
		super(parent, style);
	}

	
	public void addStatusCompositeChangedListener(IStatusCompositeChangedListener listener) {
		listeners.add(listener);
	}
	
	public void removeStatusCompositeChangedListener(IStatusCompositeChangedListener listener) {
		listeners.remove(listener);
	}
	
	protected final void fireListeners(boolean status) {
		StatusCompositeChangedEvent event = new StatusCompositeChangedEvent(this, status);
		for (IStatusCompositeChangedListener listener : listeners) 
			listener.compositeStatusChanged(event);
	}
	
}
