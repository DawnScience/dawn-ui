/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IEllipseFitSelection;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

public class EllipseFittingTool extends AbstractToolPage {
	private final static Logger logger = LoggerFactory.getLogger(EllipseFittingTool.class);

	private Composite composite;
	private IRegionListener ellipseRegionListener;
	private ITraceListener traceListener;
	private IROIListener ellipseROIListener;

	private TableViewer viewer;
	private List<IRegion> ellipses;

	private IEllipseFitSelection cRegion;

	protected boolean circleOnly;

	public EllipseFittingTool() {
		ellipses = new ArrayList<IRegion>(5);

		ellipseRegionListener = new IRegionListener.Stub() {
			@Override
			public void regionsRemoved(RegionEvent evt) {
				updateEllipses(true);
			}

			@Override
			public void regionRemoved(RegionEvent evt) {
				removeEllipse(evt.getRegion());
			}

			@Override
			public void regionAdded(RegionEvent evt) {
				updateEllipse(evt.getRegion());
			}

			@Override
			public void regionCreated(RegionEvent evt) {
				IRegion r = evt.getRegion();
				if (r instanceof IEllipseFitSelection) {
					((IEllipseFitSelection) r).setFitCircle(circleOnly);
				}
			}
		};

		ellipseROIListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				updateEllipses(false);
			}
		};

		try {
			setPlottingSystem(PlottingFactory.createPlottingSystem());
			traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent evt) {

					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}

				}
			};

		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}

	protected void updateEllipse(IRegion region) {
		if (region == null)
			return;

		if (ellipses.contains(region)) {
			viewer.setSelection(new StructuredSelection(region));
			return;
		}

		if (region.getRegionType() != RegionType.ELLIPSEFIT)
			return;

		region.addROIListener(ellipseROIListener);
		cRegion = (IEllipseFitSelection) region;
		ellipses.add(region);
		viewer.refresh();
		viewer.setSelection(new StructuredSelection(region));
	}

	protected void removeEllipse(IRegion region) {
		if (region == null)
			return;

		if (region.getRegionType() != RegionType.ELLIPSEFIT)
			return;

		if (ellipses.contains(region))
			ellipses.remove(region);

		region.removeROIListener(ellipseROIListener);
		viewer.refresh();
	}

	protected void updateEllipses(boolean removeROIListener) {
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter == null) return;

		if (removeROIListener) {
			for (IRegion r : ellipses) {
				r.removeROIListener(ellipseROIListener);
			}
		}
		ellipses.clear();

		Collection<IRegion> regions = plotter.getRegions(RegionType.ELLIPSEFIT);
		if (regions != null && regions.size() > 0) {
			IRegion r = null;
			Iterator<IRegion> it = regions.iterator();
			while (it.hasNext()) {
				r = it.next();
				ellipses.add(r);
				if (removeROIListener) {
					r.addROIListener(ellipseROIListener);
				}
			}
		}
		if (viewer != null) // can be null during activation
			viewer.refresh();
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		createColumns(viewer);
		Table t = viewer.getTable();
		t.setLinesVisible(true);
		t.setHeaderVisible(true);

		createActions();

		getSite().setSelectionProvider(viewer);

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return ellipses.toArray();
			}
		});

		viewer.setInput(ellipses);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				IRegion r = (IRegion) s.getFirstElement();
				if (r instanceof IEllipseFitSelection) {
					cRegion = (IEllipseFitSelection) r;
					cRegion.setFitCircle(circleOnly);
				}
			}
		});
		parent.layout();
	}

	private void createColumns(final TableViewer viewer) {
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
	
		int i = 0;
		TableViewerColumn col;
		TableColumn c;
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("ID");
		c.setWidth(100);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Major semi-axis");
		c.setWidth(120);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Minor semi-axis");
		c.setWidth(120);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Angle");
		c.setWidth(75);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Centre x");
		c.setWidth(75);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Centre y");
		c.setWidth(75);
	
		col = new TableViewerColumn(viewer, SWT.CENTER, i);
		col.setLabelProvider(new EllipseROILabelProvider(i++));
		c = col.getColumn();
		c.setText("Points");
		c.setWidth(20);
	}

	private void createActions() {
		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();

				if (sel != null && sel.getFirstElement() != null) {
					final IRegion region = (IRegion) sel.getFirstElement();
					getPlottingSystem().removeRegion(region);
					if (region == cRegion)
						cRegion = null;
					viewer.refresh();
				}
			}
		};
		delete.setToolTipText("Delete selected region, if there is one.");

		final Action circle = new Action("Use circle for fit", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				circleOnly = isChecked();
				logger.debug("Circle fit clicked: {}", circleOnly);

				if (cRegion != null) {
					cRegion.setFitCircle(circleOnly);
					
				}
			}
		};
		circle.setImageDescriptor(Activator.getImageDescriptor("icons/fitocircle.png"));
		circle.setToolTipText("Restrict fit to a circle");

		IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
		tbm.add(circle);
		tbm.add(delete);
		IMenuManager mm = getSite().getActionBars().getMenuManager();
		mm.add(circle);
		mm.add(delete);
	}

	class EllipseROILabelProvider extends ColumnLabelProvider {
		private final int column;

		public EllipseROILabelProvider(int i) {
			column = i;
		}

		@Override
		public String getText(Object element) {

			IROI rb = ((IRegion) element).getROI();

			if (!(rb instanceof EllipticalROI)) {
				return null;
			}
			EllipticalROI eroi = (EllipticalROI) rb;

			switch (column) {
			case 0:
				return ((IRegion) element).getName();
			case 1:
				return String.format("%.2f", eroi.getSemiAxis(0));
			case 2:
				return String.format("%.2f", eroi.getSemiAxis(1));
			case 3:
				return String.format("%.2f", eroi.getAngleDegrees());
			case 4:
				return String.format("%.2f", eroi.getPointX());
			case 5:
				return String.format("%.2f", eroi.getPointY());
			case 6:
				if (eroi instanceof EllipticalFitROI) {
					EllipticalFitROI froi = (EllipticalFitROI) eroi;
					return Integer.toString(froi.getPoints().getNumberOfPoints());
				}
				return "--";
			}
			return null;
		}
	}

	@Override
	public void activate() {
		super.activate();
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter == null) return;

		if (traceListener != null)
			plotter.addTraceListener(traceListener);
		if (ellipseRegionListener != null)
			plotter.addRegionListener(ellipseRegionListener);

		updateEllipses(true);

		// Start with a selection of the right type
		try {
			cRegion = (IEllipseFitSelection) plotter.createRegion(RegionUtils.getUniqueName("Ellipse fit", plotter), RegionType.ELLIPSEFIT);
		} catch (Exception e) {
			logger.error("Cannot create region for ellipse fitting tool!");
		}
	}

	@Override
	public void deactivate() {
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter != null) {
			plotter.removeTraceListener(traceListener);
			plotter.removeRegionListener(ellipseRegionListener);
		}

		super.deactivate();
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
	}
}
