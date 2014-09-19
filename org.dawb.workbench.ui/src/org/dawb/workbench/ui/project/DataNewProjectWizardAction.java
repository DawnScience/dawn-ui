/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.project;

import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataNewProjectWizardAction extends Action implements IIntroAction {

	private static final Logger logger = LoggerFactory.getLogger(DataNewProjectWizardAction.class);

	/**
	 * Default constructor
	 */
	public DataNewProjectWizardAction() {
	}

	/**
	 * Run action. Try to download and install sample feature if not present.
	 */
	@Override
	public void run(IIntroSite site, Properties params) {

		//		sampleId = params.getProperty("id"); //$NON-NLS-1$
		// if (sampleId == null)
		// return;
		//
		Runnable r = new Runnable() {
			@Override
			public void run() {

				// Install sample data project from this plugin

				DataNewProjectWizard wizard = new DataNewProjectWizard();
				wizard.init(PlatformUI.getWorkbench(), null);

				// Create the wizard dialog
				WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);

				// Open the wizard dialog
				int ret = dialog.open();
				if (ret == Window.CANCEL) {
					logger.debug("Installation of sampledata canceled.");
				} else {
					logger.debug("Installation of sampledata finished OK.");

					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"New Data Project Wizard", "New Data Project was successfully created");
				}

			}
		};

		Shell currentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		currentShell.getDisplay().asyncExec(r);
	}

}
