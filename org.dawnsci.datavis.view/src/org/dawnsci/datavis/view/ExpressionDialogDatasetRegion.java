/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A class to break out the creation of the left hand panel of the {@link ExpressionDialog} UI.
 * 
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class ExpressionDialogDatasetRegion {

	private Map<Did, String> varFromDataID;
	private Map<String, DataOptions> dataFromVar;

	private LoadedFile principalFile;
	private LoadedFile currentFile;
	private IFileController controller;
	
	private Map<String, LoadedFile> fileNamespaceMap;

	private TableViewer datasetVariableViewer;
	
	private int datasetWidth;
	private int varWidth;
	private int tableHeight;
	
	/**
	 * Creates the new part, and performs the initial set-up
	 * @param controller
	 * 					Interface to the available files
	 * @param selected
	 * 				Interface to the initially selected data
	 */
	public ExpressionDialogDatasetRegion(IFileController controller, DataOptions selected) {
		varFromDataID = new HashMap<>();
		dataFromVar = new HashMap<>();
		
		fileNamespaceMap = new HashMap<>();

		// this file is the one that doesn't get a prefix
		principalFile = selected.getParent();
		// File for which the datasets are displayed
		currentFile = principalFile;
		
		this.controller = controller;

		mapLoadedFiles(this.controller.getLoadedFiles());
	}
	
	/**
	 * Validates whether the requested String references a loaded variable
	 * @param datasetName
	 * 					Name to be queried.
	 * @return
	 * 		True if a dataset of that name exists.
	 */
	public boolean isValidVariable(String datasetName) {
		return dataFromVar.containsKey(datasetName);
	}
	
	/**
	 * Retrieves the data corresponding to the requested variable name.
	 * @param name
	 * 			Name of the data to be retrieved.
	 * @return
	 * 		Data corresponding to that name.
	 */
	public DataOptions getDatasetForVariable(String name) {
		return dataFromVar.get(name);
	}
	
	/**
	 * Lists all the loaded variable names.
	 * @return
	 * 		The names of variables that have had data loaded.
	 */
	public Collection<String> getVariables() {
		return dataFromVar.keySet();
	}
	
	/**
	 * Sets the width in pixels of the 'Dataset' column in the table.
	 * @param dWidth
	 * 				Width desired.
	 */
	public void setDatasetWidth(int dWidth) {
		datasetWidth = dWidth;
	}
	
	/**
	 * Sets the width in pixels of the 'var' column in the table.
	 * @param vWidth
	 * 				Width desired.
	 */
	public void setVarWidth(int vWidth) {
		varWidth = vWidth;
	}

	/**
	 * Sets the height in pixels of the table.
	 * @param tHeight
	 * 				Height desired.
	 */
	public void setTableHeight(int tHeight) {
		tableHeight = tHeight;
	}
	
	/**
	 * Performs the action of generating the UI elements.
	 * @param parent
	 * 				parent Composite to use.
	 * @return
	 * 		the new Composite.
	 */
	public Composite createComposite(Composite parent) {
		Composite datasetsCompo = new Composite(parent, SWT.NONE);
		datasetsCompo.setLayout(new RowLayout(SWT.VERTICAL));

		createFileDataCompo(datasetsCompo, principalFile);
		
		return datasetsCompo;
	}
	
	private class Did {
		String f;
		String d;
		public Did(String f, String d) {
			this.f = f;
			this.d = d;
		}
		@Override
		public boolean equals(Object b) {
			if (this == b)
				return true;
			if (b instanceof Did) {
				Did e = (Did) b;
				return (f.equals(e.f) && d.equals(e.d));
			} else {
				return false;
			}
		}
		@Override
		public int hashCode() {
			return f.hashCode() ^ d.hashCode();
		}
		public String toString() {
			return f + ":" + d;
		}
	}

	private Composite createFileDataCompo(Composite parent, LoadedFile fileLoaded) {
		Composite fileDataCompo = new Composite(parent, SWT.NONE);
		fileDataCompo.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		final ComboViewer availableFilesViewer = new ComboViewer(fileDataCompo, SWT.READ_ONLY);
		availableFilesViewer.getCombo().setLayoutData(new RowData(varWidth + datasetWidth, tableHeight));
		
		availableFilesViewer.setContentProvider(ArrayContentProvider.getInstance());
		availableFilesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				} else if (element instanceof LoadedFile){
					return ((LoadedFile) element).getName();
				} else {
					return "FILE_NOT_FOUND";
				}
			}
		});
		availableFilesViewer.setInput(controller.getLoadedFiles());
		
		availableFilesViewer.addSelectionChangedListener(new FileSelectionListener());

		availableFilesViewer.setSelection(new StructuredSelection(fileLoaded));
		
		
		fileDataCompo.setLayout(new RowLayout(SWT.VERTICAL));

		// Table of variable names and Datasets
		datasetVariableViewer = new TableViewer(fileDataCompo, SWT.FULL_SELECTION | SWT.BORDER);
		datasetVariableViewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(datasetVariableViewer);

		// short variable name column
		TableViewerColumn variableCol = new TableViewerColumn(datasetVariableViewer, SWT.NONE);
		variableCol.getColumn().setWidth(varWidth);
		variableCol.getColumn().setText("var");
		variableCol.setLabelProvider(new VarLabelProvider());
		
		// Dataset name column
		TableViewerColumn datasetCol = new TableViewerColumn(datasetVariableViewer, SWT.NONE);
		datasetCol.getColumn().setWidth(datasetWidth);
		datasetCol.getColumn().setText("Dataset");
		datasetCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DataOptions) element).getName();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((DataOptions) element).getName();
			}
		});
		
		datasetVariableViewer.getTable().setHeaderVisible(true);
		datasetVariableViewer.getTable().setLinesVisible(true);
		datasetVariableViewer.setInput(fileLoaded.getDataOptions());
		
		return fileDataCompo;
	}
	
	private class FileSelectionListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// TODO Do something when there is more than one file to select
			
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			// DEVONLY: log to the console
			System.out.println(((LoadedFile) selection.getFirstElement()).getName() + " selected");
		
			ExpressionDialogDatasetRegion.this.setCurrentFile((LoadedFile) selection.getFirstElement());
		}

	}

	private void mapLoadedFiles(Collection<LoadedFile> loadedFiles) {
		// generate the namespaces of the files
		for (LoadedFile loadedFile : loadedFiles) {
			String nameSpace = "";
			if (!loadedFile.equals(principalFile)) {
				// Split the file into dot-separated elements
				String[] components = loadedFile.getName().split("\\.");
				// Join all but the file extension without dots
				nameSpace = String.join("", components);
			}
			fileNamespaceMap.put(nameSpace, loadedFile);
		}
	}
	
	// Change the currently displayed file
	private void setCurrentFile(LoadedFile loadMe) {
		currentFile = loadMe;

		// Load the new file's Datasets to the dataset table, if the table has been constructed.
		if (datasetVariableViewer != null)
			datasetVariableViewer.setInput(currentFile.getDataOptions());
	}

	private Did generateUniqueID(DataOptions dataOp) {
		String filename = dataOp.getParent().getFilePath();
		String dataname = dataOp.getName();
		
		return new Did(filename,  dataname);
	}
	
	private String generateVariableName(String dataname) {
		final int varLen = 4; // Length of the abbreviated names
		
		// Split path into path elements, and take the last
		String[] pathElements = dataname.split(Node.SEPARATOR);
		String nodeName = pathElements[pathElements.length-1];
		String[] nameElements = nodeName.split("\\.");
		String fullName = nameElements[0];
		
		// Try the truncated name
		String shortName = fullName.substring(0, Math.min(fullName.length(), varLen));
		if (dataFromVar.containsKey(shortName)) {
			for (int i = 1; i < 1000; i++) {
				String s = Integer.toString(i);
				int len = s.length();
				
				int rightmost = fullName.length() + len;
				rightmost = Math.min(rightmost, varLen);
				int numberStart = rightmost - len;
				
				shortName = shortName.substring(0, numberStart) + s;
				if (!dataFromVar.containsKey(shortName)) break;
			}
			// take this branch if we reached i = 999 without success
			if (dataFromVar.containsKey(shortName)) {
				shortName = findUnrelatedLabel(dataFromVar.keySet());
			}
		}
		return shortName;
	}
	
	// find a valid variable label, unrelated to the original
	private String findUnrelatedLabel(Set<String> currentNames) {
		
		// 'datan', where n is an arbitrary number
		int i = 0;
		String unrelatedName;
		
		do {
			i++;
			unrelatedName = "data" + i;
		} while (currentNames.contains(unrelatedName));
	
		return unrelatedName;
	}
		
	private void addToMaps(String var, DataOptions data) {
		Did dataID = generateUniqueID(data);
		this.varFromDataID.put(dataID, var);
		this.dataFromVar.put(var, data);
	}

	// Add the unique ID and variable name of a dataset to the variable to dataset maps
	private void generateAndAddToMaps(DataOptions dataOp) {
		String var = generateVariableName(dataOp.getName());
		
		addToMaps(var, dataOp);
	}

	private class VarLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			Did dataID = generateUniqueID((DataOptions) element);
			
			if (!varFromDataID.containsKey(dataID)) {
				generateAndAddToMaps((DataOptions) element);
			}
			
			String var = varFromDataID.get(dataID);
			
			return (var != null) ? var : "datx";
		}
	}


}
