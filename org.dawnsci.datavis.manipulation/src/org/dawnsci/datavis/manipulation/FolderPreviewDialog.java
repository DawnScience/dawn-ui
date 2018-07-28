/*
 * Copyright (c) 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog that shows a table of the files in a folder.
 * <p>
 * Label column in table shows a single value entry from a hdf5 file.
 * Table can be filtered against filename and label
 *
 */
public class FolderPreviewDialog extends Dialog {

	private static final int UPDATE_NUMBER = 50;

	private static final Logger logger = LoggerFactory.getLogger(FolderPreviewDialog.class);

	private File[] filenames;
	private String[] labelOptions;
	private ContentProposalAdapter adapter;
	private String currentDatasetName = "/entry1/scan_command";
	private TableViewer viewer;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private ExecutorService executorLabel = Executors.newSingleThreadExecutor();
	private ProgressBar progress;

	private AtomicBoolean cancelMonitor = new AtomicBoolean(false);

	private String[] selectedFileNames;

	private ILoaderService lService;

	protected FolderPreviewDialog(Shell parentShell, String folderPath, File[] filenames) {
		super(parentShell);
		this.filenames = filenames;

		BundleContext bundleContext =
				FrameworkUtil.
				getBundle(this.getClass()).
				getBundleContext();

		lService = bundleContext.getService(bundleContext.getServiceReference(ILoaderService.class));
	}

