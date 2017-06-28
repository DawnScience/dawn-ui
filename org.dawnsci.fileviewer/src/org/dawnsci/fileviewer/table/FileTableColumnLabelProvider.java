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
import org.dawnsci.fileviewer.Utils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTableColumnLabelProvider extends ColumnLabelProvider {

	private static final Logger logger = LoggerFactory.getLogger(FileTableColumnLabelProvider.class);
	
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
	
	
	// ignore h5
	// if NXS, read directly via Nexusfilefactory and look for scan_command in /entry1
	
	@Override
	public String getToolTipText(Object element) {
		File file = ((FileTableContent) element).getFile();
		String scanCmd = Utils.getFileScanCmdString(file);
		if (scanCmd != null) {
			// remove everything that is between brackets
			int indexOfFirstBracket = scanCmd.indexOf('(');
			int indexOfLastBracket = scanCmd.lastIndexOf(')');
			if (indexOfFirstBracket != -1 && indexOfLastBracket != -1) {
				scanCmd = scanCmd.substring(0, indexOfFirstBracket) + scanCmd.substring(indexOfLastBracket+1);
			}
			// compress multiple spaces into a single one
			scanCmd = scanCmd.replaceAll("\\s+", " ");
			// now if scanCmd is still longer than 200 chars, consider it bad -> this will stop DAWN font rendering from going berserk
			if (scanCmd.length() > 200) {
				logger.warn("scanCmd too long for {}: {}", file.getAbsolutePath(), scanCmd.length());
				return null;
			}
			return scanCmd;
		}
		return scanCmd;
	}
}
