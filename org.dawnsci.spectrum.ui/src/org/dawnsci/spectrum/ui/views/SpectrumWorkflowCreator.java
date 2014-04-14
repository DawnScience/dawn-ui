package org.dawnsci.spectrum.ui.views;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.dawb.passerelle.common.project.PasserelleProjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumWorkflowCreator {

	private static Logger logger = LoggerFactory.getLogger(SpectrumWorkflowCreator.class);
	
	/**
	 * Create a new workflow file, if required, otherwise run with what is there.
	 * @param projectName
	 * @return
	 * @throws CoreException 
	 * @throws FileNotFoundException 
	 */
	static IFile createWorkflowFileIfRequired(final String           projectName, 
			                                  final String           runFileName,
			                                  final String           fullPath,
			                                  final IProgressMonitor monitor) throws Exception{

		IProject trace = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (trace==null || !trace.exists()) trace = createWorkflowProject(projectName);
		trace.open(new NullProgressMonitor());
		
		final IFile file = trace.getFile(runFileName);
		if (!file.exists()) {
			file.create(new FileInputStream(fullPath), false, monitor);
			trace.refreshLocal(IResource.DEPTH_ONE, monitor);
		}
		return file;
	}
	
	/**
	 * Creates a workflow project inside of the workspace
	 * @param projectName
	 */
	private static IProject createWorkflowProject(String projectName){
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (root==null) return null;
		try {
			IProject project = PasserelleProjectUtils.createWorkflowProject(projectName, root, false, null);
			project.open(new NullProgressMonitor());
			return project;
		} catch (Exception e) {
			logger.debug(e.getMessage());
			return null;
		}
	}

}
