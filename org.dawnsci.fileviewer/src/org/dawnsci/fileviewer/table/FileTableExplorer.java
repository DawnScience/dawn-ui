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
import java.util.Arrays;

import org.dawnsci.fileviewer.Activator;
import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.dawnsci.fileviewer.Utils;
import org.dawnsci.fileviewer.Utils.SortType;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTableExplorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FileTableExplorer.class);

	/* Table view */
	private final String[] tableTitles = new String[] {
			Utils.getResourceString(FileViewerConstants.NAME_TITLE),
			Utils.getResourceString(FileViewerConstants.SIZE_TITLE),
			Utils.getResourceString(FileViewerConstants.TYPE_TITLE),
			Utils.getResourceString(FileViewerConstants.MODIFIED_TITLE),
			Utils.getResourceString(FileViewerConstants.SCAN_TITLE) };
	private String[] tableToolTips;

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
	// Sort type
	private volatile SortType sortType = SortType.NAME;
	// Sort direction
	private volatile int sortDirection = 0;

	/*
	 * Job used to retrieve the list of file/directory in a folder
	 */
	private RetrieveFileListJob retrieveDirJob;

	private FileViewer viewer;

	private TableViewer tviewer;

	private String filter;
	
	private boolean useRegex = false;

	private boolean isSizeSIUnits;
	
	public FileTableExplorer(FileViewer viewer, Composite parent, int style) {
		this.viewer = viewer;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		tableContentsOfLabel = new Label(composite, SWT.BORDER);
		tableContentsOfLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite filter_composite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(3, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 2;
		filter_composite.setLayout(gridLayout);
		filter_composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filter_label = new Label(filter_composite, SWT.BORDER);
		filter_label.setText("File Filter");
		filter_label.setLayoutData(new GridData(GridData.BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		Text filter_text = new Text(filter_composite, SWT.SINGLE | SWT.BORDER);
		filter_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		filter_text.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				filter = filter_text.getText();
				//viewer.notifySelectedFiles(getSelectedFiles());
				viewer.doRefresh();
			}
		});
		
		Button filter_regex_button = new Button(filter_composite, SWT.CHECK | SWT.BORDER);
		filter_regex_button.setText("Regex?");
		filter_regex_button.setSelection(useRegex);
		filter_regex_button.setLayoutData(new GridData(GridData.END | GridData.VERTICAL_ALIGN_CENTER));
		filter_regex_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useRegex = filter_regex_button.getSelection();
				viewer.doRefresh();
			}
		});
		
		tviewer = new TableViewer(composite, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		tviewer.setContentProvider(new FileTableContentProvider(tviewer));
		tviewer.setUseHashlookup(true);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tviewer.getTable().setLayoutData(gridData);

		// set sizes
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean showSize = store.getBoolean(FileViewerConstants.SHOW_SIZE_COLUMN);
		boolean showType = store.getBoolean(FileViewerConstants.SHOW_TYPE_COLUMN);
		boolean showModified = store.getBoolean(FileViewerConstants.SHOW_MODIFIED_COLUMN);
		boolean showScanCmd = store.getBoolean(FileViewerConstants.SHOW_SCANCMD_COLUMN);
		isSizeSIUnits = store.getBoolean(FileViewerConstants.DISPLAY_WITH_SI_UNITS);
		// set tooltip accordingly
		tableToolTips = new String[] { Utils.getResourceString(FileViewerConstants.NAME_TIP),
				isSizeSIUnits ? Utils.getResourceString(FileViewerConstants.SIZE_SI_TIP)
						: Utils.getResourceString(FileViewerConstants.SIZE_BIN_TIP),
				Utils.getResourceString(FileViewerConstants.TYPE_TIP),
				Utils.getResourceString(FileViewerConstants.MODIFIED_TIP),
				Utils.getResourceString(FileViewerConstants.SCAN_TIP) };
		// we listen to the preference store property changes
		final int sizeWidth = showSize ? 75 : 0;
		final int typeWidth = showType ? 75 : 0;
		final int modifiedWidth = showModified ? 150 : 0;
		final int scanCmdWidth = showScanCmd ? 300 : 0;
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FileViewerConstants.SHOW_SIZE_COLUMN)) {
					setColumnVisible(1, 75, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(FileViewerConstants.SHOW_TYPE_COLUMN)) {
					setColumnVisible(2, 75, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(FileViewerConstants.SHOW_MODIFIED_COLUMN)) {
					setColumnVisible(3, 150, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(FileViewerConstants.SHOW_SCANCMD_COLUMN)) {
					setColumnVisible(4, 300, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(FileViewerConstants.DISPLAY_WITH_SI_UNITS)) {
					isSizeSIUnits = (Boolean) event.getNewValue();
					// change tooltip of size column
					Table table = tviewer.getTable();
					TableColumn sizeColumn = (table != null) && (!table.isDisposed()) ? table.getColumn(1) : null;
					if (sizeColumn != null) {
						sizeColumn.setToolTipText(Utils.getResourceString(
								isSizeSIUnits ? FileViewerConstants.SIZE_SI_TIP : FileViewerConstants.SIZE_BIN_TIP));
					}
					tviewer.refresh();
				}
			}
		});
		final int[] widths = { 150, sizeWidth, typeWidth, modifiedWidth, scanCmdWidth };
		createColumns(widths);

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
				viewer.doDefaultFileAction(getSelectedFiles());
			}

		});
		createTableDragSource(tviewer.getTable());
		createTableDropTarget(tviewer.getTable());
	}

	private void setColumnVisible(final int col, final int width, boolean isVis) {
		if (this.tviewer==null || this.tviewer.getControl().isDisposed()) return;
		tviewer.getTable().getColumn(col).setWidth(isVis?width:0);
		tviewer.getTable().getColumn(col).setResizable(isVis);
		tviewer.getTable().getColumn(col).setMoveable(isVis);
	}

	/**
	 * Returns the selected files in the table
	 * @return
	 */
	public FileTableContent[] getSelectedFiles() {
		StructuredSelection selection = (StructuredSelection) tviewer.getSelection();
		return Arrays.copyOf(selection.toArray(), selection.size(), FileTableContent[].class);
	}

	private void createColumns(int[] widths) {
		for (int i = 0; i < tableTitles.length; ++i) {
			TableViewerColumn column = new TableViewerColumn(tviewer, SWT.LEFT, i);
			column.getColumn().setText(tableTitles[i]);
			column.getColumn().setToolTipText(tableToolTips[i]);
			column.getColumn().setWidth(widths[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			column.setLabelProvider(new FileTableColumnLabelProvider(viewer, i));
			new FileTableViewerComparator(this, viewer, column, i);
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
					FileTableContent content = (FileTableContent) dndSelection[i].getData();
					sourceNames[i] = content.getFile().getAbsolutePath();
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
	public void workerUpdate(File dir, boolean force, SortType type, int direction) {
		if (dir == null)
			return;
		if ((!force) && (workerNextDir != null) && (workerNextDir.equals(dir)))
			return;

		synchronized (workerLock) {
			workerNextDir = dir;
			sortType = type;
			sortDirection = direction;
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
				workerExecute(sortType, sortDirection, filter, useRegex);
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
	private void workerExecute(SortType sortType, int direction, String filter, boolean useRegex) {
		// Clear existing information
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tableContentsOfLabel.setText(
						Utils.getResourceString("details.ContentsOf.text", new Object[] { workerStateDir.getPath() }));
				tviewer.getTable().removeAll();

				// Retrieve the new list of files
				if (retrieveDirJob != null && retrieveDirJob.getState() == Job.RUNNING) {
					retrieveDirJob.cancel();
				}
				retrieveDirJob = new RetrieveFileListJob(workerStateDir, sortType, direction, filter, useRegex, false);
				retrieveDirJob.setThread(workerThread);
				retrieveDirJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						FileTableContent[] dirList = retrieveDirJob.getDirList();
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (tviewer != null && dirList != null) {
									tviewer.setInput(dirList);
									tviewer.setItemCount(dirList.length);
								}
							}
						});
					}
				});
				retrieveDirJob.schedule();
			}
		});
	}

	public TableViewer getTableViewer() {
		return tviewer;
	}

	public Table getTable() {
		return tviewer.getTable();
	}

	public void setSortType(SortType type) {
		sortType = type;
	}

	public SortType getSortType() {
		return sortType;
	}

	public int getSortDirection() {
		return sortDirection;
	}

	public String getFilter() {
		return filter;
	}
	
	public boolean getUseRegex() {
		return useRegex;
	}

	public boolean isSizeSIUnit() {
		return isSizeSIUnits;
	}
}
