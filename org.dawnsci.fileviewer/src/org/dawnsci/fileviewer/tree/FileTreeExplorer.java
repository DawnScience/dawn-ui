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
package org.dawnsci.fileviewer.tree;

import java.io.File;

import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.dawnsci.fileviewer.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class FileTreeExplorer {

	private Tree tree;
	private Label treeScopeLabel;

	private FileViewer viewer;
	private Composite parent;

	public FileTreeExplorer(FileViewer viewer) {
		this.viewer = viewer;
	}

	public void createTreeView(Composite parent) {
		this.parent = parent;
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);

		treeScopeLabel = new Label(composite, SWT.BORDER);
		treeScopeLabel.setText(Utils.getResourceString("details.AllFolders.text"));
		treeScopeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		tree = new Tree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		tree.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					File file = (File) item.getData(FileViewerConstants.TREEITEMDATA_FILE);
					viewer.notifySelectedDirectory(file);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					item.setExpanded(true);
					treeExpandItem(item);
				}
			}
		});
		tree.addTreeListener(new TreeAdapter() {
			@Override
			public void treeExpanded(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(FileViewerConstants.TREEITEMDATA_IMAGEEXPANDED);
				if (image != null)
					item.setImage(image);
				treeExpandItem(item);
			}

			@Override
			public void treeCollapsed(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(FileViewerConstants.TREEITEMDATA_IMAGECOLLAPSED);
				if (image != null)
					item.setImage(image);
			}
		});
		createTreeDragSource(tree);
		createTreeDropTarget(tree);
	}

	/**
	 * Creates the Drag & Drop DragSource for items being dragged from the tree.
	 * 
	 * @return the DragSource for the tree
	 */
	private DragSource createTreeDragSource(final Tree tree) {
		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dragSource.addDragListener(new DragSourceListener() {
			TreeItem[] dndSelection = null;
			String[] sourceNames = null;

			@Override
			public void dragStart(DragSourceEvent event) {
				dndSelection = tree.getSelection();
				sourceNames = null;
				event.doit = dndSelection.length > 0;
				viewer.setIsDragging(true);
				viewer.setProcessedDropFiles(null);
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				viewer.dragSourceHandleDragFinished(event, sourceNames);
				dndSelection = null;
				sourceNames = null;
				viewer.setIsDragging(false);
				viewer.setProcessedDropFiles(null);
				viewer.handleDeferredRefresh();
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (dndSelection == null || dndSelection.length == 0)
					return;
				if (!FileTransfer.getInstance().isSupportedType(event.dataType))
					return;

				sourceNames = new String[dndSelection.length];
				for (int i = 0; i < dndSelection.length; i++) {
					File file = (File) dndSelection[i].getData(FileViewerConstants.TREEITEMDATA_FILE);
					sourceNames[i] = file.getAbsolutePath();
				}
				event.data = sourceNames;
			}
		});
		return dragSource;
	}

	/**
	 * Creates the Drag & Drop DropTarget for items being dropped onto the tree.
	 * 
	 * @return the DropTarget for the tree
	 */
	private DropTarget createTreeDropTarget(final Tree tree) {
		DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				viewer.setIsDropping(true);
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				viewer.setIsDropping(false);
				viewer.handleDeferredRefresh();
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				viewer.dropTargetValidate(event, getTargetFile(event));
				event.feedback |= DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				File targetFile = getTargetFile(event);
				if (viewer.dropTargetValidate(event, targetFile))
					viewer.dropTargetHandleDrop(event, targetFile);
			}

			private File getTargetFile(DropTargetEvent event) {
				// Determine the target File for the drop
				TreeItem item = tree.getItem(tree.toControl(new Point(event.x, event.y)));
				File targetFile = null;
				if (item != null) {
					// We are over a particular item in the tree, use the item's
					// file
					targetFile = (File) item.getData(FileViewerConstants.TREEITEMDATA_FILE);
				}
				return targetFile;
			}
		});
		return dropTarget;
	}

	/**
	 * Handles expand events on a tree item.
	 * 
	 * @param item
	 *            the TreeItem to fill in
	 */
	public void treeExpandItem(TreeItem item) {
		parent.setCursor(viewer.getIconCache().stockCursors[viewer.getIconCache().cursorWait]);
		final Object stub = item.getData(FileViewerConstants.TREEITEMDATA_STUB);
		if (stub == null)
			TreeUtils.treeRefreshItem(viewer, item, true, viewer.getIconCache());
		parent.setCursor(viewer.getIconCache().stockCursors[viewer.getIconCache().cursorDefault]);
	}

	public Tree getTree() {
		return tree;
	}
}
