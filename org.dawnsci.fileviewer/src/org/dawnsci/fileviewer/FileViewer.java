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
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.IOpenFileAction;
import org.dawnsci.fileviewer.handlers.ConvertHandler;
import org.dawnsci.fileviewer.handlers.LayoutHandler;
import org.dawnsci.fileviewer.handlers.OpenHandler;
import org.dawnsci.fileviewer.handlers.ParentHandler;
import org.dawnsci.fileviewer.handlers.PreferencesHandler;
import org.dawnsci.fileviewer.handlers.RefreshHandler;
import org.dawnsci.fileviewer.table.FileTableContent;
import org.dawnsci.fileviewer.table.FileTableExplorer;
import org.dawnsci.fileviewer.table.RetrieveFileListJob;
import org.dawnsci.fileviewer.tree.FileTreeExplorer;
import org.dawnsci.fileviewer.tree.TreeUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Viewer based on the SWT FileViewer example
 */
@SuppressWarnings("restriction")
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

	private RetrieveFileListJob retrieveDirJob;

	/* Combo view */
	private Combo combo;

	private IconCache iconCache = new IconCache();

	/* Simulate only flag */
	// when true, disables actual filesystem manipulations and outputs results
	// to standard out
	private boolean simulateOnly = true;

	private Composite parent;
	private SashForm sashForm;

	private FileTreeExplorer treeExplo;

	private FileTableExplorer tableExplo;

	private ECommandService commandService;

	private EHandlerService handlerService;

	private ESelectionService tableSelectionService;

	private ISelectionChangedListener tableSelectionChangedListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			// set the selection to the service
			tableSelectionService.setSelection(selection);
		}
	};

	/**
	 * Closes the main program.
	 */
	public void close() {
		if (tableExplo != null)
			tableExplo.workerStop();
		getIconCache().freeResources();
		if (tableExplo != null)
			tableExplo.getTableViewer().removeSelectionChangedListener(tableSelectionChangedListener);
	}

	/**
	 * Construct the UI
	 * 
	 * @param container
	 *            the ShellContainer managing the composite we are rendering inside
	 */
	public void createCompositeContents(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);
		this.parent = parent;
		
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = 200;
		createComboView(parent, gridData);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;

		createSWTToolBar(parent);

		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.horizontalSpan = 4;
		sashForm.setLayoutData(gridData);
		
		// Tree
		treeExplo = new FileTreeExplorer(this);
		treeExplo.createTreeView(sashForm);

		// Table
		tableExplo = new FileTableExplorer(this, sashForm, SWT.BORDER|SWT.V_SCROLL|SWT.FULL_SELECTION);
		createSWTPopupMenu(tableExplo.getTable());
		tableExplo.getTableViewer().addSelectionChangedListener(tableSelectionChangedListener);
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

	public void setCommandService(ECommandService service) {
		this.commandService = service;
	}

	public void setHandlerService(EHandlerService service) {
		this.handlerService = service;
	}

	public void setSelectionService(ESelectionService service) {
		this.tableSelectionService = service;
	}

	private void createSWTToolBar(Composite parent) {
		//create the toolbar programmatically
		ToolBar bar = new ToolBar(parent, SWT.NONE);
		bar.setBackground(parent.getBackground());
		ToolItem parentToolItem = new ToolItem(bar, SWT.NONE);
		parentToolItem.setToolTipText(Utils.getResourceString(FileViewerConstants.PARENT_TIP));
		parentToolItem.setImage(Activator.getImage("icons/arrow-090.png"));
		parentToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand(FileViewerConstants.PARENT_CMD, null);
				handlerService.activateHandler(FileViewerConstants.PARENT_CMD, new ParentHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});

		ToolItem refreshToolItem = new ToolItem(bar, SWT.NONE);
		refreshToolItem.setToolTipText(Utils.getResourceString(FileViewerConstants.REFRESH_TIP));
		refreshToolItem.setImage(Activator.getImage("icons/arrow-circle-double-135.png"));
		refreshToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand(FileViewerConstants.REFRESH_CMD, null);
				handlerService.activateHandler(FileViewerConstants.REFRESH_CMD, new RefreshHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});

		ToolItem layoutToolItem = new ToolItem(bar, SWT.NONE);
		layoutToolItem.setToolTipText(Utils.getResourceString(FileViewerConstants.LAYOUT_TIP));
		layoutToolItem.setImage(Activator.getImage("icons/layout-design.png"));
		layoutToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand(FileViewerConstants.LAYOUT_CMD, null);
				handlerService.activateHandler(FileViewerConstants.LAYOUT_CMD, new LayoutHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});

		ToolItem preferencesToolItem = new ToolItem(bar, SWT.NONE);
		preferencesToolItem.setToolTipText(Utils.getResourceString(FileViewerConstants.PREFERENCES_TIP));
		preferencesToolItem.setImage(Activator.getImage("icons/preferences.png"));
		preferencesToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand(FileViewerConstants.PREFERENCES_CMD, null);
				handlerService.activateHandler(FileViewerConstants.PREFERENCES_CMD, new PreferencesHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});
	}

	private void createSWTPopupMenu(Table table) {
		// Create popup menu programmatically
		Menu menuTable = new Menu(table);
		table.setMenu(menuTable);

		MenuItem openItem = new MenuItem(menuTable, SWT.None);
		openItem.setText("Open");
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand("org.dawnsci.fileviewer.openCommand", null);
				handlerService.activateHandler("org.dawnsci.fileviewer.openCommand", new OpenHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});

		MenuItem convertItem = new MenuItem(menuTable, SWT.None);
		convertItem.setText("Convert...");
		convertItem.setImage(Activator.getImage("icons/convert.png"));
		convertItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand("org.dawnsci.fileviewer.convertCommand", null);
				handlerService.activateHandler("org.dawnsci.fileviewer.convertCommand", new ConvertHandler(FileViewer.this));
				handlerService.executeHandler(myCommand);
			}
		});

		table.addListener(SWT.MouseDown, new Listener(){
			@Override
			public void handleEvent(Event event) {
				TableItem[] selection = table.getSelection();
				if (selection.length != 0 && (event.button == 3)) {
					menuTable.setVisible(true);
				}
			}
		});
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
					doDefaultFileAction(new FileTableContent[] {new FileTableContent(new File(text))});
					// open directory
					notifySelectedDirectory(new File(directory));
				}
			}
		});
	}

	/**
	 * Notifies the application components that a new current directory has been
	 * selected
	 * 
	 * @param dir
	 *            the directory that was selected, null is ignored
	 */
	public void notifySelectedDirectory(File dir) {
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
		if (tableExplo != null)
			tableExplo.workerUpdate(dir, false, tableExplo.getSortType(), tableExplo.getSortDirection());

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
		TreeItem[] items = treeExplo.getTree().getItems();
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
				treeExplo.treeExpandItem(item);
				item.setExpanded(true);
			}
			items = item.getItems();
		}
		treeExplo.getTree().setSelection((lastItem != null) ? new TreeItem[] { lastItem } : new TreeItem[0]);
	}

	/**
	 * Notifies the application components that files have been selected
	 * 
	 * @param files
	 *            the files that were selected, null or empty array indicates no
	 *            active selection
	 */
	public void notifySelectedFiles(FileTableContent[] files) {
		/*
		 * Details: Update the details that are visible on screen.
		 */
		if ((files != null) && (files.length != 0)) {
			numObjectsLabel.setText(Utils.getResourceString("details.NumberOfSelectedFiles.text",
					new Object[] { new Integer(files.length) }));
			long fileSize = 0L;
			for (int i = 0; i < files.length; ++i) {
				fileSize += files[i].getFile().length();
			}
			diskSpaceLabel.setText(Utils.getResourceString("details.FileSize.text", new Object[] { new Long(fileSize) }));
		} else {
			// No files selected
			diskSpaceLabel.setText("");
			if (currentDirectory != null) {
				// Retrieve the new list of files
				if (retrieveDirJob != null && retrieveDirJob.getState() == Job.RUNNING) {
					retrieveDirJob.cancel();
				}
				retrieveDirJob = new RetrieveFileListJob(currentDirectory, tableExplo.getSortType(), tableExplo.getSortDirection(), tableExplo.getFilter(), tableExplo.getUseRegex(), true);
//				retrieveDirJob.setThread(workerThread);
				retrieveDirJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						int numObjects = retrieveDirJob.getDirListCount();
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								numObjectsLabel.setText(Utils.getResourceString("details.DirNumberOfObjects.text", new Object[] { new Integer(numObjects) }));
							}
						});
					}
				});
				retrieveDirJob.schedule();
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
		if (refreshTable && tableExplo != null)
			tableExplo.workerUpdate(currentDirectory, true, tableExplo.getSortType(), tableExplo.getSortDirection());

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
		TreeUtils.treeRefresh(roots, treeExplo.getTree(), this, getIconCache());

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
	public void doDefaultFileAction(FileTableContent[] files) {
		// only uses the 1st file (for now)
		if (files.length == 0)
			return;
		final File file = files[0].getFile();

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
	 * Open selected files
	 */
	public void doOpen() {
		if (tableExplo != null)
			doDefaultFileAction(tableExplo.getSelectedFiles());
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
	 * Open the File Viewer preference page
	 */
	public void openPreferences() {
		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
				"org.dawnsci.fileviewer.preferencePage", null, null);
		if (pref != null) {
			pref.open();
		}
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
	public boolean dropTargetValidate(DropTargetEvent event, File targetFile) {
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
	public void dropTargetHandleDrop(DropTargetEvent event, File targetFile) {
		// Get dropped data (an array of filenames)
		if (!dropTargetValidate(event, targetFile))
			return;
		
		// no need to display a dialog with a fear inspiring message, though it's in fact harmless 
		if (simulateOnly)
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
	public void dragSourceHandleDragFinished(DragSourceEvent event, String[] sourceNames) {
		if (sourceNames == null)
			return;
		if (event.detail != DND.DROP_MOVE)
			return;

		// no need to display a dialog with a fear inspiring message, though it's in fact harmless 
		if (simulateOnly)
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
		
		File[] roots = File.listRoots();
		
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			for (File root : roots) {
				if (root.isDirectory() &&
					root.exists() &&
					initial &&
					(root.getAbsolutePath().startsWith("c") ||
					 root.getAbsolutePath().startsWith("C"))) {
					
					currentDirectory = root;
					initial = false;
				}
			}
		} else if (initial){
			currentDirectory = roots[0];
			initial = false;
		}
		return roots;
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

	public File getCurrentDirectory() {
		return currentDirectory;
	}

	public String getSavedDirectory() {
		return currentDirectory.getAbsolutePath();
	}

	public FileTableExplorer getTableExplorer() {
		return tableExplo;
	}

	public boolean isDragging() {
		return isDragging;
	}

	public void setIsDragging(boolean isDragging) {
		this.isDragging = isDragging;
	}

	public boolean isDropping() {
		return isDropping;
	}

	public void setIsDropping(boolean isDropping) {
		this.isDropping = isDropping;
	}

	public File[] getProcessedDropFiles() {
		return processedDropFiles;
	}

	public void setProcessedDropFiles(File[] processDropFiles) {
		this.processedDropFiles = processDropFiles;
	}

	public IStructuredSelection getSelections() {
		if (tableExplo != null) {
			IStructuredSelection sel = tableExplo.getTableViewer().getStructuredSelection();
			return sel;
		}
		return null;
	}

	public FileTreeExplorer getTreeExplorer() {
		return treeExplo;
	}

	/**
	 * 
	 * @return True if SI Unit is used to display size of files
	 */
	public boolean isSizeSIUnit() {
		return tableExplo.isSizeSIUnit();
	}

}
