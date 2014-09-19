/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.dialog;

import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class AddRegionDialog extends Dialog {
	
	private XYRegionGraph xyGraph;
	private RegionEditComposite regionComposite;
	private RegionType type;
	private IPlottingSystem plottingSystem;

	public AddRegionDialog(final IPlottingSystem plottingSystem, final Shell parentShell, final XYRegionGraph xyGraph, RegionType type) {
		super(parentShell);	
		
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.plottingSystem = plottingSystem;
        this.xyGraph = xyGraph;
        this.type = type;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Region");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite parent_composite = (Composite) super.createDialogArea(parent);
        this.regionComposite = new RegionEditComposite(parent_composite, plottingSystem, SWT.NONE, xyGraph, type, false);
         
		return parent_composite;
	}
	
	@Override
	protected void okPressed() {	
		try {
		    region = regionComposite.createRegion();
		} catch (Exception ne) {
			MessageDialog.openError(getShell(), "Name in use", "The region cannot be created. "+ne.getMessage()+"\n\nPlease correct this or press cancel.");
			regionComposite.disposeRegion(region);
			return;
		}
		super.okPressed();
	}

	private AbstractSelectionRegion<?> region;
	

	/**
	 * @return the annotation
	 */
	public AbstractSelectionRegion<?> getRegion() {
		return region;
	}
}
