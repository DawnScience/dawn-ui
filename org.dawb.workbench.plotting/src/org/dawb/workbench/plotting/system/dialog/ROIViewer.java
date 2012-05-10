package org.dawb.workbench.plotting.system.dialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.ui.plot.region.AbstractRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
import uk.ac.gda.richbeans.components.cell.FieldComponentCellEditor;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;

public class ROIViewer  {

	private static final Logger logger = LoggerFactory.getLogger(ROIViewer.class);
	
	private TableViewer regionTable;
	private AbstractRegion region;
	private XYGraph        graph;

	public Control createPartControl(Composite parent) {
		
		this.regionTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		final Label clickToEdit = new Label(parent, SWT.WRAP);
		clickToEdit.setText("* Click to change");
		return regionTable.getTable();
	}

	public void setRegion(final AbstractRegion region, final XYGraph graph) {
		
		this.region = region;
		this.graph  = graph;
		
		if (regionTable.getColumnProperties()!=null) return;
		
		final List<RegionRow> rows = createRegionRows(region.getROI());
		createColumns(regionTable, region.getROI(), rows);
		
		regionTable.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			
			@Override
			public void dispose() { }
			
			@Override
			public Object[] getElements(Object inputElement) {
				return rows.toArray(new RegionRow[rows.size()]);
			}			
		});
		
		regionTable.setInput(rows.get(0));
	}


    private void createColumns(TableViewer viewer, ROIBase roi, final List<RegionRow> rows) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		viewer.setColumnProperties(new String[] { "Name", "x", "y" });

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setWidth(180);
		var.setLabelProvider(new ROILabelProvider(0));

		
		var = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setWidth(150);
		ROIEditingSupport roiEditor = new ROIEditingSupport(viewer, rows, 1);
		var.setEditingSupport(roiEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ROILabelProvider(1, roiEditor)));

		
		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setWidth(150);
		roiEditor = new ROIEditingSupport(viewer, rows, 2);
		var.setEditingSupport(roiEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ROILabelProvider(2, roiEditor)));
		
		regionTable.getTable().setHeaderVisible(false);

    }
    
    public void cancelEditing() {
     	this.regionTable.cancelEditing();
    }
    
	public class ROIEditingSupport extends EditingSupport {

		private int column;
		private final List<RegionRow> rows;
        
		public ROIEditingSupport(ColumnViewer viewer, final List<RegionRow> rows, int col) {
			super(viewer);
            this.column = col;
            this.rows   = rows;
 		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			
			FieldComponentCellEditor ed = null;
			try {
				ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(), 
						                     SpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot get FieldComponentCellEditor for "+SpinnerWrapper.class.getName(), e);
				return null;
			}
			final SpinnerWrapper   rb = (SpinnerWrapper)ed.getFieldWidget();
			Range range;
            if (column==1) {
            	range = graph.primaryXAxis.getRange();
			} else {
				range = graph.primaryYAxis.getRange();
			}
            rb.setMaximum((int)Math.max(range.getUpper(), range.getLower()));
            rb.setMinimum((int)Math.min(range.getUpper(), range.getLower()));
            rb.setButtonVisible(false);
            rb.setActive(true);
            ((Spinner)rb.getControl()).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
				    setValue(element, rb.getValue(), false);
				}
			});	
			return ed;
		}

		@Override
		protected boolean canEdit(Object element) {
			double val;
			final RegionRow row = (RegionRow)element;
			if (!row.isEnabled()) return false;
			if (column==1) {
				val = row.getxLikeVal();
			} else {
				val = row.getyLikeVal();
			}
			return !Double.isNaN(val);
		}

		@Override
		protected Object getValue(Object element) {
			final RegionRow row = (RegionRow)element;
			if (column==1) {
				return (int)row.getxLikeVal();
			} else {
				return (int)row.getyLikeVal();
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
            this.setValue(element, value, true);
		}
		
		protected void setValue(Object element, Object value, boolean tableRefresh) {
			
            final RegionRow row = (RegionRow)element;
            final Number    val = (Number)value;
            if (column==1) {
            	row.setxLikeVal(val.doubleValue());
            } else {
            	row.setyLikeVal(val.doubleValue());
            }
            if (tableRefresh) {
            	getViewer().refresh();
            }
            
            final ROIBase roi = createRoi(rows);
            if (roi!=null) region.setROI(roi);
		}

	}

	public class ROILabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
		
		private int column;
        private NumberFormat format;
		private ROIEditingSupport editor;
        
		public ROILabelProvider(int col) {
			this(col, null);
		}
		public ROILabelProvider(int col, final ROIEditingSupport editor) {
            this.column = col;
            this.format = NumberFormat.getNumberInstance();
            this.editor = editor;
		}
		
		public String getText(Object element) {
			
			final RegionRow row = (RegionRow)element;
			switch (column) {
			case 0:			
				return row.getName();
			case 1:	
				if (Double.isNaN(row.getxLikeVal())) return "-";
				return format.format(row.getxLikeVal());
			case 2:			
				if (Double.isNaN(row.getyLikeVal())) return "-";
				return format.format(row.getyLikeVal());
			}
			return "";
		}
		
		@Override
		public StyledString getStyledText(Object element) {
			final StyledString ret = new StyledString(getText(element));
			if (editor!=null && editor.canEdit(element)) {
			    ret.append(new StyledString("*", StyledString.QUALIFIER_STYLER));
			}
			return ret;
		}
	}

	private List<RegionRow> createRegionRows(ROIBase roi) {
    	
		final List<RegionRow> ret = new ArrayList<ROIViewer.RegionRow>();
		
		if (roi instanceof LinearROI) {
			final LinearROI lr = (LinearROI)roi;
			ret.add(new RegionRow("Start Point (x,y)", "pixel", lr.getPoint()[0],    lr.getPoint()[1]));
			ret.add(new RegionRow("End Point (x,y)",   "pixel", lr.getEndPoint()[0], lr.getEndPoint()[1]));
			ret.add(new RegionRow("Rotation (°)",      "°",     lr.getAngleDegrees(), Double.NaN));
			
		} else if (roi instanceof PolygonalROI) {
			final PolygonalROI pr = (PolygonalROI)roi;
			for (int i = 0; i < pr.getSides(); i++) {
				ret.add(new RegionRow("Point "+(i+1)+"  (x,y)", "pixel", pr.getPointX(i),    pr.getPointY(i)));
			}
			
		} else if (roi instanceof PointROI) {
			final PointROI pr = (PointROI)roi;
			ret.add(new RegionRow("Point (x,y)", "pixel", pr.getPointX(),    pr.getPointY()));
			
		} else if (roi instanceof RectangularROI) {
			final RectangularROI rr = (RectangularROI)roi;
			ret.add(new RegionRow("Start Point (x,y)", "pixel", rr.getPoint()[0],    rr.getPoint()[1]));
			ret.add(new RegionRow("End Point (x,y)",   "pixel", rr.getEndPoint()[0], rr.getEndPoint()[1]));
			ret.add(new RegionRow("Rotation (°)",      "°",     rr.getAngleDegrees(), Double.NaN));
			
		} else if (roi instanceof SectorROI) {
			final SectorROI sr = (SectorROI)roi;
			ret.add(new RegionRow("Center (x,y)",         "pixel", sr.getPointX(),    sr.getPointY()));
			ret.add(new RegionRow("Radii (inner, outer)", "pixel", sr.getRadius(0),   sr.getRadius(1)));
			ret.add(new RegionRow("Angles (°)",           "°",  sr.getAngleDegrees(0),    sr.getAngleDegrees(1)));
			
			if (region.getRegionType()==RegionType.RING) {
				ret.get(2).setEnabled(false);
			}
		}
		
		return ret;
	}

	public ROIBase createRoi(List<RegionRow> rows) {
		
		final ROIBase roi = region.getROI();
		
		ROIBase ret = null; 
		if (roi instanceof LinearROI) {
			LinearROI lr = new LinearROI(rows.get(0).getPoint(), rows.get(1).getPoint());
			lr.setAngle(rows.get(2).getxLikeVal());
			ret = lr;
			
		} else if (roi instanceof PolygonalROI) {
			PolygonalROI pr = new PolygonalROI();
			for (RegionRow regionRow : rows) {
				pr.insertPoint(regionRow.getPoint());
			}
			ret = pr;
			
		} else if (roi instanceof PointROI) {
			PointROI pr = new PointROI(rows.get(0).getPoint());
			ret = pr;
			
		} else if (roi instanceof RectangularROI) {
			RectangularROI rr = new RectangularROI(rows.get(0).getxLikeVal(), rows.get(0).getyLikeVal(),
					                                rows.get(1).getxLikeVal()-rows.get(0).getxLikeVal(),
					                                rows.get(1).getyLikeVal()-rows.get(0).getyLikeVal(), 
					                                rows.get(2).getxLikeVal());
			ret = rr;
			
		} else if (roi instanceof SectorROI) {
			SectorROI orig = (SectorROI)roi;
			SectorROI sr = new SectorROI(rows.get(0).getxLikeVal(),
					                     rows.get(0).getyLikeVal(),
					                     rows.get(1).getxLikeVal(),
					                     rows.get(1).getyLikeVal(),
					                     Math.toRadians(rows.get(2).getxLikeVal()),
					                     Math.toRadians(rows.get(2).getyLikeVal()),
					                     orig.getDpp(),
					                     orig.isClippingCompensation(),
					                     orig.getSymmetry());
			ret = sr;
		}
		
		return ret;
	}

	public void dispose() {
    	
    }
    
    private final static class RegionRow {
       	private String name;
       	private String unit;
        private double xLikeVal;
    	private double yLikeVal;
    	private boolean enabled=true;
		public RegionRow(String name, String unit, double xLikeVal, double yLikeVal) {
			this.name     = name;
			this.unit     = unit;
			this.xLikeVal = xLikeVal;
			this.yLikeVal = yLikeVal;
		}
		
		public int[] getIntPoint() {
			return new int[]{(int)xLikeVal, (int)yLikeVal};
		}
		public double[] getPoint() {
			return new double[]{xLikeVal, yLikeVal};
		}
		public String getName() {
			return name;
		}
		public double getxLikeVal() {
			return xLikeVal;
		}
		public void setxLikeVal(double val1) {
			this.xLikeVal = val1;
		}
		public double getyLikeVal() {
			return yLikeVal;
		}
		public void setyLikeVal(double val2) {
			this.yLikeVal = val2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			long temp;
			temp = Double.doubleToLongBits(xLikeVal);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(yLikeVal);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RegionRow other = (RegionRow) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (unit == null) {
				if (other.unit != null)
					return false;
			} else if (!unit.equals(other.unit))
				return false;
			if (Double.doubleToLongBits(xLikeVal) != Double
					.doubleToLongBits(other.xLikeVal))
				return false;
			if (Double.doubleToLongBits(yLikeVal) != Double
					.doubleToLongBits(other.yLikeVal))
				return false;
			return true;
		}
		public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
    }
}
