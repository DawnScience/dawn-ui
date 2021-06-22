/*-
 * Copyright (c) 2021 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.view.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IDatasetStateChanger;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IFileStateValidator;
import org.dawnsci.datavis.model.ILoadedFileInitialiser;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlotModeXY;
import org.dawnsci.datavis.model.PlottableObject;
import org.dawnsci.datavis.view.table.DataOptionTableViewer;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessResultsUI extends Composite implements IDatasetStateChanger {
	protected static final Logger logger = LoggerFactory.getLogger(ProcessResultsUI.class);

	private List<String> allProcesses = new ArrayList<>();

	private ComboViewer processCombo;
	private DataOptionTableViewer processViewer;

	private List<DataOptions> currentSelection = new ArrayList<>();
	private String currentProcess;
	private String currentFile;

	private AlignDialog dialog;
	private IFileController fileController;
	private IPlotController plotController;

	private boolean plotUpdated = false;

	public ProcessResultsUI(Composite parent, IFileController fc, IPlotController pc) {
		super(parent, SWT.None);
		this.fileController = fc;
		this.plotController = pc;
		createPage();
	}

	private Composite createPage() {
		this.setLayout(new FillLayout());

		Composite plotComp = new Composite(this, SWT.NONE);
		plotComp.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite comboComp = new Composite(plotComp, SWT.NONE);
		comboComp.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create());
		comboComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		Label label = new Label(comboComp, SWT.NONE);
		label.setText("Process:");
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).create());
		processCombo = new ComboViewer(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		processCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection s = event.getSelection();
				String p = SelectionUtils.getFirstFromSelection(s, String.class);
				if (p != null && !p.equals(currentProcess)) {
					currentProcess = p;
					updateTable(FileControllerUtils.getLoadedFile(fileController, currentFile));
				}
			}
		});
		processCombo.setContentProvider(ArrayContentProvider.getInstance());
		final Combo combo = processCombo.getCombo();
		combo.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
				.hint(SWT.DEFAULT, 50).create());

		if (plotController instanceof ILoadedFileInitialiser) {
			processViewer = new DataOptionTableViewer(fileController, (ILoadedFileInitialiser) plotController);
		} else {
			processViewer = new DataOptionTableViewer(fileController, null);
		}
		processViewer.setUseShortName(true);
		processViewer.createControl(plotComp);
		processViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		Composite alignComp = new Composite(plotComp, SWT.NONE);
		alignComp.setLayout(new RowLayout());

		Button b = new Button(alignComp, SWT.PUSH);
		b.setText("Align...");
		b.setToolTipText("Open align dialog");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dialog == null) {
					dialog = new AlignDialog(ProcessResultsUI.this.getShell(), fileController, plotController);
				} else if (plotUpdated) {
					dialog.reset();
					plotUpdated = false;
				}
				dialog.open();
			}
		});

		return this;
	}

	private boolean isInvalid() {
		IFileStateValidator v = (LoadedFile f) -> {
			for (DataOptions op : f.getDataOptions()) {
				if (op.isSelected()) {
					if (!op.getPlottableObject().getPlotMode().getClass().equals(PlotModeXY.class)) {
						return false;
					}
				}
			}
			return true;
		};

		for (LoadedFile f : fileController.getLoadedFiles()) {
			if (f.isSelected()) {
				if (!v.validate(f)) {
					return true;
				}
			} else { // deselect all options in unselected files
				for (DataOptions d : f.getSelectedDataOptions()) {
					d.setSelected(false);
				}
			}
		}

		return false;
	}

	@Override
	public void initialize(List<LoadedFile> files) {
		if (isInvalid()) {
			boolean ok = MessageDialog.openQuestion(getShell(), "Deselect all", "Only datasets plotted as points or lines allowed. Do you wish to deselect chosen datasets?");
			if (ok) {
				deselectAll();
			}
		}

		internalInitialize(files);
		if (!files.isEmpty()) {
			LoadedFile file = files.get(0);
			currentFile = file.getFilePath();
			updateCombo(file);
			updateTable(file);
		}
	}

	private void deselectAll() {
		for (LoadedFile f : fileController.getLoadedFiles()) {
			for (DataOptions d : f.getSelectedDataOptions()) {
				d.setSelected(false);
			}
		}
	}

	private void internalInitialize(List<LoadedFile> files) {
		plotUpdated = true;
		allProcesses.clear();
		allProcesses.add(LoadedFile.RESULT);
		int n = files.size();
		if (n > 0) {
			for (LoadedFile f : files) {
				for (String p : f.getProcesses()) {
					if (!allProcesses.contains(p)) {
						allProcesses.add(p);
					}
				}
			}
		} else {
			updateCombo(null);
			updateTable(null);
		}
	}

	private void updateCombo(LoadedFile file) {
		List<String> ps;
		int previous = -1;
		
		if (file == null) {
			ps = Collections.emptyList();
		} else {
			ps = new ArrayList<>(file.getProcesses());
			previous = ps.indexOf(currentProcess);
			if (previous < 0 && ps.size() > 0) {
				currentProcess = ps.get(ps.size() - 1);
				previous = allProcesses.indexOf(currentProcess);
			}
		}

		final int choice = previous;
		Display.getDefault().asyncExec(() -> {
			processCombo.setInput(allProcesses);
			if (choice >= 0) {
				processCombo.getCombo().select(choice);
			}
			processCombo.getControl().getParent().layout(true, true);
		});
	}

	private void updateTable(LoadedFile file) {
		if (file == null) {
			currentSelection.clear();
			currentProcess = null;
			Display.getDefault().asyncExec(() -> {
				processViewer.setInput(currentSelection);
				processViewer.refresh();
			});
			return;
		}

		if (currentProcess == null) {
			return;
		}

		currentSelection.clear();
		DataOptions opt = null;
		for (DataOptions o: getProcessData(file, currentProcess)) {
			currentSelection.add(o);
			if (opt == null) {
				opt = o;
			}
		}

		final DataOptions foundOpt = opt;
		Display.getDefault().asyncExec(() -> {
			processViewer.setInput(currentSelection);
			processViewer.refresh();
			if (foundOpt != null) {
				switchToPlotXY(foundOpt);
			}
		});
	}

	private void setSlicingFull(NDimensions nd) {
		nd.setSliceFullRange(true);
		int r = nd.getRank();
		for (int i = 0; i < r; i++) {
			nd.setSlice(i, new Slice());
		}
	}

	private void updateProcessDataChoice(DataOptions opt) { // change for all files
		boolean isSelected = opt.isSelected();
		String name = opt.getName();
		for (LoadedFile f : fileController.getLoadedFiles()) {
			DataOptions d = f.getDataOption(name);
			if (d != null) {
				if (isSelected != d.isSelected()) {
					d.setSelected(isSelected);
				}
				if (isSelected) {
					PlottableObject po = d.getPlottableObject();
					NDimensions nd = po.getNDimensions();
					if (!po.getPlotMode().getClass().equals(PlotModeXY.class)) {
						nd = new NDimensions(nd);
						po = new PlottableObject(new PlotModeXY(), nd);
						nd.setOptions(po.getPlotMode().getOptions());
						d.setPlottableObject(po);
					}
					setSlicingFull(nd);
				}
			}
		}

		processViewer.refresh();
	}

	private void updateSelected(DataOptions op) {
		plotUpdated = true;
		updateProcessDataChoice(op);
		switchToPlotXY(op);
	}

	private void switchToPlotXY(DataOptions op) {
		IPlotMode[] modes = plotController.getPlotModes(1);
		for (IPlotMode m : modes) {
			if (m.getClass().equals(PlotModeXY.class)) {
				plotController.switchPlotMode(m, op);
				break;
			}
		}

		fileController.applyToAll(op.getParent());
	}
	
	public void dispose() {
		processViewer.dispose();
	}

	List<DataOptions> getProcessData(LoadedFile file, String process) {
		List<DataOptions> opts = new ArrayList<>();
		for (DataOptions o : file.getDataOptions(true)) {
			if (process.equals(o.getProcess())) {
				opts.add(o);
			}
		}
		return opts;
	}

	@Override
	public void updateOnSelectionChange(LoadedFile file) {
		if (file == null) {
			currentFile = null;
			return;
		}
		String newFile = file.getFilePath();
		if (allProcesses.isEmpty() && !newFile.equals(currentFile)) { // ignore when un-initialized
			currentFile = newFile;
			updateCombo(file);
			updateTable(file);
		}
	}

	@Override
	public void stateChanged(FileControllerStateEvent event) {
		if (event.isSelectedFileChanged()) {
			if (event.isSelectedDataChanged()) {
				// moveBefore; selectFiles (pass 1st file); setFileSelected; unloadFiles; setComparator;
				// LFL.refreshRequest; LFL.localReload;
				// replot
				// also need to reconcile file in FC with state
				List<LoadedFile> files = fileController.getLoadedFiles();
				if (files.isEmpty()) {
					internalInitialize(files);
				} else {
					LoadedFile f = event.getLoadedFile();
					if (f != null) {
						String path = f.getFilePath();
						if (!path.equals(currentFile)) {
							currentFile = path;
						}
					}
				}
			}
		} else {
			if (event.isSelectedDataChanged()) {
				// setDataSelected; setLabelName; applyToAll
				// replot only if dataOpt then update selected data (across all file states)
				DataOptions opt = event.getOption();
				if (opt != null) {
					updateSelected(opt);
				}
			} else {
				// deselect; loadFiles; setOnlySignals; validateState; LFL.fileLoaded
				// reset process state with all files
				internalInitialize(fileController.getLoadedFiles());
			}
		}
	}

	@Override
	public void refreshRequest() {
		//Do nothing - this is for SWMR updates
	}

	@Override
	public String getChangerName() {
		return "Processed results";
	}
}
