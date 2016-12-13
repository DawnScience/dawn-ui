/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Diamond Light Source - Custom modifications for Diamond's needs
 *******************************************************************************/
package org.dawnsci.fileviewer;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.fileviewer.table.FileTableViewerComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.program.Program;

import uk.ac.diamond.sda.navigator.views.IOpenFileAction;

public class Utils {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("file_viewer");

	public enum SortType {
		NAME,
		SIZE,
		TYPE,
		DATE;
	}

	/**
	 * Returns a string from the resource bundle. We don't want to crash because
	 * of a missing String. Returns the key if not found.
	 */
	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}
	}

	/**
	 * Returns a string from the resource bundle and binds it with the given
	 * arguments. If the key is not found, return the key.
	 */
	public static String getResourceString(String key, Object[] args) {
		try {
			return MessageFormat.format(getResourceString(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}
	}

	/**
	 * Sorts files lexicographically by name.
	 * 
	 * @param files
	 *            the array of Files to be sorted
	 */
	public static void sortFiles(File[] files, SortType sortType, int direction) {
		/* Very lazy merge sort algorithm */
		sortBlock(files, 0, files.length - 1, new File[files.length], sortType, direction, null);
	}

	/**
	 * Sorts files lexicographically by name.
	 * 
	 * @param files
	 *            the array of Files to be sorted
	 */
	public static void sortFiles(File[] files, SortType sortType, int direction, IProgressMonitor monitor) {
		/* Very lazy merge sort algorithm */
		sortBlock(files, 0, files.length - 1, new File[files.length], sortType, direction, monitor);
	}

	private static void sortBlock(File[] files, int start, int end, File[] mergeTemp, SortType sortType, int direction, IProgressMonitor monitor) {
		final int length = end - start + 1;
		if (length < 8) {
			for (int i = end; i > start; --i) {
				for (int j = end; j > start; --j) {
					if (compareFiles(files[j - 1], files[j], sortType, direction) > 0) {
						final File temp = files[j];
						files[j] = files[j - 1];
						files[j - 1] = temp;
						if (monitor != null && monitor.isCanceled())
							return;
					}
				}
			}
			return;
		}
		final int mid = (start + end) / 2;
		sortBlock(files, start, mid, mergeTemp, sortType, direction, monitor);
		sortBlock(files, mid + 1, end, mergeTemp, sortType, direction, monitor);
		int x = start;
		int y = mid + 1;
		for (int i = 0; i < length; ++i) {
			if ((x > mid) || ((y <= end) && compareFiles(files[x], files[y], sortType, direction) > 0)) {
				mergeTemp[i] = files[y++];
			} else {
				mergeTemp[i] = files[x++];
			}
			if (monitor != null && monitor.isCanceled())
				return;
		}
		for (int i = 0; i < length; ++i)
			files[i + start] = mergeTemp[i];
	}

	public static int compareFiles(File a, File b, SortType sortType, int direction) {
		// boolean aIsDir = a.isDirectory();
		// boolean bIsDir = b.isDirectory();
		// if (aIsDir && ! bIsDir) return -1;
		// if (bIsDir && ! aIsDir) return 1;

		// sort case-sensitive files in a case-insensitive manner
		int compare = 0;
		switch (sortType) {
		case NAME:
			compare = a.getName().compareToIgnoreCase(b.getName());
			if (compare == 0)
				compare = a.getName().compareTo(b.getName());
			break;
		case SIZE:
			long sizea = a.length(), sizeb = b.length();
			compare = sizea < sizeb ? -1 : 1;
			break;
		case TYPE:
			String typea = getFileTypeString(a), typeb = getFileTypeString(b);
			compare = typea.compareToIgnoreCase(typeb);
			if (compare == 0)
				compare = typea.compareTo(typeb);
			break;
		case DATE:
			Date date1 = new Date(a.lastModified());
			Date date2 = new Date(b.lastModified());
			compare = date1.compareTo(date2);
			break;
		default:
			return 0;
		}
		if (FileTableViewerComparator.DESC == direction)
			return (-1 * compare);
		return compare;
	}

	/**
	 * Gets a directory listing
	 * 
	 * @param file
	 *            the directory to be listed
	 * @param sort
	 *            the sorting type
	 * @return an array of files this directory contains, may be empty but not
	 *         null
	 */
	public static File[] getDirectoryList(File file, SortType sortType, int direction, String filter, boolean useRegex) {
		return getDirectoryList(file, sortType, direction, filter, useRegex, null);
	}

	/**
	 * Gets a directory listing
	 * 
	 * @param file
	 *            the directory to be listed
	 * @param sort
	 *            the sorting type
	 * @return an array of files this directory contains, may be empty but not
	 *         null
	 */
	public static File[] getDirectoryList(File file, SortType sortType, int direction, String filter, boolean useRegex, IProgressMonitor monitor) {
		File[] list = null;
		if (filter == null || filter.equals("*") || Pattern.matches("^\\s*$", filter)) {
			list = file.listFiles();
		}
		else if (useRegex) {
			try {
				list = file.listFiles((FileFilter) new RegexFileFilter(filter));
			} catch (PatternSyntaxException e) {
				list = null;
			}
		}
		else {
			list = file.listFiles((FileFilter) new WildcardFileFilter(filter));
		}
		if (list == null)
			return new File[0];
		sortFiles(list, sortType, direction, monitor);
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public static IOpenFileAction getFirstPertinentAction() {
		try {
			IConfigurationElement[] eles = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(FileViewerConstants.OPEN_FILE_EXTENSION_POINT);
			final String perspectiveId = EclipseUtils.getPage().getPerspective().getId();

			for (IConfigurationElement e : eles) {
				final String perspective = e.getAttribute("perspective");
				if (perspectiveId.equals(perspective) || perspective == null) {
					return (IOpenFileAction) e.createExecutableExtension("class");
				}
			}
			return null;
		} catch (CoreException coreEx) {
			coreEx.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the formatted File size as a String
	 * 
	 * @param file
	 * @return size as a string
	 */
	public static String getFileSizeString(File file) {
		String sizeString;
		if (file.isDirectory()) {
			sizeString = "";
		} else {
			sizeString = Utils.getResourceString("filesize.KB", new Object[] { new Long((file.length() + 512) / 1024) });
		}
		return sizeString;
	}

	/**
	 * Get the file type as a string
	 * 
	 * @param file
	 * @return type
	 */
	public static String getFileTypeString(File file) {
		String typeString;
		String nameString = file.getName();
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
	}

	/**
	 * Returns the modified date of the file
	 * 
	 * @param file
	 * @return date
	 */
	public static String getFileDateString(File file) {
		return FileViewerConstants.dateFormat.format(new Date(file.lastModified()));
	}
}
