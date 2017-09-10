package org.dawnsci.dedi.ui.widgets.units;

import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ComboUnitsProvider<T extends Quantity> extends WidgetUnitsProvider<T> {
	private Combo unitsCombo;
	private ComboViewer unitsComboViewer;

	public ComboUnitsProvider(Composite parent, List<Unit<T>> units) {
		super(parent);
		
		unitsCombo = new Combo(this, SWT.READ_ONLY);
		unitsComboViewer = new ComboViewer(unitsCombo);
		unitsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		unitsComboViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					if(element instanceof Unit<?>){
						@SuppressWarnings("unchecked")
						Unit<T> unit = (Unit<T>) element;
						return unit.toString();
					}
					return super.getText(element);
				}
		});
		
		unitsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0)
					 setCurrentUnit((Unit<T>) selection.getFirstElement());
			}
		});
		
		setUnits(units);
	}
	
	
	private void setCurrentUnit(Unit<T> newUnit){
		currentUnit = newUnit;
		notifyListeners();
		layout();
		getParent().layout();
	}
	
	
	public void setUnits(List<Unit<T>> units){
		checkWidget();
		unitsComboViewer.setInput(units);
		if(units != null && !units.isEmpty())
			unitsComboViewer.setSelection(new StructuredSelection(units.get(0)));
		else setCurrentUnit(null);
	}
}