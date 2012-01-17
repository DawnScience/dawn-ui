/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.edna.workbench.actions;

import org.eclipse.jface.wizard.Wizard;
import org.dawb.common.ui.util.CSVUtils;



/**
 *   ConvertWizard
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizard extends Wizard {

	
	private ConvertWizardPage1 convertWizardPage1;

	public ConvertWizard() {
		setWindowTitle("Convert Data Wizard");
	}

	@Override
	public void addPages() {
		this.convertWizardPage1 = new ConvertWizardPage1();
		addPage(convertWizardPage1);
	}

	@Override
	public boolean performFinish() {
		
		final Object[] sel = convertWizardPage1.getSelected();
		if  (sel==null || sel.length<1) return false;
		
		CSVUtils.createCSV(convertWizardPage1.getFile(), sel);
		
		return true;
	}

}
