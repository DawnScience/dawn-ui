package org.dawnsci.multidimensional.ui.imagecuts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.multidimensional.ui.imagecuts.CutData.CutType;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Composite to display the results of a set of perpendicular cuts through an
 * image
 * 
 */
public class PerpendicularImageCutsComposite extends Composite {

	private static final MathContext PRECISION = new MathContext(6, RoundingMode.HALF_UP);

	private IPlottingSystem<Composite> xProfile;
	private IPlottingSystem<Composite> yProfile;
	private Label sumLabel;
	private TableViewer viewer;
	private Set<CutRegionUpdateListener> listeners = new HashSet<>();

	private PerpendicularCutsRunner runner;
	private PerpendicularCutsListener listener;

	public PerpendicularImageCutsComposite(Composite parent, int style, IPlottingService plotService) throws Exception {
		super(parent, style);
		this.setLayout(new GridLayout());

		xProfile = plotService.createPlottingSystem();
		yProfile = plotService.createPlottingSystem();

		xProfile.createPlotPart(this, "PCutsXProfile", null, PlotType.XY, null);
		xProfile.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		xProfile.setShowLegend(false);
		tightenAxes(xProfile);

		yProfile.createPlotPart(this, "PCutsYProfile", null, PlotType.XY, null);
		yProfile.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		yProfile.setShowLegend(false);
		tightenAxes(yProfile);

		Composite configComposite = new Composite(this, SWT.None);
		configComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		configComposite.setLayout(new GridLayout(2, true));

		Composite tableComposite = new Composite(configComposite, SWT.NONE);
		GridData tableGridData = GridDataFactory.fillDefaults().grab(true, false).create();
		tableComposite.setLayoutData(tableGridData);

		viewer = new TableViewer(tableComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
		viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CutData) element).getLabel();
			}
		});

		name.getColumn().setText("Name");
		name.getColumn().setWidth(50);

		TableViewerColumn value = new TableViewerColumn(viewer, SWT.LEFT);
		value.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return doubleToStringWithPrecision((((CutData) element).getValue()));
			}
		});

		value.getColumn().setText("Value");
		value.getColumn().setWidth(50);
		value.setEditingSupport(new ValueEditingSupport(viewer, true));

		TableViewerColumn delta = new TableViewerColumn(viewer, SWT.LEFT);
		delta.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return doubleToStringWithPrecision((((CutData) element).getDelta()));
			}
		});

		delta.getColumn().setText("Delta");
		delta.getColumn().setWidth(50);
		delta.setEditingSupport(new ValueEditingSupport(viewer, false));

		TableColumnLayout columnLayout = new TableColumnLayout();
		columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(50, 50));
		columnLayout.setColumnData(value.getColumn(), new ColumnWeightData(50, 50));
		columnLayout.setColumnData(delta.getColumn(), new ColumnWeightData(50, 50));

		tableComposite.setLayout(columnLayout);

		int itemCount = 2;
		int itemHeight = viewer.getTable().getItemHeight()+2;
		int headerHeight = viewer.getTable().getHeaderHeight()+2;
		int h = (1 + itemCount) * itemHeight + headerHeight;
		tableGridData.minimumHeight = h;
		tableGridData.heightHint = h;

		tableComposite.getParent().layout(true, true);

		Group g = new Group(configComposite, SWT.NONE);
		g.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		g.setText("Intersection Sum");
		g.setLayout(new FillLayout());

		sumLabel = new Label(g, SWT.NONE);

		FontData[] fontData = sumLabel.getFont().getFontData();
		for (int i = 0; i < fontData.length; ++i)
			fontData[i].setHeight(20);

		final Font newFont = new Font(getDisplay(), fontData);
		sumLabel.setFont(newFont);

		// Since you created the font, you must dispose it
		sumLabel.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				newFont.dispose();
			}

		});

		runner = new PerpendicularCutsRunner();

		listener = new PerpendicularCutsListener() {

			@Override
			public void cutProcessed(IDataset xCut, IDataset yCut, double intersectionSum, CutData[] data) {
				ILineTrace xt = MetadataPlotUtils.buildLineTrace(xCut, xProfile);
				ILineTrace yt = MetadataPlotUtils.buildLineTrace(yCut, yProfile);

				Display.getDefault().syncExec(() -> {
					if (!viewer.isCellEditorActive()) {
						viewer.setInput(data);
					}
					sumLabel.setText(doubleToStringWithPrecision(intersectionSum));
					xProfile.clear();
					yProfile.clear();
					xProfile.addTrace(xt);
					yProfile.addTrace(yt);
					xProfile.getSelectedXAxis().setTitle(data[1].getLabel());
					yProfile.getSelectedXAxis().setTitle(data[0].getLabel());
					xProfile.repaint();
					yProfile.repaint();
				});

			}
		};

		runner.addListener(listener);
	}

	private void tightenAxes(IPlottingSystem<Composite> s) {
		List<IAxis> axes = s.getAxes();
		if (axes != null) {
			for (IAxis axis : axes) {
				if (axis != null) {
					axis.setAxisAutoscaleTight(true);
				}
			}
		}
	}

	@Override
	public void dispose() {
		xProfile.dispose();
		yProfile.dispose();
	}

	public void addListener(CutRegionUpdateListener l) {
		listeners.add(l);
	}

	public void removeListener(CutRegionUpdateListener l) {
		listeners.remove(l);
	}

	private void fireListeners(double value, double delta, CutType type) {
		listeners.stream().forEach(l -> l.updateRequested(value, delta, type));
	}

	public void update(IDataset data, IDataset xaxis, IDataset yaxis, RectangularROI xROI, RectangularROI yROI, AdditionalCutDimension d) {
		runner.runAsync(data, xaxis, yaxis, xROI, yROI, d);
	}

	private String doubleToStringWithPrecision(double d) {
		
		if (Double.isNaN(d)) {
			return Double.toString(Double.NaN);
		}
		
		BigDecimal bd = BigDecimal.valueOf(d).round(PRECISION).stripTrailingZeros();
		// stop 100 going to 1.0E2
		if (bd.precision() >= 1 && bd.precision() < PRECISION.getPrecision() && bd.scale() < 0
				&& bd.scale() > (-1 * PRECISION.getPrecision())) {
			bd = bd.setScale(0);
		}
		return bd.toString();
	}

	public class ValueEditingSupport extends EditingSupport {

		private TextCellEditor cellEditor;
		private boolean value = true;

		public ValueEditingSupport(ColumnViewer viewer, boolean value) {
			super(viewer);
			this.value = value;
			cellEditor = new TextCellEditor((Composite) getViewer().getControl());
		}

		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		protected Object getValue(Object element) {

			if (element instanceof CutData) {
				return doubleToStringWithPrecision(
						value ? ((CutData) element).getValue() : ((CutData) element).getDelta());
			}

			return "";
		}

		protected void setValue(Object element, Object value) {
			if (element instanceof CutData) {
				CutData d = (CutData) element;

				double valueString;
				try {
					valueString = Double.parseDouble(value.toString());
				} catch (Exception e) {
					return;
				}
				
				
				if (this.value) {
					fireListeners(valueString, d.getDelta(), d.getType());
				} else {
					fireListeners(d.getValue(), valueString, d.getType());
				}
			}
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
	}
}
