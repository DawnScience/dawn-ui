package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.dawnsci.processing.ui.slice.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationModelDialog extends Dialog implements PropertyChangeListener {

	IOperationInputData data;
	protected IOperationModel omodel;
	protected IOperationModel model;

	private final Logger logger = LoggerFactory.getLogger(AbstractOperationModelDialog.class);
	
	protected AbstractOperationModelDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
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
	

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
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
}
