package org.dawnsci.dedi.ui.widgets.units;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class WidgetUnitsProvider<T extends Quantity> extends Composite implements IUnitsProvider<T>{
	protected Unit<T> currentUnit;
	protected List<IUnitsChangeListener> listeners; 
	
	public WidgetUnitsProvider(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		
		currentUnit = null;
		listeners = new ArrayList<>();
	}
	
	@Override
	public Unit<T> getCurrentUnit(){
		checkWidget();
		return currentUnit;
	}
	
	@Override
	public void addUnitsChangeListener(IUnitsChangeListener listener){
		checkWidget();
		listeners.add(listener);
	}
	
	
	@Override
	public void removeUnitsChangeListener(IUnitsChangeListener listener) {
		checkWidget();
		listeners.remove(listener);
	}
	
	
	protected void notifyListeners(){
		for(IUnitsChangeListener listener : listeners) listener.unitsChanged();
	}
}
