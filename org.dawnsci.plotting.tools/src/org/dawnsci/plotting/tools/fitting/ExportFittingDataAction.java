/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export fitting data to HDF file action
 *
 */
public class ExportFittingDataAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(ExportFittingDataAction.class);
	
	public ExportFittingDataAction() {
		setText("Export functions");
		setToolTipText("Export function data to an HDF5 file");
		setImageDescriptor(Activator.getImageDescriptor("icons/mask-export-wiz.png"));
	}
	
	public ExportFittingDataAction(String string,
			ImageDescriptor imageDescriptor) {
		super(string, imageDescriptor);
	}

	@Override
	public void run() {
		IWizard wiz;
		try {
			wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
			WizardDialog wd = new WizardDialog(Display.getCurrent()
					.getActiveShell(), wiz);
			wd.setTitle(wiz.getWindowTitle());
			wd.open();
		} catch (Exception e) {
			logger.error("Problem opening import!", e);
		}
	}

}
