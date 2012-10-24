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

import java.lang.reflect.InvocationTargetException;

import org.dawb.common.ui.util.CSVUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *   ConvertWizard
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(ConvertWizard.class);
	
	private ConvertWizardPage1 convertWizardPage1;

	public ConvertWizard() {
		this.convertWizardPage1 = new ConvertWizardPage1();
		addPage(convertWizardPage1);
		setWindowTitle("Convert Data Wizard");
	}

	@Override
	public boolean performFinish() {
		
		final String[] dataSetNames = convertWizardPage1.getSelected();
		if  (dataSetNames==null || dataSetNames.length<1) return false;

		final IFile dataFile = convertWizardPage1.getSource();
		final IFile csv      = convertWizardPage1.getPath();
		if (dataFile==null || csv==null) return false;
		
		try {
			// Use the progressible task in the wizard
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Convert "+dataFile.getName(), dataSetNames.length*2);
						
						if (convertWizardPage1.isOverwrite() && csv.exists()) {
							csv.delete(true, monitor);
						}
						csv.create(CSVUtils.getCVSStream(dataFile.getLocation().toOSString(), dataSetNames,  monitor), true, monitor);
						csv.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
						
						if (convertWizardPage1.isOpen()) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									try {
										EclipseUtils.openEditor(csv);
									} catch (PartInitException e) {
										logger.error("Cannot open "+csv.getName(), e);
									}
								}
							});
						}
						
						monitor.done();
					} catch (Exception e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			});
		} catch (Throwable ne) {
			final String message = "The file '"+dataFile.getName()+"' was not converted to '"+csv.getName()+"'";
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"File Not Converted", 
					ne.getMessage(),
					new Status(IStatus.WARNING, "org.edna.workbench.actions", message, ne));

		}

		return true;
	}

}
