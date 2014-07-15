package org.dawb.workbench.ui.data;

import java.util.List;

import org.dawb.workbench.ui.transferable.TransferableDataObject;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

class AxisEditingSupport extends EditingSupport {

	private PlotDataComponent plotComponent;

	public AxisEditingSupport(ColumnViewer viewer, PlotDataComponent plotComponent) {
		super(viewer);
		this.plotComponent = plotComponent;
	}
	
	@Override
	protected CellEditor getCellEditor(final Object element) {
		// FIX to http://jira.diamond.ac.uk/browse/DAWNSCI-380 remove axes until they work
		ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite)getViewer().getControl(), new String[]{"X","Y1","Y2" /**,"Y3","Y4" **/} , SWT.READ_ONLY) {
			public void setFocus() {
				super.setFocus();
				((CCombo)getControl()).setListVisible(true);
			}
		};
		final CCombo ccombo = (CCombo)ce.getControl();
		ccombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setValue(element, ccombo.getSelectionIndex());
			}
		});
		return ce;
	}

	@Override
	protected boolean canEdit(Object element) {
		ITransferableDataObject co = (ITransferableDataObject)element;
		return co.isChecked();
	}

	@Override
	protected Object getValue(Object element) {
		AbstractPlottingSystem asys = (AbstractPlottingSystem)plotComponent.getAdapter(IPlottingSystem.class);
		List<ITransferableDataObject> selections = plotComponent.getSelections();
		return ((ITransferableDataObject)element).getAxisIndex(selections, asys.isXFirst());
	}

	@Override
	protected void setValue(Object element, Object value) {
		getViewer().cancelEditing();
		TransferableDataObject co = (TransferableDataObject)element;
		if (value instanceof Integer) {
			int isel = ((Integer)value).intValue();
			if (isel==0) {
				plotComponent.setAsX(co);
			} else {
				
				List<ITransferableDataObject> selections = plotComponent.getSelections();
				AbstractPlottingSystem asys = (AbstractPlottingSystem)plotComponent.getAdapter(IPlottingSystem.class);
				if (asys.isXFirst() && "X".equals(co.getAxis(selections, asys.is2D(), true))) {
					// We lost an x
					asys.setXFirst(false);
				}
				co.setYaxis(isel);
                
				asys.clear();
				plotComponent.fireSelectionListeners(selections);
				getViewer().refresh();
			}
		}
	}
	
}
