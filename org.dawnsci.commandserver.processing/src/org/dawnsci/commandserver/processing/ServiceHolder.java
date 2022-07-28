/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.commandserver.processing;

import org.eclipse.dawnsci.nexus.INexusFileFactory;

import uk.ac.gda.common.activemq.ISessionService;

public class ServiceHolder {

	private INexusFileFactory nexusFileFactory;
	private ISessionService sessionService;

	public ServiceHolder() {
		
	}

	public INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public void setNexusFileFactory(INexusFileFactory nexusFileFactory) {
		this.nexusFileFactory = nexusFileFactory;
	}

	public ISessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(ISessionService sessionService) {
		this.sessionService = sessionService;
	}
}
