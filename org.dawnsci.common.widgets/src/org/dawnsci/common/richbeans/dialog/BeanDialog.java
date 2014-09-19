/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.dialog;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This dialog can be used for UI to edit a bean. Inherit from it and
 * override the method:
 * 
 * 	protected Composite createDialogArea(Composite ancestor) {
 
    }
    
    Then simply expose the bean fields with getters of the same name in this dialog as usual.
    
    The dialog is then uses as follows:
    <code>
        MyDialog dialog = new MyDialog(getSite().getShell()); // extends BeanDialog
		dialog.create();
		dialog.getShell().setSize(400,435); // As needed
		
		dialog.setBean(bean);
        final int ok = dialog.open();
        
        if (ok == Dialog.OK) {
            bean = dialog.getBean();
            // etc
        }
    </code>
 * 
 * 
 */
public abstract class BeanDialog extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(BeanDialog.class);
	
	protected Object bean;
	

	protected BeanDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.TITLE);
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText("Configure User Input");
	}

	@Override
	public boolean close() {	
		if (getReturnCode()==OK) {
			try {
				BeanUI.switchState(bean, this, false);
				BeanUI.uiToBean(this, bean);			
			} catch (Exception e) {
				logger.error("Cannot get "+bean+" from dialog!", e);
			}	
		}
		return super.close();
	}
	
	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		try {
			this.bean = bean;
			BeanUI.beanToUI(bean, this);
			BeanUI.switchState(bean, this, true);
			BeanUI.fireValueListeners(bean, this);
		} catch (Exception e) {
			logger.error("Cannot send "+bean+" to dialog!", e);
		}		
	}

}
