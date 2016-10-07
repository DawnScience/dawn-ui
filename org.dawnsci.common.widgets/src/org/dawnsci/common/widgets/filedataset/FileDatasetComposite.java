package org.dawnsci.common.widgets.filedataset;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * After http://blog.vogella.com/2009/06/23/eclipse-rcp-file-browser/
 */

public class FileDatasetComposite extends Composite {
	private final TreeViewer treeViewer;
	private final TableViewer tableViewer;
	private final FileDatasetTreeContentProvider contentProvider = new FileDatasetTreeContentProvider();
	private final static Logger logger = LoggerFactory.getLogger(FileDatasetComposite.class);
	private volatile File currentSelection = null;
	
	public FileDatasetComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, true));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumWidth = 500;
		gridData.minimumHeight = 400;
		gridData.heightHint = 400;
		gridData.widthHint = 500;
		this.setLayoutData(gridData);
		// add a sash with a treeviewer and a tableviewer
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		treeViewer = new TreeViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new FileDatasetTreeLabelProvider());
		treeViewer.setInput(File.listRoots());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				File selectedFile = (File) treeViewer.getStructuredSelection().getFirstElement();
				if (selectedFile != null) {
					logger.debug("new selection: {}", selectedFile.toString());
					currentSelection = selectedFile;
					tableViewer.setInput(selectedFile);
				}
			}
		});
		// start at home directory
		setSelectedFile(new File(System.getProperty("user.home")));
		
		tableViewer = new TableViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new FileDatasetTableContentProvider());
		
		TableViewerColumn datasetNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		datasetNameColumn.getColumn().setText("Name");
		datasetNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILazyDataset dataset = (ILazyDataset) element;
				return dataset.getName();
			}
		});
		TableViewerColumn datasetShapeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		datasetShapeColumn.getColumn().setText("Shape");
		datasetShapeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILazyDataset dataset = (ILazyDataset) element;
				return dataset.toString();
			}
		});
		
		sashForm.setWeights(new int[]{50, 50});
	}
	
	public void setSelectedFile(File file) {
		logger.debug("selecting file: {}", file.getAbsolutePath());
		//treeViewer.expandToLevel(file, AbstractTreeViewer.ALL_LEVELS);
		// file could also be a directory...
		File origFile = file;
		Deque<File> path = new ArrayDeque<>();
		while (file != null) {
			logger.debug("builder: {}", file.getName());
			path.add(file);
			file = file.getParentFile();
		}
	
		if (checkItems(treeViewer.getTree().getItems(), path, null)) {
			treeViewer.setSelection(new StructuredSelection(origFile));
		}
		/*
		treeViewer.expandToLevel(elementOrTreePath, level);
		
		((ITreeContentProvider) treeViewer.getContentProvider()).getChildren(parentElement)
		
		TreeItem[] items = treeViewer.getTree().getItems();
		for (int i = path.size() - 1; i >= 0; --i) {
			final File pathElement = path.get(i);
		}*/
	}
	
	public File getSelectedFile() {
		return currentSelection;
	}

	private boolean checkItems(TreeItem[] items, Deque<File> path, File lastFile) {
		if (items == null || path.isEmpty())
			return false;
		File last = path.removeLast();
		String fileName = last.getName();
		if (fileName.length() ==  0) {
			fileName = last.getAbsolutePath();
		}
		logger.debug("filename {}", fileName);
		for (TreeItem item : items) {
			if (item.getText().equals(fileName)) {
				// we have a match
				// if the path is now empty, we should stop here
				logger.debug("match for {}", fileName);
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
		logger.error("checkItems: no match found! {}", fileName);
		return false;
	}
}
