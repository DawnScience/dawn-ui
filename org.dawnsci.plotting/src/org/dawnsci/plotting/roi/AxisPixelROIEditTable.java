package org.dawnsci.plotting.roi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.util.number.DoubleUtils;
import org.dawnsci.common.richbeans.components.cell.FieldComponentCellEditor;
import org.dawnsci.common.richbeans.components.wrappers.FloatSpinnerWrapper;
import org.dawnsci.common.richbeans.components.wrappers.SpinnerWrapper;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a TableViewer that shows ROI information<br>
 * both with real (axis) and pixel values.<br>
 * This table uses JFace data binding to update its content.
 * TODO make it work with all ROIs (only working for RectangularROI currently)
 * @author wqk87977
 *
 */
public class AxisPixelROIEditTable {
	private Logger logger = LoggerFactory.getLogger(AxisPixelROIEditTable.class);

	private Composite parent;
	private TableViewer regionViewer;

	private AxisPixelTableViewModel viewModel;
	private AxisPixelProfileTableViewModel profileViewModel;
	private List<AxisPixelRowDataModel> values;

	private IPlottingSystem plottingSystem;

	private IROI roi;

	private int axisPrecision = 5;
	private int pixelPrecision = 0;

	private boolean isProfile = false;

	/**
	 *
	 * @param parent
	 * @param plottingSystem
	 */
	public AxisPixelROIEditTable(Composite parent, IPlottingSystem plottingSystem) {
		this.parent = parent;
		this.plottingSystem = plottingSystem;
	}

	/**
	 * Method to create the Control
	 */
	public void createControl(){
		// if we listen to the main plottingSystem
		if(!isProfile){
			this.viewModel = new AxisPixelTableViewModel();
			final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			regionViewer = buildAndLayoutTable(table);
			regionViewer.setInput(viewModel.getValues());
		}else{
			this.profileViewModel = new AxisPixelProfileTableViewModel();
			final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			regionViewer = buildAndLayoutTable(table);
			regionViewer.setInput(profileViewModel.getValues());
		}
	}

