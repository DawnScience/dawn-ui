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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.sda.navigator.views.IOpenFileAction;

/**
 * File Viewer based on the SWT FileViewer example
 */
public class FileViewer {

	private static final Logger logger = LoggerFactory.getLogger(FileViewer.class);

	/* UI elements */
	private Label numObjectsLabel;
	private Label diskSpaceLabel;

	private File currentDirectory = null;
	private boolean initial = true;

	/* Drag and drop optimizations */
	private boolean isDragging = false; // if this app is dragging
	private boolean isDropping = false; // if this app is dropping

	private File[] processedDropFiles = null; // so Drag only deletes what it
												// needs to
	private File[] deferredRefreshFiles = null; // to defer notifyRefreshFiles
												// while we do DND
	private boolean deferredRefreshRequested = false; // to defer
														// notifyRefreshFiles
														// while we do DND
	private ProgressDialog progressDialog = null; // progress dialog for
													// locally-initiated
													// operations

	/* Combo view */
	private Combo combo;

	/* Tree view */
	private IconCache iconCache = new IconCache();
	

	private Tree tree;
	private Label treeScopeLabel;

	/* Table view */
	private final String[] tableTitles = new String[] { Utils.getResourceString("table.Name.title"),
			Utils.getResourceString("table.Size.title"), Utils.getResourceString("table.Type.title"),
			Utils.getResourceString("table.Modified.title") };
	private Table table;
	private Label tableContentsOfLabel;

	/* Table update worker */
	// Control data
	private final Object workerLock = new Object();
	// Lock for all worker control data and state
	private volatile Thread workerThread = null;
	// The worker's thread
	private volatile boolean workerStopped = false;
	// True if the worker must exit on completion of the current cycle
	private volatile boolean workerCancelled = false;
	// True if the worker must cancel its operations prematurely perhaps due to
	// a state update

	// Worker state information -- this is what gets synchronized by an update
	private volatile File workerStateDir = null;

	// State information to use for the next cycle
	private volatile File workerNextDir = null;

	/* Simulate only flag */
	// when true, disables actual filesystem manipulations and outputs results
	// to standard out
	private boolean simulateOnly = true;

	private Composite parent;
	private SashForm sashForm;

	/**
	 * Closes the main program.
	 */
	public void close() {
		workerStop();
		getIconCache().freeResources();
	}

