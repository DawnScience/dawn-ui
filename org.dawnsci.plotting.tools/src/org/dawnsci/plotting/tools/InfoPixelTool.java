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
import java.util.List;

import org.dawnsci.plotting.tools.preference.InfoPixelConstants;
import org.dawnsci.plotting.tools.preference.InfoPixelPreferencePage;
import org.dawnsci.plotting.tools.region.RegionColorListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.MouseEvent;
import org.eclipse.dawnsci.plotting.api.region.MouseListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;

public abstract class InfoPixelTool extends AbstractToolPage implements IROIListener, IRegionListener, MouseListener {

	private final static Logger logger = LoggerFactory.getLogger(InfoPixelTool.class);
	
	private   ITraceListener         traceListener;
	private   IRegion                xHair, yHair;
	
	private Composite     composite;
	private TableViewer   viewer;
	private RegionColorListener viewUpdateListener;


	// Jobs
	private Job updateInfoPixelData;
		
	// Internal jobs items
	@SuppressWarnings("unused")
	private boolean isUpdateRunning = false;

	private IPropertyChangeListener propListener;

			
	public InfoPixelTool() {
		
		try {
			
			this.traceListener = new ITraceListener.Stub() {
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

		this.propListener = new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isActive()) {
					if(isInterestedProperty(event))
						viewer.refresh();
				}
 			}

			private boolean isInterestedProperty(PropertyChangeEvent event) {
				final String propName = event.getProperty();
				return InfoPixelConstants.PIXEL_POS_FORMAT.equals(propName) ||
						InfoPixelConstants.DATA_FORMAT.equals(propName)     ||
						InfoPixelConstants.Q_FORMAT.equals(propName)        ||
						InfoPixelConstants.THETA_FORMAT.equals(propName)    ||
						InfoPixelConstants.RESOLUTION_FORMAT.equals(propName);
			}
		};
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
		
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		Activator.getPlottingPreferenceStore().addPropertyChangeListener(propListener);
		
		createRegions();
		if (xHair!=null) {
			xHair.addMouseListener(this);
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
			getPlottingSystem().setDefaultCursor(IPlottingSystem.CROSS_CURSOR);
		}

		// We stop the adding of other regions because this tool does
		// not like it when other regions are added.
		setOtherRegionsEnabled(false);
		
		// Needed to refresh the table when activated as other tools may create points
		// which should be in the table.
		try {
			if (viewer != null) {
				viewer.getTable().clearAll();
				viewer.refresh();
			}
		} catch (Throwable ignored) {
			// Not a failure if we cannot refresh.
		}
		super.activate();
	
	}
	
	private void setOtherRegionsEnabled(boolean isVisible) {

        final IActionBars bars = getPlottingSystem().getActionBars();
        if (bars.getToolBarManager().find(BasePlottingConstants.ADD_REGION)!=null) {
        	bars.getToolBarManager().find(BasePlottingConstants.ADD_REGION).setVisible(isVisible);
        	bars.getToolBarManager().update(true);
        }
        if (bars.getMenuManager().find(BasePlottingConstants.ADD_REGION)!=null) {
        	bars.getMenuManager().find(BasePlottingConstants.ADD_REGION).setVisible(isVisible);
        	bars.getMenuManager().update(true);
        }
	}

	public void deactivate() {
		
		super.deactivate();
		Activator.getPlottingPreferenceStore().removePropertyChangeListener(propListener);
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
			getPlottingSystem().setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}

		try {
			if (getPlottingSystem()!=null) {
				getPlottingSystem().removeTraceListener(traceListener);
				getPlottingSystem().removeRegionListener(this);
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions!=null)
					for (IRegion iRegion : regions)
						iRegion.removeROIListener(this);
			}
		} catch (Exception e) {
			logger.error("Cannot remove region listeners!", e);
		}
	}

	public void dispose() {
		
		Activator.getPlottingPreferenceStore().removePropertyChangeListener(propListener);
		
		if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
		viewUpdateListener = null;

		if (viewer!=null) viewer.getControl().dispose();

		super.dispose();
	}
	
	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		
		if(evt.getButton() == 1){// left click
			if (!isActive()) return;

			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions==null || regions.isEmpty()) logger.debug("no region selected");		

			try {
				// add a point region
				final IRegion point = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Point", getPlottingSystem()), RegionType.POINT);
				final PointROI regionBounds= new PointROI();
				double x = getPlottingSystem().getSelectedXAxis().getPositionValue(evt.getX());
				double y = getPlottingSystem().getSelectedYAxis().getPositionValue(evt.getY());
				regionBounds.setPoint(new double[]{x,y});
				point.setROI(regionBounds);
				point.setMobile(true);

				getPlottingSystem().addRegion(point);


			} catch (Exception e) {
				logger.error("Cannot create point!", e);
			}
		}//end if
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

		final Action copy = new Action("Copy point values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			@Override
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (sel!=null && sel.getFirstElement()!=null) {
					Object selected = sel.getFirstElement();
					if (selected instanceof IRegion) {
						final IRegion region = (IRegion)sel.getFirstElement();
						if (region==null||region.getROI()==null) return;
						final IROI bounds = region.getROI();
						if (bounds.getPointRef()==null) return;

						final Clipboard cb = new Clipboard(composite.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
					}
				}
			}
		};
		copy.setToolTipText("Copies the point values as text to clipboard which can then be pasted externally.");

		getSite().getActionBars().getToolBarManager().add(copy);
		getSite().getActionBars().getMenuManager().add(copy);

		final Action delete = new Action("Delete selected point", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
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
		delete.setToolTipText("Delete selected point, if there is one.");

		getSite().getActionBars().getToolBarManager().add(delete);
		getSite().getActionBars().getMenuManager().add(delete);

		final Separator sep = new Separator(getClass().getName()+".separator1");
		getSite().getActionBars().getToolBarManager().add(sep);
		getSite().getActionBars().getMenuManager().add(sep);

		final Action show = new Action("Show all vertex values", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(isChecked());
				}
			}
		};
		show.setToolTipText("Show vertices in all visible regions");
		show.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png"));
		
		final Action preferences = new Action("Preferences...") {
			public void run() {
				if (!isActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), InfoPixelPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		
		getSite().getActionBars().getToolBarManager().add(show);
		getSite().getActionBars().getMenuManager().add(show);
		getSite().getActionBars().getMenuManager().add(preferences);
		createRightClickMenu();
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems()) menuManager.add(item);
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
	}
	
	protected abstract void createColumns(final TableViewer viewer);
	
	
	public IROI getBounds(IRegion region) {
		return region.getROI();
	}
	
	private void updateRegion(ROIEvent evt) {

		if (viewer!=null) {
			IRegion region = null;
			try {
				region = (IRegion)evt.getSource();

				//viewer.refresh(region);
				updateInfoPixel(region);

			} catch (Exception e) {
				logger.error("problem creating point region:", e);
			} 
		}
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// TODO Auto-generated method stub
		
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
	public void regionCancelled(RegionEvent evt) {
	}


	@Override
	public void roiDragged(ROIEvent evt) {
		//logger.debug("ROI dragged ====> ");
		if (viewer != null) {
			IRegion region = (IRegion) evt.getSource();

			if (region.getRegionType() == RegionType.POINT) {
				// update table for current point region				
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
	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

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
}
