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
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.fileviewer.table.FileTableExplorer;
import org.dawnsci.fileviewer.tree.FileTreeExplorer;
import org.dawnsci.fileviewer.tree.TreeUtils;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
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

	private IconCache iconCache = new IconCache();

	/* Simulate only flag */
	// when true, disables actual filesystem manipulations and outputs results
	// to standard out
	private boolean simulateOnly = true;

	private Composite parent;
	private SashForm sashForm;

	private FileTreeExplorer treeExplo;

	private FileTableExplorer tableExplo;

	/**
	 * Closes the main program.
	 */
	public void close() {
		if (tableExplo != null)
			tableExplo.workerStop();
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
		
		// Tree
		treeExplo = new FileTreeExplorer(this);
		treeExplo.createTreeView(sashForm);
		
		// Table
		tableExplo = new FileTableExplorer(this, sashForm, SWT.BORDER|SWT.V_SCROLL|SWT.FULL_SELECTION);

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
			tableExplo.workerUpdate(dir, false);

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
	public void notifySelectedFiles(File[] files) {
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
		if (refreshTable && tableExplo != null)
			tableExplo.workerUpdate(currentDirectory, true);

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
	public void doDefaultFileAction(File[] files) {
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
	 * Performs a conversion
	 */
	public void doConvert() {
		// TODO Auto-generated method stub
		
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

	public TableViewer getTableViewer() {
		if (tableExplo != null)
			return tableExplo.getTableViewer();
		return null;
	}

	public IStructuredSelection getSelection() {
		ITransferableDataObject obj;
		if (tableExplo != null)
			return tableExplo.getTableViewer().getStructuredSelection();
		return null;
	}
}
