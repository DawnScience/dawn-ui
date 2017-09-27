/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.views;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.dawb.common.util.io.IOpenFileAction;
import org.dawnsci.spectrum.ui.file.SpectrumFileManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumFileOpen implements IOpenFileAction {

	private static final Logger logger = LoggerFactory.getLogger(SpectrumFileOpen.class);

	@Override
	public void openFile(Path file) {

		if (file == null)
			return;

		if (!Files.isDirectory(file)) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView("org.dawnsci.spectrum.ui.views.SpectrumView");
			if (view == null)
				return;

			final SpectrumFileManager manager = (SpectrumFileManager) view.getAdapter(SpectrumFileManager.class);
			if (manager != null) {
				manager.addFiles(Arrays.asList(new String[] { file.toAbsolutePath().toString() }));
			} else {
				logger.error("Could not get file manager");
			}
		}
	}

}