	private TableViewer buildAndLayoutTable(final Table table) {

		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 2));

		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		viewerColumn.getColumn().setText("Name");
		viewerColumn.getColumn().setWidth(80);
		viewerColumn.setLabelProvider(new AxisPixelLabelProvider(0));
		RegionEditingSupport regionEditor = new RegionEditingSupport(tableViewer, 0);
		viewerColumn.setEditingSupport(regionEditor);

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 1);
		viewerColumn.getColumn().setText("Start");
		viewerColumn.getColumn().setWidth(100);
		viewerColumn.setLabelProvider(new AxisPixelLabelProvider(1));
		regionEditor = new RegionEditingSupport(tableViewer, 1);
		viewerColumn.setEditingSupport(regionEditor);

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 2);
		viewerColumn.getColumn().setText("End");
		viewerColumn.getColumn().setWidth(100);
		viewerColumn.setLabelProvider(new AxisPixelLabelProvider(2));
		regionEditor = new RegionEditingSupport(tableViewer, 2);
		viewerColumn.setEditingSupport(regionEditor);

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 3);
		if(!isProfile)
			viewerColumn.getColumn().setText("Width(X), Height(Y)");
		else
			viewerColumn.getColumn().setText("Width");
		viewerColumn.getColumn().setWidth(100);
		viewerColumn.setLabelProvider(new AxisPixelLabelProvider(3));
		regionEditor = new RegionEditingSupport(tableViewer, 3);
		viewerColumn.setEditingSupport(regionEditor);

		return tableViewer;
	}

	/**
	 * Table viewer label provider
	 *
	 */
	private class AxisPixelLabelProvider extends ColumnLabelProvider {

		private int column;

		public AxisPixelLabelProvider(int column) {
			this.column = column;
		}

		@Override
		public Image getImage(Object element){
			return null;
		}

		@Override
		public String getText(Object element) {
			final AxisPixelRowDataModel model = (AxisPixelRowDataModel)element;
			DecimalFormat pointFormat = new DecimalFormat("##0.###");
			switch(column) {
			case 0:
				return model.getName();
			case 1:
				return pointFormat.format(model.getStart());
			case 2:
				return pointFormat.format(model.getEnd());
			case 3:
				return pointFormat.format(model.getDiff());
			default:
				return "";
			}
		}

		@Override
		public String getToolTipText(Object element) {
			return "";
		}

		@Override
		public void dispose(){
			super.dispose();
		}
	}
	/**
	 * EditingSupport Class
	 *
	 */
	private class RegionEditingSupport extends EditingSupport {

		private int column;

		public RegionEditingSupport(ColumnViewer viewer, int col) {
			super(viewer);
			this.column = col;
		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			AxisPixelRowDataModel model = (AxisPixelRowDataModel) element;
			FieldComponentCellEditor ed = null;
			try {
				ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(),
						                     FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot get FieldComponentCellEditor for "+SpinnerWrapper.class.getName(), e);
				return null;
			}

			final FloatSpinnerWrapper   rb = (FloatSpinnerWrapper)ed.getFieldWidget();

			if(model != null && model.name.endsWith("Pixel")){
				if (rb.getPrecision() < 3)
					rb.setFormat(rb.getWidth(), 0);
			}

			if(model != null && model.name.endsWith("Axis")){
				if (rb.getPrecision() < 3)
					rb.setFormat(rb.getWidth(), 3);
			}

			rb.setMaximum(Double.MAX_VALUE);
			rb.setMinimum(-Double.MAX_VALUE);

			rb.setButtonVisible(false);
			rb.setActive(true);

			((Spinner) rb.getControl())
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								setValue(element, rb.getValue(), false);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
			return ed;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (column==0) return false;
			else return true;
		}

		@Override
		protected Object getValue(Object element) {
			try {
				return getRowValue(element);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		private Object getRowValue(Object element) throws Exception {

			final AxisPixelRowDataModel row = (AxisPixelRowDataModel)element;
			switch (column){
			case 0:
				return row.getName();
			case 1:
				return row.getStart();
			case 2:
				return row.getEnd();
			case 3:
				return row.getDiff();
			default:
				return null;
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				this.setValue(element, value, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
			final AxisPixelRowDataModel row = (AxisPixelRowDataModel) element;

			switch (column){
			case 0:
				row.setName((String)value);
				break;
			case 1:
				row.setStart((Double)value);
				row.setDiff(row.getEnd() - row.getStart());
				break;
			case 2:
				row.setEnd((Double)value);
				// set new diff
				row.setDiff(row.getEnd() - row.getStart());
				break;
			case 3:
				row.setDiff((Double)value);
				// set new end
				row.setEnd(row.getStart() + row.getDiff());
				break;
			default:
				break;
			}
			if (tableRefresh) {
				getViewer().refresh();
			}
			if(!isProfile)
				roi = createRoi(viewModel.getValues(), row.getName());
			else
				roi = createProfileRoi(profileViewModel.getValues());
			setTableValues(roi);
			// set new region
			IRegion region = plottingSystem.getRegions().iterator().next();
			region.setROI(roi);
		}
	}

	/**
	 * Method that creates a ROI using the input of the table viewer
	 * @param rows
	 * @param rowName
	 * @return IROI
	 * @throws Exception
	 */
	private IROI createRoi(List<AxisPixelRowDataModel> rows, String rowName) throws Exception{

		double ptx = 0, pty = 0, ptxEnd = 0, ptyEnd = 0, width = 0, height = 0, angle = 0,
			pixelPtx = 0, pixelPty = 0, pixelEndPtx = 0, pixelEndPty = 0;

		IROI ret = null;
		if (roi == null)
			roi = plottingSystem.getRegions().iterator().next().getROI();
		if (roi instanceof RectangularROI) {
			// if axis rows are being modified
			if (rowName.equals(RowName.XAXIS_ROW) || rowName.equals(RowName.YAXIS_ROW)) {
				AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel) rows.get(0);
				AxisPixelRowDataModel yAxisRow = (AxisPixelRowDataModel) rows.get(1);
				//Convert from Axis to Pixel values
				// We get the axes data to convert from the axis to pixel values
				Collection<ITrace> traces = plottingSystem.getTraces();
				Iterator<ITrace> it = traces.iterator();
				while(it.hasNext()){
					ITrace trace = it.next();
					if(trace instanceof IImageTrace){
						IImageTrace image = (IImageTrace)trace;
						// x axis and width
						ptx = xAxisRow.getStart();
						pty = yAxisRow.getStart();
						ptxEnd =xAxisRow.getEnd();
						ptyEnd = yAxisRow.getEnd();
						double[] newStartPoint = image.getPointInImageCoordinates(new double[]{ptx, pty});
						pixelPtx = newStartPoint[0];
						pixelPty = newStartPoint[1];
						double[] newEndPoint = image.getPointInImageCoordinates(new double[]{ptxEnd, ptyEnd});
						pixelEndPtx = newEndPoint[0];
						pixelEndPty = newEndPoint[1];
						width = pixelEndPtx - pixelPtx;
						height = pixelEndPty - pixelPty;
						ret = new RectangularROI(pixelPtx, pixelPty, width, height, angle);
					}
				}
			}
			// if Pixel rows are being modified
			else if (rowName.equals(RowName.XPIXEL_ROW) || rowName.equals(RowName.YPIXEL_ROW)) {
				AxisPixelRowDataModel xPixelRow = (AxisPixelRowDataModel) rows.get(2);
				AxisPixelRowDataModel yPixelRow = (AxisPixelRowDataModel) rows.get(3);
				// x axis and width
				ptx = xPixelRow.getStart();
				pty = yPixelRow.getStart();
				ptxEnd =xPixelRow.getEnd();
				ptyEnd = yPixelRow.getEnd();
				width = ptxEnd - ptx;
				height = ptyEnd - pty;
				ret = new RectangularROI(ptx, pty, width, height, angle);
			}
		}
		return ret;
	}

	/**
	 * Method that creates a ROI using the x row a profile table viewer
	 * @param rows
	 * @return IROI
	 * @throws Exception
	 */
	private IROI createProfileRoi(List<AxisPixelRowDataModel> rows) throws Exception{
		double ptx = 0, pty = 0, ptxEnd = 0, width = 0, height = 0, angle = 0;
		IROI ret = null;
		if (roi == null)
			roi = plottingSystem.getRegions().iterator().next().getROI();
		if (roi instanceof RectangularROI) {
			//Convert from Axis to Pixel values
			AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel) rows.get(0);
			ptx = xAxisRow.getStart();
			ptxEnd = xAxisRow.getEnd();
			width = ptxEnd - ptx;
			pty = roi.getPointY();
			height = ((RectangularROI) roi).getEndPoint()[1] - pty;
			RectangularROI rr = new RectangularROI(ptx, pty, width, height, angle);
			ret = rr;
		}
		return ret;
	}

	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	public double[] getAxisPoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getValueAxisLocation(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	/**
	 * get point in image coords
	 * @param coords
	 * @return
	 */
	public double[] getImagePoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getAxisLocationValue(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) regionViewer.getSelection();
	}

	public void setSelection(IStructuredSelection selection) {
		regionViewer.setSelection(selection, true);
	}

	/**
	 * Method that returns the TableViewer
	 * @return TableViewer
	 */
	public TableViewer getTableViewer(){
		return regionViewer;
	}

	/**
	 * Method used to set the {@link}AxisPixelROIEditTable<br>
	 * to listen to the main plottingSystem or to a profile plottingSystems<br>
	 * By default this class will create a table viewer used to listen to a<br>
	 * main plottingSystem.
	 *
	 * @param isProfileTable
	 */
	public void setIsProfileTable(boolean isProfileTable){
		this.isProfile  = isProfileTable;
	}

	/**
	 * Method that sets the table viewer values given a Region of Interest
	 * @param region
	 */
	public void setTableValues(IROI region) {
		roi = region;

		RectangularROI rroi = (RectangularROI)roi;
		if(roi == null) return;
		double xStart = roi.getPointX();
		double yStart = roi.getPointY();
		double xEnd = rroi.getEndPoint()[0];
		double yEnd = rroi.getEndPoint()[1];

		if(!isProfile){
			values = viewModel.getValues();
			AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel)values.get(0);
			AxisPixelRowDataModel yAxisRow = (AxisPixelRowDataModel)values.get(1);
			AxisPixelRowDataModel xPixelRow = (AxisPixelRowDataModel)values.get(2);
			AxisPixelRowDataModel yPixelRow = (AxisPixelRowDataModel)values.get(3);
			try{
				// We get the axes data to convert from the pixel to axis values
				IImageTrace image = null;
				ITrace trace = plottingSystem.getTraces().iterator().next();
				if(trace instanceof IImageTrace){
					image = (IImageTrace)trace;
					double[] startPoint = image.getPointInAxisCoordinates(new double[]{xStart, yStart});
					double[] endPoint = image.getPointInAxisCoordinates(new double[]{xEnd, yEnd});

					xAxisRow.setStart(DoubleUtils.roundDouble(startPoint[0], axisPrecision));
					xAxisRow.setEnd(DoubleUtils.roundDouble(endPoint[0], axisPrecision));
					xAxisRow.setDiff(DoubleUtils.roundDouble(endPoint[0]-startPoint[0], axisPrecision));
					yAxisRow.setStart(DoubleUtils.roundDouble(startPoint[1], axisPrecision));
					yAxisRow.setEnd(DoubleUtils.roundDouble(endPoint[1], axisPrecision));
					yAxisRow.setDiff(DoubleUtils.roundDouble(endPoint[1]-startPoint[1], axisPrecision));
				}

				xPixelRow.setStart(DoubleUtils.roundDouble(xStart, pixelPrecision));
				xPixelRow.setEnd(DoubleUtils.roundDouble(xEnd, pixelPrecision));
				xPixelRow.setDiff(DoubleUtils.roundDouble(xEnd-xStart, pixelPrecision));
				yPixelRow.setStart(DoubleUtils.roundDouble(yStart, pixelPrecision));
				yPixelRow.setEnd(DoubleUtils.roundDouble(yEnd, pixelPrecision));
				yPixelRow.setDiff(DoubleUtils.roundDouble(yEnd-yStart, pixelPrecision));
			} catch (ArrayIndexOutOfBoundsException ae) {
				// do nothing
			} catch (Exception e) {
				logger .debug("Error while updating the AxisPixelEditTable:"+ e);
			}
		} else {
			values = profileViewModel.getValues();
			AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel)values.get(0);
			xAxisRow.setStart(DoubleUtils.roundDouble(xStart, axisPrecision));
			xAxisRow.setEnd(DoubleUtils.roundDouble(xEnd, axisPrecision));
			xAxisRow.setDiff(DoubleUtils.roundDouble(xEnd-xStart, axisPrecision));
		}
		if(regionViewer.isCellEditorActive())
			return;
		regionViewer.refresh();
	}

	/**
	 * Methods that returns the current ROI
	 * @return ROIBase
	 */
	public IROI getROI(){
		return roi;
	}

	public void setValues(List<AxisPixelRowDataModel> values) {
		this.values = values;
	}

	/**
	 * Method to add a SelectionChangedListener to the TableViewer
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		regionViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Method to remove a SelectionChangedListener from the TableViewer
	 * @param listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		regionViewer.removeSelectionChangedListener(listener);
	}

	private class RowName {
		public static final String XAXIS_ROW = "X Axis";
		public static final String YAXIS_ROW = "Y Axis";
		public static final String XPIXEL_ROW = "X Pixel";
		public static final String YPIXEL_ROW = "Y Pixel";
	}
	/**
	 * View Model of  AxisPixel Table: main one
	 *
	 */
	private class AxisPixelTableViewModel {
		private List<AxisPixelRowDataModel> rows = new ArrayList<AxisPixelRowDataModel>();

		private AxisPixelRowDataModel xAxisRow;
		private AxisPixelRowDataModel yAxisRow;
		private AxisPixelRowDataModel xPixelRow;
		private AxisPixelRowDataModel yPixelRow;

		final private String description0 = "X-Axis values (Not yet editable)";
		final private String description1 = "Y-Axis values (Not yet editable)";
		final private String description2 = "X values as pixels (Editable)";
		final private String description3 = "X values as pixels (Editable)";

		{
			xAxisRow = new AxisPixelRowDataModel(new String(RowName.XAXIS_ROW), new Double(0), new Double(0), new Double(0), description0);
			yAxisRow = new AxisPixelRowDataModel(new String(RowName.YAXIS_ROW), new Double(0), new Double(0), new Double(0), description1);
			xPixelRow = new AxisPixelRowDataModel(new String(RowName.XPIXEL_ROW), new Double(0), new Double(0), new Double(0), description2);
			yPixelRow = new AxisPixelRowDataModel(new String(RowName.YPIXEL_ROW), new Double(0), new Double(0), new Double(0), description3);

			rows.add(xAxisRow);
			rows.add(yAxisRow);
			rows.add(xPixelRow);
			rows.add(yPixelRow);
		}

		public List<AxisPixelRowDataModel> getValues() {
			return rows;
		}
	}

	/**
	 * View Model of  AxisPixel Table: profile one
	 *
	 */
	private class AxisPixelProfileTableViewModel {

		private List<AxisPixelRowDataModel> rows = new ArrayList<AxisPixelRowDataModel>();

		final private String description = "X-Axis values";
		private AxisPixelRowDataModel xAxisRow;

		{
			xAxisRow = new AxisPixelRowDataModel(new String(RowName.XAXIS_ROW), new Double(0), new Double(0), new Double(0), description);
			rows.add(xAxisRow);
		}

		public List<AxisPixelRowDataModel> getValues() {
			return rows;
		}
	}

	/**
	 * Model object for a Region Of Interest row used in an AxisPixel Table
	 * @author wqk87977
	 *
	 */
	private class AxisPixelRowDataModel extends AbstractOperationModel {
		private String name;
		private double start;
		private double end;
		private double diff;
		private String description;

		public AxisPixelRowDataModel(String name, double start, double end, double diff, String description) {
			this.name = name;
			this.start = start;
			this.end = end;
			this.diff = diff;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

		public double getDiff() {
			return diff;
		}

		@SuppressWarnings("unused")
		/**
		 * TODO add a description in a tool tip<br>
		 * but this can only be done by providing our own LabelProvider<br>
		 * which is currently done by the ViewerSupport.bind mechanism.<br>
		 *
		 * @return string
		 */
		public String getDescription(){
			return description;
		}

		public void setName(String name){
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, this.name);
		}

		public void setStart(double start) {
			double oldValue = this.start;
			this.start = start;
			firePropertyChange("start", oldValue, this.start);
		}

		public void setEnd(double end) {
			double oldValue = this.end;
			this.end = end;
			firePropertyChange("end", oldValue, this.end);
		}

		public void setDiff(double diff) {
			double oldValue = this.diff;
			this.diff = diff;
			firePropertyChange("diff", oldValue, this.diff);
		}
	}

	public void cancelEditing() {
		regionViewer.cancelEditing();
	}
}