package org.dawnsci.processing.ui.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelView extends ViewPart implements ISelectionListener {

	private OperationModelViewer modelEditor;
	private IOperationInputData inputData;
	private IAction configure;
	private IAction xrfDialog;
	final private HashMap<String, Constructor<?>> operationDialogConstructors = new HashMap<>(); 
	private final static Logger logger = LoggerFactory.getLogger(OperationModelView.class);
	
	@Override
	public void createPartControl(Composite parent) {
		EclipseUtils.getPage(getSite()).addSelectionListener(this);

		modelEditor = new OperationModelViewer(EclipseUtils.getPage(getSite()));
		modelEditor.createPartControl(parent);
		
		getSite().setSelectionProvider(modelEditor);
		
		
		xrfDialog = new Action("XRF Dialog", Activator.getImageDescriptor("icons/xrf_tool.png")) {
			public void run() {
				IOperationModel model = modelEditor.getModel();
				if (inputData == null) return;
				if (!inputData.getCurrentOperation().getModel().equals(model)) return;
				
				String operationdialogId;
				try {
					operationdialogId = ServiceHolder
									.getOperationService()
									.getOperationDialogId(inputData
										.getCurrentOperation()
										.getId()
									);
				} catch (Exception e3) {
					logger.error("No id found!");
					return;
				}
				
				logger.debug("operationdialogId: {}", operationdialogId);
				
				// check our map if we already determined the constructor of this operationDialog
				Constructor<?> ctor = operationDialogConstructors.get(operationdialogId);
				
				if (ctor == null) {
					try {
						ctor = updateConstructors(operationdialogId);
					} catch (Exception e) {
						logger.error("Could not get operationdialog!");
						return;
					}
				}
				
				try {
					AbstractOperationModelDialog dialog = (AbstractOperationModelDialog) ctor.newInstance(getSite().getShell());
					dialog.create();
					dialog.setOperationInputData(inputData);
					if (dialog.open() == Dialog.OK) {
						logger.debug("OperationModelView: OK button clicked on close");
						EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
						Map<String,IOperationInputData> props = new HashMap<>();
						eventAdmin.postEvent(new Event("org/dawnsci/events/processing/PROCESSUPDATE", props));
						modelEditor.refresh();
					}
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e1) {
					logger.error("Could not open " + operationdialogId + " dialog!", e1);
				}
			}
		};
		xrfDialog.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().add(xrfDialog);
		
		configure = new Action("Live setup", Activator.getImageDescriptor("icons/application-dialog.png")) {
			public void run() {
				IOperationModel model = modelEditor.getModel();
				if (inputData == null) return;
				if (!inputData.getCurrentOperation().getModel().equals(model)) return;
				
				//ConfigureOperationModelDialog dialog = new ConfigureOperationModelDialog(getSite().getShell());
				IOperationSetupWizardPage wizardPage = new ConfigureOperationModelWizardPage(inputData.getCurrentOperation().getName(), inputData.getCurrentOperation().getDescription());
				OperationModelWizard wizard = new OperationModelWizard(wizardPage);
				wizard.setWindowTitle("Operation Model Configuration");
				OperationModelWizardDialog dialog = new OperationModelWizardDialog(getSite().getShell(), wizard);
				dialog.create();
				wizardPage.setOperationInputData(inputData);
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
					xrfDialog.setEnabled(false);
					return;
				}
				inputData = data;
				configure.setEnabled(true);
				String id = data.getCurrentOperation().getId();
				logger.debug("handleEvent id {}", id);
				try {
					ServiceHolder.getOperationService().getOperationDialogId(id);
					xrfDialog.setEnabled(true);
				} catch (Exception e) {
					xrfDialog.setEnabled(false);
				}
			}
		};
		
		Dictionary<String, String> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, "org/dawnsci/events/processing/DATAUPDATE");
		ctx.registerService(EventHandler.class, handler, props);
	}

	private Constructor<?> updateConstructors(String operationdialogId) throws Exception {
		IConfigurationElement[] eles = Platform
			.getExtensionRegistry()
			.getConfigurationElementsFor(
				"org.eclipse.dawnsci.analysis.api",
				"operationdialog"
			);
		
		logger.debug("eles.length: {}", eles.length);
		
		for (IConfigurationElement e : eles) {
			if (!e.getName().equals("operationdialog")) 
				continue;
			
			final String id = e.getAttribute("id");
			if (!id.equals(operationdialogId))
				continue;
			
			final String name = e.getAttribute("name");
			final String operationid = e.getAttribute("operationid");
			Class<? extends AbstractOperationModelDialog> clazz = null;
			try {
				clazz = ((AbstractOperationModelDialog) e.createExecutableExtension("class")).getClass();
			} catch (CoreException e1) {
				e1.printStackTrace();
				continue;
			}
			logger.debug("id: " + id);
			logger.debug("name: " + name);
			logger.debug("operationid: " + operationid);
			logger.debug("class: " + clazz.getName());
			// search through constructors for the one we need
			for (Constructor<?> constructor : clazz.getConstructors()) {
				if (constructor.getParameterCount() == 1 && 
						constructor.getParameterTypes()[0].equals(Shell.class)) {
					
					operationDialogConstructors.put(operationdialogId, constructor);
					return constructor;
				}
			}
		}
		
		throw new Exception("Could not find a dialog to open for the operation!");
		
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
				xrfDialog.setEnabled(false);
				OperationDescriptor des = (OperationDescriptor)ob;
				final String       name = des.getName();
				setPartName("Model '"+name+"'");
				
			}
		}		
	}


}
