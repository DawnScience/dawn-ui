package org.dawnsci.fileviewer;

import java.io.File;
import java.text.DateFormat;

public class FileViewerConstants {

	public final static String DRIVE_A = "a:" + File.separator;
	public final static String DRIVE_B = "b:" + File.separator;

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

	public static final int[] tableWidths = new int[] { 150, 60, 75, 150 };

	/**
	 * Extension point used for opening files with special actions
	 */
	public static final String OPEN_FILE_EXTENSION_POINT = "uk.ac.diamond.sda.navigator.openFile";

}
