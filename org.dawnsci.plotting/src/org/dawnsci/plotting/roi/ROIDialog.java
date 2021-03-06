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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog for editing a ROI. Uses ROIViewer table.
 * @author Matthew Gerring
 *
 */
public class ROIDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ROIDialog.class);

	private ROIEditTable roiEditor;
	private CCombo roiType;

	private Class<? extends IROI> clazz;

	public ROIDialog(Shell parentShell, Class<? extends IROI> clazz) {
		super(parentShell);
		this.clazz = clazz;
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
		roiType.setItems(ROIType.getTypes(clazz));
		roiType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		roiType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					roiEditor.setRegion(ROIType.createNew(roiType.getItem(roiType.getSelectionIndex())), null, null);
				} catch (Exception e1) {
					logger.error("Cannot create roi {}", roiType.getItem(roiType.getSelectionIndex()), e1);
				}
			}
		});


		this.roiEditor = new ROIEditTable();
		roiEditor.createPartControl(main);

		return main;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button b = createButton(parent, IDialogConstants.NO_ID, "Remove", false);
		b.setToolTipText("Remove region");
	}

	/**
	 * Return code from ROIDialog to indicate that the region is to be removed
	 */
	public static final int REMOVE_ROI = -1237;

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == IDialogConstants.NO_ID) {
			setReturnCode(REMOVE_ROI);
			close();
		}
	}

	public void setROI(IROI roi) {
		if (roi==null) {
			return;
		}

		String n = ROIType.getName(roi.getClass());
		if (n == null) {
			return;
		}

		String[] rois = roiType.getItems();
		for (int i = 0; i < rois.length; i++) {
			if (n.equals(rois[i])) {
				roiType.select(i);
				break;
			}
		}
		// could fail if roi not in combo
		roiEditor.setRegion(roi, null, null);
	}

	public IROI getROI() {
		return roiEditor.getRoi();
	}
}
