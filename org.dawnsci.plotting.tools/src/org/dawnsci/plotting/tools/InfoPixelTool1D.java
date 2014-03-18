/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.tools;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;


public class InfoPixelTool1D extends InfoPixelTool {

	public final static String INFOPIXELTOOL1D_ID="org.dawb.workbench.plotting.tools.InfoPixel1D";

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}
	
	
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

	}

}
