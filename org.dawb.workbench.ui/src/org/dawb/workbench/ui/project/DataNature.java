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

import org.dawb.common.ui.project.XMLBuilder;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;


public class DataNature implements IProjectNature {

	private IProject project;
	/**
	 * 
	 */
	public static String ID = "org.dawb.common.ui.DataNature";

	@Override
	public void configure() throws CoreException {
		
		if (project==null) return;
		
		EclipseUtils.addBuilderToProject(project, XMLBuilder.ID);
	}

	@Override
	public void deconfigure() throws CoreException {
		project = null;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
