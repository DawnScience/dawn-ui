/*-
 * Copyright (c) 2021 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.manipulation.aggregate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.manipulation.DataManipulationUtils;
import org.dawnsci.datavis.manipulation.FileWritingUtils;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Comparisons.Monotonicity;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;
import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram2D;

/**
 * Dialog to allow 1D plot data to aggregated into a 2D or 3D assembly
 */
public class AggregateDialog extends Dialog {
	private static final String AGGREGATED_ENTRY = "aggregated";

	private static final Logger logger = LoggerFactory.getLogger(AggregateDialog.class);

	private static final String FILE_NAME = "Filename";

	private static final int FILE_NAME_WIDTH = 240;

	private IPlottingSystem<Composite> system;
	private TableViewer viewer;
	private final List<IDataFilePackage> data;
	private List<LabelOption> chosenLabels;
	private boolean[] undefinedAxis = new boolean[2];
	private List<LabelOption> labelOptions;

	private HistogramBin yBins;

	private HistogramBin xBins;

	private boolean showHistogram;

	private Button plot;

	private LabelColumn xBinsParameters;

	private LabelColumn yBinsParameters;

	private IExpressionEngine engine;

	private Dataset aggregate;

	private Button saveButton;

	private String lastPath;

	public AggregateDialog(Shell parentShell, List<IDataFilePackage> data) {
		super(parentShell);

		try {
			system = ServiceProvider.getService(IPlottingService.class).createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating Aggregate plotting system:", e);
		}

		engine = ServiceProvider.getService(IExpressionService.class).getExpressionEngine();
		this.data = data;
		labelOptions = getLabelOptions();
		initializeChosenLabels();
	}

