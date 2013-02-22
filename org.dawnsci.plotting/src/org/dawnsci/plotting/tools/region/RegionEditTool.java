package org.dawnsci.plotting.tools.region;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Spinner;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.richbeans.components.cell.FieldComponentCellEditor;
import uk.ac.gda.richbeans.components.wrappers.FloatSpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;

/**
 * 
 * @author fcp94556
 *
 */
public class RegionEditTool extends AbstractRegionTableTool {


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(this, 0));
		RegionEditingSupport regionEditor = new RegionEditingSupport(viewer, 0);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Start point x");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 1));
		regionEditor = new RegionEditingSupport(viewer, 1);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Start point y");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 2));
		regionEditor = new RegionEditingSupport(viewer, 2);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("End Point x");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 3));
		regionEditor = new RegionEditingSupport(viewer, 3);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("End point y");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 4));
		regionEditor = new RegionEditingSupport(viewer, 4);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Max Intensity");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 5));
		regionEditor = new RegionEditingSupport(viewer, 5);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 6);
		var.getColumn().setText("Sum");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 6));
		regionEditor = new RegionEditingSupport(viewer, 6);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 7);
		var.getColumn().setText("Active");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 7));
		regionEditor = new RegionEditingSupport(viewer, 7);
		var.setEditingSupport(regionEditor);
	}
	
	
	protected void createNewRegion() {
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
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
			CellEditor ed = null;
			
			if(column > 0 && column < 7){
				try {
					ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(), 
							                     FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
				} catch (ClassNotFoundException e) {
					logger.error("Cannot get FieldComponentCellEditor for "+SpinnerWrapper.class.getName(), e);
					return null;
				}
				
				final FloatSpinnerWrapper   rb = (FloatSpinnerWrapper)((FieldComponentCellEditor)ed).getFieldWidget();
				if (rb.getPrecision() < 3)
					rb.setFormat(rb.getWidth(), 0);
				
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
									logger.debug("Error while setting table value");
									e1.printStackTrace();
								}
								
							}
						});	
				return ed;
			} else if(column == 0){
				ed = new TextCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
				
				return ed;
			} else if(column == 7){
				ed = new CheckboxCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
				return ed;
			}
			return null;
			
		}

		@Override
		protected boolean canEdit(Object element) {
			if (column == 5 || column == 6) return false;
			else return true;
		}

		@Override
		protected Object getValue(Object element) {
			final IRegion region = (IRegion)element;
			ROIBase roi = region.getROI();

			switch (column){
			case 0:
				return region.getLabel();
			case 1:
				return region.getROI().getPointX();
			case 2:
				return region.getROI().getPointY();
			case 3:
				if(roi instanceof RectangularROI)
					return ((RectangularROI)roi).getEndPoint()[0];
				else if(roi instanceof LinearROI)
					return ((LinearROI)roi).getEndPoint()[0];
				else 
					return null;
			case 4:
				if(roi instanceof RectangularROI)
					return ((RectangularROI)roi).getEndPoint()[1];
				else if(roi instanceof LinearROI)
					return ((LinearROI)roi).getEndPoint()[1];
				else 
					return null;
			case 5: //max intensity
				return null;
			case 6: //sum
				return null;
			case 7: //isPlot
				return roi.isPlot();
			default:
				return null;
			}
			
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				this.setValue(element, value, true);
			} catch (Exception e) {
				logger.debug("Error while setting table value");
				e.printStackTrace();
			}
		}
		
		protected void setValue(Object element, Object value, boolean tableRefresh) throws Exception {

			final IRegion region = (IRegion) element;
			ROIBase myRoi = region.getROI();
			switch (column){
			case 0:
				// takes care of renaming the region (label and key value in hash table)
				getPlottingSystem().renameRegion(region, (String)value);
				break;
			case 1:
				if(myRoi instanceof LinearROI){
					LinearROI lroi = (LinearROI)myRoi;
					double[] endPoint = lroi.getEndPoint();
					lroi.setPoint((Double)value, lroi.getPointY());
					lroi.setEndPoint(endPoint);
					myRoi = lroi;
				} else if(myRoi instanceof RectangularROI){
					RectangularROI rroi = (RectangularROI)myRoi;
					double[] endPoint = rroi.getEndPoint();
					rroi.setPoint((Double)value, myRoi.getPointY());
					rroi.setLengths(endPoint[0] - (Double)value, rroi.getLengths()[1]); //set new endpoint
					myRoi = rroi;
				}
				break;
			case 2:
				if(myRoi instanceof LinearROI){
					LinearROI lroi = (LinearROI)myRoi;
					double[] endPoint = lroi.getEndPoint();
					lroi.setPoint(lroi.getPointX(), (Double)value);
					lroi.setEndPoint(endPoint);
					myRoi = lroi;
				} else if(myRoi instanceof RectangularROI){
					RectangularROI rroi = (RectangularROI)myRoi;
					double[] endPoint = rroi.getEndPoint();
					rroi.setPoint(myRoi.getPointX(), (Double)value);
					rroi.setLengths(rroi.getLengths()[0], endPoint[1] - (Double)value); //set new endpoint
					myRoi = rroi;
				}
				break;
			case 3:
				if(myRoi instanceof LinearROI)
					((LinearROI)myRoi).setEndPoint((Double)value, ((LinearROI)myRoi).getEndPoint()[1]);
				else if (myRoi instanceof RectangularROI)
					((RectangularROI)myRoi).setEndPoint(new double[]{(Double)value, ((RectangularROI)myRoi).getEndPoint()[1]});
				break;
			case 4:
				if(myRoi instanceof LinearROI)
					((LinearROI)myRoi).setEndPoint(((LinearROI)myRoi).getEndPoint()[0], (Double)value);
				else if (myRoi instanceof RectangularROI)
					((RectangularROI)myRoi).setEndPoint(new double[]{((RectangularROI)myRoi).getEndPoint()[0], (Double)value});
				break;
			case 5: //intensity
				break;
			case 6: //sum
				break;
			case 7: //isPlot
				myRoi.setPlot((Boolean)value);
//				if(isActive){
//					if (region!=null) region.setRegionColor(ColorConstants.green);
//				} else {
//					if (region!=null) region.setRegionColor(ColorConstants.gray);
//				}
				break;
			default:
				break;
			}

			if (tableRefresh) {
				getViewer().refresh();
			}
			
			roi = myRoi;
			IRegion myregion = getPlottingSystem().getRegion(region.getName());
			if(myregion!= null)
				myregion.setROI(myRoi);
			
		}

		
	}

	
}
