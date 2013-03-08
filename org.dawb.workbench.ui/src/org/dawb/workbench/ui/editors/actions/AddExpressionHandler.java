/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.actions;

import org.dawb.common.services.IVariableManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class AddExpressionHandler extends AbstractHandler implements IEditorActionDelegate{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
        doAction();		
		return Boolean.TRUE;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(IAction action) {
		doAction();		
	}

	private void doAction() {
		IVariableManager sets = DataSetComponentUtils.getActiveComponent();
		if (sets==null) return;
		
		sets.addExpression();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
