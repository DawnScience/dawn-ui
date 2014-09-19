/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.algorithm.ui.views.runner;

import org.dawb.passerelle.common.remote.RemoteWorkbenchImpl;
import org.dawb.workbench.jmx.IRemoteServiceProvider;
import org.dawb.workbench.jmx.IRemoteWorkbench;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;

import uk.ac.diamond.scisoft.analysis.utils.OSUtils;

public class AlgorithmServiceProvider implements IRemoteServiceProvider {

	private IFile momlFile;

	public AlgorithmServiceProvider(IFile momlFile) {
		this.momlFile = momlFile;
	}
	
	@Override
	public IRemoteWorkbench getRemoteWorkbench() throws Exception {
		return null;
	}

	@Override
	public int getStartPort() {
		return 21701;
	}

	@Override
	public String getWorkspacePath() {
		String path =  momlFile.getWorkspace().getRoot().getLocation().toOSString();
		return path;
	}

	@Override
	public String getModelPath() {
		String path = momlFile.getLocation().toOSString();
		return path;
	}

	@Override
	public String getInstallationPath() {
		String path = Platform.getInstallLocation().getURL().getFile();
		if (path.startsWith("/")) path = path.substring(1);
		if (System.getProperty("eclipse.debug.workflow.executable")!=null) {
			path = System.getProperty("eclipse.debug.workflow.executable");
		}
		if (!path.endsWith("\\")&&!path.endsWith("/")) path = path+"/";
		if (OSUtils.isWindowsOS()) {
			path = path+"dawn.exe";
		} else {
			path = path+"dawn";
		}
		return path;
	}

	@Override
	public boolean getServiceTerminate() {
		return true;
	}

	@Override
	public boolean getTangoSpecMockMode() {
		return false;
	}

}
