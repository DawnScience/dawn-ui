/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.table;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

/**
 * This class may be extended to provide custom rendering.
 * @author Matthew Gerring
 *
 */
public class SeriesItemLabelProvider extends ColumnLabelProvider  {

	private Image newImage;
	protected int column=-1;

	public void update(ViewerCell cell) {
		this.column = cell.getColumnIndex();
        super.update(cell);
		this.column = -1;
	}
	
	@Override
	public Image getImage(Object element) {
				
		if (element == ISeriesItemDescriptor.NEW || element == ISeriesItemDescriptor.INSERT) {
			if (newImage == null) newImage = Activator.getImage("icons/new.png");
			return newImage;

		}
		return null;
	}

	@Override
	public String getText(Object element) {
		
		if (column==0) return ((ISeriesItemDescriptor)element).getName();
		return null;
	}

	public void dispose() {
		super.dispose();
		if (newImage!=null) newImage.dispose();
	}
}
