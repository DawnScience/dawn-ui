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

import org.dawnsci.fileviewer.Activator;
import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.dawnsci.fileviewer.Utils;
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
		if (element instanceof File && columnIndex == 0) {
			File file = (File) element;
			String nameString = file.getName();
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
		File file = (File) element;
		switch (this.columnIndex) {
		case 0:
			return file.getName();
		case 1:
			return Utils.getFileSizeString(file, viewer.isSizeSIUnit());
		case 2:
			return Utils.getFileTypeString(file);
		case 3:
			return Utils.getFileDateString(file);
		case 4:
			if (Activator.getDefault().getPreferenceStore().getBoolean(FileViewerConstants.SHOW_SCANCMD_COLUMN))
				return Utils.getFileScanCmdString(file);
			return null;
		default:
			break;
		}
		return null;
	}
}
