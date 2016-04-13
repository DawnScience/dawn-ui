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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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

	private final static String DRIVE_A = "a:" + File.separator;
	private final static String DRIVE_B = "b:" + File.separator;

	/* UI elements */
	private ToolBar toolBar;

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
	private static final String COMBODATA_ROOTS = "Combo.roots";
	// File[]: Array of files whose paths are currently displayed in the combo
	private static final String COMBODATA_LASTTEXT = "Combo.lastText";
	// String: Previous selection text string

	private Combo combo;

	/* Tree view */
	private IconCache iconCache = new IconCache();
	private static final String TREEITEMDATA_FILE = "TreeItem.file";
	// File: File associated with tree item
	private static final String TREEITEMDATA_IMAGEEXPANDED = "TreeItem.imageExpanded";
	// Image: shown when item is expanded
	private static final String TREEITEMDATA_IMAGECOLLAPSED = "TreeItem.imageCollapsed";
	// Image: shown when item is collapsed
	private static final String TREEITEMDATA_STUB = "TreeItem.stub";
	// Object: if not present or null then the item has not been populated

	private Tree tree;
	private Label treeScopeLabel;

	/* Table view */
	private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	private static final String TABLEITEMDATA_FILE = "TableItem.file";
	// File: File associated with table row
	private static final String TABLEDATA_DIR = "Table.dir";
	// File: Currently visible directory
	private static final int[] tableWidths = new int[] { 150, 60, 75, 150 };

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
	 * Extension point used for opening files with special actions
	 */
	private static final String OPEN_FILE_EXTENSION_POINT = "uk.ac.diamond.sda.navigator.openFile";

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
		gridData.widthHint = 200;
		createComboView(parent, gridData);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		createToolBar(parent, gridData);

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
	 * Creates the File Menu.
	 * 
	 * @param parent
	 *            the parent menu
	 */
	@SuppressWarnings("unused")
	private void createFileMenu(Menu parent) {
		Menu menu = new Menu(parent);
		MenuItem header = new MenuItem(parent, SWT.CASCADE);
		header.setText(Utils.getResourceString("menu.File.text"));
		header.setMenu(menu);

		final MenuItem simulateItem = new MenuItem(menu, SWT.CHECK);
		simulateItem.setText(Utils.getResourceString("menu.File.SimulateOnly.text"));
		simulateItem.setSelection(simulateOnly);
		simulateItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				simulateOnly = simulateItem.getSelection();
			}
		});

		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Utils.getResourceString("menu.File.Close.text"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				parent.close();
			}
		});
	}

	/**
	 * Creates the Help Menu.
	 * 
	 * @param parent
	 *            the parent menu
	 */
	@SuppressWarnings("unused")
	private void createHelpMenu(Menu parent) {
		Menu menu = new Menu(parent);
		MenuItem header = new MenuItem(parent, SWT.CASCADE);
		header.setText(Utils.getResourceString("menu.Help.text"));
		header.setMenu(menu);

		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Utils.getResourceString("menu.Help.About.text"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
				box.setText(Utils.getResourceString("dialog.About.title"));
				box.setMessage(Utils.getResourceString("dialog.About.description", new Object[] { System.getProperty("os.name") }));
				box.open();
			}
		});
	}

	/**
	 * Creates the toolbar
	 * 
	 * @param shell
	 *            the shell on which to attach the toolbar
	 * @param layoutData
	 *            the layout data
	 */
	private void createToolBar(final Composite comp, Object layoutData) {
		toolBar = new ToolBar(comp, SWT.NONE);
		toolBar.setLayoutData(layoutData);
		toolBar.setBackground(comp.getBackground());
		ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(getIconCache().stockImages[getIconCache().cmdParent]);
		item.setToolTipText(Utils.getResourceString("tool.Parent.tiptext"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doParent();
			}
		});
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(getIconCache().stockImages[getIconCache().cmdRefresh]);
		item.setToolTipText(Utils.getResourceString("tool.Refresh.tiptext"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRefresh();
			}
		});

		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(getIconCache().stockImages[getIconCache().cmdLayoutEdit]);
		item.setToolTipText(Utils.getResourceString("tool.LayoutEdit.tiptext"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if ((sashForm == null) || sashForm.isDisposed())
					return;
				int orientation = sashForm.getOrientation();
				sashForm.setOrientation(orientation == SWT.HORIZONTAL ? SWT.VERTICAL : SWT.HORIZONTAL);
				parent.layout();
			}
		});

