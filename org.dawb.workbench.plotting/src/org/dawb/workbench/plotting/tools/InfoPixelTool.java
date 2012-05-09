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

package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.IAxis;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.tools.MeasurementTool.RegionColorListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public class InfoPixelTool extends AbstractToolPage implements IROIListener, IRegionListener, MouseListener  {

	private final static Logger logger = LoggerFactory.getLogger(InfoPixelTool.class);
	
	protected IPlottingSystem        plotter;
	private   ITraceListener         traceListener;
	private   IRegion                xHair, yHair;
	
	private Composite     composite;
	private TableViewer   viewer;
	private RegionColorListener viewUpdateListener;
	private Map<String,ROIBase> dragBounds;
	public double xValues [] = new double[1];	public double yValues [] = new double[1];

		
	public InfoPixelTool() {
		dragBounds = new HashMap<String,ROIBase>(7);
		
		try {
			
			plotter = PlottingFactory.getPlottingSystem();
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					
				}
			};
						
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createActions();

		getSite().setSelectionProvider(viewer);

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub

			}
			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}
			@Override
			public Object[] getElements(Object inputElement) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
								
				final List<IRegion> visible = new ArrayList<IRegion>(regions.size()/2);
				
				if(regions.size() % 2 == 0){
					// add the intersection region between the two line regions					
					for (int i=0; i< regions.size(); i = i +2){
						// add only one region
						IRegion pointRegion = (IRegion)(regions.toArray())[0];
						// TODO Rita test that this is a point region, others may exist.
						visible.add(pointRegion);
					}
				}
				
				return visible.toArray(new IRegion[visible.size()]);
			}
		});

		viewer.setInput(new Object());
		
		
		//this.viewUpdateListener = new RegionColorListener();

		activate();
	}
	

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plotter;
		} else {
			return super.getAdapter(clazz);
		}
	}

	private void createRegions() {
		
		if (getPlottingSystem()==null) return;
		try {
			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y Profile", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
				addRegion("Updating x cross hair", xHair);

			}
			
			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X Profile", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
				addRegion("Updating x cross hair", yHair);
			}
			
		} catch (Exception ne) {
			logger.error("Cannot create information box cross-hairs!", ne);
		}
	}
	
	private void addRegion(String jobName, IRegion region) {
		region.setVisible(false);
		region.setTrackMouse(true);
		region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change it!
		getPlottingSystem().addRegion(region);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	public void activate() {
		
		createRegions();
		if (xHair!=null) {
			if (!isActive()) xHair.addMouseListener(this);
			xHair.setVisible(true);
			xHair.addROIListener(this);
		}
		if (yHair!=null) {
			yHair.setVisible(true);
			yHair.addROIListener(this);
		}

		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		
		// We stop the adding of other regions because this tool does
		// not like it when other regions are added.
		setOtherRegionsEnabled(false);
		
		super.activate();	
	}
	
	private static final String regionId = "org.dawb.workbench.ui.editors.plotting.swtxy.addRegions";
	
	private void setOtherRegionsEnabled(boolean isVisible) {

        final IActionBars bars = getPlottingSystem().getActionBars();
        if (bars.getToolBarManager().find(regionId)!=null) {
        	bars.getToolBarManager().find(regionId).setVisible(isVisible);
        	bars.getToolBarManager().update(true);
        }
        if (bars.getMenuManager().find(regionId)!=null) {
        	bars.getMenuManager().find(regionId).setVisible(isVisible);
        	bars.getMenuManager().update(true);
        }
	}

	public void deactivate() {
		super.deactivate();
		setOtherRegionsEnabled(true);

		if (xHair!=null) {
			xHair.removeMouseListener(this);
			xHair.setVisible(false);
			xHair.removeROIListener(this);
		}
		if (yHair!=null) {
			yHair.setVisible(false);
			yHair.removeROIListener(this);
		}
		plotter.clear();

		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}
	
	public void dispose() {
//		if (getPlottingSystem()!=null) {
//			getPlottingSystem().removeRegionListener(this);
//		}
		if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
		viewUpdateListener = null;

		if (viewer!=null) viewer.getControl().dispose();

		dragBounds.clear();
		dragBounds = null;

		super.dispose();
	}
	
	@Override
	public Control getControl() {
		return composite;
	}

	
	@Override
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().addROIListener(this);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().removeROIListener(this);
		}
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		
		if (!isActive()) return;
		
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions==null || regions.isEmpty()) logger.debug("no region selected");//return new Object[]{"-"};
		
		// add the resulting point region which is the intersection between the 2 line regions
		IRegion pointRegion = (IRegion)(regions.toArray())[0];
			
		viewer.refresh(pointRegion);
		viewer.add(pointRegion);

	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	private void createActions() {

		//TODO When I test actions, they are not working.
		//Is it the same for you?
		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					if (region==null||region.getROI()==null) return;
					final ROIBase bounds = region.getROI();
					if (bounds.getPoint()==null) return;

					final Clipboard cb = new Clipboard(composite.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
				}
			}
		};
		copy.setToolTipText("Copies the region values as text to clipboard which can then be pasted externally.");

		getSite().getActionBars().getToolBarManager().add(copy);
		getSite().getActionBars().getMenuManager().add(copy);

		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					getPlottingSystem().removeRegion(region);
				}
			}
		};
		delete.setToolTipText("Delete selected region, if there is one.");

		getSite().getActionBars().getToolBarManager().add(delete);
		getSite().getActionBars().getMenuManager().add(delete);

		final Separator sep = new Separator(getClass().getName()+".separator1");
		getSite().getActionBars().getToolBarManager().add(sep);
		getSite().getActionBars().getMenuManager().add(sep);

		final Action show = new Action("Show all vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(true);
				}
			}
		};
		show.setToolTipText("Show vertices in all visible regions");

		getSite().getActionBars().getToolBarManager().add(show);
		getSite().getActionBars().getMenuManager().add(show);


		final Action clear = new Action("Show no vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-clear.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(false);
				}
			}
		};
		clear.setToolTipText("Clear all vertices shown in the plotting");

		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);

		createRightClickMenu();
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems()) menuManager.add(item);
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
	}
	
	private void createColumns(final TableViewer viewer) {

		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		var.getColumn().setText("X position");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 0));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Y position");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 1));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Data value");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 2));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("q X (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 3));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("q Y (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 4));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
		var.getColumn().setText("q Z (1/\u00c5)");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 5));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 6);
		var.getColumn().setText("2\u03b8 (\u00b0)");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 6));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 7);
		var.getColumn().setText("Resolution (\u00c5)");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 7));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 8);
		var.getColumn().setText("Dataset name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider(this, 8));
		
	}

	public ROIBase getBounds(IRegion region) {
		if (dragBounds!=null&&dragBounds.containsKey(region.getName())) return dragBounds.get(region.getName());
		return region.getROI();
	}
	
	private void updateRegion(ROIEvent evt) {

		if (viewer!=null) {
			IRegion  region = (IRegion)evt.getSource();

			if (region.getRegionType().toString().contains("XAXIS_LINE")){
				this.xValues[0] = evt.getROI().getPointX();
			}
			if (region.getRegionType().toString().contains("YAXIS_LINE")){
				this.yValues[0] = evt.getROI().getPointY();
			}
			
			ROIBase rb = evt.getROI();
			
			dragBounds.put(region.getName(), rb);
			viewer.refresh(region);
		}
	}
	

	@Override
	public void regionCreated(RegionEvent evt) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		updateRegion(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {

	}
	
}
