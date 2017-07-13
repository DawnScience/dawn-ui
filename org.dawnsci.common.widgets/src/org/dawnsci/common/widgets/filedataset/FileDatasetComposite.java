package org.dawnsci.common.widgets.filedataset;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringJoiner;

import org.dawnsci.common.widgets.statuscomposite.StatusComposite;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

/*
 * After http://blog.vogella.com/2009/06/23/eclipse-rcp-file-browser/
 */

public class FileDatasetComposite extends StatusComposite {

	private final TreeViewer treeViewer;
	private final TableViewer tableViewer;
	private final FileDatasetTreeContentProvider contentProvider = new FileDatasetTreeContentProvider();
	private volatile File currentSelectedFile = null;
	private volatile ILazyDataset currentSelectedDataset = null;
	
	
	public FileDatasetComposite(Composite parent, IFileDatasetFilter filter, int style) {
		this(parent, null, filter, style);
	}
	
	public FileDatasetComposite(Composite parent, File initialFile, IFileDatasetFilter filter, int style) {
		super(parent, style);
		
		if (initialFile == null)
			initialFile = new File(System.getProperty("user.home"));
		
		if (filter == null) {
			// default filter allows for everything to get through
			filter = FileDatasetFilterFactory.FILTER_TRUE;
		}
		
		this.setLayout(new GridLayout(1, true));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumWidth = 800;
		gridData.widthHint = 800;
		gridData.minimumHeight = 600;
		gridData.heightHint = 600;
		this.setLayoutData(gridData);
		
		// add a sash with a treeviewer and a tableviewer
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		treeViewer = new TreeViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new FileDatasetTreeLabelProvider());
		treeViewer.setInput(File.listRoots());
		
		tableViewer = new TableViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new FileDatasetTableContentProvider(filter));
		
		treeViewer.addSelectionChangedListener(event -> {
			File selectedFile = (File) treeViewer.getStructuredSelection().getFirstElement();
			if (selectedFile != null) {
				currentSelectedFile = selectedFile;
				tableViewer.setInput(selectedFile);
			}
			// changes in the treeViewer always set the status to false
			fireListeners(false);
		});
		// taken from http://stackoverflow.com/a/22453339
		treeViewer.addDoubleClickListener(event -> {
			final IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			if (selection == null || selection.isEmpty())
			    return;

			final Object sel = selection.getFirstElement();

			if (!contentProvider.hasChildren(sel))
			    return;

			if (treeViewer.getExpandedState(sel))
			    treeViewer.collapseToLevel(sel, AbstractTreeViewer.ALL_LEVELS);
			else
			    treeViewer.expandToLevel(sel, 1);
		});
		
		TableViewerColumn datasetNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		datasetNameColumn.getColumn().setText("Name");
		datasetNameColumn.getColumn().setWidth(200);
		datasetNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILazyDataset dataset = (ILazyDataset) element;
				return dataset.getName();
			}
		});
		TableViewerColumn datasetShapeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		datasetShapeColumn.getColumn().setText("Shape");
		datasetShapeColumn.getColumn().setWidth(200);
		datasetShapeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILazyDataset dataset = (ILazyDataset) element;
				int[] shape = dataset.getShape();
				StringJoiner joiner = new StringJoiner(", ", "[", "]");
				for (int s : shape)
					joiner.add(Integer.toString(s));
				return joiner.toString();
			}
		});
		
		tableViewer.addSelectionChangedListener(event -> {
			ILazyDataset selectedDataset = (ILazyDataset) tableViewer.getStructuredSelection().getFirstElement();
			if (selectedDataset != null) {
				currentSelectedDataset = selectedDataset;
				fireListeners(true);
			} else {
				fireListeners(false);
			}
		});
		
		sashForm.setWeights(new int[]{50, 50});

		// start at home directory
		setSelectedFile(initialFile);
	}
	
	public void setSelectedFile(File file) {
		// file could also be a directory...
		File origFile = file;
		Deque<File> path = new ArrayDeque<>();
		while (file != null) {
			path.add(file);
			file = file.getParentFile();
		}
	
		if (checkItems(treeViewer.getTree().getItems(), path, null)) {
			treeViewer.setSelection(new StructuredSelection(origFile), true);
		}
	}
	
	public File getSelectedFile() {
		return currentSelectedFile;
	}
	
	public ILazyDataset getSelectedDataset() {
		return currentSelectedDataset;
	}

	private boolean checkItems(TreeItem[] items, Deque<File> path, File lastFile) {
		if (items == null || path.isEmpty())
			return false;
		File last = path.removeLast();
		String fileName = last.getName();
		if (fileName.length() ==  0) {
			fileName = last.getAbsolutePath();
		}
		for (TreeItem item : items) {
			if (item.getText().equals(fileName)) {
				// we have a match
				// if the path is now empty, we should stop here
				if (path.isEmpty()) {
					if (last.isDirectory()) {
						treeViewer.expandToLevel(last, 1);
					} else {
						treeViewer.expandToLevel(lastFile, 0);
					}
					return true;
				} else {
					treeViewer.expandToLevel(last, 1);
					// recursive call...
					return checkItems(item.getItems(), path, last);
				} 
			}
		}
		return false;
	}

}
