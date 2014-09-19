/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.data.wizard;

import java.io.ByteArrayInputStream;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class PythonFilterWizard extends Wizard implements INewWizard {
	
	public final static String ID = "org.dawb.workbench.ui.data.pythonFilterWizard";
	
	private PythonFilterPage     page;
	private IResource selection;

	private String pythonPath;

	public PythonFilterWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new PythonFilterPage("Create or Choose Python File");
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection sel) {
		if (sel.getFirstElement() instanceof IResource) {
		    this.selection = (IResource)sel.getFirstElement();
		} else {
			this.selection = (IFile)EclipseUtils.getActiveEditor().getEditorInput().getAdapter(IFile.class);
		}
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {

		this.pythonPath     = page.getPythonFile();
		if (pythonPath==null || "".equals(pythonPath)) {
			pythonPath=null;
			return false;
		}
		
		final boolean isNewFile =  page.isNewFile();
		
		IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(pythonPath);
		if (isNewFile) {
			if (file == null) { // Create a new one in the folder
				pythonPath = pythonPath.replace('\\',  '/');
				final String     last = pythonPath.substring(0, pythonPath.lastIndexOf('/'));
				final String     name = pythonPath.substring(pythonPath.lastIndexOf('/')+1);
				final IResource  fold = ResourcesPlugin.getWorkspace().getRoot().findMember(last);
				file = ((IContainer)fold).getFile(new Path(name));
			}
			if (file.exists()) {
	        	boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Overwrite", 
		                  "Do you want to replace filter file '"+pythonPath+"' with a template new one?");
	        	if (!ok) return false; 
			}
        	
        	try {
        		if (!file.exists()) {
        			((IFile)file).create(new ByteArrayInputStream(page.getPythonContents().getBytes("UTF-8")), 
											                  IResource.FORCE, 
											                  new NullProgressMonitor());
        		} else {
    				((IFile)file).setContents(new ByteArrayInputStream(page.getPythonContents().getBytes("UTF-8")), 
											                  IResource.FORCE, 
											                  new NullProgressMonitor());
        		}
			} catch (Exception e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), 
						                "Problem writing '"+file.getName()+"'", 
		                                e.getMessage());
				pythonPath=null;
				return false;
			}
		}

		return true;
	}
	
	public String getPythonPath() {
		return pythonPath;
	}

}
