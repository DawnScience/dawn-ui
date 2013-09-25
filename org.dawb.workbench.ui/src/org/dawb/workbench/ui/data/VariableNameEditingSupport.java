package org.dawb.workbench.ui.data;

import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.dawb.common.services.ServiceManager;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

class VariableNameEditingSupport extends EditingSupport {

	
	private boolean isVariableNameActive = false;
	private IExpressionObjectService service;
	private IVariableManager manager;

	public VariableNameEditingSupport(ColumnViewer viewer, IVariableManager manager) throws Exception {
		super(viewer);
		this.service  = (IExpressionObjectService)ServiceManager.getService(IExpressionObjectService.class);
		this.manager  = manager;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor((Composite)getViewer().getControl());
	}

	@Override
	protected boolean canEdit(Object element) {
		return isVariableNameActive;
	}

	@Override
	protected Object getValue(Object element) {
		return ((ITransferableDataObject)element).getVariable();
	}

	@Override
	protected void setValue(Object element, Object value) {
		ITransferableDataObject data  = (ITransferableDataObject)element;
		try {
    		if (data.getVariable()!=null && data.getVariable().equals(value)) return;
    		if (data.getVariable()!=null && value!=null && data.getVariable().equals(((String)value).trim())) return;
            String variableName = service.validate(manager, (String)value);

			manager.clearExpressionCache(variableName, data.getVariable());
			data.setVariable(variableName);
			manager.saveExpressions();
			
		} catch (Exception e) {
			final String message = "The name '"+value+"' is not valid.";
			final Status status  = new Status(Status.WARNING, "org.dawb.workbench.ui", message, e);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot rename data", message, status);
		    return;
		}
		getViewer().refresh();
	}

	public boolean isVariableNameActive() {
		return isVariableNameActive;
	}

	public void setVariableNameActive(boolean isVariableNameActive) {
		this.isVariableNameActive = isVariableNameActive;
	}

}