	/**
	 * Construct the UI
	 * 
	 * @param container
	 *            the ShellContainer managing the composite we are rendering inside
	 */
	public void createCompositeContents(Composite parent) {
		this.parent = parent;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		gridData.widthHint = 200;
		createComboView(parent, gridData);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;

		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.horizontalSpan = 4;
		sashForm.setLayoutData(gridData);
		createTreeView(sashForm);
		createTableView(sashForm);
		sashForm.setWeights(new int[] { 5, 2 });

		numObjectsLabel = new Label(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 185;
		numObjectsLabel.setLayoutData(gridData);

		diskSpaceLabel = new Label(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
//		gridData.horizontalSpan = 1;
		diskSpaceLabel.setLayoutData(gridData);
	}

	/**
	 * Creates the combo box view.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createComboView(Composite parent, Object layoutData) {
		combo = new Combo(parent, SWT.NONE);
		combo.setLayoutData(layoutData);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final File[] roots = (File[]) combo.getData(FileViewerConstants.COMBODATA_ROOTS);
				if (roots == null)
					return;
				int selection = combo.getSelectionIndex();
				if (selection >= 0 && selection < roots.length) {
					notifySelectedDirectory(roots[selection]);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String lastText = (String) combo.getData(FileViewerConstants.COMBODATA_LASTTEXT);
				String text = combo.getText();
				if (text == null)
					return;
				if (lastText != null && lastText.equals(text))
					return;
				combo.setData(FileViewerConstants.COMBODATA_LASTTEXT, text);
				File file = new File(text);
				if(!file.exists())
					return;
				if(file.isDirectory()) {
					notifySelectedDirectory(file);
				} else {
					String directory = text.substring(0, text.lastIndexOf(File.separator));
					// open file in editor
					doDefaultFileAction(new File[] {new File(text)});
					// open directory
					notifySelectedDirectory(new File(directory));
				}
			}
		});
	}

	/**
	 * Creates the file tree view.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createTreeView(Composite parent) {
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
					notifySelectedDirectory(file);
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
				isDragging = true;
				processedDropFiles = null;
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				dragSourceHandleDragFinished(event, sourceNames);
				dndSelection = null;
				sourceNames = null;
				isDragging = false;
				processedDropFiles = null;
				handleDeferredRefresh();
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
				isDropping = true;
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				isDropping = false;
				handleDeferredRefresh();
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				dropTargetValidate(event, getTargetFile(event));
				event.feedback |= DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				File targetFile = getTargetFile(event);
				if (dropTargetValidate(event, targetFile))
					dropTargetHandleDrop(event, targetFile);
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
	private void treeExpandItem(TreeItem item) {
		parent.setCursor(getIconCache().stockCursors[getIconCache().cursorWait]);
		final Object stub = item.getData(FileViewerConstants.TREEITEMDATA_STUB);
		if (stub == null)
			TreeUtils.treeRefreshItem(this, item, true, getIconCache());
		parent.setCursor(getIconCache().stockCursors[getIconCache().cursorDefault]);
	}

	/**
	 * Creates the file details table.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createTableView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		tableContentsOfLabel = new Label(composite, SWT.BORDER);
		tableContentsOfLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		for (int i = 0; i < tableTitles.length; ++i) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(tableTitles[i]);
			column.setWidth(FileViewerConstants.tableWidths[i]);
		}
		table.setHeaderVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				notifySelectedFiles(getSelectedFiles());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				doDefaultFileAction(getSelectedFiles());
			}

			private File[] getSelectedFiles() {
				final TableItem[] items = table.getSelection();
				final File[] files = new File[items.length];

				for (int i = 0; i < items.length; ++i) {
					files[i] = (File) items[i].getData(FileViewerConstants.TABLEITEMDATA_FILE);
				}
				return files;
			}
		});

		createTableDragSource(table);
		createTableDropTarget(table);
	}
	
	/**
	 * Creates the Drag & Drop DragSource for items being dragged from the
	 * table.
	 * 
	 * @return the DragSource for the table
	 */
	private DragSource createTableDragSource(final Table table) {
		DragSource dragSource = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dragSource.addDragListener(new DragSourceListener() {
			TableItem[] dndSelection = null;
			String[] sourceNames = null;

			@Override
			public void dragStart(DragSourceEvent event) {
				dndSelection = table.getSelection();
				sourceNames = null;
				event.doit = dndSelection.length > 0;
				isDragging = true;
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				dragSourceHandleDragFinished(event, sourceNames);
				dndSelection = null;
				sourceNames = null;
				isDragging = false;
				handleDeferredRefresh();
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (dndSelection == null || dndSelection.length == 0)
					return;
				if (!FileTransfer.getInstance().isSupportedType(event.dataType))
					return;

				sourceNames = new String[dndSelection.length];
				for (int i = 0; i < dndSelection.length; i++) {
					File file = (File) dndSelection[i].getData(FileViewerConstants.TABLEITEMDATA_FILE);
					sourceNames[i] = file.getAbsolutePath();
				}
				event.data = sourceNames;
			}
		});
		return dragSource;
	}

	/**
	 * Creates the Drag & Drop DropTarget for items being dropped onto the
	 * table.
	 * 
	 * @return the DropTarget for the table
	 */
	private DropTarget createTableDropTarget(final Table table) {
		DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				isDropping = true;
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				isDropping = false;
				handleDeferredRefresh();
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				dropTargetValidate(event, getTargetFile(event));
				event.feedback |= DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				File targetFile = getTargetFile(event);
				if (dropTargetValidate(event, targetFile))
					dropTargetHandleDrop(event, targetFile);
			}

			private File getTargetFile(DropTargetEvent event) {
				// Determine the target File for the drop
				TableItem item = table.getItem(table.toControl(new Point(event.x, event.y)));
				File targetFile = null;
				if (item == null) {
					// We are over an unoccupied area of the table.
					// If it is a COPY, we can use the table's root file.
					if (event.detail == DND.DROP_COPY) {
						targetFile = (File) table.getData(FileViewerConstants.TABLEDATA_DIR);
					}
				} else {
					// We are over a particular item in the table, use the
					// item's file
					targetFile = (File) item.getData(FileViewerConstants.TABLEITEMDATA_FILE);
				}
				return targetFile;
			}
		});
		return dropTarget;
	}

	/**
	 * Notifies the application components that a new current directory has been
	 * selected
	 * 
	 * @param dir
	 *            the directory that was selected, null is ignored
	 */
	private void notifySelectedDirectory(File dir) {
		if (dir == null)
			return;
		if (currentDirectory != null && dir.equals(currentDirectory))
			return;
		currentDirectory = dir;
		notifySelectedFiles(null);

		/*
		 * Shell: Sets the title to indicate the selected directory
		 */
//		parent.setText(getResourceString("Title", new Object[] { currentDirectory.getPath() }));

		/*
		 * Table view: Displays the contents of the selected directory.
		 */
		workerUpdate(dir, false);

		/*
		 * Combo view: Sets the combo box to point to the selected directory.
		 */
		final File[] comboRoots = (File[]) combo.getData(FileViewerConstants.COMBODATA_ROOTS);
		int comboEntry = -1;
		if (comboRoots != null) {
			for (int i = 0; i < comboRoots.length; ++i) {
				if (dir.equals(comboRoots[i])) {
					comboEntry = i;
					break;
				}
			}
		}
		if (comboEntry == -1)
			combo.setText(dir.getPath());
		else
			combo.select(comboEntry);

		/*
		 * Tree view: If not already expanded, recursively expands the parents
		 * of the specified directory until it is visible.
		 */
		List<File> path = new ArrayList<File>();
		// Build a stack of paths from the root of the tree
		while (dir != null) {
			path.add(dir);
			dir = dir.getParentFile();
		}
		// Recursively expand the tree to get to the specified directory
		TreeItem[] items = tree.getItems();
		TreeItem lastItem = null;
		for (int i = path.size() - 1; i >= 0; --i) {
			final File pathElement = path.get(i);

			// Search for a particular File in the array of tree items
			// No guarantee that the items are sorted in any recognizable
			// fashion, so we'll
			// just sequential scan. There shouldn't be more than a few thousand
			// entries.
			TreeItem item = null;
			for (int k = 0; k < items.length; ++k) {
				item = items[k];
				if (item.isDisposed())
					continue;
				final File itemFile = (File) item.getData(FileViewerConstants.TREEITEMDATA_FILE);
				if (itemFile != null && itemFile.equals(pathElement))
					break;
			}
			if (item == null)
				break;
			lastItem = item;
			if (i != 0 && !item.getExpanded()) {
				treeExpandItem(item);
				item.setExpanded(true);
			}
			items = item.getItems();
		}
		tree.setSelection((lastItem != null) ? new TreeItem[] { lastItem } : new TreeItem[0]);
	}

	/**
	 * Notifies the application components that files have been selected
	 * 
	 * @param files
	 *            the files that were selected, null or empty array indicates no
	 *            active selection
	 */
	private void notifySelectedFiles(File[] files) {
		/*
		 * Details: Update the details that are visible on screen.
		 */
		if ((files != null) && (files.length != 0)) {
			numObjectsLabel.setText(Utils.getResourceString("details.NumberOfSelectedFiles.text",
					new Object[] { new Integer(files.length) }));
			long fileSize = 0L;
			for (int i = 0; i < files.length; ++i) {
				fileSize += files[i].length();
			}
			diskSpaceLabel.setText(Utils.getResourceString("details.FileSize.text", new Object[] { new Long(fileSize) }));
		} else {
			// No files selected
			diskSpaceLabel.setText("");
			if (currentDirectory != null) {
				int numObjects = Utils.getDirectoryList(currentDirectory).length;
				numObjectsLabel.setText(Utils.
						getResourceString("details.DirNumberOfObjects.text", new Object[] { new Integer(numObjects) }));
			} else {
				numObjectsLabel.setText("");
			}
		}
	}

	/**
	 * Notifies the application components that files must be refreshed
	 * 
	 * @param files
	 *            the files that need refreshing, empty array is a no-op, null
	 *            refreshes all
	 */
	public void notifyRefreshFiles(File[] files) {
		if (files != null && files.length == 0)
			return;

		if ((deferredRefreshRequested) && (deferredRefreshFiles != null) && (files != null)) {
			// merge requests
			File[] newRequest = new File[deferredRefreshFiles.length + files.length];
			System.arraycopy(deferredRefreshFiles, 0, newRequest, 0, deferredRefreshFiles.length);
			System.arraycopy(files, 0, newRequest, deferredRefreshFiles.length, files.length);
			deferredRefreshFiles = newRequest;
		} else {
			deferredRefreshFiles = files;
			deferredRefreshRequested = true;
		}
		handleDeferredRefresh();
	}

	/**
	 * Handles deferred Refresh notifications (due to Drag & Drop)
	 */
	public void handleDeferredRefresh() {
		if (isDragging || isDropping || !deferredRefreshRequested)
			return;
		if (progressDialog != null) {
			progressDialog.close();
			progressDialog = null;
		}

		deferredRefreshRequested = false;
		File[] files = deferredRefreshFiles;
		deferredRefreshFiles = null;
		if (parent == null)
			return;
		parent.setCursor(getIconCache().stockCursors[getIconCache().cursorWait]);

		/*
		 * Table view: Refreshes information about any files in the list and
		 * their children.
		 */
		boolean refreshTable = false;
		if (files != null) {
			for (int i = 0; i < files.length; ++i) {
				final File file = files[i];
				if (file.equals(currentDirectory)) {
					refreshTable = true;
					break;
				}
				File parentFile = file.getParentFile();
				if ((parentFile != null) && (parentFile.equals(currentDirectory))) {
					refreshTable = true;
					break;
				}
			}
		} else
			refreshTable = true;
		if (refreshTable)
			workerUpdate(currentDirectory, true);

		/*
		 * Combo view: Refreshes the list of roots
		 */
		final File[] roots = getRoots();

		if (files == null) {
			boolean refreshCombo = false;
			final File[] comboRoots = (File[]) combo.getData(FileViewerConstants.COMBODATA_ROOTS);

			if ((comboRoots != null) && (comboRoots.length == roots.length)) {
				for (int i = 0; i < roots.length; ++i) {
					if (!roots[i].equals(comboRoots[i])) {
						refreshCombo = true;
						break;
					}
				}
			} else
				refreshCombo = true;

			if (refreshCombo) {
				combo.removeAll();
				combo.setData(FileViewerConstants.COMBODATA_ROOTS, roots);
				for (int i = 0; i < roots.length; ++i) {
					final File file = roots[i];
					combo.add(file.getPath());
				}
			}
		}

		/*
		 * Tree view: Refreshes information about any files in the list and
		 * their children.
		 */
		TreeUtils.treeRefresh(roots, tree, this, getIconCache());

		// Remind everyone where we are in the filesystem
		final File dir = currentDirectory;
		currentDirectory = null;
		notifySelectedDirectory(dir);

		parent.setCursor(getIconCache().stockCursors[getIconCache().cursorDefault]);
	}

	/**
	 * Performs the default action on a set of files.
	 * 
	 * @param files
	 *            the array of files to process
	 */
	private void doDefaultFileAction(File[] files) {
		// only uses the 1st file (for now)
		if (files.length == 0)
			return;
		final File file = files[0];

		if (file.isDirectory()) {
			notifySelectedDirectory(file);
		} else {
			final IOpenFileAction action = Utils.getFirstPertinentAction();
			if (action!=null) {
				action.openFile(file.toPath());
				return;
			}
			final String fileName = file.getAbsolutePath();
			try {
				EclipseUtils.openExternalEditor(fileName);
			} catch (PartInitException e) {
				logger.error("Cannot open file " + file, e);
			}
		}
	}

	/**
	 * Navigates to the parent directory
	 */
	public void doParent() {
		if (currentDirectory == null)
			return;
		File parentDirectory = currentDirectory.getParentFile();
		notifySelectedDirectory(parentDirectory);
	}

	/**
	 * Performs a refresh
	 */
	public void doRefresh() {
		notifyRefreshFiles(null);
	}

	/**
	 * Change the viewer layout
	 */
	public void doLayout() {
		if ((sashForm == null) || sashForm.isDisposed())
			return;
		int orientation = sashForm.getOrientation();
		sashForm.setOrientation(orientation == SWT.HORIZONTAL ? SWT.VERTICAL : SWT.HORIZONTAL);
		parent.layout();
	}

	/**
	 * Validates a drop target as a candidate for a drop operation.
	 * <p>
	 * Used in dragOver() and dropAccept().<br>
	 * Note event.detail is set to DND.DROP_NONE by this method if the target is
	 * not valid.
	 * </p>
	 * 
	 * @param event
	 *            the DropTargetEvent to validate
	 * @param targetFile
	 *            the File representing the drop target location under
	 *            inspection, or null if none
	 */
	private boolean dropTargetValidate(DropTargetEvent event, File targetFile) {
		if (targetFile != null && targetFile.isDirectory()) {
			if (event.detail != DND.DROP_COPY && event.detail != DND.DROP_MOVE) {
				event.detail = DND.DROP_MOVE;
			}
		} else {
			event.detail = DND.DROP_NONE;
		}
		return event.detail != DND.DROP_NONE;
	}

	/**
	 * Handles a drop on a dropTarget.
	 * <p>
	 * Used in drop().<br>
	 * Note event.detail is modified by this method.
	 * </p>
	 * 
	 * @param event
	 *            the DropTargetEvent passed as parameter to the drop() method
	 * @param targetFile
	 *            the File representing the drop target location under
	 *            inspection, or null if none
	 */
	private void dropTargetHandleDrop(DropTargetEvent event, File targetFile) {
		// Get dropped data (an array of filenames)
		if (!dropTargetValidate(event, targetFile))
			return;
		final String[] sourceNames = (String[]) event.data;
		if (sourceNames == null)
			event.detail = DND.DROP_NONE;
		if (event.detail == DND.DROP_NONE)
			return;

		// Open progress dialog
		progressDialog = new ProgressDialog(Display.getDefault().getActiveShell(),
				(event.detail == DND.DROP_MOVE) ? ProgressDialog.MOVE : ProgressDialog.COPY);
		progressDialog.setTotalWorkUnits(sourceNames.length);
		progressDialog.open();

		// Copy each file
		List<File> processedFiles = new ArrayList<File>();
		for (int i = 0; (i < sourceNames.length) && (!progressDialog.isCancelled()); i++) {
			final File source = new File(sourceNames[i]);
			final File dest = new File(targetFile, source.getName());
			if (source.equals(dest))
				continue; // ignore if in same location

			progressDialog.setDetailFile(source, ProgressDialog.COPY);
			while (!progressDialog.isCancelled()) {
				if (FileUtils.copyFileStructure(source, dest, simulateOnly, progressDialog)) {
					processedFiles.add(source);
					break;
				} else if (!progressDialog.isCancelled()) {
					if (event.detail == DND.DROP_MOVE && (!isDragging)) {
						// It is not possible to notify an external drag source
						// that a drop
						// operation was only partially successful. This is
						// particularly a
						// problem for DROP_MOVE operations since unless the
						// source gets
						// DROP_NONE, it will delete the original data including
						// bits that
						// may not have been transferred successfully.
						MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
						box.setText(Utils.getResourceString("dialog.FailedCopy.title"));
						box.setMessage(Utils.
								getResourceString("dialog.FailedCopy.description", new Object[] { source, dest }));
						int button = box.open();
						if (button == SWT.CANCEL) {
							i = sourceNames.length;
							event.detail = DND.DROP_NONE;
							break;
						}
					} else {
						// We can recover gracefully from errors if the drag
						// source belongs
						// to this application since it will look at
						// processedDropFiles.
						MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
						box.setText(Utils.getResourceString("dialog.FailedCopy.title"));
						box.setMessage(Utils.
								getResourceString("dialog.FailedCopy.description", new Object[] { source, dest }));
						int button = box.open();
						if (button == SWT.ABORT)
							i = sourceNames.length;
						if (button != SWT.RETRY)
							break;
					}
				}
				progressDialog.addProgress(1);
			}
		}
		if (isDragging) {
			// Remember exactly which files we processed
			processedDropFiles = processedFiles.toArray(new File[processedFiles.size()]);
		} else {
			progressDialog.close();
			progressDialog = null;
		}
		notifyRefreshFiles(new File[] { targetFile });
	}

	/**
	 * Handles the completion of a drag on a dragSource.
	 * <p>
	 * Used in dragFinished().<br>
	 * </p>
	 * 
	 * @param event
	 *            the DragSourceEvent passed as parameter to the dragFinished()
	 *            method
	 * @param sourceNames
	 *            the names of the files that were dragged (event.data is
	 *            invalid)
	 */
	private void dragSourceHandleDragFinished(DragSourceEvent event, String[] sourceNames) {
		if (sourceNames == null)
			return;
		if (event.detail != DND.DROP_MOVE)
			return;

		// Get array of files that were actually transferred
		final File[] sourceFiles;
		if (processedDropFiles != null) {
			sourceFiles = processedDropFiles;
		} else {
			sourceFiles = new File[sourceNames.length];
			for (int i = 0; i < sourceNames.length; ++i)
				sourceFiles[i] = new File(sourceNames[i]);
		}
		if (progressDialog == null)
			progressDialog = new ProgressDialog(Display.getDefault().getActiveShell(), ProgressDialog.MOVE);
		progressDialog.setTotalWorkUnits(sourceFiles.length);
		progressDialog.setProgress(0);
		progressDialog.open();

		// Delete each file
		for (int i = 0; (i < sourceFiles.length) && (!progressDialog.isCancelled()); i++) {
			final File source = sourceFiles[i];
			progressDialog.setDetailFile(source, ProgressDialog.DELETE);
			while (!progressDialog.isCancelled()) {
				if (FileUtils.deleteFileStructure(source, simulateOnly, progressDialog)) {
					break;
				} else if (!progressDialog.isCancelled()) {
					MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
					box.setText(Utils.getResourceString("dialog.FailedDelete.title"));
					box.setMessage(Utils.getResourceString("dialog.FailedDelete.description", new Object[] { source }));
					int button = box.open();
					if (button == SWT.ABORT)
						i = sourceNames.length;
					if (button == SWT.RETRY)
						break;
				}
			}
			progressDialog.addProgress(1);
		}
		notifyRefreshFiles(sourceFiles);
		progressDialog.close();
		progressDialog = null;
	}

	/**
	 * Gets filesystem root entries
	 * 
	 * @return an array of Files corresponding to the root directories on the
	 *         platform, may be empty but not null
	 */
	private File[] getRoots() {
		/*
		 * On JDK 1.22 only...
		 */
		// return File.listRoots();

		/*
		 * On JDK 1.1.7 and beyond... -- PORTABILITY ISSUES HERE --
		 */
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			List<File> list = new ArrayList<File>();
			list.add(new File(FileViewerConstants.DRIVE_A));
			list.add(new File(FileViewerConstants.DRIVE_B));
			for (char i = 'c'; i <= 'z'; ++i) {
				File drive = new File(i + ":" + File.separator);
				if (drive.isDirectory() && drive.exists()) {
					list.add(drive);
					if (initial && i == 'c') {
						currentDirectory = drive;
						initial = false;
					}
				}
			}
			File[] roots = list.toArray(new File[list.size()]);
			Utils.sortFiles(roots);
			return roots;
		}
		File root = new File(File.separator);
		if (initial) {
			currentDirectory = root;
			initial = false;
		}
		return new File[] { root };
	}

	/*
	 * This worker updates the table with file information in the background.
	 * <p> Implementation notes: <ul> <li> It is designed such that it can be
	 * interrupted cleanly. <li> It uses asyncExec() in some places to ensure
	 * that SWT Widgets are manipulated in the right thread. Exclusive use of
	 * syncExec() would be inappropriate as it would require a pair of context
	 * switches between each table update operation. </ul> </p>
	 */

	/**
	 * Stops the worker and waits for it to terminate.
	 */
	private void workerStop() {
		if (workerThread == null)
			return;
		synchronized (workerLock) {
			workerCancelled = true;
			workerStopped = true;
			workerLock.notifyAll();
		}
		while (workerThread != null) {
			if (!Display.getDefault().readAndDispatch())
				Display.getDefault().sleep();
		}
	}

	/**
	 * Notifies the worker that it should update itself with new data. Cancels
	 * any previous operation and begins a new one.
	 * 
	 * @param dir
	 *            the new base directory for the table, null is ignored
	 * @param force
	 *            if true causes a refresh even if the data is the same
	 */
	private void workerUpdate(File dir, boolean force) {
		if (dir == null)
			return;
		if ((!force) && (workerNextDir != null) && (workerNextDir.equals(dir)))
			return;

		synchronized (workerLock) {
			workerNextDir = dir;
			workerStopped = false;
			workerCancelled = true;
			workerLock.notifyAll();
		}
		if (workerThread == null) {
			workerThread = new Thread(workerRunnable);
			workerThread.start();
		}
	}

	/**
	 * Manages the worker's thread
	 */
	private final Runnable workerRunnable = new Runnable() {
		@Override
		public void run() {
			while (!workerStopped) {
				synchronized (workerLock) {
					workerCancelled = false;
					workerStateDir = workerNextDir;
				}
				workerExecute();
				synchronized (workerLock) {
					try {
						if ((!workerCancelled) && (workerStateDir == workerNextDir))
							workerLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			workerThread = null;
			// wake up UI thread in case it is in a modal loop awaiting thread
			// termination
			// (see workerStop())
			Display.getDefault().wake();
		}
	};

	/**
	 * Updates the table's contents
	 */
	private void workerExecute() {
		File[] dirList;
		// Clear existing information
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tableContentsOfLabel.setText(Utils.getResourceString("details.ContentsOf.text",
						new Object[] { workerStateDir.getPath() }));
				table.removeAll();
				table.setData(FileViewerConstants.TABLEDATA_DIR, workerStateDir);
			}
		});
		dirList = Utils.getDirectoryList(workerStateDir);

		for (int i = 0; (!workerCancelled) && (i < dirList.length); i++) {
			workerAddFileDetails(dirList[i]);
		}

	}

	/**
	 * Adds a file's detail information to the directory list
	 */
	private void workerAddFileDetails(final File file) {
		final String nameString = file.getName();
		final String dateString = FileViewerConstants.dateFormat.format(new Date(file.lastModified()));
		final String sizeString;
		final String typeString;
		final Image iconImage;

		if (file.isDirectory()) {
			typeString = Utils.getResourceString("filetype.Folder");
			sizeString = "";
			iconImage = getIconCache().stockImages[getIconCache().iconClosedFolder];
		} else {
			sizeString = Utils.getResourceString("filesize.KB", new Object[] { new Long((file.length() + 512) / 1024) });

			int dot = nameString.lastIndexOf('.');
			if (dot != -1) {
				String extension = nameString.substring(dot);
				Program program = Program.findProgram(extension);
				if (program != null) {
					typeString = program.getName();
					iconImage = getIconCache().getIconFromProgram(program);
				} else {
					typeString = Utils.getResourceString("filetype.Unknown", new Object[] { extension.toUpperCase() });
					iconImage = getIconCache().stockImages[getIconCache().iconFile];
				}
			} else {
				typeString = Utils.getResourceString("filetype.None");
				iconImage = getIconCache().stockImages[getIconCache().iconFile];
			}
		}
		final String[] strings = new String[] { nameString, sizeString, typeString, dateString };

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				// guard against the shell being closed before this runs
				if (parent.isDisposed())
					return;
				TableItem tableItem = new TableItem(table, 0);
				tableItem.setText(strings);
				tableItem.setImage(iconImage);
				tableItem.setData(FileViewerConstants.TABLEITEMDATA_FILE, file);
			}
		});
	}

	public IconCache getIconCache() {
		return iconCache;
	}

	public void setIconCache(IconCache iconCache) {
		this.iconCache = iconCache;
	}

	public void setCurrentDirectory(String savedDirectory) {
		currentDirectory = new File(savedDirectory);
		doRefresh();
	}

	public String getSavedDirectory() {
		return currentDirectory.getAbsolutePath();
	}
}
