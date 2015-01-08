package org.dawnsci.processing.ui.model;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.dawnsci.processing.ui.slice.IOperationGUIRunnerListener;
import org.dawnsci.processing.ui.slice.IOperationInputData;
import org.dawnsci.processing.ui.slice.OperationEventManager;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class OperationModelView extends ViewPart implements ISelectionListener {

	private OperationModelViewer modelEditor;
	private IOperationInputData inputData;
	private IAction configure;
	
	@Override
	public void createPartControl(Composite parent) {
		EclipseUtils.getPage(getSite()).addSelectionListener(this);

		modelEditor = new OperationModelViewer(EclipseUtils.getPage(getSite()));
		modelEditor.createPartControl(parent);
		
		getSite().setSelectionProvider(modelEditor);
		
		IViewPart view = EclipseUtils.getPage(getSite()).findView("org.dawnsci.processing.ui.DataFileSliceView");
		final OperationEventManager man = (OperationEventManager)view.getAdapter(OperationEventManager.class);
		
		configure = new Action("Live setup", Activator.getImageDescriptor("icons/application-dialog.png")) {
			public void run() {
				IOperationModel model = modelEditor.getModel();
				if (inputData == null) return;
				if (!inputData.getCurrentOperation().getModel().equals(model)) return;
				
				ConfigureOperationModelDialog dialog = new ConfigureOperationModelDialog(getSite().getShell());
				dialog.create();
				dialog.setOperationInputData(inputData);
				if (dialog.open() == Dialog.OK) {
					man.requestUpdate();
					modelEditor.refresh();
				}
				
			}
		};
		configure.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().add(configure);
		
		
		man.addOperationRunnerListener(new IOperationGUIRunnerListener.Stub() {
			
			@Override
			public void inputDataAvaliable(IOperationInputData data) {
				if (data == null || data.getCurrentOperation().getModel() != modelEditor.getModel()) {
					inputData = null;
					configure.setEnabled(false);
					return;
				}
				inputData = data;
				configure.setEnabled(true);
			}

		});
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
				configure.setEnabled(false);
				OperationDescriptor des = (OperationDescriptor)ob;
				final String       name = des.getName();
				setPartName("Model '"+name+"'");
			}
		}		
	}

}
