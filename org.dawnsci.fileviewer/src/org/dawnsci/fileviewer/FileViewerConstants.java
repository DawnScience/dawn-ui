/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer;

import java.text.DateFormat;

public class FileViewerConstants {

	/**
	 * File[]: Array of files whose paths are currently displayed in the combo
	 */
	public static final String COMBODATA_ROOTS = "Combo.roots";

	/**
	 * String: Previous selection text string
	 */
	public static final String COMBODATA_LASTTEXT = "Combo.lastText";

	/**
	 * File: File associated with tree item
	 */
	public static final String TREEITEMDATA_FILE = "TreeItem.file";

	/**
	 * Image: shown when item is expanded
	 */
	public static final String TREEITEMDATA_IMAGEEXPANDED = "TreeItem.imageExpanded";

	/**
	 * Image: shown when item is collapsed
	 */
	public static final String TREEITEMDATA_IMAGECOLLAPSED = "TreeItem.imageCollapsed";

	/**
	 * Object: if not present or null then the item has not been populated
	 */
	public static final String TREEITEMDATA_STUB = "TreeItem.stub";

	public static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	/**
	 * File: File associated with table row
	 */
	public static final String TABLEITEMDATA_FILE = "TableItem.file";

	/**
	 * File: Currently visible directory
	 */
	public static final String TABLEDATA_DIR = "Table.dir";

	/**
	 * Extension point used for opening files with special actions
	 */
	public static final String OPEN_FILE_EXTENSION_POINT = "org.dawnsci.fileviewer.openFile";

	/**
	 * constants to store preferences to display or not columns of table
	 */
	public static final String SHOW_SIZE_COLUMN = "org.dawnsci.fileviewer.showsize";
	public static final String SHOW_TYPE_COLUMN = "org.dawnsci.fileviewer.showtype";
	public static final String SHOW_MODIFIED_COLUMN = "org.dawnsci.fileviewer.showmodified";
	public static final String DISPLAY_WITH_SI_UNITS = "org.dawnsci.fileviewer.displaySIUnits";

	/**
	 * Tooltip texts
	 */
	/**
	 * Table tooltip
	 */
	public static final String NAME_TIP = "table.Name.tooltip";
	public static final String SIZE_SI_TIP = "table.SizeSI.tooltip";
	public static final String SIZE_BIN_TIP = "table.SizeBIN.tooltip";
	public static final String TYPE_TIP = "table.Type.tooltip";
	public static final String MODIFIED_TIP = "table.Modified.tooltip";
	public static final String SCAN_TIP = "table.ScanCmd.tooltip";
	public static final String PARENT_TIP = "tool.Parent.tiptext";
	public static final String REFRESH_TIP = "tool.Refresh.tiptext";
	public static final String LAYOUT_TIP = "tool.LayoutEdit.tiptext";
	public static final String PREFERENCES_TIP = "tool.Preferences.tiptext";

	/**
	 * Commands constants
	 */
	public static final String PARENT_CMD = "org.dawnsci.fileviewer.parentCommand";
	public static final String REFRESH_CMD = "org.dawnsci.fileviewer.refreshCommand";
	public static final String LAYOUT_CMD = "org.dawnsci.fileviewer.layoutCommand";
	public static final String PREFERENCES_CMD = "org.dawnsci.fileviewer.preferencesCommand";

	/**
	 * Table titles
	 */
	public static final String NAME_TITLE = "table.Name.title";
	public static final String SIZE_TITLE = "table.Size.title";
	public static final String TYPE_TITLE = "table.Type.title";
	public static final String MODIFIED_TITLE = "table.Modified.title";

}
