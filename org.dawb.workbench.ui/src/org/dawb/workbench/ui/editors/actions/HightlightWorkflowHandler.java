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

import org.dawb.workbench.ui.editors.preference.EditorConstants;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HightlightWorkflowHandler extends AbstractHandler implements IEditorActionDelegate {
	
	private static final Logger logger = LoggerFactory.getLogger(HightlightWorkflowHandler.class);
	
	private ScopedPreferenceStore store;

	public HightlightWorkflowHandler() {
		super();
		
		this.store = new ScopedPreferenceStore(new InstanceScope(),"org.dawb.workbench.ui");
		
	}

	@Override
	public void run(IAction action) {
		toggleHighlightSelection();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		toggleHighlightSelection();
		return Boolean.TRUE;
	}

	private void toggleHighlightSelection() {
		
		final boolean isSel = store.getBoolean(EditorConstants.HIGHLIGHT_ACTORS_CHOICE);
		store.setValue(EditorConstants.HIGHLIGHT_ACTORS_CHOICE, !isSel);
		
		IWorkspace ws = ResourcesPlugin.getWorkspace();
	    try {
			ws.save(true, new NullProgressMonitor());
		} catch (CoreException e) {
			logger.error("Cannot save workspace", e);
		}
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
