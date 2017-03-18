/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.handlers;

import javax.inject.Inject;

import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.e4.core.di.annotations.Execute;

public class PreferencesHandler {

	private FileViewer fileviewer;

	@Inject
	public PreferencesHandler(FileViewer viewer) {
		fileviewer = viewer;
	}

	@Execute
	public void execute() {
		fileviewer.openPreferences();
	}
}