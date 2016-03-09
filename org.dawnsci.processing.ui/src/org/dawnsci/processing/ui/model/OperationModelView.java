package org.dawnsci.processing.ui.model;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.dawnsci.processing.ui.slice.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
		
		configure = new Action("Live setup", Activator.getImageDescriptor("icons/application-dialog.png")) {
			public void run() {
				IOperationModel model = modelEditor.getModel();
				if (inputData == null) return;
				if (!inputData.getCurrentOperation().getModel().equals(model)) return;
				
				ConfigureOperationModelDialog dialog = new ConfigureOperationModelDialog(getSite().getShell());
				dialog.create();
				dialog.setOperationInputData(inputData);
				if (dialog.open() == Dialog.OK) {
					EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
					Map<String,IOperationInputData> props = new HashMap<>();
					eventAdmin.postEvent(new Event("org/dawnsci/events/processing/PROCESSUPDATE", props));
					modelEditor.refresh();
				}
				
			}
		};
		configure.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().add(configure);
		
		BundleContext ctx = FrameworkUtil.getBundle(OperationModelView.class).getBundleContext();
		EventHandler handler = new EventHandler() {
			
			@Override
			public void handleEvent(Event event) {
				IOperationInputData data = (IOperationInputData)event.getProperty("data");
				
				if (data == null || data.getCurrentOperation().getModel() != modelEditor.getModel()) {
					inputData = null;
					configure.setEnabled(false);
					return;
				}
				inputData = data;
				configure.setEnabled(true);
				
			}
		};
		
		Dictionary<String, String> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, "org/dawnsci/events/processing/DATAUPDATE");
		ctx.registerService(EventHandler.class, handler, props);
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
