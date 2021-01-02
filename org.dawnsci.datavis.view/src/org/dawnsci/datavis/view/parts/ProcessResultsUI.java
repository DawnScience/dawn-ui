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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.ILoadedFileInitialiser;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlotModeXY;
import org.dawnsci.datavis.view.table.DataOptionTableViewer;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.january.dataset.Slice;
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

public class ProcessResultsUI {
	protected static final Logger logger = LoggerFactory.getLogger(ProcessResultsUI.class);

	// all data options short name per process
	private Map<String, Set<String>> processDataOptions = new LinkedHashMap<>();
	// only chosen data options short name per process
	private Map<String, Set<String>> processDataChosen = new LinkedHashMap<>();

	private ComboViewer processCombo;
	private DataOptionTableViewer processViewer;

	private List<DataOptions> currentSelection = new ArrayList<>();
	private String currentProcess;

	private AlignDialog dialog;
	private IFileController fileController;
	private IPlotController plotController;

	public ProcessResultsUI(IFileController fc, IPlotController pc) {
		this.fileController = fc;
		this.plotController = pc;
	}

	public Composite createPage(Composite parent) {
		Composite stack = new Composite(parent, SWT.NONE);
		stack.setLayout(new FillLayout());

		Composite plotComp = new Composite(stack, SWT.NONE);
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
					updateProcessedDataTable(p, FileControllerUtils.getSelectedFiles(fileController));

					processCombo.getControl().getParent().layout(true, true);
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
					dialog = new AlignDialog(stack.getShell(), fileController, plotController);
				}
				dialog.open();
			}
		});

		return stack;
	}

	public void initialize(List<LoadedFile> files) {
		// populate data option short names per process
		processDataOptions.clear();
		for (LoadedFile f : files) {
			logger.debug("Update P GUI: {}", f.getFilePath());
			List<DataOptions> opts = f.getDataOptions(true);
			for (DataOptions o : opts) {
				if (!updateProcessedData(o.getName()) && o.isSelected()) {
					o.setSelected(false);
				}
			}
			for (String n : f.getLabelOptions()) {
				if (f.isSignal(n)) {
					updateProcessedData(n);
				}
			}
		}

		// parse for processes
		List<String> ps = new ArrayList<>(processDataOptions.keySet());
		int previous = ps.indexOf(currentProcess);
		if (previous < 0 && !ps.isEmpty()) {
			previous = ps.size() - 1;
			if (RESULT.equals(ps.get(previous)) && previous > 0) { // do not choose RESULT
				previous--;
			}
		}

		final int choice = previous;
		Display.getDefault().asyncExec(() -> {
			processCombo.setInput(ps);
			processCombo.getCombo().select(choice);
			processCombo.getControl().getParent().layout(true, true);
		});

		updateProcessedDataTable(choice >= 0 ? ps.get(choice) : null, files);
	}

	// capture /processed/(auxiliary|summary)/%d-PROCESS_NAME/DATA_NAME
	static final Pattern PROCESS_REGEX = Pattern.compile("/[^/]+/[^/]+/\\d+-([^/]+)/(.+)");
	static final String DATA = "/data";
	static final String RESULT = "result";
	static final String RESULT_SUFFIX = RESULT + DATA;

	/**
	 * @param n name to add
	 * @return true if process is current
	 */
	private boolean updateProcessedData(String n) {
		Matcher m = PROCESS_REGEX.matcher(n);
		boolean isCurrent = false;
		if (m.matches()) {
			String p = m.group(1);
			String d = m.group(2);
			if (d.endsWith(DATA)) {
				d = d.substring(0, d.length() - DATA.length());
			}
//			logger.debug("\t{} : {}", p, d);
			Set<String> s = processDataOptions.get(p);
			if (s == null) {
				s = new LinkedHashSet<>();
				processDataOptions.put(p, s);
			}
			s.add(d);
			isCurrent = p.equals(currentProcess);
		} else if (n.endsWith(RESULT_SUFFIX)) {
//			logger.debug("\t{}", n);
			processDataOptions.put(RESULT, null);
			isCurrent = RESULT.equals(currentProcess);
		} else {
//			logger.debug("\tIgnoring {}", n);
		}
		return isCurrent;
	}

	/*
	 * TODO FC.getImmutableFileState() only hands out selected files' selected data
	 * options to PC FC.getLoadedFiles() gets all files
	 * FileControllerUtils.getSelectedFiles hands out selected files PR == processed
	 * results Too many events??? Process Combo save/restore data option and apply
	 * to all files (in case list of files have changed) File selection changes file
	 * added in PR mode then select current data option (if one exists) None/PR
	 * switch entering PR, recall
	 * 
	 * update process table when changing from process combo clean up chosen
	 * options(?)
	 * 
	 * When changing back to None, remember chosen options
	 * 
	 * 
	 * Need to make offset plot modifier work
	 */
	private void updateProcessedDataTable(String process, List<LoadedFile> files) {
		currentSelection.clear();

		currentProcess = process;

		Set<String> names = process == null ? null : processDataOptions.get(process);
		DataOptions opt = null;
		if (names != null) {
			final Set<String> chosen = processDataChosen.get(currentProcess);
			for (String n : names) {
				boolean select = chosen == null ? false : chosen.contains(n);
				DataOptions d = updateCurrentSelection(files, n, n + DATA, select);
				if (d != null) {
					opt = d;
				}
			}
		} else if (RESULT.equals(process)) {
			opt = updateCurrentSelection(files, RESULT, RESULT_SUFFIX, false);
		}

		final DataOptions foundOpt = opt;
		Display.getDefault().asyncExec(() -> {
			processViewer.setInput(currentSelection);
			processViewer.refresh();

			if (foundOpt != null) {
				updateSelected(foundOpt);
			}
		});
	}

	public void updateTable(LoadedFile file) {
		if (file == null) {
			return;
		}

		final Set<String> names = processDataOptions.get(currentProcess);
		if (names == null) {
			return;
		}

		final Set<String> chosen = processDataChosen.get(currentProcess);
		currentSelection.clear();
		DataOptions opt = null;
		for (String n : names) {
			boolean select = chosen == null ? false : chosen.contains(n);
			DataOptions t = updateCurrentSelection(file, n, n + DATA, select, false);
			if (t != null) {
				if (opt == null) {
					opt = t;
				}
				currentSelection.add(t);
			}
		}

		final DataOptions foundOpt = opt;
		Display.getDefault().asyncExec(() -> {
			processViewer.setInput(currentSelection);
			processViewer.refresh();
			switchToPlotXY(foundOpt);
		});
	}

	private DataOptions updateCurrentSelection(List<LoadedFile> files, String name, String suffix, boolean select) {
		DataOptions found = null; // found and selected

		for (LoadedFile f : files) {
			DataOptions t = updateCurrentSelection(f, name, suffix, select, true);
			if (found == null && t != null) {
				found = t;
				currentSelection.add(t);
			}
		}

		return found;
	}

	private DataOptions updateCurrentSelection(LoadedFile f, String name, String suffix, boolean select,
			boolean resetOthers) {
		for (DataOptions d : f.getDataOptions()) {
			if (d.getName().endsWith(suffix)) {
				if (d.isSelected() != select) {
					d.setSelected(select);
				}
				d.setShortName(name);
				if (select) {
					NDimensions nd = d.getPlottableObject().getNDimensions();
					setSlicingFull(nd);
				}
				return d;
			} else if (resetOthers) {
				if (d.isSelected()) {
					d.setSelected(false);
				}
			}
		}

		return null;
	}

	private void setSlicingFull(NDimensions nd) {
		nd.setSliceFullRange(true);
		int r = nd.getRank();
		for (int i = 0; i < r; i++) {
			nd.setSlice(i, new Slice());
		}
	}

	private Set<String> getProcessDataChoice() {
		Set<String> chosen = processDataChosen.get(currentProcess);
		if (chosen == null) {
			chosen = new HashSet<>();
			processDataChosen.put(currentProcess, chosen);
		}
		return chosen;
	}

	private void updateProcessDataChoice(DataOptions opt) { // change for all files
		if (opt != null) {
			boolean isSelected = opt.isSelected();
			String name = opt.getShortName();
			if (name != null) {
				Set<String> chosen = getProcessDataChoice();
				if (isSelected) {
					chosen.add(name);
				} else {
					chosen.remove(name);
				}
			}

			name = opt.getName();
			for (LoadedFile f : fileController.getLoadedFiles()) {
				DataOptions d = f.getDataOption(name);
				if (d != null) {
					if (isSelected != d.isSelected()) {
						d.setSelected(isSelected);
					}
					if (isSelected) {
						NDimensions nd = d.getPlottableObject().getNDimensions();
						setSlicingFull(nd);
					}
				}
			}
		}

		processViewer.refresh();
	}

	public  void updateSelected(DataOptions op) {
		updateProcessDataChoice(op);
		if (op != null) {
			switchToPlotXY(op);
		}
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

	public boolean isEmpty() {
		return processViewer.isEmpty();
	}

	public void reselect() {
		processViewer.reselect();
	}

	public void dispose() {
		processViewer.dispose();
	}
}
