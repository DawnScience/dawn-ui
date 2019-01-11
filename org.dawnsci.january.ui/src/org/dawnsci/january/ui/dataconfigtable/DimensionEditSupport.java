package org.dawnsci.january.ui.dataconfigtable;

import java.util.LinkedHashSet;
import java.util.Set;

import org.dawnsci.january.model.NDimensions;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class DimensionEditSupport extends EditingSupport {

	private ComboBoxViewerCellEditor dimEditor;
	
	public DimensionEditSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		dimEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
		dimEditor.setLabelProvider(new LabelProvider());
		dimEditor.setContentProvider(new ArrayContentProvider());
		dimEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		Object[] dataOptions =((NDimensions)getViewer().getInput()).getOptions();
		Set<String> opSet = new LinkedHashSet<>();
		for (Object o : dataOptions) {
			opSet.add(o.toString());
		}
		
		dimEditor.setInput(opSet.toArray(new String[opSet.size()]));
		
		dimEditor.getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = getViewer().getSelection();
				if (!selection.isEmpty()) {
					CCombo cCombo = dimEditor.getViewer().getCCombo();
					String text = cCombo.getText();
					DimensionEditSupport.this.setValue(((StructuredSelection)selection).getFirstElement(), text);
					getViewer().refresh();
					
				}
			}
		});
		
		return dimEditor;
	}
	
	public boolean isActive() {
		return dimEditor != null && dimEditor.isActivated();
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((NDimensions)getViewer().getInput()).getDescription((int) element);
	}

	@Override
	protected void setValue(Object element, Object value) {
		String val = "";
		if (value != null) val = value.toString();
		((NDimensions)getViewer().getInput()).setDescription((int) element, val);
		getViewer().refresh();
	}

}
