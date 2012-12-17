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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.tools.MeasurementTool.RegionColorListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.rcp.pixelinfoutils.Vector3dutil;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public class InfoPixelTool1D extends AbstractToolPage implements IROIListener, IRegionListener, MouseListener {

	private final static Logger logger = LoggerFactory.getLogger(InfoPixelTool1D.class);
	
	protected IPlottingSystem        plotter;
	private   ITraceListener         traceListener;
	private   IRegion                xHair, yHair;
	
	private Composite     composite;
	private TableViewer   viewer;
	private RegionColorListener viewUpdateListener;
	private Map<String,ROIBase> dragBounds;
	public double xValues [] = new double[1];	public double yValues [] = new double[1];

	// Jobs
	private Job updateInfoPixelData;
		
	// Internal jobs items
	@SuppressWarnings("unused")
	private boolean isUpdateRunning = false;
	
	// values arrayList
    ArrayList<String> values = new ArrayList<String>();
	
	// values hashmap
    Hashtable<String, ArrayList<String>> valuesHash = new Hashtable<String, ArrayList<String>>();

			
	public InfoPixelTool1D() {
		dragBounds = new HashMap<String,ROIBase>(7);
		
		try {
			
			plotter = PlottingFactory.createPlottingSystem();
			this.traceListener = new ITraceListener.Stub() {
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
			}
			@Override
			public void dispose() {
			}
			@Override
			public Object[] getElements(Object inputElement) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
				
				final List<IRegion> visible = new ArrayList<IRegion>();
				
				for (IRegion region : regions) {
					
					if (region.getRegionType() == RegionType.XAXIS_LINE)
						visible.add(region);
					
				}
				
				for (IRegion region : regions) {
					
					if (region.getRegionType() == RegionType.POINT)
						visible.add(region);
					
				}

				return visible.toArray(new IRegion[visible.size()]);
			}
		});

		viewer.setInput(new Object());
			
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
				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X Driver", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
				addRegion("Updating x cross hair", xHair);

			}
			
			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y Driver", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
				addRegion("Updating y cross hair", yHair);
			}

		} catch (Exception ne) {
			logger.error("Cannot create initial regions in pixel info tool!", ne);
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
			getPlottingSystem().addRegionListener(this);
//epg			getPlottingSystem().setDefaultCursor(IPlottingSystem..CROSS_CURSOR);
		}
		
		// We stop the adding of other regions because this tool does
		// not like it when other regions are added.
		setOtherRegionsEnabled(false);
		
		// Needed to refresh the table when activated as other tools may create points
		// which should be in the table.
		try {
			viewer.getTable().clearAll();
			viewer.refresh();
		} catch (Throwable ignored) {
			// Not a failure if we cannot refresh.
		}
		
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
			getPlottingSystem().removeRegion(xHair);
			xHair = null;
			updateInfoPixelData = null;
		}
		if (yHair!=null) {
			yHair.setVisible(false);
			yHair.removeROIListener(this);
			getPlottingSystem().removeRegion(yHair);
			yHair = null;
//epg			getPlottingSystem().setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}
		
		plotter.clear();

		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
			getPlottingSystem().removeRegionListener(this);
		}
		
	}
	
	public void dispose() {
		
		if (isActive()){
			deactivate();
		}
		
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
	public void mousePressed(MouseEvent evt) {
		
		if(evt.button == 1){// left click
			if (!isActive()) return;

			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions==null || regions.isEmpty()) logger.debug("no region selected");		

			try {
				// add a point region
				final IRegion point = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Point", getPlottingSystem()), RegionType.POINT);
				final PointROI regionBounds= new PointROI();
				double x = getPlottingSystem().getSelectedXAxis().getPositionValue(evt.x);
				double y = getPlottingSystem().getSelectedYAxis().getPositionValue(evt.y);
				regionBounds.setPoint(new double[]{x,y});
				point.setROI(regionBounds);
				point.setMobile(true);
				point.setTrackMouse(true);

				getPlottingSystem().addRegion(point);


			} catch (Exception e) {
				logger.error("Cannot create point!", e);
			}
		}//end if
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	
	@Override
	public void mouseDoubleClicked(MouseEvent me) {
	}

	private void createActions() {

		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					if (region==null||region.getROI()==null) return;
					final ROIBase bounds = region.getROI();
					if (bounds.getPointRef()==null) return;

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
				
				// ignore table item at index 0 as represents current point
				if (sel!=null && sel.getFirstElement()!=null && viewer.getTable().getSelectionIndex() != 0) {
					final IRegion region = (IRegion)sel.getFirstElement();
					getPlottingSystem().removeRegion(region);
					viewer.refresh();
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
	

	public ROIBase getBounds(IRegion region) {
		if (dragBounds!=null&&dragBounds.containsKey(region.getName())) return dragBounds.get(region.getName());
		return region.getROI();
	}
	
	private void updateRegion(ROIEvent evt) {

		if (viewer!=null) {
			IRegion region = null;
			try {
				region = (IRegion)evt.getSource();
			
				if (region.getRegionType() == RegionType.XAXIS_LINE){
				this.xValues[0] = evt.getROI().getPointX();
			  }
			    if (region.getRegionType() == RegionType.YAXIS_LINE){
				this.yValues[0] = evt.getROI().getPointY();
			  }
							    
				ROIBase rb = evt.getROI();
							
				dragBounds.put(region.getName(), rb);
				//viewer.refresh(region);
				updateInfoPixel(region);
				
			} catch (Exception e) {
				logger.error("problem creating point region:", e);
			} 
		}
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
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive()) return;
		createRegions();
		if (viewer!=null) viewer.refresh();		
	}
	
	@Override
	public void regionCreated(RegionEvent evt) {
	}


	@Override
	public void roiDragged(ROIEvent evt) {
		//logger.debug("ROI dragged ====> ");
		if (viewer != null) {
			IRegion region = (IRegion) evt.getSource();

			if (region.getRegionType() == RegionType.POINT) {
				// update table for current point region
				ROIBase rb = evt.getROI();
				
				dragBounds.put(region.getName(), rb);
				viewer.refresh(region);
				//updateInfoPixel(region);
			} else {
				updateRegion(evt);
			}
		}
	}

	@Override
	public void roiChanged(ROIEvent evt) {
	}
	
	private synchronized void updateInfoPixel(final IRegion region) {

		if (updateInfoPixelData==null) {
			updateInfoPixelData = new Job("Info Pixel update") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						isUpdateRunning = true;
						//logger.debug("Update Running");
						if (monitor.isCanceled()) return Status.CANCEL_STATUS;
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								//Run the update table viewer on a separate thread than the GUI
								viewer.refresh(region);
							
							}

						});
						if (!isActive()) return Status.CANCEL_STATUS;
						return Status.OK_STATUS;
						
					}finally {
						isUpdateRunning = false;
					}
				}
				
			};
			
			updateInfoPixelData.setSystem(true);
			updateInfoPixelData.setUser(false);
			updateInfoPixelData.setPriority(Job.INTERACTIVE);
		}
		updateInfoPixelData.cancel();
		updateInfoPixelData.schedule();

	}	
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}
	
	
	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		var.getColumn().setText("Point ID");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider1D(this, 0));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("X position");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new InfoPixelLabelProvider1D(this, 1));

		var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Y position");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new InfoPixelLabelProvider1D(this, 2));

	}	
}
class InfoPixelLabelProvider1D extends ColumnLabelProvider {


