package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationModelWizardPage extends WizardPage implements IOperationSetupWizardPage, PropertyChangeListener {

	protected IOperationInputData data;
	protected IOperationModel omodel;
	protected IOperationModel model;

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
	}
}
