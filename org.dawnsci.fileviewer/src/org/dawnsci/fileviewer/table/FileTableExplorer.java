/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Diamond Light Source - Custom modifications for Diamond's needs, use a JFace TableViewer
 *******************************************************************************/
package org.dawnsci.fileviewer.table;

import java.io.File;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.dawnsci.fileviewer.Utils;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.sda.navigator.views.IOpenFileAction;

public class FileTableExplorer {

	private static final Logger logger = LoggerFactory.getLogger(FileTableExplorer.class);

	/* Table view */
	private final String[] tableTitles = new String[] { Utils.getResourceString("table.Name.title"),
			Utils.getResourceString("table.Size.title"), Utils.getResourceString("table.Type.title"),
			Utils.getResourceString("table.Modified.title") };
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
	private FileViewer viewer;
	private Composite parent;

	private TableViewer tviewer;

	public FileTableExplorer(FileViewer viewer, Composite parent, int style) {
		this.parent = parent;
		this.viewer = viewer;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		tableContentsOfLabel = new Label(composite, SWT.BORDER);
		tableContentsOfLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		tviewer = new TableViewer(composite, SWT.VIRTUAL | SWT.BORDER|SWT.V_SCROLL|SWT.FULL_SELECTION);
//		tviewer.setLabelProvider(new FileTableLabelProvider(viewer));
		tviewer.setContentProvider(new FileTableContentProvider(tviewer));
		tviewer.setUseHashlookup(true);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tviewer.getTable().setLayoutData(gridData);
		createColumns();
		// make the selection available to other views
		tviewer.getTable().setHeaderVisible(true);
		tviewer.getTable().setLinesVisible(true);
		tviewer.getTable().setHeaderVisible(true);
		tviewer.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				viewer.notifySelectedFiles(getSelectedFiles());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				doDefaultFileAction(getSelectedFiles());
			}

			private File[] getSelectedFiles() {
				final TableItem[] items = tviewer.getTable().getSelection();
				final File[] files = new File[items.length];

				for (int i = 0; i < items.length; ++i) {
					files[i] = (File) items[i].getData(FileViewerConstants.TABLEITEMDATA_FILE);
				}
				return files;
			}
		});

		createTableDragSource(tviewer.getTable());
		createTableDropTarget(tviewer.getTable());
	}

	private void createColumns() {
		for (int i = 0; i < tableTitles.length; ++i) {
			TableViewerColumn column = createTableViewerColumn(tableTitles[i], FileViewerConstants.tableWidths[i], i);
			column.setLabelProvider(new FileColumnLabelProvider(viewer, i));
		}

	}

	private TableViewerColumn createTableViewerColumn(String header, int width, int idx) {
		TableViewerColumn column = new TableViewerColumn(tviewer, SWT.LEFT, idx);
		column.getColumn().setText(header);
		column.getColumn().setWidth(width);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(true);

		return column;
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
			viewer.notifySelectedDirectory(file);
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
				viewer.setIsDragging(true);
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				viewer.dragSourceHandleDragFinished(event, sourceNames);
				dndSelection = null;
				sourceNames = null;
				viewer.setIsDragging(false);
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
	public void workerStop() {
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
	public void workerUpdate(File dir, boolean force) {
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
		final File[] dirList;
		// Clear existing information
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tableContentsOfLabel.setText(Utils.getResourceString("details.ContentsOf.text",
						new Object[] { workerStateDir.getPath() }));
//				tviewer.getTable().removeAll();
//				tviewer.setInput(input);
//				tviewer.getTable().setData(FileViewerConstants.TABLEDATA_DIR, workerStateDir);
				File[] dirList = Utils.getDirectoryList(workerStateDir);
				tviewer.setInput(dirList);
				tviewer.setItemCount(dirList.length);
			}
		});
		

		
//		for (int i = 0; (!workerCancelled) && (i < dirList.length); i++) {
//			workerAddFileDetails(dirList[i]);
//		}

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
			iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconClosedFolder];
		} else {
			sizeString = Utils.getResourceString("filesize.KB", new Object[] { new Long((file.length() + 512) / 1024) });

			int dot = nameString.lastIndexOf('.');
			if (dot != -1) {
				String extension = nameString.substring(dot);
				Program program = Program.findProgram(extension);
				if (program != null) {
					typeString = program.getName();
					iconImage = viewer.getIconCache().getIconFromProgram(program);
				} else {
					typeString = Utils.getResourceString("filetype.Unknown", new Object[] { extension.toUpperCase() });
					iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconFile];
				}
			} else {
				typeString = Utils.getResourceString("filetype.None");
				iconImage = viewer.getIconCache().stockImages[viewer.getIconCache().iconFile];
			}
		}
		final String[] strings = new String[] { nameString, sizeString, typeString, dateString };

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				// guard against the shell being closed before this runs
				if (parent.isDisposed())
					return;
				TableItem tableItem = new TableItem(tviewer.getTable(), 0);
				tableItem.setText(strings);
				tableItem.setImage(iconImage);
				tableItem.setData(FileViewerConstants.TABLEITEMDATA_FILE, file);
			}
		});
	}

	public TableViewer getTableViewer() {
		return tviewer;
	}

	public Table getTable() {
		return tviewer.getTable();
	}
}
