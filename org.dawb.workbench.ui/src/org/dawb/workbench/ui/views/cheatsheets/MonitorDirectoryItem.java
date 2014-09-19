/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.views.cheatsheets;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.ImageMonitorView;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorDirectoryItem extends AbstractItemExtensionElement {

	private static Logger logger = LoggerFactory.getLogger(MonitorDirectoryItem.class);
	
	private String path;

	public MonitorDirectoryItem(String attributeName) {
		super(attributeName);
	}

	@Override
	public void handleAttribute(String attributeValue) {
		this.path = attributeValue;
	}

	@Override
	public void createControl(Composite composite) {

		final Button set = new Button(composite, SWT.NONE);
		set.setText("Monitor");
		set.setToolTipText("Click to set the monitor directory to "+getPath());
		set.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					final ImageMonitorView view = (ImageMonitorView)EclipseUtils.getActivePage().showView(ImageMonitorView.ID);
					view.setDirectoryPath(getPath());
				} catch (PartInitException e1) {
					logger.error("Cannot find Image Monitor Part",e1);
				}
				
			}
		});
	}

	private String getPath() {
		final String wPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		return wPath+"/"+path;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
