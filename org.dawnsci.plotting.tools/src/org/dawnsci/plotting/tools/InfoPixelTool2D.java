/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;


public class InfoPixelTool2D extends InfoPixelTool {

	public final static String INFOPIXELTOOL2D_ID="org.dawb.workbench.plotting.tools.InfoPixel2D";

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	@Override
	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		var.getColumn().setText("Point ID");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 0));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("X position");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 1));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Y position");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 2));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Data value");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 3));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("q X (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 4));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
		var.getColumn().setText("q Y (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 5));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 6);
		var.getColumn().setText("q Z (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 6));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 7);
		var.getColumn().setText("2\u03b8 (\u00b0)");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 7));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 8);
		var.getColumn().setText("Resolution (\u00c5)");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 8));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 9);
		var.getColumn().setText("Dataset name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 9));

	}

}
