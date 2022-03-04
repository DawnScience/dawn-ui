/*******************************************************************************
 * Copyright (c) 2017 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.dawnsci.plotting.system.dialog;

import java.util.Collections;

import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.nebula.visualization.internal.xygraph.toolbar.TraceConfigPage;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Custom trace config page for Diamond.
 * 
 * @author Baha El-Kassaby
 *
 */
public class DTraceConfigPage extends TraceConfigPage {

	public DTraceConfigPage(IXYGraph xyGraph, Trace trace) {
		super(xyGraph, trace);
	}

	@Override
	public void addCustomButton(Composite composite) {
		Button export = new Button(composite, SWT.NONE);
		export.setText("Export data...");
		export.setToolTipText("Export trace to ascii (dat file)");
		PlottingSystemActivator.setButtonImage(export, "icons/data-export.png");
		export.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		export.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// WARNING: this action uses the convert command in
				// org.dawnsci.plotting plugin
				// and its handler is
				// org.dawnsci.plotting.command.ExportLineTraceCommand.
				// TODO Replace this by an extension point so we can take out
				// the unnecessary dependencies
				try {
					final ICommandService service = (ICommandService) PlatformUI.getWorkbench()
							.getService(ICommandService.class);
					final Command export = service.getCommand("org.dawnsci.plotting.export.line.trace.command");
					final ExecutionEvent event = new ExecutionEvent(export, Collections.EMPTY_MAP, null, getTrace());
					export.executeWithChecks(event);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

}
