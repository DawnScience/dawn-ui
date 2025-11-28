package org.dawnsci.datavis.manipulation.overlapmerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.PlotDataConversionWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.common.widgets.NumberText;
import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.api.IXYData;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.eclipse.january.metadata.OriginMetadata;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.GroupFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.fitting.OverlapMerge;
import uk.ac.diamond.scisoft.analysis.fitting.OverlapMerge.NormOption;

public class OverlapMergeDialog extends Dialog implements IROIListener {
	private static final Logger logger = LoggerFactory.getLogger(OverlapMergeDialog.class);

	private List<IXYData> xyData;
	private IPlottingSystem<?> system;
	private IRegion region;

	private NormOption normOpt;

	private double[] ends;

	private OverlapMerge overlapMerge;

	private boolean ignoreNextChange;

	private NumberText regionStart;

	private NumberText regionStop;

	private String savePath;

	private Dataset[] allYA;

	private Dataset[] allYB;

	private boolean plotAll;
	private int pairCount;

	private static double mergeFraction = 0.95;
	private static final String X_AXIS_REGION = "X overlap merge region";
	private static final String OM_TRACE_NAME = "Overlap merged";
	private static final String OM_DATASET_NAME = "merged";
	private static final int DECIMAL_PLACES = 6;

	public OverlapMergeDialog(Shell parentShell, List<IXYData> xyData) {
		super(parentShell);

		this.xyData = xyData;
		IXYData xyA = xyData.get(0);
		IXYData xyB = xyData.get(1);
		Dataset xA = DatasetUtils.convertToDataset(xyA.getX());
		int axisSliceCount = getSliceCount(xA.getFirstMetadata(OriginMetadata.class));
		if (axisSliceCount != 1) {
			throw new IllegalArgumentException(String.format("First x-axis must be a single slice (not %d)", axisSliceCount));
		}
		xA.setName(MetadataPlotUtils.removeSquareBrackets(xA.getName()));
		Dataset xB = DatasetUtils.convertToDataset(xyB.getX());
		axisSliceCount = getSliceCount(xB.getFirstMetadata(OriginMetadata.class));
		if (axisSliceCount != 1) {
			throw new IllegalArgumentException(String.format("Second x-axis must be a single slice (not %d)", axisSliceCount));
		}
		xB.setName(MetadataPlotUtils.removeSquareBrackets(xB.getName()));

		pairCount = 1;
		OriginMetadata omdYA = xyA.getY().getFirstMetadata(OriginMetadata.class);
		ILazyDataset allA = omdYA.getParent();
		if (allA.getRank() > 1) {
			pairCount = getSliceCount(omdYA);
		}

		OriginMetadata omdYB = xyB.getY().getFirstMetadata(OriginMetadata.class);
		ILazyDataset allB = omdYB.getParent();
		if (allB.getRank() > 1) {
			int sliceCount = getSliceCount(omdYB);
			if (pairCount != sliceCount) {
				logger.warn("Number of lines ({}) from {}#{} does not match ({}) from {}#{}",
						pairCount, sliceCount,
						omdYA.getFilePath(), omdYA.getDatasetName(),
						omdYB.getFilePath(), omdYB.getDatasetName());
				pairCount = 1;
			}
		}

		allYA = new Dataset[pairCount];
		allYB = new Dataset[pairCount];
		getSlicedData(omdYA, xyA, allA, allYA);
		getSlicedData(omdYB, xyB, allB, allYB);
		plotAll = false;

		overlapMerge = new OverlapMerge(xA, xB);
		ends = overlapMerge.getOverlap();
	}

	/**
	 * @param omd
	 * @return number of slices in shape
	 */
	private int getSliceCount(OriginMetadata omd) {
		Slice[] oSlice = omd.getSliceInOutput();
		int[] shape = omd.getParent().getShape();
		int slicedCount = 1;
		for (int i = 0; i < oSlice.length; i++) {
			Slice s = oSlice[i];
			int l = shape[i];
			if (l != 1 && s.getNumSteps() == 1) {
				int step = s.getStep();
				Slice nSlice = new Slice(s.getStart(), null, step);
				nSlice.setLength(l);
				slicedCount *= nSlice.getNumSteps();
			}
		}
		return slicedCount;
	}

