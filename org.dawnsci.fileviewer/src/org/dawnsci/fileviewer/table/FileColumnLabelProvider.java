/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import java.io.File;
import java.util.Date;

import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.dawnsci.fileviewer.Utils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;

public class FileColumnLabelProvider extends ColumnLabelProvider {

	private FileViewer viewer;
	private int columnIndex;

	public FileColumnLabelProvider(FileViewer viewer, int columnIndex) {
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
					if (program != null) {
						iconImage = viewer.getIconCache().getIconFromProgram(program);
					} else {
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
		String nameString = file.getName(), sizeString, typeString;

		switch (this.columnIndex) {
		case 0:
			return nameString;
		case 1:
			if (file.isDirectory()) {
				sizeString = "";
			} else {
				sizeString = Utils.getResourceString("filesize.KB", new Object[] { new Long((file.length() + 512) / 1024) });
			}
			return sizeString;
		case 2:
			if (file.isDirectory()) {
				typeString = Utils.getResourceString("filetype.Folder");
			} else {
				int dot = nameString.lastIndexOf('.');
				if (dot != -1) {
					String extension = nameString.substring(dot);
					Program program = Program.findProgram(extension);
					if (program != null) {
						typeString = program.getName();
					} else {
						typeString = Utils.getResourceString("filetype.Unknown", new Object[] { extension.toUpperCase() });
					}
				} else {
					typeString = Utils.getResourceString("filetype.None");
				}
			}
			return typeString;
		case 3:
			return FileViewerConstants.dateFormat.format(new Date(file.lastModified()));
		default:
			break;
		}	
		return null;
	}
}
