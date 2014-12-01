package org.dawnsci.processing.ui.model;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class OperationModelView extends ViewPart implements ISelectionListener {

	private OperationModelViewer modelEditor;
	
	@Override
	public void createPartControl(Composite parent) {
		EclipseUtils.getPage().addSelectionListener(this);

		modelEditor = new OperationModelViewer();
		modelEditor.createPartControl(parent);
		
		getSite().setSelectionProvider(modelEditor);
	}

	@Override
	public void setFocus() {
		modelEditor.setFocus();
	}
	
	@Override
	public void dispose() {
		if (modelEditor!=null) modelEditor.dispose();
		if (EclipseUtils.getPage()!=null) EclipseUtils.getPage().removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			if (ob instanceof OperationDescriptor) {
				OperationDescriptor des = (OperationDescriptor)ob;
				final String       name = des.getName();
				setPartName("Model '"+name+"'");
			}
		}		
	}

}
