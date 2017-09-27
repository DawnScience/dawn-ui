/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.IOpenFileAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("file_viewer");
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	public enum SortType {
		NAME,
		SIZE,
		TYPE,
		DATE,
	}

	public enum SortDirection {
		ASC,
		NONE,
		DESC;
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
			logger.debug("getFirstPertinentAction: {}", coreEx);
			return null;
		}
	}

	/**
	 * Get the formatted File size as a String <br>
	 * See {@link http://stackoverflow.com/questions/3758606/}
	 * for more information on how to properly format the size
	 * 
	 * @param file
	 * @param si
	 *         if True, the SI Units will be used, otherwise the binary ones will be used
	 * @return size as a string
	 */
	public static String getFileSizeString(File file, boolean si) {
		if (!file.isDirectory()) {
			long bytes = file.length();
			int unit = si ? 1000 : 1024;
			if (bytes < unit)
				return bytes + " B";
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
		return "";
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

	private static String getScanCmd(ILazyDataset dataset) {
			StringDataset scanCommandDataset;
			try {
				scanCommandDataset = DatasetUtils.cast(StringDataset.class, DatasetUtils.sliceAndConvertLazyDataset(dataset));
			} catch (DatasetException e) {
				return null;
			}
			return scanCommandDataset.get();
	}
	
	private static String getFileScanCmdStringNexus(String filePath) {
		try (NexusFile nxsFile = ServiceHolder.getNexusFactory().newNexusFile(filePath)) {
			nxsFile.openToRead();
			GroupNode rootNode = nxsFile.getGroup("/", false);
			if (rootNode == null)
				return null;
			
			for (String name : rootNode.getNames()) {
				GroupNode node = nxsFile.getGroup("/"+name, false);
				DataNode scanCommandNode = node.getDataNode("scan_command");
				if (scanCommandNode == null)
					continue;
				ILazyDataset scanCommandLazyDataset = scanCommandNode.getDataset();
				String scanCmd = getScanCmd(scanCommandLazyDataset);
				if (scanCmd != null)
					return scanCmd;
			}
		} catch (Exception e) {
			return null;
		} 
		return null;
	}
	
	private static String getFileScanCmdStringDat(String filePath) {
		try {
			ILoaderService loader = ServiceHolder.getLoaderService();
			IDataHolder dh = loader.getData(filePath, true, null);
			IMetadata meta = dh.getMetadata();
			Collection<String> metanames = meta.getMetaNames();
			for (Iterator<String> iterator = metanames.iterator(); iterator.hasNext();) {
				String string = iterator.next();
				if (string.contains("scan_command")) {
					Serializable value = meta.getMetaValue(string);
					return (String) value;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	/**
	 * Get the Scan Command if file contains one
	 * 
	 * @param file
	 * @return scan command
	 */
	public static String getFileScanCmdString(File file) {
		if (!file.isFile())
			return null;
		String extension = getFileExtension(file);
		String filePath = file.getAbsolutePath();
		String scanCmdString = null;
		if (extension.equals("nxs")) {
			scanCmdString = getFileScanCmdStringNexus(filePath);
		} else if (extension.equals(".dat")){
			scanCmdString = getFileScanCmdStringDat(filePath);
		}
		return scanCmdString;
	}

	private static String getFileExtension(File file) {
		String name = file.getName();
		try {
			return name.substring(name.lastIndexOf('.') + 1);
		} catch (Exception e) {
			return "";
		}
	}

}
