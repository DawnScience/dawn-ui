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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 *   ConvertWizard shows a wizard for converting synchrotron data
 *   to more common file types.
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizardHandler extends AbstractHandler implements IObjectActionDelegate {


	private IWorkbenchPart targetPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        openWizard(HandlerUtil.getActiveShell(event));
		return Boolean.FALSE;
	}

	private void openWizard(final Shell shell) {
		WizardDialog dialog = new WizardDialog(shell, new ConvertWizard());
        dialog.setPageSize(new Point(400, 300));
        dialog.create();
        dialog.open();
	}
	
	@Override
	public void run(IAction action) {
	    openWizard(targetPart.getSite().getShell());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}
	
	public boolean isEnabled() {
		final ISelection selection = EclipseUtils.getActivePage().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection)selection;
			final Object        o = s.getFirstElement();
			if (o instanceof IFile) return true;
		}
        return false;
	}
	public boolean isHandled() {
		return  isEnabled();
	}
	
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
