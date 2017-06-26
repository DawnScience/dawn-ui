/*
 * Copyright (c) 2016-2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import java.io.File;

import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;

public class FileTableColumnLabelProvider extends ColumnLabelProvider {

	private FileViewer viewer;
	private int columnIndex;

	public FileTableColumnLabelProvider(FileViewer viewer, int columnIndex) {
		this.viewer = viewer;
		this.columnIndex = columnIndex;
	}
	
	@Override
	public Image getImage(Object element) {
		Image iconImage = null;
		if (element instanceof FileTableContent && columnIndex == 0) {
			FileTableContent content = (FileTableContent) element;
			File file = content.getFile();
			String nameString = content.getFileName();
			if (file.isDirectory()) {
				iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconClosedFolder];
			} else {
				int dot = nameString.lastIndexOf('.');
				if (dot != -1) {
					String extension = nameString.substring(dot);
					Program program = Program.findProgram(extension);
					// Check for nxs file
					if (program != null && !extension.equals(".nxs")) {
						iconImage = viewer.getIconCache().getIconFromProgram(program);
					} else {
						if(extension.equals(".nxs"))
							iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconNxs];
						else
							iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconFile];
					}
				} else {
					iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconFile];
				}
			}
		}
		return iconImage;
	}

	@Override
	public String getText(Object element) {
		FileTableContent content = (FileTableContent) element;
		switch (this.columnIndex) {
		case 0:
			return content.getFileName();
		case 1:
			return viewer.isSizeSIUnit() ? content.getFileSizeSI() : content.getFileSizeReg();
		case 2:
			return content.getFileType();
		case 3:
			return content.getFileDate();
		default:
			break;
		}
		return null;
	}
}
