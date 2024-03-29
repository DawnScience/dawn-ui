/*
 * Copyright (c) 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Handler to launch the FolderPreviewDialog
 * <p>
 * Uses IRecentPlaces to set filter path.
 * Sends file open event, so can be used from multiple perspectives
 * 
 */
public class FolderPreviewHandler extends AbstractHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(FolderPreviewHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		DirectoryDialog dialog = new DirectoryDialog(HandlerUtil.getActiveShell(event),SWT.NONE);
		
		IRecentPlaces recentPlaces = ServiceProvider.getService(IRecentPlaces.class);
		final EventAdmin admin = ServiceProvider.getService(EventAdmin.class);
		
		if (!recentPlaces.getRecentDirectories().isEmpty()) {
			dialog.setFilterPath(recentPlaces.getRecentDirectories().get(0));
		}

		String open = dialog.open();
		
		if (open == null) return null;
		
		IProgressService progressService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IProgressService.class);
		
		IRunnableWithProgress r = new IRunnableWithProgress() {
			
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				File f = new File(open);
				File[] listFiles = f.listFiles(File::isFile);
				
				if (listFiles == null) {
					return;
				}
				
				FolderPreviewDialog d = new FolderPreviewDialog(HandlerUtil.getActiveShell(event), listFiles);
				
				Display.getDefault().asyncExec(() -> {
					if(d.open() == Dialog.OK) {
						String[] selectedFileNames = d.getSelectedFileNames();
						if (selectedFileNames == null || selectedFileNames.length == 0) {
							return;
						}
						Map<String,String[]> props = new HashMap<>();
						props.put(PlottingEventConstants.MULTIPLE_FILE_PROPERTY, selectedFileNames);
						admin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
					}
				});
			}
		};
		
		try {
			progressService.busyCursorWhile(r);
		} catch (InvocationTargetException e) {
			logger.error("Error in busy cursor",e);
		} catch (InterruptedException e) {
			logger.error("Error in busy cursor",e);
		}
		
		return null;
	}
}
