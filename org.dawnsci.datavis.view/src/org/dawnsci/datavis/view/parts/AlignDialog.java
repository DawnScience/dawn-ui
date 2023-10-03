/*-
 * Copyright (c) 2018 Diamond Light Source Ltd.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IPlotDataModifier;
import org.dawnsci.datavis.model.PlotEventObject;
import org.dawnsci.datavis.model.PlotEventObject.PlotEventType;
import org.dawnsci.datavis.model.PlotModeChangeEventListener;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.function.AlignToHalfGaussianPeak;

public class AlignDialog extends Dialog implements IRegionListener, PlotModeChangeEventListener {

	protected static final Logger logger = LoggerFactory.getLogger(AlignDialog.class);

	private IFileController fileController;
	private IPlotController plotController;
	private IPlottingSystem<?> plottingSystem;

	private Map<String, PlotItem> plotItems = new LinkedHashMap<>();

	private IRectangularROI currentROI = null;
	private boolean forceToZero;
	private boolean resampleX;
	private boolean plotAverage;
	private AlignToHalfGaussianPeak align = new AlignToHalfGaussianPeak(false);

	private Button resetButton;
	private Button plotAverageButton;
	private Button storeAutoAlignShifts;
	private Button restoreAutoAlignShifts;
	private TableViewer resultTable;
	private Color originalColour = null;
	private static Color doneColour = null;
	private List<Double> shiftsStore = new ArrayList<>();

	public AlignDialog(Shell parentShell, IFileController fc, IPlotController pc) {
		super(parentShell);

		setShellStyle(SWT.MODELESS | SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);

		fileController = fc;
		plotController = pc; 
		plottingSystem = pc.getPlottingSystem();
		plottingSystem.addRegionListener(this);
		plotController.addPlotModeListener(this);
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		resetPlotItems();
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		IRegion r = evt.getRegion();
		if (r != null && ALIGN_REGION.equals(r.getName())) {
			resetPlotItems();
		}
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
	}

	@Override
	public void regionCreated(RegionEvent evt) {
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void regionAdded(RegionEvent evt) {
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (doneColour == null) {
			Display d = Display.getCurrent();
			doneColour = d.getSystemColor(SWT.COLOR_DARK_CYAN);
		}
		Composite comp = (Composite) super.createDialogArea(parent); // implementation returns a composite with grid layout

		Composite alignComp = new Composite(comp, SWT.NONE);
		alignComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		alignComp.setLayout(new RowLayout());

		Button b = new Button(alignComp, SWT.PUSH);
		b.setText("Align");
		b.setToolTipText("Click and drag to select region on plot.\n"
				+ "Align spectra using leftmost leading slope in selected region.\n"
				+ "It aligns to first line or to zero if the selected region encloses zero.");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectRegion(false);
			}
		});

		resetButton = b = new Button(alignComp, SWT.PUSH);
		b.setText("Reset");
		b.setToolTipText("Use original spectra");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				plotOriginal();
			}
		});
		b.setEnabled(false);

		b = new Button(alignComp, SWT.CHECK);
		b.setText("Force to zero");
		b.setToolTipText("Make align to zero unconditionally; reselect region");
		b.setSelection(forceToZero);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				plotOriginal();
				Button button = (Button) e.getSource();
				forceToZero = button.getSelection();
				selectRegion(true);
			}
		});

		b = new Button(alignComp, SWT.CHECK);
		b.setText("Resample");
		b.setToolTipText("Interpolate to common x points");
		b.setSelection(resampleX);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				resampleX = button.getSelection();
				plotAverageButton.setEnabled(resampleX);
				if (!resampleX) {
					plotAverage = false;
					plotAverageButton.setSelection(plotAverage);
				}
				selectRegion(false);
			}
		});

		plotAverageButton = new Button(alignComp, SWT.CHECK);
		plotAverageButton.setText("Show average");
		plotAverageButton.setToolTipText("Average data and plot it");
		plotAverageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				plotAverage = button.getSelection();
				selectRegion(false);
			}
		});
		plotAverageButton.setEnabled(resampleX);

		Composite resultComp = new Composite(comp, SWT.NONE);
		resultComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		resultComp.setLayout(new FillLayout());

		resultTable = new TableViewer(resultComp, SWT.NONE);
		ColumnViewerToolTipSupport.enableFor(resultTable);

		resultTable.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("unchecked")
				Map<String, PlotItem> input = (Map<String, PlotItem>) inputElement;
				return input.values().toArray(new PlotItem[input.size()]);
			}
		});

		resultTable.getTable().setHeaderVisible(true);

		TableViewerColumn name = new TableViewerColumn(resultTable, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((PlotItem) element).getName();
			}

			@Override
			public String getToolTipText(Object element) {
				return getText(element);
			}
		});
		TableColumn column = name.getColumn();
		column.setText("Dataset Name");
		column.setWidth(250);

		TableViewerColumn auto = new TableViewerColumn(resultTable, SWT.LEFT);
		auto.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Double.toString(((PlotItem) element).getAuto());
			}
		});
		column = auto.getColumn();
		column.setText("Auto-align");
		column.setToolTipText("Values found by auto-aligner");
		column.setWidth(80);

		TableViewerColumn manual = new TableViewerColumn(resultTable, SWT.LEFT);
		manual.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((PlotItem) element).getManual());
			}
		});
		column = manual.getColumn();
		column.setText("Manual");
		column.setToolTipText("Edit to manually adjust plot after auto-align");
		column.setWidth(50);
		manual.setEditingSupport(new ManualAdjustEditingSupport(resultTable));

		// add tab navigation
		ColumnViewerEditorActivationStrategy actStrategy = new ColumnViewerEditorActivationStrategy(resultTable) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return ((ViewerCell) event.getSource()).getColumnIndex() == 2;
			}
		};
		TableViewerEditor.create(resultTable, actStrategy, ColumnViewerEditor.TABBING_HORIZONTAL 
			| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);

		resultTable.setInput(plotItems);
		updatePlotItems();
		resultTable.refresh();
		resultTable.getControl().pack(); // update to display as many rows in table as possible
		updateResetButton();

		Composite storeComp = new Composite(comp, SWT.NONE);
		storeComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		storeComp.setLayout(new RowLayout());
		storeAutoAlignShifts = new Button(storeComp, SWT.PUSH);
		storeAutoAlignShifts.setText("Store");
		storeAutoAlignShifts.setToolTipText("Save auto-align shifts");
		storeAutoAlignShifts.setEnabled(false);
		storeAutoAlignShifts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				restoreAutoAlignShifts.setEnabled(true);
				storeShiftsFromAutoAlign();
			}
		});
		restoreAutoAlignShifts = new Button(storeComp, SWT.PUSH);
		restoreAutoAlignShifts.setText("Restore");
		restoreAutoAlignShifts.setToolTipText("Reload saved auto-align shifts as manual shifts");
		restoreAutoAlignShifts.setEnabled(!shiftsStore.isEmpty());
		restoreAutoAlignShifts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!setManualShiftsFromStore()) {
					MessageDialog.openError(getParentShell(), "Error", "There are more plots than stored shifts. Re-align plots and store.");
				}
			}
		});

		return comp;
	}

	@Override
	public int open() { // can do this as dialog is non-modal
		int code = super.open();
		getButton(IDialogConstants.CANCEL_ID).setToolTipText("Reset plots");
		getButton(IDialogConstants.OK_ID).setToolTipText("Keep alignment");
		setRegionVisible(true);

		return code;
	}

	private void updatePlotItems() {
		// update index
		List<? extends IDataPackage> plotData = fileController.getImmutableFileState();
		List<IXYData> xyData = DataPackageUtils.getXYData(plotData, false);
		int lmax = xyData.size();
		if (lmax < 2) {
			return;
		}
		List<String> plotNames = new ArrayList<String>();
		for (ILineTrace t : plottingSystem.getTracesByClass(ILineTrace.class)) {
			String n = t.getName();
			if (RESAMPLE_AVERAGE.equals(n)) {
				continue;
			}
			plotNames.add(n);
		}

		IDataPackage pd = plotData.get(0);
		String fn = pd.getFilePath();
		String dn = pd.getName();
		int ip = 0;
		int ipmax = plotData.size();
		int i = 0;
		int[] pdIndex = new int[lmax]; // index values for plotData list
		int[] xyIndex = new int[lmax]; // index values for xyData list
		int si = 0;
		IPlotDataModifier modifier = plotController.getEnabledPlotModifier();
		if (modifier != null && !modifier.supportsRank(1)) {
			modifier = null;
		}
		for (IXYData d : xyData) { // gather inputs to shifter
			Dataset x = DatasetUtils.convertToDataset(d.getX());
			Dataset y = DatasetUtils.convertToDataset(d.getY());
			String n = plotNames.get(i);
			plotNames.add(n);
			PlotItem pi = plotItems.get(n);
			if (pi == null) {
				pi = new PlotItem(n);
				plotItems.put(n, pi);
			} else {
				pi.setManual(0);
			}
			pi.setAuto(0);
			pi.setX(x);
			if (modifier != null) {
				y = DatasetUtils.convertToDataset(modifier.modifyForDisplay(y));
				setAxesMetadata(x, y);
			}
			pi.setY(y);
			logger.debug("Trace {}: {}", n, y.getSliceView(new Slice(12)).toString(true));

			while (!fn.equals(d.getFileName()) || !dn.equals(d.getDatasetName())) {
				si = 0;
				if (++ip < ipmax) {
					pd = plotData.get(ip);
					fn = pd.getFilePath();
					dn = pd.getName();
				} else {
					pd = null;
					break;
				}
			}
			pdIndex[i] = ip;
			xyIndex[i] = si++;
			i++;
		}

		Set<String> removeItems = new HashSet<>(plotItems.keySet());
		removeItems.removeAll(plotNames);
		for (String n: removeItems) {
			plotItems.remove(n);
		}

		i = 0;
		for (PlotItem pi : plotItems.values()) {
			pi.setIndex(pdIndex[i], xyIndex[i]);
			i++;
		}
	}

	private void updateResetButton() {
		if (resetButton.getEnabled() == plotItems.isEmpty()) {
			Display.getDefault().asyncExec(() -> resetButton.setEnabled(!plotItems.isEmpty()));
		}
	}

	private static final String ALIGN_REGION = "Align region";

	private void selectRegion(boolean removeOld) {
		IRegion r = plottingSystem.getRegion(ALIGN_REGION);
		if (r != null) {
			if (r.getRegionType() != RegionType.XAXIS) {
				plottingSystem.renameRegion(r, "Not " + ALIGN_REGION);
				r = null;
			} else if (removeOld) {
				plottingSystem.removeRegion(r);
				r = null;
			}
		}
		if (r == null) {
			r = createRegion();
		} else {
			if (!r.isVisible()) {
				r.setVisible(true);
			}

			double[] xs = getLimits();
			if (xs != null) {
				if (!ensureRegionOK(r, xs)) {
					return;
				}

				alignPlots(xs[0], xs[1]);
			}
		}
	}

	private IRegion createRegion() {
		IRegion r = null;
		try {
			r = plottingSystem.createRegion(ALIGN_REGION, RegionType.XAXIS);
			originalColour = r.getRegionColor();
			r.addROIListener(new IROIListener() {

				@Override
				public void roiSelected(ROIEvent evt) {
				}

				@Override
				public void roiDragged(ROIEvent evt) {
					setRegionDone(false);
				}

				@Override
				public void roiChanged(ROIEvent evt) {
					currentROI = (IRectangularROI) evt.getROI();
					double[] xs = getLimits();
					if (xs != null) {
						alignPlots(xs[0], xs[1]);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Could not create alignment region", e);
		}
		return r;
	}

	/**
	 * Set selection region's visibility
	 * @param visible
	 */
	public void setRegionVisible(boolean visible) {
		IRegion r = plottingSystem.getRegion(ALIGN_REGION);
		if (r != null) {
			r.setVisible(visible);
		}
	}

	/**
	 * Set selection region's done state by changing colour
	 * @param done
	 */
	public void setRegionDone(boolean done) {
		IRegion r = plottingSystem.getRegion(ALIGN_REGION);
		if (r != null) {
			r.setRegionColor(done ? doneColour : originalColour);
		}
	}

	private boolean ensureRegionOK(IRegion r, double[] xs) {
		IAxis axis = plottingSystem.getSelectedXAxis();
		double l = axis.getLower();
		double u = axis.getUpper();
		if (xs[0] > u || xs[1] < l) {
			currentROI.setPoint(0.5 * (l + u), currentROI.getPointY());
			r.setROI(currentROI);
			return false;
		}
		return true;
	}

	private double[] getLimits() {
		if (currentROI == null) {
			return null;
		}
		double lx = currentROI.getPointX();
		double dx = currentROI.getLength(0);
		double hx;
		if (dx < 0) {
			hx = lx;
			lx -= dx;
		} else if (dx > 0) {
			hx = lx + dx;
		} else {
			return null;
		}
		return new double[] {lx, hx};
	}

	private static final String RESAMPLE_AVERAGE = "Average";

	/*
	 * FIXME
	 * issues:
	 *  in LoadedFile (from DO's parent) as virtual option (for each file too) and update table of current file.
	 *  See DataOptionTableViewer#163 (action to apply expression)
	 */
	private void alignPlots(double lx, double hx) {
		logger.debug("Region bounds are {}, {}", lx, hx);
		removeAveragePlot();

		int imax = plotItems.size();
		if (imax < 2) {
			return;
		}
		Dataset[] input = new Dataset[2 * imax];
		int i = 0;
		for (PlotItem pi : plotItems.values()) {
			input[i++] = pi.getX();
			input[i++] = pi.getY();
		}

		align.setPeakZone(lx, hx);
		List<Double> posn = align.value(input);
		List<Double> shifts = AlignToHalfGaussianPeak.calculateShifts(resampleX, forceToZero || (lx <= 0 && hx >= 0), 0, posn, input);

		i = 0;
		int[] pdIndex = new int[imax]; // index values for plotData list
		Dataset[] data = new Dataset[input.length];
		double[] firstXShift = {Double.NaN};
		for (PlotItem pi : plotItems.values()) {
			pdIndex[i/2] = pi.getIndex()[0];
			double delta = pi.getManual();
			Dataset x = pi.getX();
			Double sX = shifts.get(i);
			Double sY = null;
			Double s = sX;
			if (sX == null) {
				sY = shifts.get(i + 1);
				if (sY != null) { // translate to index-space
					double xd = Math.abs(x.getDouble(1) - x.getDouble(0));
					s = sY * xd; // so displays are in x-space
					delta /= xd; // TODO remove when shifts are all in x-space
					sY += delta;
				}
			} else {
				if (delta != 0) {
					sX += delta;
				}
			}
			if (s != null) {
				pi.setAuto(s);
			}
			Dataset[] results = AlignToHalfGaussianPeak.shiftData(firstXShift, sX, sY, input[i], input[i+1]);
			data[i] = results[0];
			data[i + 1] = results[1];
			i += 2;
		}
		logger.debug("Plot align: first shift in X = {}", firstXShift[0]);

		// gather and store in corresponding plot data
		List<Dataset> store = new ArrayList<>();
		int cip = pdIndex[0];
		List<? extends IDataPackage> plotData = fileController.getImmutableFileState();
		IDataPackage pd = plotData.get(cip);
		for (int j = 0; j < data.length; j += 2) {
			int nip = pdIndex[j/2]; 
			if (nip != cip) {
				storeXYData(pd, store);
				store.clear();
				cip = nip;
				pd = plotData.get(cip);
			}
			Dataset x = data[j];
			Dataset y = data[j + 1];
			setAxesMetadata(x, y);
			logger.debug("Aligned: {}", y.getSlice(new Slice(8)).toString(true));
			store.add(x);
			store.add(y);
		}
		storeXYData(pd, store);

		i = 0;
		int minSize = Integer.MAX_VALUE;
		IPlotDataModifier modifier = plotController.getEnabledPlotModifier();
		if (modifier != null && !modifier.supportsRank(1)) {
			modifier = null;
		}
		for (ILineTrace t : plottingSystem.getTracesByClass(ILineTrace.class)) {
			String n = t.getName();
			if (RESAMPLE_AVERAGE.equals(n)) {
				continue;
			}
			Dataset x = data[i];
			Dataset d = data[i + 1];
			PlotItem pi = plotItems.get(n);
			Dataset ox = pi.getX();
			if (resampleX || x != ox) {
				x.setName(ox.getName());
				minSize = Math.min(minSize, x.getSize());
			}

			if (modifier != null) {
				d = DatasetUtils.convertToDataset(modifier.modifyForDisplay(d));
			}
			t.setData(x, d);

			i += 2;
		}

		if (plotAverage) {
			plotAverage(data, minSize);
		}

		setRegionDone(true);
		plottingSystem.repaint(false);
		resultTable.refresh();
		storeAutoAlignShifts.setEnabled(true);
	}

	// store aligned data in original data option
	// rather than synthesize new DataOptions for each file
	private void storeXYData(IDataPackage dp, List<Dataset> xy) {
		int n = xy == null ? 0 : xy.size();
		if (n % 2 == 1) {
			logger.warn("List has odd number of datasets; last item will be ignored");
		}
		if (dp instanceof DataOptions) {
			DataOptions o = (DataOptions) dp;
			o = o.getParent().getDataOption(dp.getName());

			if (n == 0) {
				o.removeDerivedData(IXYData.class);
			} else {
				List<IXYData> list = new ArrayList<>();
				for (int i = 0; i < n; i+=2) {
					list.add(DataPackageUtils.createXYData(xy.get(i), xy.get(i + 1), o));
				}
	
				o.addDerivedData(list);
			}
		}
	}

	private void storeShiftsFromAutoAlign() {
		shiftsStore.clear();
		for (PlotItem pi : plotItems.values()) {
			shiftsStore.add(pi.getAuto());
		}
	}

	private boolean setManualShiftsFromStore() {
		if (plotItems.size() > shiftsStore.size()) {
			return false;
		}
		int i = 0;
		for (PlotItem pi : plotItems.values()) {
			double d = shiftsStore.get(i++);
			pi.setManual(d);
			updateDerivedDataAndTrace(pi);
		}
		plottingSystem.repaint(false);
		resultTable.refresh();
		return true;
	}

	private void plotAverage(Dataset[] data, int minSize) {
		Slice s = new Slice(minSize);
		Dataset sum = data[1].getSlice(s);
		int max = data.length;
		for (int i = 3; i < max; i += 2) {
			Dataset d = DatasetUtils.convertToDataset(data[i]);
			sum.iadd(d.getSliceView(s));
		}
		sum.idivide(max/2);
		ILineTrace t = plottingSystem.createLineTrace(RESAMPLE_AVERAGE);
		IDataset ox = data[0];
		Dataset x = DatasetUtils.convertToDataset(ox).getSliceView(s);
		x.setName(ox.getName());
		t.setData(x, sum);
		plottingSystem.addTrace(t);
	}

	private static void setAxesMetadata(IDataset x, IDataset y) {
		try {
			AxesMetadata am = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			am.setAxis(0, x);
			y.setMetadata(am);
		} catch (MetadataException e) {
			logger.error("Could not create axes metadata", e);
		}
	}

	private void updateDerivedDataAndTrace(PlotItem pi) {
		String firstName = plotItems.keySet().iterator().next();
		double firstXShift = 0;
		if (resampleX && !firstName.equals(pi.getName())) {
			PlotItem firstItem = plotItems.get(firstName);
			firstXShift = firstItem.getAuto();
			if (firstXShift == 0) {
				firstXShift = firstItem.getManual();
			}
		}
		ITrace t = plottingSystem.getTrace(pi.getName());
		if (t instanceof ILineTrace) {
			ILineTrace lt = (ILineTrace) t;
			Dataset x = pi.getX();
			logger.debug("Updated x-axis: {}", x.getSlice(new Slice(8)).toString(true));
			double delta = pi.getAuto() + pi.getManual();
			logger.debug("Shifting by {} and {}", delta, firstXShift);
			Dataset nx = Maths.add(x, delta + firstXShift);
			Dataset ny = pi.getY();
			if (resampleX) {
				ny = Maths.interpolate(nx, ny, x, null, null);
				nx = x;
			}
			setAxesMetadata(nx, ny);

			int[] index = pi.getIndex();
			if (index[0] >= 0) {
				updateDerivedData(index, nx, ny);
			}
			IPlotDataModifier modifier = plotController.getEnabledPlotModifier();
			if (modifier != null && modifier.supportsRank(1)) {
				ny = DatasetUtils.convertToDataset(modifier.modifyForDisplay(ny));
			}
			logger.debug("Updated trace {}: {}", pi.getName(), ny.getSlice(new Slice(8)).toString(true));

			lt.setData(nx, ny);
			lt.repaint();

			if (plotAverage) {
				removeAveragePlot();

				Dataset[] data = new Dataset[2 * plotItems.size()];
				int minSize = Integer.MAX_VALUE;
				int i = 0;
				for (ILineTrace nt : plottingSystem.getTracesByClass(ILineTrace.class)) {
					if (RESAMPLE_AVERAGE.equals(nt.getName())) {
						continue;
					}
					Dataset ntx = DatasetUtils.convertToDataset(nt.getXData());
					minSize = Math.min(minSize, ntx.getSize());
					data[i++] = ntx;
					data[i++] = DatasetUtils.convertToDataset(nt.getYData());
				}

				plotAverage(data, minSize);
			}
		}
	}

	private void updateDerivedData(int[] index, Dataset nx, IDataset ny) {
		List<? extends IDataPackage> plotData = fileController.getImmutableFileState();
		IDataPackage dp = plotData.get(index[0]);

		if (dp instanceof DataOptions) {
			DataOptions o = (DataOptions) dp;
			o = o.getParent().getDataOption(dp.getName());
			List<IXYData> list = o.getDerivedData(IXYData.class);
			if (list == null) {
				list = addDerivedData(o, index[0]);
			}
			int i = index[1];
			list.remove(i);
			list.add(i, DataPackageUtils.createXYData(nx, ny, o));
		}
	}

	private List<IXYData> addDerivedData(DataOptions o, int pdIndex) {
		List<Dataset> store = new ArrayList<>();

		for (PlotItem pi : plotItems.values()) {
			if (pi.getIndex()[0] == pdIndex) {
				store.add(pi.getX());
				store.add(pi.getY());
			}
		}
		storeXYData(o, store);
		return o.getDerivedData(IXYData.class);
	}

	/**
	 * Reset plot to unaligned and clear old state if needed
	 */
	public void resetPlotItems() {
		plotOriginal();
		updateResetButton();
	}

	private void plotOriginal() {
		removeAveragePlot();
		for (ILineTrace t : plottingSystem.getTracesByClass(ILineTrace.class)) {
			PlotItem pi = plotItems.get(t.getName());
			Dataset x = pi.getX();
			Dataset y = pi.getY();
			if (x != null) {
				t.setData(x, y);
			}
			pi.setAuto(0);
			pi.setManual(0);
		}
		for (DataOptions o : fileController.getImmutableFileState()) {
			storeXYData(o, null); // remove data 
		}
		setRegionDone(false);
		if (!resultTable.getControl().isDisposed()) {
			resultTable.refresh();
		}
		plottingSystem.repaint(false);
	}

	private void removeAveragePlot() {
		ITrace at = plottingSystem.getTrace(RESAMPLE_AVERAGE);
		if (at != null) {
			plottingSystem.removeTrace(at);
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Plot aligner");
	}

	@Override
	public boolean close() {
		plottingSystem.removeRegionListener(this);
		if (getReturnCode() == Window.CANCEL) {
			plotOriginal();
			removeRegion();
			plotItems.clear();
		} else {
			setRegionVisible(false);
		}
		currentROI = null;
		return super.close();
	}

	private void removeRegion() {
		IRegion r = plottingSystem.getRegion(ALIGN_REGION);
		if (r != null) {
			if (plottingSystem.getTracesByClass(ILineTrace.class).size() == 0) {
				currentROI = null;
			}
			plottingSystem.removeRegion(r);
		}
	}

	/**
	 * Refresh region
	 */
	public void refreshRegion() {
		if (currentROI != null) {
			IRegion r = plottingSystem.getRegion(ALIGN_REGION);
			if (r == null) {
				r = createRegion();
				plottingSystem.addRegion(r);
			}
			if (r != null) {
				if (!r.isVisible()) {
					r.setVisible(true);
				}
				if (ensureRegionOK(r, getLimits()) && r.getROI() != currentROI) {
					r.setROI(currentROI);
				}
			}
		}
	}

	private static class PlotItem {
		private String name;
		private double auto;
		private double manual;
		private int[] index;
		private Dataset x, y;

		public PlotItem(String name) {
			this.name = name;
			index = new int[] {-1, -1};
		}

		public String getName() {
			return name;
		}

		public Dataset getX() {
			return x;
		}

		public void setX(Dataset x) {
			this.x = x;
		}

		public Dataset getY() {
			return y;
		}

		public void setY(Dataset y) {
			this.y = y;
		}

		public double getAuto() {
			return auto;
		}

		public void setAuto(double auto) {
			this.auto = auto;
		}

		public double getManual() {
			return manual;
		}

		public void setManual(double manual) {
			this.manual = manual;
		}

		public int[] getIndex() {
			return index;
		}

		/**
		 * Set plot data index
		 * @param index
		 * @param subIndex 
		 */
		public void setIndex(int index, int subIndex) {
			this.index[0] = index;
			this.index[1] = subIndex;
		}
	}

	private class ManualAdjustEditingSupport extends EditingSupport {
		private Table table;

		public ManualAdjustEditingSupport(TableViewer viewer) {
			super(viewer);
			table = viewer.getTable();
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(table);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof PlotItem) {
				return String.valueOf(((PlotItem) element).getManual());
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				double delta = Double.parseDouble((String) value);
				PlotItem pi = ((PlotItem) element);
				pi.setManual(delta);
				getViewer().update(element, null);
				updateDerivedDataAndTrace(pi);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Reset internal state
	 */
	public void reset() {
		plotItems.clear();
	}


	@Override
	public void plotModeChanged() {
		// do nothing
	}


	@Override
	public void plotStateEvent(PlotEventObject event) {
		if (event.getEventType() == PlotEventType.READY) {
			logger.debug("Updating plot items");
			plotItems.clear();
			updatePlotItems();
			Display.getDefault().asyncExec(() -> {
				removeRegion();
				if (!resultTable.getTable().isDisposed()) resultTable.refresh();
			});
		}
	}
}