//		item = new ToolItem(toolBar, SWT.SEPARATOR);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdCut]);
//		item.setToolTipText(getResourceString("tool.Cut.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdCopy]);
//		item.setToolTipText(getResourceString("tool.Copy.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdPaste]);
//		item.setToolTipText(getResourceString("tool.Paste.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//
//		item = new ToolItem(toolBar, SWT.SEPARATOR);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdDelete]);
//		item.setToolTipText(getResourceString("tool.Delete.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdRename]);
//		item.setToolTipText(getResourceString("tool.Rename.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//
//		item = new ToolItem(toolBar, SWT.SEPARATOR);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdSearch]);
//		item.setToolTipText(getResourceString("tool.Search.tiptext"));
//		item.addSelectionListener(unimplementedListener);
//		item = new ToolItem(toolBar, SWT.PUSH);
//		item.setImage(getIconCache().stockImages[getIconCache().cmdPrint]);
//		item.setToolTipText(getResourceString("tool.Print.tiptext"));
//		item.addSelectionListener(unimplementedListener);
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
				final File[] roots = (File[]) combo.getData(COMBODATA_ROOTS);
				if (roots == null)
					return;
				int selection = combo.getSelectionIndex();
				if (selection >= 0 && selection < roots.length) {
					notifySelectedDirectory(roots[selection]);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String lastText = (String) combo.getData(COMBODATA_LASTTEXT);
				String text = combo.getText();
				if (text == null)
					return;
				if (lastText != null && lastText.equals(text))
					return;
				combo.setData(COMBODATA_LASTTEXT, text);
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
					File file = (File) item.getData(TREEITEMDATA_FILE);
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
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGEEXPANDED);
				if (image != null)
					item.setImage(image);
				treeExpandItem(item);
			}

			@Override
			public void treeCollapsed(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGECOLLAPSED);
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
					File file = (File) dndSelection[i].getData(TREEITEMDATA_FILE);
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
					targetFile = (File) item.getData(TREEITEMDATA_FILE);
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
		final Object stub = item.getData(TREEITEMDATA_STUB);
		if (stub == null)
			treeRefreshItem(item, true);
		parent.setCursor(getIconCache().stockCursors[getIconCache().cursorDefault]);
	}

	/**
	 * Traverse the entire tree and update only what has changed.
	 * 
	 * @param roots
	 *            the root directory listing
	 */
	private void treeRefresh(File[] masterFiles) {
		TreeItem[] items = tree.getItems();
		int masterIndex = 0;
		int itemIndex = 0;
		for (int i = 0; i < items.length; ++i) {
			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterIndex == masterFiles.length)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			final File masterFile = masterFiles[masterIndex];
			int compare = Utils.compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				++itemIndex;
				++masterIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(tree, SWT.NONE, itemIndex);
				treeInitVolume(newItem, masterFile);
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
			treeInitVolume(newItem, masterFile);
			new TreeItem(newItem, SWT.NONE); // placeholder child item to get
												// "expand" button
		}
	}

	/**
	 * Traverse an item in the tree and update only what has changed.
	 * 
	 * @param dirItem
	 *            the tree item of the directory
	 * @param forcePopulate
	 *            true iff we should populate non-expanded items as well
	 */
	private void treeRefreshItem(TreeItem dirItem, boolean forcePopulate) {
		final File dir = (File) dirItem.getData(TREEITEMDATA_FILE);

		if (!forcePopulate && !dirItem.getExpanded()) {
			// Refresh non-expanded item
			if (dirItem.getData(TREEITEMDATA_STUB) != null) {
				treeItemRemoveAll(dirItem);
				new TreeItem(dirItem, SWT.NONE); // placeholder child item to
													// get "expand" button
				dirItem.setData(TREEITEMDATA_STUB, null);
			}
			return;
		}
		// Refresh expanded item
		dirItem.setData(TREEITEMDATA_STUB, this); // clear stub flag

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
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterFile == null)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			int compare = Utils.compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				masterFile = null;
				++itemIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE, itemIndex);
				treeInitFolder(newItem, masterFile);
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
				treeInitFolder(newItem, masterFile);
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
	private void treeInitFolder(TreeItem item, File folder) {
		item.setText(folder.getName());
		item.setImage(getIconCache().stockImages[getIconCache().iconClosedFolder]);
		item.setData(TREEITEMDATA_FILE, folder);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, getIconCache().stockImages[getIconCache().iconOpenFolder]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, getIconCache().stockImages[getIconCache().iconClosedFolder]);
	}

	/**
	 * Initializes a volume item.
	 * 
	 * @param item
	 *            the TreeItem to initialize
	 * @param volume
	 *            the File associated with this TreeItem
	 */
	private void treeInitVolume(TreeItem item, File volume) {
		item.setText(volume.getPath());
		item.setImage(getIconCache().stockImages[getIconCache().iconClosedDrive]);
		item.setData(TREEITEMDATA_FILE, volume);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, getIconCache().stockImages[getIconCache().iconOpenDrive]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, getIconCache().stockImages[getIconCache().iconClosedDrive]);
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
			column.setWidth(tableWidths[i]);
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
					files[i] = (File) items[i].getData(TABLEITEMDATA_FILE);
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
					File file = (File) dndSelection[i].getData(TABLEITEMDATA_FILE);
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
						targetFile = (File) table.getData(TABLEDATA_DIR);
					}
				} else {
					// We are over a particular item in the table, use the
					// item's file
					targetFile = (File) item.getData(TABLEITEMDATA_FILE);
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
	void notifySelectedDirectory(File dir) {
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
		final File[] comboRoots = (File[]) combo.getData(COMBODATA_ROOTS);
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
				final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
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
	void notifySelectedFiles(File[] files) {
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
	void handleDeferredRefresh() {
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
			final File[] comboRoots = (File[]) combo.getData(COMBODATA_ROOTS);

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
				combo.setData(COMBODATA_ROOTS, roots);
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
		treeRefresh(roots);

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
	void doDefaultFileAction(File[] files) {
		// only uses the 1st file (for now)
		if (files.length == 0)
			return;
		final File file = files[0];

		if (file.isDirectory()) {
			notifySelectedDirectory(file);
		} else {
			final IOpenFileAction action = getFirstPertinentAction();
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

	private IOpenFileAction getFirstPertinentAction() {
		try {
			IConfigurationElement[] eles = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(OPEN_FILE_EXTENSION_POINT);
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
	 * Navigates to the parent directory
	 */
	void doParent() {
		if (currentDirectory == null)
			return;
		File parentDirectory = currentDirectory.getParentFile();
		notifySelectedDirectory(parentDirectory);
	}

	/**
	 * Performs a refresh
	 */
	void doRefresh() {
		notifyRefreshFiles(null);
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
				if (copyFileStructure(source, dest)) {
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
				if (deleteFileStructure(source)) {
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
	File[] getRoots() {
		/*
		 * On JDK 1.22 only...
		 */
		// return File.listRoots();

		/*
		 * On JDK 1.1.7 and beyond... -- PORTABILITY ISSUES HERE --
		 */
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			List<File> list = new ArrayList<File>();
			list.add(new File(DRIVE_A));
			list.add(new File(DRIVE_B));
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

	/**
	 * Copies a file or entire directory structure.
	 * 
	 * @param oldFile
	 *            the location of the old file or directory
	 * @param newFile
	 *            the location of the new file or directory
	 * @return true iff the operation succeeds without errors
	 */
	boolean copyFileStructure(File oldFile, File newFile) {
		if (oldFile == null || newFile == null)
			return false;

		// ensure that newFile is not a child of oldFile or a dupe
		File searchFile = newFile;
		do {
			if (oldFile.equals(searchFile))
				return false;
			searchFile = searchFile.getParentFile();
		} while (searchFile != null);

		if (oldFile.isDirectory()) {
			/*
			 * Copy a directory
			 */
			if (progressDialog != null) {
				progressDialog.setDetailFile(oldFile, ProgressDialog.COPY);
			}
			if (simulateOnly) {
				// System.out.println(getResourceString("simulate.DirectoriesCreated.text",
				// new Object[] { newFile.getPath() }));
			} else {
				if (!newFile.mkdirs())
					return false;
			}
			File[] subFiles = oldFile.listFiles();
			if (subFiles != null) {
				if (progressDialog != null) {
					progressDialog.addWorkUnits(subFiles.length);
				}
				for (int i = 0; i < subFiles.length; i++) {
					File oldSubFile = subFiles[i];
					File newSubFile = new File(newFile, oldSubFile.getName());
					if (!copyFileStructure(oldSubFile, newSubFile))
						return false;
					if (progressDialog != null) {
						progressDialog.addProgress(1);
						if (progressDialog.isCancelled())
							return false;
					}
				}
			}
		} else {
			/*
			 * Copy a file
			 */
			if (simulateOnly) {
				// System.out.println(getResourceString("simulate.CopyFromTo.text",
				// new Object[] { oldFile.getPath(), newFile.getPath() }));
			} else {
				FileReader in = null;
				FileWriter out = null;
				try {
					in = new FileReader(oldFile);
					out = new FileWriter(newFile);

					int count;
					while ((count = in.read()) != -1)
						out.write(count);
				} catch (FileNotFoundException e) {
					return false;
				} catch (IOException e) {
					return false;
				} finally {
					try {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					} catch (IOException e) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Deletes a file or entire directory structure.
	 * 
	 * @param oldFile
	 *            the location of the old file or directory
	 * @return true iff the operation succeeds without errors
	 */
	boolean deleteFileStructure(File oldFile) {
		if (oldFile == null)
			return false;
		if (oldFile.isDirectory()) {
			/*
			 * Delete a directory
			 */
			if (progressDialog != null) {
				progressDialog.setDetailFile(oldFile, ProgressDialog.DELETE);
			}
			File[] subFiles = oldFile.listFiles();
			if (subFiles != null) {
				if (progressDialog != null) {
					progressDialog.addWorkUnits(subFiles.length);
				}
				for (int i = 0; i < subFiles.length; i++) {
					File oldSubFile = subFiles[i];
					if (!deleteFileStructure(oldSubFile))
						return false;
					if (progressDialog != null) {
						progressDialog.addProgress(1);
						if (progressDialog.isCancelled())
							return false;
					}
				}
			}
		}
		if (simulateOnly) {
			// System.out.println(getResourceString("simulate.Delete.text",
			// new Object[] { oldFile.getPath(), oldFile.getPath() }));
			return true;
		}
		return oldFile.delete();
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
	void workerStop() {
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
	void workerUpdate(File dir, boolean force) {
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
				table.setData(TABLEDATA_DIR, workerStateDir);
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
		final String dateString = dateFormat.format(new Date(file.lastModified()));
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
				tableItem.setData(TABLEITEMDATA_FILE, file);
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
