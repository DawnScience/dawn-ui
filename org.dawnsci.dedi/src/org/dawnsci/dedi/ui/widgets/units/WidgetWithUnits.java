package org.dawnsci.dedi.ui.widgets.units;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jscience.physics.amount.Amount;

public abstract class WidgetWithUnits<T extends Quantity> extends Composite {
	protected IUnitsProvider<T> unitsProvider;
	protected Amount<T> currentAmount;
	private List<IAmountChangeListener> listeners; 
	
	public WidgetWithUnits(Composite parent, String name, IUnitsProvider<T> provider) {
		super(parent, SWT.NONE);
		
		GuiHelper.createLabel(this, name);
		unitsProvider = provider;
		listeners = new ArrayList<>(); 
		
		provider.addUnitsChangeListener(new IUnitsChangeListener() {
			@Override
			public void unitsChanged() {
				if(currentAmount != null) 
					setValue(currentAmount);
			}
		});
	}
	
	
	public void addAmountChangeListener(IAmountChangeListener listener){
		checkWidget();
		listeners.add(listener);
	}
	
	
	public void removeAmountChangeListener(IAmountChangeListener listener){
		checkWidget();
		listeners.remove(listener);
	}
	
	
	public void addUnitsChangeListener(IUnitsChangeListener listener){
		checkWidget();
		unitsProvider.addUnitsChangeListener(listener);
	}
	
	
	public void removeUnitsChangeListener(IUnitsChangeListener listener){
		checkWidget();
		unitsProvider.removeUnitsChangeListener(listener);
	}
	
	
	protected void notifyListeners(){
		for(IAmountChangeListener listener : listeners) listener.amountChanged();
	}
	
	
	public Amount<T> getValue(Unit<T> unit){
		checkWidget();
		if(currentAmount == null) return null;
		return currentAmount.to(unit);
	}
	

	public Amount<T> getValue(){
		checkWidget();
		return getValue(unitsProvider.getCurrentUnit());
	}
	
	
	public Unit<T> getCurrentUnit(){
		checkWidget();
		return unitsProvider.getCurrentUnit();
	}
	
	
	public void clear(){
		checkWidget();
		currentAmount = null;
		notifyListeners();
	}
	
	
	public void setValue(Amount<T> value){
		checkWidget();
		currentAmount = (value == null) ? null : value.to(unitsProvider.getCurrentUnit());
		notifyListeners();
		layout();
		getParent().layout();
	}
	
	
	public void setValue(double value){
		checkWidget();
		currentAmount = Amount.valueOf(value, unitsProvider.getCurrentUnit());
		notifyListeners();
		layout();
		getParent().layout();
	}
}
