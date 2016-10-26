package org.dawnsci.processing.ui.model;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.dawnsci.processing.ui.slice.EscapableSliceVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.january.dataset.IDataset;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelView extends ViewPart implements ISelectionListener {

	private OperationModelViewer modelEditor;
	private IOperationInputData inputData;
	private IAction configure;
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(OperationModelView.class);
	
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
			
				/*
				// check if this operation has a wizardpage 
				IOperationSetupWizardPage wizardPage = ServiceHolder.getOperationUIService().getWizardPage(inputData.getCurrentOperation().getId());
				
				if (wizardPage == null)
					wizardPage = new ConfigureOperationModelWizardPage(inputData.getCurrentOperation().getName(), inputData.getCurrentOperation().getDescription());
				OperationModelWizard wizard = new OperationModelWizard(wizardPage);
				wizard.setWindowTitle("Operation Model Configuration");
				OperationModelWizardDialog dialog = new OperationModelWizardDialog(getSite().getShell(), wizard, new OperationData(inputData.getInputData()));
				dialog.create();
				wizardPage.setOperationInputData(inputData);
				if (dialog.open() == Dialog.OK) {
					EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
					Map<String,IOperationInputData> props = new HashMap<>();
					eventAdmin.postEvent(new Event("org/dawnsci/events/processing/PROCESSUPDATE", props));
					modelEditor.refresh();
				}
				*/
				
				try {
					IOperation<? extends IOperationModel, ? extends OperationData> operation1 = ServiceHolder.getOperationService().create("uk.ac.diamond.scisoft.spectroscopy.operations.XRFGenerateEnergyAxisOperation");
					IOperation<? extends IOperationModel, ? extends OperationData> operation2 = ServiceHolder.getOperationService().create("uk.ac.diamond.scisoft.spectroscopy.operations.XAFSShiftEnergyAxisOperation");
					IOperation<? extends IOperationModel, ? extends OperationData> operationXRF = ServiceHolder.getOperationService().create("uk.ac.diamond.scisoft.spectroscopy.operations.XRFElementalMappingROIOperation");
					IOperation<? extends IOperationModel, ? extends OperationData> operation3 = ServiceHolder.getOperationService().create("uk.ac.diamond.scisoft.analysis.processing.operations.oned.Crop1DOperation");
					IOperationInputData inputData1 = new OperationInputDataImpl(inputData.getInputData(), operation1);
					OperationData operationResults1 = operation1.execute(inputData.getInputData(), null);
					IOperationInputData inputData2 = new OperationInputDataImpl(operationResults1.getData(), operation2);
					OperationData operationResults2 = operation2.execute(operationResults1.getData(), null);
					IOperationInputData inputDataXRF = new OperationInputDataImpl(operationResults2.getData(), operationXRF);
					OperationData operationResultsXRF = operationXRF.execute(operationResults2.getData(), null);
					IOperationInputData inputData3 = new OperationInputDataImpl(operationResultsXRF.getData(), operation3);
					IOperationSetupWizardPage page1 = new ConfigureOperationModelWizardPage(operation1.getName(), operation1.getDescription());
					IOperationSetupWizardPage page2 = new ConfigureOperationModelWizardPage(operation2.getName(), operation2.getDescription());
					IOperationSetupWizardPage pageXRF = ServiceHolder.getOperationUIService().getWizardPage(inputDataXRF.getCurrentOperation().getId());
					IOperationSetupWizardPage page3 = new ConfigureOperationModelWizardPage(operation3.getName(), operation3.getDescription());
					OperationModelWizard wizard = new OperationModelWizard(page1, page2, pageXRF, page3);
					wizard.setWindowTitle("Testttttttt");
					OperationModelWizardDialog dialog = new OperationModelWizardDialog(getSite().getShell(), wizard, new OperationData(inputData1.getInputData()));
					dialog.create();
					page1.setOperationInputData(inputData1);
					page2.setOperationInputData(inputData2);
					pageXRF.setOperationInputData(inputDataXRF);
					page3.setOperationInputData(inputData3);
					if (dialog.open() == Dialog.OK) {
						logger.debug("OK clicked");
					} else {
						logger.debug("Cancel clicked");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				String id = data.getCurrentOperation().getId();
				try {
					ServiceHolder.getOperationService().getOperationDialogId(id);
				} catch (Exception e) {
				}
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

	private class OperationInputDataImpl implements IOperationInputData {

		private IDataset ds;
		private IOperation<? extends IOperationModel, ? extends OperationData> op;
		
		public OperationInputDataImpl(IDataset ds, IOperation<? extends IOperationModel, ? extends OperationData> op) {
			this.ds = ds;
			this.op = op;
		}
		
		@Override
		public void setInputData(IDataset ds) {
			this.ds = ds;
		}	
		
		@Override
		public IDataset getInputData() {
			return ds;
		}

		@Override
		public IOperation<? extends IOperationModel, ? extends OperationData> getCurrentOperation() {
			return op;
		}
		
	}

}
