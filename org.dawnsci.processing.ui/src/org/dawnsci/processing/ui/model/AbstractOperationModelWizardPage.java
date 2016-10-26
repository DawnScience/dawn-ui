package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.commons.beanutils.BeanUtils;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationModelWizardPage extends WizardPage implements IOperationSetupWizardPage, PropertyChangeListener {

	protected IOperationInputData data;
	protected IOperationModel omodel;
	protected IOperationModel model;
	protected OperationData od = null;
	protected OperationData id = null;
	private Job update;

	private static final Logger logger = LoggerFactory.getLogger(AbstractOperationModelWizardPage.class);
	
	protected AbstractOperationModelWizardPage(String name) {
		super(name);
	}
	
	protected AbstractOperationModelWizardPage(String name, String description) {
		super(name, description, null);
	}

	protected AbstractOperationModelWizardPage(String name, String description, ImageDescriptor image) {
		super(name, description, image);
	}
	
	@Override
	public void setOperationInputData(final IOperationInputData data) {
		
		this.data = data;
		model = data.getCurrentOperation().getModel();
		
		try {
			omodel = (IOperationModel)BeanUtils.cloneBean(model);
		} catch (Exception e) {
			logger.warn("Could not clone model: " + e.getMessage());
		} 
		
		if (model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).addPropertyChangeListener(this);
		}
		update();
	}

	@Override
	public IOperationInputData getOperationInputData() {
		return data;
	}
	
	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		if (model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).removePropertyChangeListener(this);
		}
		
		if (buttonId == Dialog.CANCEL) {
			try {
				BeanUtils.copyProperties(model, omodel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void update() {

		if (update == null) {
			update = new Job("calculate...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						od = data.getCurrentOperation().execute(id.getData() ,new ProgressMonitorWrapper(monitor));
					} catch (final Exception e) {
						logger.error("Exception caught: ", e);
					}
					return Status.OK_STATUS;
				}
			};
		}

		update.cancel();
		update.schedule();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		update();
	}
	
	@Override
	public OperationData getOperationData() {
		return od;
	}
	
	@Override
	public void setOperationData(OperationData id) {
		this.id = id;
	}
	
	/*@Override
	public IWizardPage getNextPage() {
		IWizardPage page = super.getNextPage();
		if (page != null) {
			page.setVisible(true);
			page.getControl().redraw();
			page.getControl().pack(true);;
			page.getControl().setFocus();
		}
		return page;
	}*/
}
