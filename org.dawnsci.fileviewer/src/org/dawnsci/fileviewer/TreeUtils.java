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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeUtils {

	/**
	 * Traverse the entire tree and update only what has changed.
	 * 
	 * @param roots
	 *            the root directory listing
	 */
	public static void treeRefresh(File[] masterFiles, Tree tree, FileViewer viewer, IconCache iconCache) {
		TreeItem[] items = tree.getItems();
		int masterIndex = 0;
		int itemIndex = 0;
		for (int i = 0; i < items.length; ++i) {
			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(FileViewerConstants.TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterIndex == masterFiles.length)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			final File masterFile = masterFiles[masterIndex];
			int compare = Utils.compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(viewer, item, false, iconCache);
				++itemIndex;
				++masterIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(tree, SWT.NONE, itemIndex);
				treeInitVolume(newItem, masterFile, iconCache);
				new TreeItem(newItem, SWT.NONE); // placeholder child item to
													// get "expand" button
				++itemIndex;
				++masterIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		for (; masterIndex < masterFiles.length; ++masterIndex) {
			final File masterFile = masterFiles[masterIndex];
			TreeItem newItem = new TreeItem(tree, SWT.NONE);
			treeInitVolume(newItem, masterFile, iconCache);
			new TreeItem(newItem, SWT.NONE); // placeholder child item to get
												// "expand" button
		}
	}

	/**
	 * Traverse an item in the tree and update only what has changed.
	 * 
	 * @param viewer
	 *            the file viewer to set the data on
	 * @param dirItem
	 *            the tree item of the directory
	 * @param forcePopulate
	 *            true iff we should populate non-expanded items as well
	 * @param iconCache
	 *            the icon cache used to handle icons
	 */
	public static void treeRefreshItem(FileViewer viewer, TreeItem dirItem, boolean forcePopulate, IconCache iconCache) {
		final File dir = (File) dirItem.getData(FileViewerConstants.TREEITEMDATA_FILE);

		if (!forcePopulate && !dirItem.getExpanded()) {
			// Refresh non-expanded item
			if (dirItem.getData(FileViewerConstants.TREEITEMDATA_STUB) != null) {
				treeItemRemoveAll(dirItem);
				new TreeItem(dirItem, SWT.NONE); // placeholder child item to
													// get "expand" button
				dirItem.setData(FileViewerConstants.TREEITEMDATA_STUB, null);
			}
			return;
		}
		// Refresh expanded item
		dirItem.setData(FileViewerConstants.TREEITEMDATA_STUB, viewer); // clear stub flag

		/* Get directory listing */
		File[] subFiles = (dir != null) ? Utils.getDirectoryList(dir) : null;
		if (subFiles == null || subFiles.length == 0) {
			/* Error or no contents */
			treeItemRemoveAll(dirItem);
			dirItem.setExpanded(false);
			return;
		}

		/* Refresh sub-items */
		TreeItem[] items = dirItem.getItems();
		final File[] masterFiles = subFiles;
		int masterIndex = 0;
		int itemIndex = 0;
		File masterFile = null;
		for (int i = 0; i < items.length; ++i) {
			while ((masterFile == null) && (masterIndex < masterFiles.length)) {
				masterFile = masterFiles[masterIndex++];
				if (!masterFile.isDirectory())
					masterFile = null;
			}

			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(FileViewerConstants.TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterFile == null)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			int compare = Utils.compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(viewer, item, false, iconCache);
				masterFile = null;
				++itemIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE, itemIndex);
				treeInitFolder(newItem, masterFile, iconCache);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item
													// so we get the "expand"
													// button
				masterFile = null;
				++itemIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		while ((masterFile != null) || (masterIndex < masterFiles.length)) {
			if (masterFile != null) {
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE);
				treeInitFolder(newItem, masterFile, iconCache);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item
													// so we get the "expand"
													// button
				if (masterIndex == masterFiles.length)
					break;
			}
			masterFile = masterFiles[masterIndex++];
			if (!masterFile.isDirectory())
				masterFile = null;
		}
	}

	/**
	 * Foreign method: removes all children of a TreeItem.
	 * 
	 * @param treeItem
	 *            the TreeItem
	 */
	private static void treeItemRemoveAll(TreeItem treeItem) {
		final TreeItem[] children = treeItem.getItems();
		for (TreeItem child : children) {
			child.dispose();
		}
	}

	/**
	 * Initializes a folder item.
	 * 
	 * @param item
	 *            the TreeItem to initialize
	 * @param folder
	 *            the File associated with this TreeItem
	 */
	private static void treeInitFolder(TreeItem item, File folder, IconCache cache) {
		item.setText(folder.getName());
		item.setImage(cache.stockImages[cache.iconClosedFolder]);
		item.setData(FileViewerConstants.TREEITEMDATA_FILE, folder);
		item.setData(FileViewerConstants.TREEITEMDATA_IMAGEEXPANDED, cache.stockImages[cache.iconOpenFolder]);
		item.setData(FileViewerConstants.TREEITEMDATA_IMAGECOLLAPSED, cache.stockImages[cache.iconClosedFolder]);
	}

	/**
	 * Initializes a volume item.
	 * 
	 * @param item
	 *            the TreeItem to initialize
	 * @param volume
	 *            the File associated with this TreeItem
	 */
	private static void treeInitVolume(TreeItem item, File volume, IconCache cache) {
		item.setText(volume.getPath());
		item.setImage(cache.stockImages[cache.iconClosedDrive]);
		item.setData(FileViewerConstants.TREEITEMDATA_FILE, volume);
		item.setData(FileViewerConstants.TREEITEMDATA_IMAGEEXPANDED, cache.stockImages[cache.iconOpenDrive]);
		item.setData(FileViewerConstants.TREEITEMDATA_IMAGECOLLAPSED, cache.stockImages[cache.iconClosedDrive]);
	}
}
