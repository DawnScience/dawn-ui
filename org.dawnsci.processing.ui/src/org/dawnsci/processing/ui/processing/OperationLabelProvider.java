/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.processing;

import org.dawnsci.common.widgets.table.SeriesItemLabelProvider;
import org.dawnsci.processing.ui.model.OperationDescriptor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

final class OperationLabelProvider extends SeriesItemLabelProvider  {

	@Override
	public String getText(Object element) {
		
		if(!(element instanceof OperationDescriptor)) return super.getText(element);
		
		OperationDescriptor des = (OperationDescriptor)element;
		
		// Other columns
		if (column>0) {
			try {
				switch (column) {
				case 1:
					return des.getSeriesObject().getInputRank().getLabel();
				case 2:
					return des.getSeriesObject().getOutputRank().getLabel();
				}
			} catch (Exception ne) {
				return ne.getMessage();
			}
		}
		
		StringBuilder buf = new StringBuilder(" ");

		try {
			IOperation<? extends IOperationModel, ? extends OperationData> op = des.getSeriesObject();
			if (op.isPassUnmodifiedData()) {
				buf.append(" \u25BC   ");
			}
			
			buf.append(op.getName());
			
			if (op.isStoreOutput()) {
				buf.append(" [Save]");
			} 


		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buf.toString();
		
	}
	
	private Font italicFont;
	public Font getFont(Object element) {
		
		if(!(element instanceof OperationDescriptor)) return super.getFont(element);
		
		OperationDescriptor des = (OperationDescriptor)element;
		
		try {
			IOperation<? extends IOperationModel, ? extends OperationData> op = des.getSeriesObject();
			if (op.isPassUnmodifiedData()) {
				if (italicFont == null) {
					final FontData shellFd = Display.getDefault().getActiveShell().getFont().getFontData()[0];
					FontData fd      = new FontData(shellFd.getName(), shellFd.getHeight(), SWT.ITALIC);
					italicFont = new Font(null, fd);
				}
				return italicFont;
			}
			

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public Image getImage(Object element) {
		if (column>0) return null;
		if(!(element instanceof OperationDescriptor)) return super.getImage(element);
		OperationDescriptor des = (OperationDescriptor)element;
		return des.getImage();
	}

	public void dispose() {
		super.dispose();
		if (italicFont!=null) {
			italicFont.dispose();
			italicFont = null;
		}
	}
}