	private final int column;
	private final InfoPixelTool1D tool;
	private final IPlottingSystem plotSystem;

	private static final Logger logger = LoggerFactory.getLogger(InfoPixelLabelProvider1D.class);

	public InfoPixelLabelProvider1D(InfoPixelTool1D tool, int i) {

		this.column = i;
		this.tool   = tool;
		this.plotSystem = tool.getPlottingSystem();
	}


	@Override
	public String getText(Object element) {

		double xIndex = 0.0;
		double yIndex = 0.0;
		double xLabel = Double.NaN;
		double yLabel = Double.NaN;
		
		try {
			if (element instanceof IRegion){
				
				final IRegion region = (IRegion)element;
				IImageTrace trace = tool.getImageTrace();
				
				if (region.getRegionType()==RegionType.POINT) {
					PointROI pr = (PointROI)tool.getBounds(region);
					xIndex = pr.getPointX();
					yIndex = pr.getPointY();
					
					// Sometimes the image can have axes set. In this case we need the point
					// ROI in the axes coordinates
					if (trace!=null) {
						pr = (PointROI)trace.getRegionInAxisCoordinates(pr);
						xLabel = pr.getPointX();
						yLabel = pr.getPointY();
					}
					
				} else {
					xIndex = tool.xValues[0];
					yIndex = tool.yValues[0];
					final double[] dp = new double[]{tool.xValues[0], tool.yValues[0]};
					if (trace!=null) trace.getPointInAxisCoordinates(dp);
					xLabel = dp[0];
					yLabel = dp[1];
				}
	
			}else {
				return null;
			}
			
			if (Double.isNaN(xLabel)) xLabel = xIndex;
			if (Double.isNaN(yLabel)) yLabel = yIndex;
	
			IDiffractionMetadata dmeta = null;
			AbstractDataset set = null;
			final Collection<ITrace> traces = plotSystem.getTraces(IImageTrace.class);
			final IImageTrace trace = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
			if (trace!=null) {
				set = trace.getData();
				final IMetaData      meta = set.getMetadata();
				if (meta instanceof IDiffractionMetadata) {
	
					dmeta = (IDiffractionMetadata)meta;
				}
			}
	
			QSpace qSpace  = null;
			Vector3dutil vectorUtil= null;
			if (dmeta != null) {
	
				try {
					DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
					DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
					
					if (!(detector2dProperties == null)){
						qSpace = new QSpace(detector2dProperties,
								diffractionCrystalEnvironment);
										
						vectorUtil = new Vector3dutil(qSpace, xIndex, yIndex);
					}
				} catch (Exception e) {
					logger.error("Could not create a detector properties object from metadata", e);
				}
			}
	
			switch(column) {
			case 0: // "Point Id"
				return ( ( (IRegion)element).getRegionType() == RegionType.POINT) ? ((IRegion)element).getName(): "";
			case 1: // "X position"
				return String.format("% 4.4f", xLabel);
			case 2: // "Y position"
				return String.format("% 4.4f", yLabel);
			case 3: // "Data value"
				//if (set == null || vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (set == null) return "-";
				return String.format("% 4.4f", set.getDouble((int)yIndex, (int) xIndex));
			case 4: // q X
				//if (vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				return String.format("% 4.4f", vectorUtil.getQx());
			case 5: // q Y
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				return String.format("% 4.4f", vectorUtil.getQy());
			case 6: // q Z
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				return String.format("% 4.4f", vectorUtil.getQz());
			case 7: // 20
				if (vectorUtil==null || qSpace == null) return "-";
				return String.format("% 3.3f", Math.toDegrees(vectorUtil.getQScatteringAngle(qSpace)));
			case 8: // resolution
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				return String.format("% 4.4f", (2*Math.PI)/vectorUtil.getQlength());
			case 9: // Dataset name
				if (set == null) return "-";
				return set.getName();
	
			default:
				return "Not found";
			}
		} catch (Throwable ne) { 
			// Must not throw anything from this method - user sees millions of messages!
			logger.error("Cannot get label!", ne);
			return "";
		}
		
	}

	@Override
	public String getToolTipText(Object element) {
		return "Any selection region can be used in information box tool.";
	}

}
