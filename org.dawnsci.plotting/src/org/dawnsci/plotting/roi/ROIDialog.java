/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.roi;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog for editing a ROI. Uses ROIViewer table.
 * @author fcp94556
 *
 */
public class ROIDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ROIDialog.class);

	private ROIEditTable roiEditor;
	private CCombo roiType;

	public ROIDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public Control createDialogArea(Composite parent) {

		final Composite main = (Composite)super.createDialogArea(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite top= new Composite(main, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		top.setLayout(new GridLayout(2, false));

		final Label label = new Label(top, SWT.NONE);
		label.setText("Region type    ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		roiType = new CCombo(top, SWT.READ_ONLY|SWT.BORDER);
		roiType.setItems(ROIType.getTypes());
		roiType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		roiType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					roiEditor.setRegion(ROIType.createNew(roiType.getSelectionIndex()), null, null);
				} catch (Exception e1) {
					logger.error("Cannot create roi "+ROIType.getType(roiType.getSelectionIndex()).getName(), e1);
				}
			}
		});


		this.roiEditor = new ROIEditTable();
		roiEditor.createPartControl(main);

		return main;
	}

	public void setROI(IROI roi) {
		final int index = ROIType.getIndex(roi.getClass());
		roiType.select(index);
		roiEditor.setRegion(roi, null, null);
	}

	public IROI getROI() {
		return roiEditor.getRoi();
	}
}
