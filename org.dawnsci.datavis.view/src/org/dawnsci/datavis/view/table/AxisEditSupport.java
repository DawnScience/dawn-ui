package org.dawnsci.datavis.view.table;

import org.dawnsci.datavis.model.NDimensions;
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

public class AxisEditSupport extends EditingSupport {
	
	private ComboBoxViewerCellEditor axisEditor;
	
	public AxisEditSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		axisEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
		axisEditor.setLabelProvider(new LabelProvider());
		axisEditor.setContentProvider(new ArrayContentProvider());
		axisEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		String[] axisOptions = ((NDimensions)getViewer().getInput()).getAxisOptions((int)element);
		axisEditor.setInput(axisOptions);
		
		axisEditor.getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = getViewer().getSelection();
				if (!selection.isEmpty()) {
					CCombo cCombo = axisEditor.getViewer().getCCombo();
					String text = cCombo.getText();
					AxisEditSupport.this.setValue(((StructuredSelection)selection).getFirstElement(), text);
					getViewer().refresh();
					
				}
			}
		});
		
		return axisEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	public boolean isActive() {
		return axisEditor != null && axisEditor.isActivated();
	}
	
	@Override
	protected Object getValue(Object element) {
		return ((NDimensions)getViewer().getInput()).getAxis((int)element);
	}

	@Override
	protected void setValue(Object element, Object value) {
		((NDimensions)getViewer().getInput()).setAxis((int)element,(String)value);
		getViewer().refresh();

	}

}
