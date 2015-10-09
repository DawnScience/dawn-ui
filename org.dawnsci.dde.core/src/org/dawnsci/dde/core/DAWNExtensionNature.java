/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package org.dawnsci.dde.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class DAWNExtensionNature implements IProjectNature {

	public static final String IDENTIFIER = "org.dawnsci.dde.core.DAWNExtensionNature";

	private IProject project;

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	@Override
	public void configure() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = IDENTIFIER;
		IStatus status = workspace.validateNatureSet(natures);
		if (status.getCode() == IStatus.OK) {
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} else {
			throw new CoreException(status);
		}
	}

	@Override
	public void deconfigure() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length];
		int pos = 0;
		for (int i = 0; i < natures.length; i++) {
			if (natures[i].equals(IDENTIFIER)) {
			} else
				newNatures[pos++] = natures[i];
		}
		IStatus status = workspace.validateNatureSet(natures);
		if (status.getCode() == IStatus.OK) {
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} else {
			throw new CoreException(status);
		}
	}

}