	private SliceNDIterator getSliceIterator(OriginMetadata omd) {
		Slice[] oSlice = omd.getSliceInOutput();
		int[] shape = omd.getParent().getShape();
		List<Integer> dataDims = new ArrayList<>();
		Slice[] nSlice = new Slice[oSlice.length];
		for (int i = 0; i < oSlice.length; i++) {
			Slice s = oSlice[i];
			if (s.getNumSteps() != 1) {
				nSlice[i] = s.clone();
				dataDims.add(i);
			}
		}
		int[] ddims = dataDims.stream().mapToInt(Integer::intValue).toArray();
		return new SliceNDIterator(new SliceND(shape, nSlice), ddims);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.MODELESS | SWT.DIALOG_TRIM);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Merge overlapping plots");
	}

	private void updateROI(double value, boolean start) {
		IROI roi = region.getROI();
		if (roi instanceof XAxisBoxROI aROI) {
			if (start) {
				value -= aROI.getPointX();
				if (value != 0) {
					aROI = aROI.copy();
					aROI.setPointKeepEndPoint(new double[] {value, 0}, true, false);
					region.setROI(aROI);
				}
			} else {
				if (value != aROI.getEndPoint()[0]) {
					aROI = aROI.copy();
					aROI.setEndPoint(new double[] {value, aROI.getEndPoint()[1]});
					region.setROI(aROI);
				}
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayoutFactory gLFactory = GridLayoutFactory.swtDefaults();
		GridLayout gLayout = gLFactory.create();
		container.setLayout(gLayout);

		RowLayoutFactory rlFactory = RowLayoutFactory.swtDefaults();
		GroupFactory gFactory = WidgetFactory.group(SWT.NONE);
		Group dataGroup = gFactory.create(container);
		dataGroup.setLayout(gLFactory.create());
		dataGroup.setText("Data");
		Composite dComp = new Composite(dataGroup, SWT.NONE);
		RowLayout dLayout = rlFactory.create();
		dLayout.center = true;
		dComp.setLayout(dLayout);
		Label pairCountLabel = WidgetFactory.label(SWT.NONE).create(dComp);
		pairCountLabel.setText(String.format("Pairs: %d ", pairCount));
		pairCountLabel.setToolTipText("Number of pairs of lines available");
		Button useAllPairs = WidgetFactory.button(SWT.PUSH).create(dComp);
		useAllPairs.setText("Plot all");
		useAllPairs.setToolTipText("Click to plot all pairs of lines");
		useAllPairs.setEnabled(pairCount > 1);
		useAllPairs.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			if (!plotAll) {
				plotAll = true;
				plotAllPairs();
			}
		}));

		Group olGroup = gFactory.create(container);
		olGroup.setLayout(gLFactory.create());
		olGroup.setText("Overlap");

		Composite finder = new Composite(olGroup, SWT.NONE);
		RowLayout rLayout = rlFactory.create();
		rLayout.center = true; // bug in JFace 3.27 does not copy over center field
		finder.setLayout(rLayout);
		WidgetFactory.label(SWT.NONE).create(finder).setText("Fraction:");
		NumberText regionFraction = new NumberText(finder, SWT.BORDER);
		regionFraction.setDecimalPlaces(2);
		regionFraction.setToolTipText("Fraction of overlap to auto-select - must be in (0,1)");
		regionFraction.setValue(mergeFraction);
		regionFraction.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> {
			double value = regionFraction.getDouble();
			if (value > 0 && value < 1) {
				if (mergeFraction != value) {
					mergeFraction = value;
					if (autoSelectRegion(true)) {
						mergeAndPlot();
					}
				}
			} else {
				// reset
				regionFraction.setValue(mergeFraction);
			}
		}));
		WidgetFactory.label(SWT.NONE).create(finder).setText(" ");
		Button regionAutoFind = WidgetFactory.button(SWT.PUSH).create(finder);
		regionAutoFind.setText("Find");
		regionAutoFind.setToolTipText("Update region");
		regionAutoFind.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			if (autoSelectRegion(true)) {
				mergeAndPlot();
			}
		}));

		Composite manual = new Composite(olGroup, SWT.NONE);
		manual.setLayout(rLayout);
		WidgetFactory.label(SWT.NONE).create(manual).setText(" Start:");
		regionStart = new NumberText(manual, SWT.BORDER);
		regionStart.setDecimalPlaces(DECIMAL_PLACES);
		regionStart.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> {
			double value = regionStart.getDouble();
			if (value < ends[0]) {
				value = ends[0];
				regionStart.setValue(value);
			}
			ignoreNextChange = true;
			updateROI(value, true);
			if (overlapMerge.setStart(value)) {
				mergeAndPlot();
			}
		}));
		WidgetFactory.label(SWT.NONE).create(manual).setText(" Stop:");
		regionStop = new NumberText(manual, SWT.BORDER);
		regionStop.setDecimalPlaces(DECIMAL_PLACES);
		regionStop.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> {
			double value = regionStop.getDouble();
			if (value > ends[1]) {
				value = ends[1];
				regionStop.setValue(value);
			}
			ignoreNextChange = true;
			updateROI(value, false);
			if (overlapMerge.setStop(value)) {
				mergeAndPlot();
			}
		}));

		Group fitGroup = gFactory.create(container);
		fitGroup.setLayout(rlFactory.justify(true).create());
		fitGroup.setText("Normalization option");
		ButtonFactory rbFactory = WidgetFactory.button(SWT.RADIO);

		normOpt = OverlapMerge.NormOption.FIRST;
		Button fixFirstButton = rbFactory.create(fitGroup);
		fixFirstButton.setText("1st");
		fixFirstButton.setSelection(true);
		fixFirstButton.setToolTipText("Normalize to 1st line");
		fixFirstButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				normOpt = OverlapMerge.NormOption.FIRST;
				mergeAndPlot();
			}));
		Button fixSecondButton = rbFactory.create(fitGroup);
		fixSecondButton.setText("2nd");
		fixSecondButton.setToolTipText("Normalize to 2nd line");
		fixSecondButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				normOpt = OverlapMerge.NormOption.SECOND;
				mergeAndPlot();
			}));
		Button noFixButton = rbFactory.create(fitGroup);
		noFixButton.setText("None");
		noFixButton.setToolTipText("Do not normalize");
		noFixButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				normOpt = OverlapMerge.NormOption.NEITHER;
				mergeAndPlot();
			}));

		Group exportGroup = gFactory.create(container);
		exportGroup.setLayout(rlFactory.justify(true).create());
		exportGroup.setText("Export");
		Button exportTextButton = WidgetFactory.button(SWT.PUSH).create(exportGroup);
		exportTextButton.setText("dat/csv");
		exportTextButton.setToolTipText("Export to a text file with values separated by tabs (.dat) or commas (.csv)");
		exportTextButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				saveTextData();
			}));
		Button exportNeXusButton = WidgetFactory.button(SWT.PUSH).create(exportGroup);
		exportNeXusButton.setText("nxs");
		exportNeXusButton.setToolTipText("Export to a NeXus/HDF5 file");
		exportNeXusButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				saveNeXusData();
			}));

		if (autoSelectRegion(false)) {
			mergeAndPlot();
			IROI roi = region.getROI();
			if (roi instanceof XAxisBoxROI aROI) {
				regionStart.setValue(aROI.getPointX());
				regionStop.setValue(aROI.getEndPoint()[0]);
			}
		}
		return container;
	}

	private void saveNeXusData() {
		IWizard wiz;
		try {
			wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
			if (wiz instanceof PersistenceExportWizard eWiz) {
				eWiz.setPlottingSystem(system);
			}
			
			WizardDialog wd = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
			wd.setTitle(wiz.getWindowTitle());
			wd.open();
		} catch (Exception e) {
			logger.error("Could not save data as text", e);
		}
	}

	private void saveTextData() {
		try {
			IWizard wiz = EclipseUtils.openWizard(PlotDataConversionWizard.ID, false);
			if (wiz instanceof PlotDataConversionWizard pWiz) {
				pWiz.setFilePath(savePath);
				pWiz.setPlottingSystem(system);
			}

			WizardDialog wd = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
			wd.setTitle(wiz.getWindowTitle());
			wd.open();
			if (wiz instanceof PlotDataConversionWizard pWiz) {
				savePath = pWiz.getFilePath();
			}
		} catch (Exception e) {
			logger.error("Could not save data as NeXus", e);
		}
	}

	private IPlottingSystem<?> getPlottingSystem() {
		if (system == null) {
			try {
				String id = DataVisConstants.PLOT_ID.substring(DataVisConstants.PLOT_ID.lastIndexOf('.')+1);
				system = ServiceProvider.getService(IPlottingService.class).getPlottingSystem(id);
			} catch (Exception e) {
				logger.error("Error get DataVis plotting system:", e);
			}
		}
		return system;
	}

	private boolean regionUpdate() {
		boolean rescale = false;
		if (getPlottingSystem() == null) {
			return rescale;
		}

		Collection<IRegion> aRegions = system.getRegions(RegionType.XAXIS);
		if (aRegions != null && !aRegions.isEmpty()) {
			region = aRegions.iterator().next();
			if (region.getName().equals(X_AXIS_REGION)) {
				region.setVisible(true);
			}
		} else {
			try {
				region = system.createRegion(X_AXIS_REGION, RegionType.XAXIS);
				system.addRegion(region);
				rescale = true;
			} catch (Exception e) {
				logger.error("Could not create an X axis region", e);
			}
		}
		if (region != null) {
			region.addROIListener(this);
		}
		return rescale;
	}

	private boolean autoSelectRegion(boolean force) {
		boolean rescale = regionUpdate();
		if (region == null) {
			return false;
		}
		if (rescale || force) {
			double mid = 0.5 * (ends[0] + ends[1]);
			double halfWidth = 0.5 * (ends[1] - ends[0]);
			halfWidth *= mergeFraction;
			XAxisBoxROI roi = new XAxisBoxROI();
			roi.setPoint(mid - halfWidth, 0);
			roi.setLengths(halfWidth * 2, 10);
			region.setROI(roi);
		}
		return true;
	}

	private boolean trimROI(XAxisBoxROI roi) {
		double roiEnd = roi.getEndPoint()[0];
		boolean trimmed = false;
		if (ends[0] > roi.getPointX()) {
			roi.setPoint(ends[0], 0);
			trimmed = true;
		}
		if (roiEnd > ends[1]) {
			roi.setEndPoint(new double[] {ends[1], 10});
			trimmed = true;
		}

		return trimmed;
	}

	public void requireOverlap() throws DatasetException {
		if (!overlapMerge.isOverlapping()) {
			throw new DatasetException("Lines do not overlap");
		}
	}

	private String getName(String fileName) {
		String name = fileName;
		if (name.contains(Node.SEPARATOR)) {
			name = name.substring(name.lastIndexOf(Node.SEPARATOR) + 1);
		}
		return name;
	}

	private void getSlicedData(OriginMetadata omd, IXYData xy, ILazyDataset whole, Dataset[] all) {
		SliceNDIterator it = getSliceIterator(omd);
		String yName = getName(xy.getFileName());

		int i = 0;
		SliceND s = it.getCurrentSlice();
		while (it.hasNext() && i < pairCount) {
			Dataset y = null;
			try {
				y = DatasetUtils.convertToDataset(whole.getSlice(s)).squeeze(true);
				y.setName(String.format("%s[%s]", yName, s));
			} catch (DatasetException e) {
				logger.error("Could not load data from {} for {}", xy.getFileName(), s);
			}
			all[i++] = y;
		}
	}

	private void plotAllPairs() {
		IXYData firstXY = xyData.get(0);
		IXYData secondXY = xyData.get(1);

		// replace first lines' names
		String firstFileName = getName(firstXY.getFileName());
		String secondFileName = getName(secondXY.getFileName());
		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
		for (ITrace t : traces) {
			if (t instanceof ILineTrace lt) {
				String dName = lt.getDataName();
				if (dName != null) {
					if (dName.startsWith(firstFileName)) {
						lt.setName(allYA[0].getName());
					} else if (dName.startsWith(secondFileName)) {
						lt.setName(allYB[0].getName());
					}
				}
			}
		}

		plotSlices(firstXY.getX(), allYA);
		plotSlices(secondXY.getX(), allYB);
		mergeAndPlot();
	}

	private void plotSlices(IDataset x, Dataset[] all) {
		for (Dataset y : all) {
			if (y == null) {
				continue;
			}
			String tName = y.getName();
			ITrace trace = system.getTrace(tName);
			ILineTrace lTrace;
			if (trace instanceof ILineTrace lineTrace) {
				lTrace = lineTrace;
			} else {
				lTrace = system.createLineTrace(tName);
				system.addTrace(lTrace);
			}
			lTrace.setData(x, y);
		}
	}

	private void mergeAndPlot() {
		IXYData firstXY = xyData.get(0);
		IXYData secondXY = xyData.get(1);
		Dataset yA = DatasetUtils.convertToDataset(firstXY.getY());
		Dataset yB = DatasetUtils.convertToDataset(secondXY.getY());

		String suffix = "";
		if (pairCount > 1) {
			suffix = "-0";
		}
		mergeAndPlot(OM_TRACE_NAME + suffix, OM_DATASET_NAME + suffix, yA, yB);
		if (plotAll) {
			for (int i = 1; i < pairCount; i++) {
				Dataset a = allYA[i];
				Dataset b = allYB[i];
				suffix = "-" + i;
				mergeAndPlot(OM_TRACE_NAME + suffix, OM_DATASET_NAME + suffix, a, b);
			}
		}
	}

	private void mergeAndPlot(String tName, String dName, Dataset yA, Dataset yB) {
		try {
			ITrace trace = system.getTrace(tName);
			ILineTrace lTrace;
			if (trace instanceof ILineTrace lineTrace) {
				lTrace = lineTrace;
			} else {
				lTrace = system.createLineTrace(tName);
				system.addTrace(lTrace);
			}
			lTrace.setVisible(true);
			lTrace.setTraceType(TraceType.DASH_LINE);
			Dataset[] mergedData = overlapMerge.mergeOverlap(yA, yB, normOpt);
			mergedData[1].setName(dName);
			lTrace.setData(mergedData[0], mergedData[1]);
			lTrace.setTraceType(TraceType.SOLID_LINE);
		} catch (DatasetException e) {
			logger.error("Could not merge the overlap", e);
		}
	}

	@Override
	public boolean close() {
		if (region != null) {
			region.removeROIListener(this);
			if (region.getName().equals(X_AXIS_REGION)) {
				region.setVisible(false);
			}
		}
		if (getReturnCode() == CANCEL) {
			ITrace trace = system.getTrace(OM_TRACE_NAME);
			if (trace instanceof ILineTrace lineTrace) {
				lineTrace.setVisible(false);
			}
		}
		return super.close();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(300, 440);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		// do nothing
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		if (!ignoreNextChange) {
			IROI roi = evt.getROI();
			if (roi instanceof XAxisBoxROI aROI) {
				aROI = aROI.copy();
				if (trimROI(aROI)) {
					ignoreNextChange = true; // prevent changed loop
					region.setROI(aROI);
				}
				double start = aROI.getPointX();
				regionStart.setValue(start);
				boolean update = overlapMerge.setStart(start);
				double stop = aROI.getEndPoint()[0];
				regionStop.setValue(stop);
				update |= overlapMerge.setStop(stop);
				if (update) {
					mergeAndPlot();
				}
			}
		}

		ignoreNextChange = false;
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// do nothing
	}
}