	@Override
	public Control createDialogArea(Composite parent)  {
		Composite container = (Composite) super.createDialogArea(parent);

		container.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).create());

		FileCachedValuesWithLabel[] input = new FileCachedValuesWithLabel[filenames.length];
		for (int i = 0; i < filenames.length; i++) {
			input[i] = new FileCachedValuesWithLabel(filenames[i]);
		}

		Label fileLabel = new Label(container, SWT.NONE);
		fileLabel.setText("File Label:");
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().create());

		final Text labelText = new Text(container, SWT.BORDER | SWT.SEARCH);
		labelText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		labelText.setText(currentDatasetName);
		labelText.addTraverseListener(e -> {
			if( SWT.TRAVERSE_RETURN == e.detail ) {
				//dont let enter trigger the ok button 
				e.doit = false;
			}
		});
		
		labelText.setToolTipText("Choose the dataset to use for the label column."
				+ " Autocomplete options come from the file selected in the table");

		final Button btn = new Button(container, SWT.PUSH);
		btn.setText("Update Label");
		btn.setLayoutData(GridDataFactory.fillDefaults().create());
		btn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLableName(input, labelText);
			}
		});

		adapter = new ContentProposalAdapter(
				labelText, new TextContentAdapter(), null,
				null, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		Label searchLabel = new Label(container, SWT.NONE);
		searchLabel.setText("Search:");
		searchLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		searchText.setToolTipText("Filter file table by matching text in filename or label. Case insensitive.");

		FileCachedValuesWithLabelComparator comparator = new FileCachedValuesWithLabelComparator();
		FileCachedValuesWithLabelFilter filter = new FileCachedValuesWithLabelFilter();

		viewer = new TableViewer(container, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		viewer.setUseHashlookup(true);
		viewer.getTable().setItemCount(filenames.length);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(3, 1).create());
		viewer.setComparator(comparator);
		viewer.setFilters(new ViewerFilter[] {filter});

		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				filter.setSearchText(searchText.getText());
				viewer.refresh();
			}

		});

		progress = new ProgressBar(container, SWT.None);
		progress.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(3, 1).create());

		TableViewerColumn fileNames   = new TableViewerColumn(viewer, SWT.LEFT, 0);

		fileNames.getColumn().setText("Filename");
		fileNames.getColumn().setWidth(300);
		fileNames.getColumn().setAlignment(SWT.LEFT);

		fileNames.getColumn().addSelectionListener(getSelectionAdapter(fileNames.getColumn(), 0, comparator, viewer));

		fileNames.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileCachedValuesWithLabel f = (FileCachedValuesWithLabel)element;
				return f.getName();
			}
		});

		TableViewerColumn labels   = new TableViewerColumn(viewer, SWT.LEFT, 1);

		labels.getColumn().setText("Label");
		labels.getColumn().setWidth(300);
		labels.getColumn().setAlignment(SWT.LEFT);


		labels.getColumn().addSelectionListener(getSelectionAdapter(labels.getColumn(), 1, comparator, viewer));

		labels.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileCachedValuesWithLabel f = (FileCachedValuesWithLabel)element;
				return f.getLabel();
			}
		});

		TableViewerColumn size   = new TableViewerColumn(viewer, SWT.RIGHT, 2);

		size.getColumn().setText("Size");
		size.getColumn().setWidth(100);
		size.getColumn().setAlignment(SWT.RIGHT);

		size.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileCachedValuesWithLabel f = (FileCachedValuesWithLabel)element;
				return f.getSize();
			}
		});

		size.getColumn().addSelectionListener(getSelectionAdapter(size.getColumn(), 2, comparator, viewer));

		TableViewerColumn date   = new TableViewerColumn(viewer, SWT.CENTER, 3);

		date.getColumn().setText("Date Modified");
		date.getColumn().setWidth(100);

		date.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileCachedValuesWithLabel f = (FileCachedValuesWithLabel)element;
				return f.getDate();
			}
		});

		date.getColumn().addSelectionListener(getSelectionAdapter(date.getColumn(), 3, comparator, viewer));

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(input);
		viewer.getTable().setSelection(0);
		updateContext();

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateContext();
				updateSelectedFiles();
			}
		});

		populateFileSize(input);
		populateFileLabel(input,currentDatasetName,cancelMonitor);

		return container;
	}

	private void populateFileSize(FileCachedValuesWithLabel[] input) {
		executorLabel.execute(() ->  
		{

			for (FileCachedValuesWithLabel f : input) {
				f.populate();

			}
			Display.getDefault().asyncExec( () -> viewer.refresh());
		});
	}

	@Override
	public boolean close() {
		cancelMonitor.set(true);
		return super.close();
	}

	private void populateFileLabel(FileCachedValuesWithLabel[] input, String labelPath, AtomicBoolean toCancel) {
		executorLabel.execute(() ->  
		{

			for (FileCachedValuesWithLabel f : input) {
				f.clearLabel();
			}

			Display.getDefault().asyncExec( () -> {
				viewer.refresh();
				progress.setSelection(0);
				progress.setMinimum(0);
				progress.setMaximum(input.length);
			});

			int counter = 0;
			long time = System.currentTimeMillis();
			for (FileCachedValuesWithLabel f : input) {
				f.populateLabel(labelPath);
				if (toCancel.get()) {
					logger.info("Cancelled!");
					break;
				}
				if ((counter++ % UPDATE_NUMBER) == 0) {
					final int localCounter = counter;
					Display.getDefault().asyncExec( () -> {
						viewer.refresh();
						progress.setSelection(localCounter);
					});
				}
			}
			logger.info("Time to parse {}: {}s ",labelPath ,Long.toString((System.currentTimeMillis()-time)/1000));
			Display.getDefault().asyncExec( () -> {
				viewer.refresh();
				progress.setSelection(0);
			});
		});
	}

	private void updateLableName(FileCachedValuesWithLabel[] input, Text labelText) {
		String text = labelText.getText();

		Optional<String> first = Arrays.stream(labelOptions).filter(s -> text.equals(s)).findFirst();

		if (!first.isPresent()) {
			labelText.setText(currentDatasetName);
		}

		currentDatasetName = labelText.getText();
		cancelMonitor.set(true);
		cancelMonitor = new AtomicBoolean(false);
		populateFileLabel(input,currentDatasetName, cancelMonitor);
	}

	public String[] getSelectedFileNames() {
		return selectedFileNames;
	}

	private void updateSelectedFiles() {

		ISelection selection = viewer.getSelection();

		if (selection instanceof StructuredSelection) {
			List<String> names = new ArrayList<>();
			@SuppressWarnings("rawtypes")
			Iterator iterator = ((StructuredSelection) selection).iterator();

			while (iterator.hasNext()) {
				Object next = iterator.next();
				if (next instanceof FileCachedValuesWithLabel) {
					names.add(((FileCachedValuesWithLabel) next).getPath());
				}
			}

			selectedFileNames = names.toArray(new String[names.size()]);
		}

	}

	private void updateContext() {
		adapter.setContentProposalProvider(null);

		ISelection selection = viewer.getSelection();

		if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).getFirstElement() instanceof FileCachedValuesWithLabel) {
			FileCachedValuesWithLabel f = (FileCachedValuesWithLabel)((IStructuredSelection)selection).getFirstElement();
			String path = f.getPath();

			Runnable r = () -> {

				IDataHolder data;
				try {
					data = lService.getData(path, null);
				} catch (Exception e) {
					return;
				}

				String[] dataNames = data.getNames();

				List<String> labels = new ArrayList<String>();

				for (String n: dataNames) {
					if (data.getLazyDataset(n).getSize() == 1) {
						labels.add(n);
					}
				}

				String[] names = labels.toArray(new String[labels.size()]);

				Arrays.sort(names);

				labelOptions = names;

				SimpleContentProposalProvider p = new SimpleContentProposalProvider(names) {

					@Override
					public IContentProposal[] getProposals(String contents, int position) {

						String lower = contents.toLowerCase();
						ArrayList<ContentProposal> list = new ArrayList<ContentProposal>();
						for (int i = 0; i < names.length; i++) {
							if (names[i].toLowerCase().contains(lower)) {
								list.add(new ContentProposal(names[i]));
							}
						}

						return list.toArray(new IContentProposal[list
						                                         .size()]);
					}

				};

				adapter.setContentProposalProvider(p);

			};

			executor.submit(r);
		}



	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index, FileCachedValuesWithLabelComparator comparator, TableViewer viewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 800);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private class FileCachedValuesWithLabel {

		private File file;
		private AtomicReference<String> label = new AtomicReference<String>("");
		private AtomicReference<String> size = new AtomicReference<String>("");
		private AtomicLong lsize = new AtomicLong(0);
		private AtomicReference<String> date = new AtomicReference<String>("");
		private AtomicLong ldate = new AtomicLong(0);

		public FileCachedValuesWithLabel(File f) {
			this.file = f;
		}

		public String getPath() {
			return this.file.getAbsolutePath();
		}

		public String getLabel() {
			return label.get();
		}

		public void clearLabel() {
			label.set("");
		}

		public String getName() {
			return file.getName();
		}

		public long getLongSize() {
			return lsize.get();
		}

		public String getSize() {
			return size.get();
		}

		public String getDate() {
			return date.get();
		}

		public long getlongDate() {
			return ldate.get();
		}

		public void populate() {
			long bytes = file.length();
			lsize.set(bytes);
			int unit = 1024;
			if (bytes < unit) {
				size.set(bytes + " B");
			} else {
				int exp = (int) (Math.log(bytes) / Math.log(unit));
				String pre = Character.toString(("KMGTPE").charAt(exp - 1));
				size.set(String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre));

			}

			long lm = file.lastModified();

			ldate.set(lm);
			date.set(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(lm)));
		}

		public void populateLabel(String labelPath) {

			String full = file.getAbsolutePath();
			if (HDF5Utils.isHDF5(full)) {
				try {
					this.label.set("");
					if (HDF5Utils.hasDataset(full, labelPath) && isSizeOne(HDF5Utils.getDatasetShape(full, labelPath))) {
						Dataset d = HDF5Utils.loadDataset(full, labelPath);
						String s = d.getString();
						this.label.set(s);
					} 

				} catch (ScanFileHolderException e) {
					// dont always expect the label to be there
				}
			}
		}
	}

	private boolean isSizeOne(int[][] shape) {
		if (shape == null) return false;

		if (shape[0].length == 0) return true;

		for (int i : shape[0]) {
			if (i != 1) {
				return false;
			}
		}
		return true;
	}


	public class FileCachedValuesWithLabelComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public FileCachedValuesWithLabelComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			FileCachedValuesWithLabel p1 = (FileCachedValuesWithLabel) e1;
			FileCachedValuesWithLabel p2 = (FileCachedValuesWithLabel) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = p1.getName().compareTo(p2.getName());
				break;
			case 1:
				rc = p1.getLabel().compareTo(p2.getLabel());
				break;
			case 2:
				rc = new Long(p1.getLongSize()).compareTo(p2.getLongSize());
				break;
			case 3:
				rc = new Long(p1.getlongDate()).compareTo(p2.getlongDate());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}

	public class FileCachedValuesWithLabelFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			// ensure that the value can be used for matching
			this.searchString = ".*" + s.toLowerCase() + ".*";
		}

		@Override
		public boolean select(Viewer viewer,
				Object parentElement,
				Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			FileCachedValuesWithLabel p = (FileCachedValuesWithLabel) element;
			if (p.getName().toLowerCase().matches(searchString)) {
				return true;
			}
			if (p.getLabel().toLowerCase().matches(searchString)) {
				return true;
			}

			return false;
		}
	}

}
