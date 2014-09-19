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
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionCellEditor extends DialogCellEditor {

	private static final Logger logger = LoggerFactory.getLogger(RegionCellEditor.class);
	private IRegionTransformer transformer;
	
	public RegionCellEditor(Composite parent) {
		this(parent, null);
	}

	public RegionCellEditor(Composite parent, IRegionTransformer transformer) {
		super(parent);
		this.transformer = transformer;
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
						
		final ROIDialog dialog = new ROIDialog(cellEditorWindow.getShell()); // extends BeanDialog
		dialog.create();
		dialog.getShell().setSize(550,450); // As needed
		dialog.getShell().setText("Edit Region of Interest");
	
		try {
			dialog.setROI(transformer!=null ? transformer.getROI() : (IROI)getValue());
	        final int ok = dialog.open();
	        if (ok == Dialog.OK) {
	            return transformer!=null ? transformer.getValue(dialog.getROI()) : dialog.getROI();
	        }
		} catch (Exception ne) {
			logger.error("Problem decoding and/or encoding bean!", ne);
		}
        
        return null;
	}
    protected void updateContents(Object value) {
        if ( getDefaultLabel() == null) {
			return;
		}
        if (value == null && transformer==null) return;
        getDefaultLabel().setText(transformer!=null ? transformer.getRendererText() : value.toString());
    }

};

