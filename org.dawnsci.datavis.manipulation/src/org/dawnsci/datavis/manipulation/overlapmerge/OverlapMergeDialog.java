package org.dawnsci.datavis.manipulation.overlapmerge;

import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.PlotDataConversionWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.common.widgets.NumberText;
import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.api.IXYData;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
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

	private NumberText regionFraction;

	private NormOption normOpt;

	private double[] ends;

	private OverlapMerge overlapMerge;

	private boolean ignoreNextChange;

	private NumberText regionStart;

	private NumberText regionStop;

	private Dataset[] mergedData;

	private String savePath;

	private static double mergeFraction = 0.95;
	private static final String X_AXIS_REGION = "X overlap merge region";
	private static final String OM_TRACE_NAME = "Overlap merged";
	private static final String OM_DATASET_NAME = "merged";
	private static final int DECIMAL_PLACES = 6;

	public OverlapMergeDialog(Shell parentShell, List<IXYData> xyData) {
		super(parentShell);

		this.xyData = xyData;
		IXYData firstXY = xyData.get(0);
		IXYData secondXY = xyData.get(1);
		Dataset xA = DatasetUtils.convertToDataset(firstXY.getX());
		xA.setName(MetadataPlotUtils.removeSquareBrackets(xA.getName()));
		Dataset xB = DatasetUtils.convertToDataset(secondXY.getX());
		xB.setName(MetadataPlotUtils.removeSquareBrackets(xB.getName()));
		overlapMerge = new OverlapMerge(xA, xB);
		ends = overlapMerge.getOverlap();
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
		Group olGroup = gFactory.create(container);
		olGroup.setLayout(gLFactory.create());
		olGroup.setText("Overlap");

		Composite finder = new Composite(olGroup, SWT.NONE);
		RowLayout rLayout = rlFactory.create();
		rLayout.center = true; // bug in JFace 3.27 does not copy over center field
		finder.setLayout(rLayout);
		WidgetFactory.label(SWT.NONE).create(finder).setText("Fraction:");
		regionFraction = new NumberText(finder, SWT.BORDER);
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
		fixFirstButton.setToolTipText("Normalize to 1st curve");
		fixFirstButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
			e -> {
				normOpt = OverlapMerge.NormOption.FIRST;
				mergeAndPlot();
			}));
		Button fixSecondButton = rbFactory.create(fitGroup);
		fixSecondButton.setText("2nd");
		fixSecondButton.setToolTipText("Normalize to 2nd curve");
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
			throw new DatasetException("Curves do not overlap");
		}
	}

	private void mergeAndPlot() {
		IXYData firstXY = xyData.get(0);
		IXYData secondXY = xyData.get(1);
		Dataset yA = DatasetUtils.convertToDataset(firstXY.getY());
		Dataset yB = DatasetUtils.convertToDataset(secondXY.getY());

		try {
			ITrace trace = system.getTrace(OM_TRACE_NAME);
			ILineTrace lTrace;
			if (trace instanceof ILineTrace lineTrace) {
				lTrace = lineTrace;
			} else {
				lTrace = system.createLineTrace(OM_TRACE_NAME);
				system.addTrace(lTrace);
			}
			lTrace.setVisible(true);
			lTrace.setTraceType(TraceType.DASH_LINE);
			mergedData = overlapMerge.mergeOverlap(yA, yB, normOpt);
			mergedData[1].setName(OM_DATASET_NAME);
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
		return new Point(300, 360);
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
