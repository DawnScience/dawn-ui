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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class DimensionEditSupport extends EditingSupport {

	private ComboBoxViewerCellEditor dimensionEditor = null;
	
	public DimensionEditSupport(ColumnViewer viewer, NDimensions ndims) {
		super(viewer);
		dimensionEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl());
		dimensionEditor.setLabelProvider(new LabelProvider());
		dimensionEditor.setContentProvider(new ArrayContentProvider());
		dimensionEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		setNDimensions(ndims);
		dimensionEditor.getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (!selection.isEmpty()) {
					CCombo cCombo = dimensionEditor.getViewer().getCCombo();
					String text = cCombo.getText();
					DimensionEditSupport.this.setValue(((StructuredSelection)selection).getFirstElement(), text);
					
				}
			}
		});
	}
	
	public void setNDimensions(NDimensions d){
		if (d == null || d.getOptions() == null) return;
		dimensionEditor.setInput(d.getOptions());
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return dimensionEditor;
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
