/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.project;

import java.io.File;

import org.dawb.common.util.eclipse.BundleUtils;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProjectUtils {

	private static Logger logger = LoggerFactory.getLogger(DataProjectUtils.class);
    /**
     * 
     * @param name
     * @param root
     * @param mon
     * @return
     * @throws Exception
     */
	public static IProject createDataProject(final String           name, 
			                                 final IWorkspaceRoot   root,
			                                 final boolean          createExamples,
			                                 final IProgressMonitor mon) throws Exception {
		
		if (root.getProject(name).exists()) return root.getProject(name);

		final IProject data = root.getProject(name);
		data.create(mon);
		data.open(mon);

		if (createExamples) {
			
			// We copy all the data from examples here.
			// Use bundle as works even in debug mode.
	        final File examplesDir = BundleUtils.getBundleLocation("org.dawb.workbench.examples");
	        final File dataDir     = new File(examplesDir, "data");
	        logger.debug("Using data folder "+dataDir.getAbsolutePath());
	        if (dataDir.exists()) {
	        	
		        final IFolder examples = data.getFolder("examples");
		        examples.createLink(new Path(dataDir.getAbsolutePath()), IResource.REPLACE, mon);
	        }
	        
	        final IFolder src = data.getFolder("src");
	        src.create(true, true, mon);

	        final File pythonDir     = new File(examplesDir, "python");
	        if (pythonDir.exists()) {
	        	FileUtils.recursiveCopy(pythonDir, new File(src.getLocation().toOSString()));
	        }

		}
	        
        DataProjectUtils.addDataNature(data, mon);
        
        data.refreshLocal(IResource.DEPTH_INFINITE, mon);

        return data;
	}

	
	/**
	 * 
	 * @param workflows
	 * @param mon
	 * @throws CoreException 
	 */
	private static void addDataNature(final IProject data,
			                          final IProgressMonitor mon) throws CoreException {
		
		IProjectDescription description = data.getDescription();
		description.setNatureIds(new String[]{DataNature.ID});
		data.setDescription(description, mon);
		
	}


}