	private void initializeChosenLabels() {
		chosenLabels = new ArrayList<>();
		String label = data.get(0).getLabelName();
		LabelOption first = null;
		if (!label.isEmpty()) {
			first = labelOptions.stream().filter(l -> label.equals(l.getLabel())).findAny().orElse(null);
		}
		chosenLabels.add(first);
		chosenLabels.add(null);
		undefinedAxis[0] = label.isEmpty();
		undefinedAxis[1] = true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Aggregate plots by label values");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true);
		GridLayoutFactory glf = GridLayoutFactory.fillDefaults();

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayoutData(gdf.create());
		container.setLayout(glf.create());

		Composite topPane = WidgetFactory.composite(SWT.NONE).create(container);
		topPane.setLayoutData(gdf.copy().minSize(800, 350).create());
		topPane.setLayout(glf.create());
		system.createPlotPart(topPane, "Label Value Plot", null, PlotType.XY, null);
		system.getPlotComposite().setLayoutData(gdf.create());
		system.setShowLegend(false);

		Composite bottomPane = WidgetFactory.composite(SWT.NONE).create(container);
		bottomPane.setLayoutData(gdf.create());
		bottomPane.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).create());
		
		viewer = new TableViewer(bottomPane, SWT.BORDER | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(gdf.copy().grab(false, true).create());

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<?>) inputElement).toArray();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return new File(((IDataFilePackage)element).getFilePath()).getName();
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDataFilePackage) return ((IDataFilePackage) element).getFilePath();
				return null;
			};
		});
		TableColumn nc = name.getColumn();
		nc.setText(FILE_NAME);
		nc.setWidth(FILE_NAME_WIDTH);

		TableColumn xc = setUpColumn(0);
		TableColumn yc = setUpColumn(1);

		TableColumnLayout columnLayout = new TableColumnLayout();
		columnLayout.setColumnData(nc, new ColumnWeightData(40,200));
		columnLayout.setColumnData(xc, new ColumnWeightData(30,40));
		columnLayout.setColumnData(yc, new ColumnWeightData(30,40));

		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(data);

		Composite entryPane = WidgetFactory.composite(SWT.NONE).create(bottomPane);
		entryPane.setLayoutData(gdf.create());
		entryPane.setLayout(glf.create());

		plot = WidgetFactory.button(SWT.CHECK).create(entryPane);
		plot.setText("Show histogram");
		plot.setToolTipText("Plot histogram of label value positions");
		plot.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			showHistogram = plot.getSelection();
			processAndPlotValues(false);
		}));
		plot.setSelection(false);
		plot.setEnabled(!undefinedAxis[0]);

		WidgetFactory.button(SWT.PUSH).text("Create label").tooltip("Make new label from expression")
				.onSelect(e -> {createLabelExpression();}).create(entryPane);

		Composite detailsPane = WidgetFactory.composite(SWT.NONE).create(entryPane);
		detailsPane.setLayoutData(gdf.copy().align(SWT.BEGINNING, SWT.BEGINNING).create());
		detailsPane.setLayout(glf.copy().numColumns(3).create());

		// name, start, stop, step, bins
		Composite textColumn = WidgetFactory.composite(SWT.NONE).create(detailsPane);
		textColumn.setLayoutData(gdf.create());
		textColumn.setLayout(glf.create());
		WidgetFactory.text(SWT.NONE).text("Name").layoutData(gdf.create()).create(textColumn);
		WidgetFactory.text(SWT.NONE).text("Start").layoutData(gdf.create()).create(textColumn);
		WidgetFactory.text(SWT.NONE).text("Stop").layoutData(gdf.create()).create(textColumn);
		WidgetFactory.text(SWT.NONE).text("Step").layoutData(gdf.create()).create(textColumn);
		WidgetFactory.text(SWT.NONE).text("Bins").layoutData(gdf.create()).create(textColumn);

		Composite labelXColumn = WidgetFactory.composite(SWT.NONE).create(detailsPane);
		labelXColumn.setLayoutData(gdf.create());
		labelXColumn.setLayout(glf.create());
		xBinsParameters = new LabelColumn(labelXColumn);

		Composite labelYColumn = WidgetFactory.composite(SWT.NONE).create(detailsPane);
		labelXColumn.setLayoutData(gdf.create());
		labelYColumn.setLayout(glf.create());
		yBinsParameters = new LabelColumn(labelYColumn);

		// space
		new Text(entryPane, SWT.NONE);

		// TODO
		// add GridROI?
		// allow more files (quicker loading with known dataset path)

		processAndPlotValues(true);

		return container;
	}

	private void createLabelExpression() {
		Map<String, Dataset> datasets = new LinkedHashMap<>();
		Map<String, LabelOption> options = new HashMap<>();
		for (LabelOption l : labelOptions) {
			Dataset d = getLabelDataset(l);
			if (d.getSize() > 0) {
				datasets.put(l.getLabel(), d);
				options.put(l.getName(), l);
			}
		}
		ExpressionLabelDialog d = new ExpressionLabelDialog(getShell(), datasets);

		int r = d.open();
		if (r == Dialog.CANCEL) {
			return;
		}
		List<String> result = d.getResult();
		String expression = result.remove(0);

		ExpressionLabelOption l = new ExpressionLabelOption(expression);
		for (String v : result) {
			l.addVariable(options.get(v));
		}
		labelOptions.add(0, l);
	}

	class LabelColumn { // better as table?
		private Text name;
		private Text start;
		private Text stop;
		private Text delta;
		private Text steps;
		private HistogramBin bin;

		public LabelColumn(Composite parent) {
			GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, false);
			name = TextFactory.newText(SWT.NONE | SWT.READ_ONLY)
					.layoutData(gdf.create()).create(parent);

			start = TextFactory.newText(SWT.BORDER | SWT.SINGLE | SWT.RIGHT).limitTo(10)
					.layoutData(gdf.create()).create(parent);
			addListeners(start);

			stop = TextFactory.newText(SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT).limitTo(10)
					.layoutData(gdf.create()).create(parent);

			delta = TextFactory.newText(SWT.BORDER | SWT.SINGLE | SWT.RIGHT).limitTo(10)
					.layoutData(gdf.create()).create(parent);
			addListeners(delta);

			steps = TextFactory.newText(SWT.BORDER | SWT.SINGLE | SWT.RIGHT).limitTo(4)
					.layoutData(gdf.create()).create(parent);
			addListeners(steps);
		}

		private void addListeners(Text text) { // TODO check if works on win32 and macOS
			text.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
				if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
					try {
						updateAll(text);
					} catch (Exception ex) {
						// do nothing
					}
				}
			}));
			text.addFocusListener(FocusListener.focusLostAdapter(e -> {
				try {
					updateAll(text);
				} catch (Exception ex) {
					// do nothing
				}
			}));
			text.addTraverseListener(e -> {
				if (e.keyCode  == '\r') {
					e.doit = false; // swallow to stop dialog closing
				}
			});
		}

		void update(HistogramBin b) {
			bin = b.clone();
			name.setText(bin.getName());
			name.setEnabled(true);
			name.requestLayout();
			updateAll(null);
		}

		void updateAll(Text origin) {
			boolean reset = true;
			if (origin == start) {
				try {
					bin.setStart(Double.parseDouble(origin.getText()));
					reset = false;
				} catch (Exception e) {
				}
			}
			if (reset) {
				start.setText(Double.toString(bin.getStart()));
				start.setEnabled(true);
				start.requestLayout();
			}

			reset = true;
			if (origin == delta) {
				try {
					bin.setDelta(Double.parseDouble(origin.getText()));
					reset = false;
				} catch (Exception e) {
				}
			}
			if (reset) {
				delta.setText(Double.toString(bin.getDelta()));
				delta.setEnabled(true);
				delta.requestLayout();
			}

			reset = true;
			if (origin == steps) {
				try {
					bin.setSteps(Integer.parseInt(origin.getText()));
					reset = false;
				} catch (Exception e) {
				}
			}
			if (reset) {
				steps.setText(Integer.toString(bin.getSteps()));
				steps.setEnabled(true);
				steps.requestLayout();
			}

			// last as depends on other bin settings
			stop.setText(Double.toString(bin.getStop()));
			stop.setEnabled(true);
			stop.requestLayout();
		}

		public HistogramBin getBin() {
			return bin;
		}

		void clear() {
			name.setText(EMPTY);
			name.setEnabled(false);
			start.setText(EMPTY);
			start.setEnabled(false);
			stop.setText(EMPTY);
			stop.setEnabled(false);
			delta.setText(EMPTY);
			delta.setEnabled(false);
			steps.setText(EMPTY);
			steps.setEnabled(false);
			name.getParent().layout();
		}

		public void setFocus() {
			start.setFocus();
		}
	}

	private static final String EMPTY = "";

	private TableColumn setUpColumn(final int i) {
		TableViewerColumn labelColumn = new TableViewerColumn(viewer, SWT.LEFT);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getLabelValue((IDataFilePackage) element, i);
			}
		});

		TableColumn c = labelColumn.getColumn();
		c.setText(getAxis(i) + " label...");
		c.setToolTipText("Click to select label");
		c.setWidth(80);
		c.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			if (handleLabelChoice(c, i)) {
				IntegerDataset fileIndex = null;
				if (i == 0) {
					boolean disable = undefinedAxis[0];
					if (disable) {
						plot.setSelection(false);
						showHistogram = false;
					} else {
						Dataset x = getLabelValues(0);
						Monotonicity m = Comparisons.findMonotonicity(x);
						if (m == Monotonicity.NOT_ORDERED) {
							MessageDialog.openWarning(getShell(), "Label values unordered", "Files will be sorted");
							fileIndex = DatasetUtils.indexSort(x, null);
							sortData(fileIndex);
						}
					}
					plot.setEnabled(!disable);
				}
				sortTable(fileIndex);
				processAndPlotValues(true);
				saveButton.setEnabled(false);
			}
		}));
		return c;
	}

	private void sortData(IntegerDataset index) {
		LabelOption opt = labelOptions.get(0);
		Dataset v = opt.getUnsortedValues();
		Dataset sv = v.getBy1DIndex(index);
		sv.setName(v.getName());
		opt.setSortedValues(sv);
	}

	private void sortTable(IntegerDataset index) {
		if (index == null) {
			viewer.setInput(data);
		} else {
			List<IDataFilePackage> sortedData = new ArrayList<>();
			IndexIterator it = index.getIterator();
			while (it.hasNext()) {
				sortedData.add(data.get(index.getAbs(it.index)));
			}
			viewer.setInput(sortedData);
		}
		viewer.refresh();

		for (TableColumn c : viewer.getTable().getColumns()) {
			c.pack();
			if (FILE_NAME.equals(c.getText()) && c.getWidth() > FILE_NAME_WIDTH) {
				c.setWidth(FILE_NAME_WIDTH);
			}
		}
		viewer.getControl().getParent().layout();
	}

	private String getAxis(int i) {
		return i == 0 ? "X" : "Y";
	}

	private boolean handleLabelChoice(TableColumn c, int i) {
		// do not allow choice of label Y when label X undefined
		if (i == 1 && undefinedAxis[0]) {
			return false;
		}
		if (!undefinedAxis[0]) {
			LabelOption opt = labelOptions.get(0);
			opt.setSortedValues(null);
		}

		ResettableListDialog d = new ResettableListDialog(getShell());
		d.setTitle("Select item for label " + getAxis(i));

		LabelOption [] choice;
		if (i == 0) {
			choice = labelOptions.toArray(new LabelOption[labelOptions.size()]);
		} else { // remove chosen label X for label Y
			LabelOption other = chosenLabels.get(0);
			choice = labelOptions.stream().filter(o -> !o.equals(other))
					.collect(Collectors.toList()).toArray(new LabelOption[0]);
		}
		d.setElements(choice);
		LabelOption chosen = chosenLabels.get(i);
		if (chosen != null) {
			d.setInitialSelections(chosen);
		}

		// only allow label X to be reset if label Y is not defined
		if (i == 0 && !undefinedAxis[1]) {
			d.setResettable(false);
		}

		int r = d.open();
		if (r == Dialog.CANCEL) {
			return false;
		} else if (r == ResettableListDialog.RESET) {
			c.setText(getAxis(i) + " label...");
			chosenLabels.set(i, null);
		} else {
			LabelOption l = (LabelOption) d.getResult()[0];
			c.setText(l.getName());
			chosenLabels.set(i, l);
		}
		undefinedAxis[i] = chosenLabels.get(i) == null;
		if (!undefinedAxis[i]) {
			if (i == 0) {
				xBinsParameters.setFocus();
			} else {
				yBinsParameters.setFocus();
			}
		}
		return true;
	}

	private String getLabelValue(IDataFilePackage d, int i) {
		if (undefinedAxis[i]) {
			return EMPTY;
		}
		LabelOption o = chosenLabels.get(i);
		if (o instanceof ExpressionLabelOption) {
			Dataset v = getLabelDataset(o);
			return v == null ? EMPTY : v.getString(data.indexOf(d));
		}
		String l = o.getLabel();
		Dataset v = d.getLabelValue(l);
		return v == null ? l : v.getString();
	}

	private List<LabelOption> getLabelOptions() {
		return data.stream()
				.flatMap(f -> f.getLabelOptions().stream())
				.distinct()
				.map(l -> new LabelOption(l))
				.collect(Collectors.toList());
	}

	private Dataset getLabelValues(int i) {
		if (undefinedAxis[i]) {
			Dataset d = DatasetFactory.createRange(IntegerDataset.class, data.size());
			d.setName(getAxis(i) + "-Axis");
			return d;
		}

		return getLabelDataset(chosenLabels.get(i));
	}

	public Dataset getLabelDataset(LabelOption labelOption) {
		if (labelOption instanceof ExpressionLabelOption) {
			ExpressionLabelOption elo = (ExpressionLabelOption) labelOption;
			Map<String, Object> vars = new LinkedHashMap<>();
			for (LabelOption l : elo.getVariables()) {
				Dataset d = getLabelDataset(l);
				if (d.getSize() > 0) {
					vars.put(l.getName(), d);
				}
			}
			return elo.evaluate(engine, vars);
		}

		Dataset values = labelOption.getValues();
		if (values == null) {
			String label = labelOption.getLabel();
			List<Double> v = data.stream()
					.map(f -> f.getLabelValue(label))
					.filter(Objects::nonNull)
					.filter(d -> InterfaceUtils.isNumerical(d.getClass()))
					.map(Dataset::getDouble)
					.collect(Collectors.toList());
			values = DatasetFactory.createFromList(DoubleDataset.class, v);
			values.setName(labelOption.getName());
			labelOption.setValues(values);
		}
		return values;
	}

	private void processAndPlotValues(boolean updateBins) {
		Dataset x = getLabelValues(0);
		Dataset y = getLabelValues(1);

		if (updateBins) {
			if (undefinedAxis[0]) {
				xBins = null;
				xBinsParameters.clear();
			} else {
				xBins = HistogramBin.calculateQuantization(x);
				xBinsParameters.update(xBins);
				logger.debug("x histogram bins: {}", xBins);
			}
			if (undefinedAxis[1]) {
				yBins = null;
				yBinsParameters.clear();
			} else {
				yBins = HistogramBin.calculateQuantization(y);
				yBinsParameters.update(yBins);
				logger.debug("y histogram bins: {}", xBins);
			}
		}

		if (showHistogram) {
			binAndPlotLabelValues(x, y);
		} else {
			plotLabelValuePositions(x, y);
		}
	}

	private void binAndPlotLabelValues(Dataset x, Dataset y) {
		if (undefinedAxis[1] || yBins.getSteps() == 1) {
			// 1D
			Histogram h = new Histogram(xBins.createBinEdges(true));
			List<Dataset> v = h.value(x);
			try {
				system.reset();
				ILineTrace lt = system.createLineTrace("label values histogram");
				lt.setTraceType(TraceType.STEP_HORIZONTALLY);
				lt.setData(v.get(1), v.get(0));
				system.getSelectedXAxis().setTitle(x.getName());
				system.getSelectedYAxis().setTitle("Occupancy");
				system.addTrace(lt);
				system.autoscaleAxes();
			} catch (Exception e) {
				logger.error("Could not plot label values", e);
			}
		} else {
			Histogram2D h = new Histogram2D(xBins.createBinEdges(true), yBins.createBinEdges(true));
			List<Dataset> v = h.value(x, y);
			Dataset hd = v.get(0).getTransposedView();
			try {
				system.reset();
				system.getSelectedXAxis().setTitle(x.getName());
				system.getSelectedYAxis().setTitle(y.getName());
				system.createPlot2D(hd, null, "label values histogram", null);
			} catch (Exception e) {
				logger.error("Could not plot label values", e);
			}
		}
	}

	private void plotLabelValuePositions(Dataset x, Dataset y) {
		try {
			int l = chosenLabels.stream().mapToInt(c ->  c == null ? 0 : 1).sum();
			system.reset();
			ILineTrace lt = system.createLineTrace("label values");
			lt.setPointStyle(PointStyle.XCROSS);
			lt.setPointSize(6);
			if (l < 2) {
				lt.setTraceType(TraceType.DASH_LINE);
			} else {
				lt.setTraceType(TraceType.POINT);
			}
			lt.setData(x, y);
			system.getSelectedXAxis().setTitle(x.getName());
			system.getSelectedYAxis().setTitle(y.getName());
			system.addTrace(lt);
			system.autoscaleAxes();
		} catch (Exception e) {
			logger.error("Could not plot label values", e);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.DETAILS_ID, "Aggregate", false);
		saveButton = createButton(parent, IDialogConstants.FINISH_ID, "Save", false);
		saveButton.setEnabled(false);

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DETAILS_ID) {
			try {
				aggregateData();
				plotAggregate();
				saveButton.setEnabled(true);
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Error in aggregating data", e.getMessage());
			}
			return;
		} else if (buttonId == IDialogConstants.FINISH_ID) {
			saveAggregate();
		}
		super.buttonPressed(buttonId);
	}

	private void aggregateData() {
		List<IXYData> xy = DataPackageUtils.getXYData(data, true, false);
		if (xy.size() < 2) {
			MessageDialog.openError(getShell(), "Error in aggregating data", "Need more than one set of XY data");
			return;
		}
		xy = DataManipulationUtils.getCompatibleDatasets(xy, null, null, true);
		aggregate = null;
		if (undefinedAxis[0]) {
			// concatenate 1D
			aggregate = verticalStack(xy, xy.size());
			setAxes(aggregate, xy.get(0).getX(), null);
		} else if (undefinedAxis[1]) {
			// interpolate 1D across x label bins
			Dataset l = getLabelDataset(chosenLabels.get(0));
			Dataset tmp = verticalStack(xy); // so axis runs down column
			Dataset x = xBins.createBinEdges(false).getSliceView(new Slice(-1));
			int xSize = x.getSize(); // label x
			int aSize = tmp.getShapeRef()[0]; // axis
			DoubleDataset agg = DatasetFactory.zeros(aSize, xSize);
			SliceND s = new SliceND(agg.getShapeRef());
			for (int i = 0; i < aSize; i++) { // for each axis value
				s.setSlice(0, i, i+1, 1);
				Dataset ia = Maths.interpolate(l, tmp.getSliceView(s).squeeze(), x, null, null);
				agg.setSlice(ia, s);
			}
			aggregate = agg;
			setAxes(aggregate, xy.get(0).getX(), x);
		} else {
			// interpolated 2D across x and y label bins
			Dataset x = xBins.createBinEdges(false).getSliceView(new Slice(-1));
			Dataset y = yBins.createBinEdges(false).getSliceView(new Slice(-1));

			Dataset lx = getLabelDataset(chosenLabels.get(0));
			Dataset ly = getLabelDataset(chosenLabels.get(1));

			List<Dataset> values = new ArrayList<>();
			for (IXYData v : xy) {
				values.add(DatasetUtils.convertToDataset(v.getY()).squeeze());
			}
			aggregate = DelaunayInterpolation.gridInterpolate(lx, ly, values, x, y);
			setAxes(aggregate, xy.get(0).getX(), y, x);
		}
	}

	private void setAxes(Dataset d, IDataset... axes) {
		try {
			int r = d.getRank();
			AxesMetadata am = MetadataFactory.createMetadata(AxesMetadata.class, r);
			for (int i = 0; i < r; i++) {
				ILazyDataset a = i < axes.length ? axes[i] : null;
				if (a != null) {
					a = a.getSliceView();
					a.setName(MetadataPlotUtils.removeSquareBrackets(a.getName()));
				} else {
					a = DatasetFactory.createRange(IntegerDataset.class, d.getShapeRef()[i]);
				}
				try {
					am.setAxis(i, a);
				} catch (Exception e) {
					logger.error("{} cf {}", Arrays.toString(a.getShape()), Arrays.toString(d.getShape()));
				}
			}
			d.addMetadata(am);
		} catch (Exception e) {
			logger.error("Could not set axes", e);
		}
	}

	private static Dataset verticalStack(List<IXYData> list, int width) {
		if (list == null || list.isEmpty()) return null;

		IDataset[] all = new IDataset[width];

		IXYData fxy = list.get(0);
		String fn = fxy.getFileName();
		String dn = fxy.getDatasetName();
		Dataset sum = DatasetFactory.zeros(fxy.getY().getShape());
		int count = 0;
		int w = 0;
		for (IXYData xy : list) {
			Dataset ds = DatasetUtils.convertToDataset(xy.getY()).getSliceView().squeeze();
			if (!fn.equals(xy.getFileName()) || !dn.equals(xy.getDatasetName())) {
				if (count > 1) {
					sum.idivide(count);
				}
				sum.setShape(sum.getSize(), 1);
				count = 1;
				all[w++] = sum;
				sum = DatasetUtils.copy(DoubleDataset.class, ds);
				fn = xy.getFileName();
				dn = xy.getDatasetName();
			} else {
				count++;
				sum.iadd(ds);
			}
		}
		sum.idivide(count);
		sum.setShape(sum.getSize(), 1);
		all[w++] = sum;
		if (w != width) {
			throw new IllegalArgumentException("Could not average XY correctly");
		}

		return DatasetUtils.concatenate(all, 1);
	}

	private static Dataset verticalStack(List<IXYData> list) {
		if (list == null || list.isEmpty()) return null;

		IDataset[] all = new IDataset[list.size()];
		int w = 0;
		for (IXYData file : list) {
			Dataset ds = DatasetUtils.convertToDataset(file.getY()).getSliceView().squeeze();
			ds.setShape(ds.getSize(), 1);
			all[w++] = ds;
		}

		return DatasetUtils.concatenate(all, 1);
	}

	private void plotAggregate() {
		if (aggregate == null) {
			return;
		}

		Dataset d = aggregate;
		int r = d.getRank();
		if (r > 3) {
			Slice[] s = new Slice[r];
			for (int i = 2; i < r; i++) {
				s[i] = new Slice(1);
			}
			d = d.getSliceView(s).squeeze();
		}
		try {
			AxesMetadata am = d.getFirstMetadata(AxesMetadata.class);
			List<IDataset> plotAxes = null;
			system.reset();
			if (am != null) {
				ILazyDataset[] axes = am.getAxes();
				Dataset ay = DatasetUtils.sliceAndConvertLazyDataset(axes[0]).squeeze();
				if (ay != null) {
					system.getSelectedYAxis().setTitle(ay.getName());
				}
				Dataset ax = DatasetUtils.sliceAndConvertLazyDataset(axes[1]).squeeze();
				if (ax != null) {
					system.getSelectedXAxis().setTitle(ax.getName());
				}
				plotAxes = List.of(ax, ay);
			}
			system.createPlot2D(d, plotAxes, "aggregate", null);
			system.setKeepAspect(false);
		} catch (Exception e) {
			logger.error("Could not plot label values", e);
		}
	}

	private void saveAggregate() {
		FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
		dialog.setNewFile(true);
		dialog.setFolderSelector(false);
		if (lastPath != null) {
			File f = new File(lastPath);
			if (!f.isDirectory()) {
				lastPath = f.getParent();
			}
			dialog.setPath(lastPath);
		} else {
			dialog.setPath(System.getProperty("user.home"));
		}
		
		dialog.create();
		if (dialog.open() == Dialog.CANCEL) return;
		lastPath = dialog.getPath();

		boolean success = FileWritingUtils.writeProcessedData(ServiceProvider.getService(INexusFileFactory.class),
				getShell(), getClass().getSimpleName(), lastPath, AGGREGATED_ENTRY, aggregate);

		if (success) {
			IFileController fc = ServiceProvider.getService(IFileController.class);
			FileControllerUtils.loadFile(fc, lastPath);
		}
	}

	@Override
	public boolean close() {
		system.dispose();
		return super.close();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 800);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
