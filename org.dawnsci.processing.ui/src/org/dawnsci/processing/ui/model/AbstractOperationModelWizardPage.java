package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationModelWizardPage extends AbstractOperationSetupWizardPage implements PropertyChangeListener {

	protected IOperationModel model;
	protected IOperationModel omodel;
	@SuppressWarnings("rawtypes")
	final protected IOperation operation;
	private Job update;

	private static final Logger logger = LoggerFactory.getLogger(AbstractOperationModelWizardPage.class);

	protected AbstractOperationModelWizardPage() {
		super("");
		operation = null;
	}
	
	protected AbstractOperationModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		this(operation, null);
	}
	
	protected AbstractOperationModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation, ImageDescriptor image) {
		super(operation.getName(), operation.getDescription(), image);
		this.operation = operation;
		initAbstractOperationModelWizardPage();
	}
	
	private void initAbstractOperationModelWizardPage() {
		try {
			this.model = operation.getModel().getClass().newInstance(); // instantiate a new model
			this.omodel = operation.getModel(); // get the old model
			BeanUtils.copyProperties(this.model, this.omodel); // copy the properties from the old model back to the new one
			operation.setModel(model);
		} catch (Exception e) {
			logger.error("Could not instantiate default model!", e);
		}
		if (model != null && model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).addPropertyChangeListener(this);
		}
	}
	
	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		if (model != null && model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).removePropertyChangeListener(this);
		}
		if (buttonId == Dialog.OK) {
			try {
				BeanUtils.copyProperties(omodel, model);
			} catch (IllegalAccessException | InvocationTargetException e) {
				logger.error(e.getMessage());
			}
		}
		operation.setModel(omodel); // when the dialog closes, the operation gets the old model back, but possibly with new values!
	}
	
	@Override
	protected void update() {

		if (update == null) {
			update = new Job("calculate...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (id != null)
							od = operation.execute(id.getData() ,new ProgressMonitorWrapper(monitor));
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
	
	public IOperationModel getModel() {
		return model;
	}
	
	protected IOperation getOperation() {
		return operation;
	}
}
