/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.algorithm.ui.views.runner;

import java.util.Arrays;

import org.dawb.workbench.jmx.service.IWorkflowService;
import org.dawb.workbench.jmx.service.WorkflowFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.ISourceProvider;

import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.StopAction;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;

class AlgorithmProcessContext implements IAlgorithmProcessContext {
	
	private ISourceProvider[] providers;
	private AlgorithmView   view;
	private String workflowFilePath;
	private String title;
	private String fullPath;

	AlgorithmProcessContext(AlgorithmView view, ISourceProvider[] providers) {
		this.view      = view;
		this.providers = providers;
	}

	@Override
	public ISourceProvider[] getSourceProviders() {
		return providers;
	}
	
	private ModelRunner modelRunner;

	@Override
	public void execute(final String momlPath, boolean sameVm, IProgressMonitor monitor) throws Exception {
		
		if (sameVm) {
			try {
				ActionContributionItem run = (ActionContributionItem)view.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.RUN_ID_STUB+getTitle());
				run.getAction().setEnabled(false);
				ActionContributionItem stop = (ActionContributionItem)view.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.STOP_ID_STUB+getTitle());
				stop.getAction().setEnabled(true);
				modelRunner = new ModelRunner();
				modelRunner.runModel(momlPath,false);
				modelRunner = null;
				
			} finally {
				ActionContributionItem run = (ActionContributionItem)view.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.RUN_ID_STUB+getTitle());
				run.getAction().setEnabled(true);
				ActionContributionItem stop = (ActionContributionItem)view.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.STOP_ID_STUB+getTitle());
				stop.getAction().setEnabled(false);
				
				// RemoteWorkbenchImpl sets the status to the actor running.
				view.getViewSite().getActionBars().getStatusLineManager().setMessage("");
			}
			
		} else {
			// Try to find IFile or throw exception.
			IFile file = getResource(momlPath);
			if (file==null) throw new Exception("The path '"+momlPath+"' is not a file in a project in the workspace. This is required for running in own VM (as JDT is used).");
			
			final IWorkflowService service  = WorkflowFactory.createWorkflowService(new AlgorithmServiceProvider(file));
			final Process          workflow = service.start();
			
			workflow.waitFor(); // Waits until it is finished.
			
			// Release any memory used by the object
			service.clear();
		}
	}

	private IFile getResource(String fullPath) {
		
		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fullPath));				
		if (res==null) {
			String localPath;
			try {
				localPath = fullPath.substring(workspacePath.length());
			} catch (StringIndexOutOfBoundsException ne) {
				localPath = fullPath;
			}
            res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(localPath));
		}
		if (res==null) {
            res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(workspacePath+fullPath));
		}
		
		if (res !=null && res instanceof IFile) return (IFile)res;
		return null;
	}

	@Override
	public void stop() {
		if (modelRunner!=null) {
			modelRunner.stop();
			modelRunner = null;
		} else {
			(new StopAction()).run();
		}
		view.getViewSite().getActionBars().getStatusLineManager().setMessage("");
	}

	@Override
	public boolean isRunning() {
		if (modelRunner!=null) return true;
		return (new StopAction()).isEnabled();
	}

	@Override
	public void setFilePath(String workflowFilePath) {
		this.workflowFilePath = workflowFilePath;
	}

	@Override
	public String getFilePath(){
		return workflowFilePath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result
				+ ((modelRunner == null) ? 0 : modelRunner.hashCode());
		result = prime * result + Arrays.hashCode(providers);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((view == null) ? 0 : view.hashCode());
		result = prime
				* result
				+ ((workflowFilePath == null) ? 0 : workflowFilePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlgorithmProcessContext other = (AlgorithmProcessContext) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (modelRunner == null) {
			if (other.modelRunner != null)
				return false;
		} else if (!modelRunner.equals(other.modelRunner))
			return false;
		if (!Arrays.equals(providers, other.providers))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (view == null) {
			if (other.view != null)
				return false;
		} else if (!view.equals(other.view))
			return false;
		if (workflowFilePath == null) {
			if (other.workflowFilePath != null)
				return false;
		} else if (!workflowFilePath.equals(other.workflowFilePath))
			return false;
		return true;
	}
	
	
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

}
