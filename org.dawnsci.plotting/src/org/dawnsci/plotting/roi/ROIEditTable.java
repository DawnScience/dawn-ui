/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.roi;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.eclipse.dawnsci.analysis.api.roi.IPolylineROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
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
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A widget for editing any ROI
 * 
 * @author Matthew Gerring
 *
 */
public class ROIEditTable  {
	
	private TableViewer regionTable;
	private IROI        roi;
	private ICoordinateSystem coords;
	private IROI        originalRoi;
	private double      xLowerBound=Double.NaN, xUpperBound=Double.NaN; // Optional bounds
	private double      yLowerBound=Double.NaN, yUpperBound=Double.NaN; // Optional bounds
	private List<IRegionRow> rows;
	
	// preferences
	private boolean roundButtonAndAsterisk;
	private int height;
	private int[] columnWidths;
	
	public ROIEditTable() {
		// default preferences
		roundButtonAndAsterisk = true;
		height = 100;
		columnWidths = new int[]{180,150};
	}
	
	public ROIEditTable(int height, int[] columnWidths, boolean roundButtonAndAsterisk) {
		this.roundButtonAndAsterisk = roundButtonAndAsterisk;
		this.height = height;
		this.columnWidths = columnWidths;
	}

	public Control createPartControl(Composite parent) {
		
		this.regionTable     = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		GridData tableData   = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		tableData.heightHint = height;
		regionTable.getTable().setLayoutData(tableData);
		createColumns(regionTable);
		
		if (roundButtonAndAsterisk) {
			final Button round = new Button(parent, SWT.PUSH);
			round.setText("Round");
			round.setToolTipText("Round values of region to nearest integer");
			round.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
			round.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					for (IRegionRow row : rows) {
						if (row instanceof RegionRow) {
							((RegionRow)row).round();
						}
					}
					regionTable.refresh();
		            roi = createRoi(rows, null, coords);
					fireROIListeners();
				}
			});
			
			final Label clickToEdit = new Label(parent, SWT.RIGHT);
			clickToEdit.setText("* Click to change");
			clickToEdit.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, true, false));
		}
		
		return regionTable.getTable();
	}

	/**
	 * Can be called also to change to editing a different region.
	 * @param roi
	 * @param regionType - may be null
	 * @param coords     - may be null
	 */
	public void setRegion(final IROI roi, final RegionType regionType, final ICoordinateSystem coords) {
		
		this.originalRoi = roi!=null ? roi : null;
		this.roi         = roi!=null ? roi.copy() : null;
		this.coords      = coords;
				
		this.rows = createRegionRows(roi, coords);
		
		regionTable.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			
			@Override
			public void dispose() { }
			
			@Override
			public Object[] getElements(Object inputElement) {
				return rows.toArray(new IRegionRow[rows.size()]);
			}			
		});
		
		regionTable.setInput(rows.get(0));
	}


    private void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		viewer.setColumnProperties(new String[] { "Name", "x", "y" });

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setWidth(columnWidths[0]);
		var.setLabelProvider(new ROILabelProvider(0));

		
		var = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setWidth(columnWidths[1]);
		ROIEditingSupport roiEditor = new ROIEditingSupport(viewer, 1);
		var.setEditingSupport(roiEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ROILabelProvider(1, roiEditor)));

		
		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setWidth(columnWidths[1]);
		roiEditor = new ROIEditingSupport(viewer, 2);
		var.setEditingSupport(roiEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ROILabelProvider(2, roiEditor)));
		
		regionTable.getTable().setHeaderVisible(false);
    }
    
    public void cancelEditing() {
    	this.regionTable.cancelEditing();
    }
    
	public class ROIEditingSupport extends EditingSupport {

		private int column;
        
		public ROIEditingSupport(ColumnViewer viewer,  int col) {
			super(viewer);
            this.column = col;
  		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			
			if (element instanceof SymmetryRow) {
				Collection<String> values = SectorROI.getSymmetriesPossible().values();
				final String[] items =  (String[]) values.toArray(new String[values.size()]);
				final CComboCellEditor ed = new CComboCellEditor(((TableViewer)getViewer()).getTable(), items) {
		    	    protected void doSetValue(Object value) {
		                Integer ordinal = SectorROI.getSymmetry((String)value);
		                super.doSetValue(ordinal);
		    	    }
		    		protected Object doGetValue() {
		    			Integer ordinal = (Integer)super.doGetValue();
		    			return items[ordinal];
		    		}
				};
				return ed;
			}
			final FloatSpinnerCellEditor ed = new FloatSpinnerCellEditor(((TableViewer)getViewer()).getTable(),SWT.RIGHT);
			ed.setFormat(7, 3);
			ed.setIncrement(0.1d);

			if (element instanceof LinearROI || element instanceof PointROI || element instanceof IPolylineROI
					|| element instanceof RectangularROI || element instanceof PerimeterBoxROI) {
				if (column==1) {
					if (!Double.isNaN(xLowerBound)) ed.setMinimum(xLowerBound);
					if (!Double.isNaN(xUpperBound)) ed.setMaximum(xUpperBound);
				} else {
					if (!Double.isNaN(yLowerBound)) ed.setMinimum(yLowerBound);
					if (!Double.isNaN(yUpperBound)) ed.setMaximum(yUpperBound);
				}
			} else {
				ed.setMaximum(Double.MAX_VALUE);
				ed.setMinimum(-Double.MAX_VALUE);
			}

			ed.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setValue(element, ed.getValue(), false);
				}
			});
			return ed;
		}
		
		@Override
		protected boolean canEdit(Object element) {
			double val;
			final IRegionRow row = (IRegionRow)element;
			if (!row.isEnabled()) return false;
			if (row instanceof RegionRow) {
				if (column==1) {
					val = ((RegionRow)row).getxLikeVal();
				} else {
					val = ((RegionRow)row).getyLikeVal();
				}

				return !Double.isNaN(val);
			}
			if (column == 1) {
				return true;
			}
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			final IRegionRow row = (IRegionRow)element;
			if (row instanceof RegionRow) {
				if (column==1) {
					return ((RegionRow)row).getxLikeVal();
				} else {
					return ((RegionRow)row).getyLikeVal();
				}
			}
			if (column == 1) {
				return ((SymmetryRow) row).getSymmetryName();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
            this.setValue(element, value, true);
		}
		
		protected void setValue(Object element, Object value, boolean tableRefresh) {
			
            final IRegionRow row = (IRegionRow)element;
            if (row instanceof RegionRow) {
                final Number    val = (Number)value;
            	if (column==1) {
            		((RegionRow)row).setxLikeVal(val.doubleValue());
            	} else {
            		((RegionRow)row).setyLikeVal(val.doubleValue());
            	}
            }
            else {
            	if (column==1) {
            		((SymmetryRow)row).setSymmetryName((String)value);
            	}
            }
            if (tableRefresh) {
            	getViewer().refresh();
            }
            
            roi = createRoi(rows, row, coords);
            fireROIListeners();
		}

	}
	
	private Collection<IROIListener> roiListeners;
	protected void fireROIListeners() {
		if (roiListeners==null) return;
		final ROIEvent evt = new ROIEvent(this, roi);
		for (IROIListener l : roiListeners) {
			l.roiChanged(evt);
		}
	}

	public boolean addROIListener(final IROIListener l) {
		if (roiListeners==null) roiListeners = new HashSet<IROIListener>(11);
		if (!roiListeners.contains(l)) return roiListeners.add(l);
		return false;
	}
	
	public boolean removeROIListener(final IROIListener l) {
		if (roiListeners==null) return false;
		return roiListeners.remove(l);
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
			
			final IRegionRow row = (IRegionRow)element;
			switch (column) {
			case 0:			
				return row.getName();
			case 1:	
				if (row instanceof SymmetryRow) {
					return ((SymmetryRow)row).getSymmetryName();
				}
				if (Double.isNaN(((RegionRow)row).getxLikeVal())) return "-";
				return format.format(((RegionRow)row).getxLikeVal());
			case 2:			
				if (row instanceof SymmetryRow) {
					return "";
				}
				if (Double.isNaN(((RegionRow)row).getyLikeVal())) return "-";
				return format.format(((RegionRow)row).getyLikeVal());
			}
			return "";
		}
		
		@Override
		public StyledString getStyledText(Object element) {
			final StyledString ret = new StyledString(getText(element));
			if (editor!=null && editor.canEdit(element) && roundButtonAndAsterisk) {
			    ret.append(new StyledString("*", StyledString.QUALIFIER_STYLER));
			}
			return ret;
		}
	}

	private List<IRegionRow> createRegionRows(IROI roi, final ICoordinateSystem coords) {
    	
		final List<IRegionRow> ret = new ArrayList<ROIEditTable.IRegionRow>();
		
		if (roi instanceof LinearROI) {
			final LinearROI lr = (LinearROI)roi;
			ret.add(new RegionRow("Start Point (x,y)", "pixel", getAxis(coords, lr.getPoint())));
			ret.add(new RegionRow("End Point (x,y)",   "pixel", getAxis(coords, lr.getEndPoint())));
			ret.add(new RegionRow("Rotation (°)",      "°",     lr.getAngleDegrees(), Double.NaN));
			
		} else if (roi instanceof IPolylineROI) {
			final IPolylineROI pr = (IPolylineROI)roi;
			for (int i = 0, imax = pr.getNumberOfPoints(); i < imax; i++) {
				ret.add(new RegionRow("Point "+(i+1)+"  (x,y)", "pixel", getAxis(coords, pr.getPoint(i).getPoint())));
			}
			
		} else if (roi instanceof PointROI) {
			final PointROI pr = (PointROI)roi;
			ret.add(new RegionRow("Point (x,y)", "pixel", getAxis(coords, pr.getPoint())));
			
		} else if (roi instanceof PerimeterBoxROI) {
			final PerimeterBoxROI pr = (PerimeterBoxROI)roi;
			ret.add(new RegionRow("Start Point (x,y)", "pixel", getAxis(coords, pr.getPoint())));
			ret.add(new RegionRow("Lengths (x,y)",     "pixel", getAxis(coords, pr.getLengths())));
			ret.add(new RegionRow("Rotation (°)",      "°",     pr.getAngleDegrees(), Double.NaN));
			
		} else if (roi instanceof RectangularROI) {
			final RectangularROI rr = (RectangularROI)roi;
			ret.add(new RegionRow("Start Point (x,y)", "pixel", getAxis(coords, rr.getPoint())));
			ret.add(new RegionRow("Lengths (x,y)",     "pixel", getAxis(coords, rr.getLengths())));
			ret.add(new RegionRow("Rotation (°)",      "°",     rr.getAngleDegrees(), Double.NaN));

			
		} else if (roi instanceof RingROI) {
			final RingROI rr = (RingROI)roi;
			ret.add(new RegionRow("Centre (x,y)",         "pixel", getAxis(coords, rr.getPoint())));
			ret.add(new RegionRow("Radii (inner, outer)", "pixel", rr.getRadii()));
			if (roi instanceof SectorROI) {
				SectorROI sr = (SectorROI) rr;
				ret.add(new RegionRow("Angles (°)",           "°", sr.getAngleDegrees(0), sr.getAngleDegrees(1)));			
				ret.add(new SymmetryRow("Symmetry", sr.getSymmetryText()));
			}
		} else if (roi instanceof CircularROI) {
			final CircularROI cr = (CircularROI) roi;
			ret.add(new RegionRow("Centre (x,y)", "pixel", getAxis(coords, cr.getPoint())));
			ret.add(new RegionRow("Radius",       "pixel", cr.getRadius(), Double.NaN));

			if (cr instanceof CircularFitROI) {
				ret.get(0).setEnabled(false);
				ret.get(1).setEnabled(false);
				final IPolylineROI pr = ((CircularFitROI) cr).getPoints();
				for (int i = 0, imax = pr.getNumberOfPoints(); i < imax; i++) {
					ret.add(new RegionRow("Point "+(i+1)+"  (x,y)", "pixel", getAxis(coords, pr.getPoint(i).getPoint())));
				}
			}
		} else if (roi instanceof EllipticalROI) {
			final EllipticalROI er = (EllipticalROI) roi;
			ret.add(new RegionRow("Centre (x,y)",             "pixel", getAxis(coords, er.getPoint())));
			ret.add(new RegionRow("Semi-axes (major, minor)", "pixel", er.getSemiAxis(0), er.getSemiAxis(1)));
			ret.add(new RegionRow("Rotation (°)",             "°",     er.getAngleDegrees(), Double.NaN));

			if (er instanceof EllipticalFitROI) {
				ret.get(0).setEnabled(false);
				ret.get(1).setEnabled(false);
				ret.get(2).setEnabled(false);
				final IPolylineROI pr = ((EllipticalFitROI) er).getPoints();
				for (int i = 0, imax = pr.getNumberOfPoints(); i < imax; i++) {
					ret.add(new RegionRow("Point "+(i+1)+"  (x,y)", "pixel", getAxis(coords, pr.getPoint(i).getPoint())));
				}
			}
		} else if (roi instanceof ParabolicROI) {
			final ParabolicROI pr = (ParabolicROI) roi;
			ret.add(new RegionRow("Focus (x,y)",             "pixel", getAxis(coords, pr.getPointRef())));
			ret.add(new RegionRow("Focal parameter",         "pixel", pr.getFocalParameter(), Double.NaN));
			ret.add(new RegionRow("Rotation (°)",            "°",     pr.getAngleDegrees(), Double.NaN));
		} else if (roi instanceof HyperbolicROI) {
			final HyperbolicROI hr = (HyperbolicROI) roi;
			ret.add(new RegionRow("Focus (x,y)",             "pixel", getAxis(coords, hr.getPointRef())));
			ret.add(new RegionRow("Semi-latus rectum",       "pixel", hr.getSemilatusRectum(), Double.NaN));
			ret.add(new RegionRow("Eccentricity",            "",      hr.getEccentricity(), Double.NaN));
			ret.add(new RegionRow("Rotation (°)",            "°",     hr.getAngleDegrees(), Double.NaN));
		} else if (roi != null) {
			ret.add(new RegionRow("Unknown type (x,y)", "pixel", getAxis(coords, roi.getPoint())));
			
		} else {
			ret.add(new RegionRow("Null type (x,y)", "None", Double.NaN, Double.NaN));
			ret.get(0).setEnabled(false);
		}
		
		return ret;
	}
	
	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	private double[] getAxis(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getValueAxisLocation(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	/**
	 * Creates a roi from the current table data
	 * @param rows
	 * @param changed
	 * @param coords
	 * @return
	 */
	private IROI createRoi(List<IRegionRow> rows, IRegionRow changed, ICoordinateSystem coords) {
				
		IROI ret = null; 
		if (roi instanceof LinearROI) {
			if (changed!=null && changed==rows.get(2)) {
				LinearROI lr = new LinearROI(getPoint(coords, (RegionRow)rows.get(0)), getPoint(coords, (RegionRow)rows.get(1)));
				lr.setAngle(Math.toRadians(((RegionRow)rows.get(2)).getxLikeVal()));
				ret = lr;
			} else {
				LinearROI lr = new LinearROI(getPoint(coords, (RegionRow)rows.get(0)), getPoint(coords, (RegionRow)rows.get(1)));
				if (changed==rows.get(1)) ((RegionRow)rows.get(2)).setxLikeVal(0d);
				ret = lr;
			}
			
		} else if (roi instanceof IPolylineROI) {
			PolylineROI pr = (roi instanceof PolygonalROI) ? new PolygonalROI() : new PolylineROI();
			for (IRegionRow regionRow : rows) {
				pr.insertPoint(getPoint(coords, (RegionRow)regionRow));
			}
			ret = pr;
			
		} else if (roi instanceof PointROI || roi==null) {
			PointROI pr = new PointROI(getPoint(coords, (RegionRow)rows.get(0)));
			ret = pr;
			
		} else if (roi instanceof RectangularROI) {
			
			final double[] start = getPoint(coords, (RegionRow)rows.get(0));
			final double[] length   = getPoint(coords, (RegionRow)rows.get(1));
			final double angle = Math.toRadians(((RegionRow)rows.get(2)).getxLikeVal());
			
			// TODO don't have to do it this way - reflection would solve all the tests with identical blocks.
			if (roi instanceof PerimeterBoxROI) {
				PerimeterBoxROI pr = new PerimeterBoxROI(start[0],         start[1],
								                         length[0],       length[1], 
								                         angle);
				ret = pr;
				
			} else if (roi instanceof GridROI) {
				GridROI gr = ((GridROI)roi).copy();
				gr.setPoint(start);
				gr.setLengths(length[0],  length[1]);
				gr.setAngle(angle);
				
				ret = gr;

			} else {
				RectangularROI rr = new RectangularROI(start[0], start[1], length[0], length[1],
						angle);
				ret = rr;
			}
			
		} else if (roi instanceof RingROI) {
			RingROI orig = (RingROI) roi;
			final double[] cent  = getPoint(coords, (RegionRow)rows.get(0));
			final double[] radii = ((RegionRow)rows.get(1)).getPoint();

			if (orig instanceof SectorROI) {
				final String symmetryName = ((SymmetryRow)rows.get(3)).getSymmetryName();
				SectorROI so = (SectorROI) orig;
				SectorROI sr = new SectorROI(cent[0],
						 cent[1],
						 radii[0],
						 radii[1],
						 Math.toRadians(((RegionRow)rows.get(2)).getxLikeVal()),
						 Math.toRadians(((RegionRow)rows.get(2)).getyLikeVal()),
						 so.getDpp(),
						 so.isClippingCompensation(),
						 so.getSymmetry());
				sr.setSymmetry(SectorROI.getSymmetry(symmetryName));
				sr.setCombineSymmetry(so.isCombineSymmetry());
				ret = sr;
			} else {
				RingROI rr = new RingROI(radii[0], radii[1]);

				rr.setPoint(cent);
				ret = rr;
			}
		} else if (roi instanceof CircularFitROI) {
			PolylineROI pr = new PolylineROI();

			for (int i = 2, imax = rows.size(); i < imax; i++) {
				pr.insertPoint(getPoint(coords, (RegionRow)rows.get(i)));
			}
			ret = new CircularFitROI(pr);
		} else if (roi instanceof CircularROI) {
			
			final double[] cent = getPoint(coords, (RegionRow)rows.get(0));
			final double   rad  = ((RegionRow)rows.get(1)).getxLikeVal();

			CircularROI cr = new CircularROI(Math.abs(rad), cent[0], cent[1]);
			ret = cr;
		} else if (roi instanceof EllipticalFitROI) {
			PolylineROI pr = new PolylineROI();

			for (int i = 3, imax = rows.size(); i < imax; i++) {
				pr.insertPoint(getPoint(coords, (RegionRow)rows.get(i)));
			}
			ret = new EllipticalFitROI(pr);
		} else if (roi instanceof EllipticalROI) {
			
			final double[] cent = getPoint(coords, (RegionRow)rows.get(0));
			final double[] maj  = ((RegionRow)rows.get(1)).getPoint();
			final double   ang  = ((RegionRow)rows.get(2)).getxLikeVal();
			
			EllipticalROI er = new EllipticalROI(maj[0],maj[1],
					                             Math.toRadians(ang), 
					                             cent[0], 
					                             cent[1]);
			ret = er;
		} else if (roi instanceof ParabolicROI) {
			final double[] cent = getPoint(coords, (RegionRow)rows.get(0));
			final double   fpar  = ((RegionRow)rows.get(1)).getxLikeVal();
			final double   ang  = ((RegionRow)rows.get(2)).getxLikeVal();

			ParabolicROI pr = new ParabolicROI(fpar, Math.toRadians(ang), cent[0], cent[1]);
			ret = pr;
		} else if (roi instanceof HyperbolicROI) {
			final double[] cent = getPoint(coords, (RegionRow)rows.get(0));
			final double   semi = ((RegionRow)rows.get(1)).getxLikeVal();
			final double   ecc  = ((RegionRow)rows.get(2)).getxLikeVal();
			final double   ang  = ((RegionRow)rows.get(3)).getxLikeVal();

			HyperbolicROI hr = new HyperbolicROI(semi, ecc, Math.toRadians(ang), cent[0], cent[1]);
			ret = hr;
		}

		if (ret != null)
			ret.setName(roi.getName());
		return ret;
	}
	
	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	private double[] getPoint(ICoordinateSystem coords, RegionRow row) {
		double[] vals = row.getPoint();
		if (coords==null) return vals;

		try {
			return coords.getAxisLocationValue(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	public void dispose() {
		roi=null;
		originalRoi=null;
		coords=null;
		rows.clear();
		rows=null;
    }
    
	private abstract class IRegionRow {
		protected String name;
		protected boolean enabled=true;
		public String getName() {
			return name;
		}
		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
	
	private class SymmetryRow extends IRegionRow {
		private String symmetryName;

		public String getSymmetryName() {
			return symmetryName;
		}

		public void setSymmetryName(String symmetryName) {
			this.symmetryName = symmetryName;
		}
		
		public SymmetryRow(String name, String symmetryName) {
			this.name = name;
			this.symmetryName = symmetryName;
		}
	}

    private class RegionRow extends IRegionRow {
       	private String unit;
        private double xLikeVal;
    	private double yLikeVal;
		public RegionRow(String name, String unit, double... vals) {
			this.name     = name;
			this.unit     = unit;
			this.xLikeVal = vals[0];
			this.yLikeVal = vals[1];
		}
		public void round() {
			xLikeVal = Math.round(xLikeVal);
			yLikeVal = Math.round(yLikeVal);
		}
		public double[] getPoint() {
			return new double[]{xLikeVal, yLikeVal};
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
		@SuppressWarnings("unused")
		public String getUnit() {
			return unit;
		}
		@SuppressWarnings("unused")
		public void setUnit(String unit) {
			this.unit = unit;
		}
    }

	public IROI getOriginalRoi() {
		return originalRoi;
	}
	public IROI getRoi() {
		return roi;
	}

	public double getXLowerBound() {
		return xLowerBound;
	}

	public void setXLowerBound(double lowerBound) {
		this.xLowerBound = lowerBound;
	}

	public double getXUpperBound() {
		return xUpperBound;
	}

	public void setXUpperBound(double upperBound) {
		this.xUpperBound = upperBound;
	}

	public double getYLowerBound() {
		return yLowerBound;
	}

	public void setYLowerBound(double yLowerBound) {
		this.yLowerBound = yLowerBound;
	}

	public double getYUpperBound() {
		return yUpperBound;
	}

	public void setYUpperBound(double yUpperBound) {
		this.yUpperBound = yUpperBound;
	}

}
