/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.TraceUtils;
import org.dawnsci.slicing.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * Display a 3D dataset across two plots with ROI slicing
 */
public class HyperComponent { 
	
	private IPlottingSystem mainSystem;
	private IPlottingSystem sideSystem;
	private IRegionListener regionListenerLeft;
	private IROIListener roiListenerLeft;
	private IROIListener roiListenerRight;
	private HyperDeligateJob leftJob;
	private HyperDeligateJob rightJob;
	private IAction reselect;
	private IAction baseline;
	private IRegion windowRegion;
	private Composite mainComposite;
	private IWorkbenchPart part;
	private final static Logger logger = LoggerFactory.getLogger(HyperComponent.class);
	
	public HyperComponent(IWorkbenchPart part) {
		this.part = part;
	}

	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		sashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		
		createPlottingSystems(sashForm);
	}
	
	public void setData(ILazyDataset lazy, List<AbstractDataset> daxes, Slice[] slices, int[] order) {
		this.setData(lazy, daxes, slices, order, new TraceReducer(), new ImageTrapeziumBaselineReducer());
	}
	
	public void setData(ILazyDataset lazy, List<AbstractDataset> daxes, Slice[] slices, int[] order,
			IDatasetROIReducer mainReducer, IDatasetROIReducer sideReducer) {
		
		this.leftJob = new HyperDeligateJob("Left update",
				sideSystem,
				lazy,
				daxes,
				slices, order, mainReducer);
		
		this.rightJob = new HyperDeligateJob("Right update",
				mainSystem,
				lazy,
				daxes,
				slices,
				order, sideReducer);
		
		if (sideReducer instanceof ImageTrapeziumBaselineReducer) {
			((ImageTrapeziumBaselineReducer)rightJob.getReducer()).setSubtractBaseline(baseline.isChecked());
		}
		
		if (mainReducer.isOutput1D()) {
			baseline.setEnabled(true);
		} else {
			baseline.setEnabled(false);
		}
		
		if (mainReducer.supportsMultipleRegions()) {
			reselect.setEnabled(true);
		} else {
			reselect.setEnabled(false);
		}
		
		mainSystem.clear();
		mainSystem.getAxes().clear();
		List<AbstractDataset> ax2d = new ArrayList<AbstractDataset>();
		ax2d.add(daxes.get(0));
		ax2d.add(daxes.get(1));
		mainSystem.createPlot2D(AbstractDataset.zeros(new int[] {(int)daxes.get(0).count(), (int)daxes.get(1).count()}, AbstractDataset.INT16), ax2d, null);
		
		for (IRegion region : mainSystem.getRegions()) {
			mainSystem.removeRegion(region);
		}
		
		sideSystem.clear();
		sideSystem.getAxes().clear();
		
		if (mainReducer.isOutput1D()) {
			List<AbstractDataset> xd = new ArrayList<AbstractDataset>();
			xd.add(AbstractDataset.zeros(new int[] {(int)daxes.get(2).count()},AbstractDataset.INT16));
			sideSystem.createPlot1D(daxes.get(2),xd, null);
		} else {
			List<AbstractDataset> xd = new ArrayList<AbstractDataset>();
			xd.add(AbstractDataset.arange(10, AbstractDataset.INT32));
			xd.add(daxes.get(2));
			sideSystem.createPlot2D(AbstractDataset.ones(new int[] {10,(int)xd.get(1).count()}, AbstractDataset.INT32), xd, null);
		}
		
		for (IRegion region : sideSystem.getRegions()) {
			sideSystem.removeRegion(region);
		}
		
		try {
			IRegion region = mainSystem.createRegion("Image Region 1", mainReducer.getSupportedRegionType().get(0));
			
			mainSystem.addRegion(region);
			IROI rroi = mainReducer.getInitialROI(daxes,order);
			region.setROI(rroi);
			region.addROIListener(this.roiListenerLeft);
			sideSystem.clear();
			updateRight(region, rroi);
			
			windowRegion = sideSystem.createRegion("Trace Region 1", sideReducer.getSupportedRegionType().get(0));
			
			IROI broi = sideReducer.getInitialROI(daxes,order);
			windowRegion.setROI(broi);
			windowRegion.setUserRegion(false);
			windowRegion.addROIListener(this.roiListenerRight);
			sideSystem.addRegion(windowRegion);
			updateLeft(windowRegion,broi);
			
		} catch (Exception e) {
			logger.error("Error adding regions to hyperview: " + e.getMessage());
		}
	}

	public void setFocus() {
		mainComposite.setFocus();
	}
	
	public void dispose() {
		
		if (mainSystem != null && !mainSystem.isDisposed()) mainSystem.dispose();
		if (sideSystem != null && !sideSystem.isDisposed()) sideSystem.dispose();
		
		if (leftJob != null) leftJob.cancel();
		if (rightJob != null) rightJob.cancel();
		
	}
	
	private void createPlottingSystems(SashForm sashForm) {
		try {
			mainSystem = PlottingFactory.createPlottingSystem();
			mainSystem.setColorOption(ColorOption.NONE);
			mainComposite = new Composite(sashForm, SWT.NONE);
			mainComposite.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(mainComposite);

			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(mainComposite, null);

			reselect = new Action("Create new profile", SWT.TOGGLE) {
				@Override
				public void run() {
					if (reselect.isChecked()) {
						createNewRegion();
					} else {
						IContributionItem item = mainSystem.getActionBars().getToolBarManager().find("org.csstudio.swt.xygraph.undo.ZoomType.NONE");
						if (item != null && item instanceof ActionContributionItem) {
							((ActionContributionItem)item).getAction().run();
						}
					}
				}
			};
			
			reselect.setImageDescriptor(Activator.getImageDescriptor("icons/ProfileBox2.png"));
			
			actionBarWrapper.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newProfileGroup"));
			actionBarWrapper.getToolBarManager().add(reselect);
			actionBarWrapper.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newProfileGroupAfter"));
			
			Composite displayPlotComp  = new Composite(mainComposite, SWT.BORDER);
			displayPlotComp.setLayout(new FillLayout());
			displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mainSystem.createPlotPart(displayPlotComp, 
													 "HyperImage", 
													 actionBarWrapper, 
													 PlotType.IMAGE, 
													 part);
			
			mainSystem.repaint();
			
			sideSystem = PlottingFactory.createPlottingSystem();
			sideSystem.setColorOption(ColorOption.NONE);
			Composite sideComp = new Composite(sashForm, SWT.NONE);
			sideComp.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(sideComp);
			ActionBarWrapper actionBarWrapper1 = ActionBarWrapper.createActionBars(sideComp, null);
			Composite sidePlotComp  = new Composite(sideComp, SWT.BORDER);
			sidePlotComp.setLayout(new FillLayout());
			sidePlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			baseline = new Action("Linear baseline", SWT.TOGGLE) {
				@Override
				public void run() {
					if (rightJob != null && rightJob.getReducer() instanceof ImageTrapeziumBaselineReducer) {
						ImageTrapeziumBaselineReducer reducer = (ImageTrapeziumBaselineReducer)rightJob.getReducer();
						reducer.setSubtractBaseline(isChecked());
					}
					
					IROI roi = windowRegion.getROI();
					updateLeft(windowRegion,roi);
				}
			};
			
			baseline.setImageDescriptor(Activator.getImageDescriptor("icons/LinearBase.png"));
			actionBarWrapper1.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newBaselineGroup"));
			actionBarWrapper1.getToolBarManager().add(baseline);
			actionBarWrapper1.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newBaselineGroup"));
			
			sideSystem.createPlotPart(sidePlotComp, 
													 "HyperTrace", 
													 actionBarWrapper1, 
													 PlotType.XY, 
													 null);
			
			regionListenerLeft = getRegionListenerToLeft();
			mainSystem.addRegionListener(regionListenerLeft);
			roiListenerLeft = getROIListenerToRight();
			roiListenerRight = getROIListenerLeft();
			
		} catch (Exception e) {
			logger.error("Error creating hyperview plotting systems: " + e.getMessage());
		}
	}
	
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		
		if (clazz == Page.class) {
			// TODO Page for helping with part
		} else if (clazz == IToolPageSystem.class || clazz == IPlottingSystem.class) {
			return mainSystem;
		}
		return null;
	}

	
	protected final void createNewRegion() {
		// Start with a selection of the right type
		try {
			IRegion region = mainSystem.createRegion(RegionUtils.getUniqueName("Image Region", mainSystem),leftJob.getReducer().getSupportedRegionType().get(0));
			region.addROIListener(roiListenerLeft);
		} catch (Exception e) {
			logger.error("Error creating hyperview new region: " + e.getMessage());
		}
	}
	
	private IRegionListener getRegionListenerToLeft() {
		return new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() instanceof IRegion) {
						if (((IRegion)trace.getUserObject()).isUserRegion()) {
							sideSystem.removeTrace(trace);
						}
					}
				}
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() == evt.getSource()) {
						sideSystem.removeTrace(trace);
					}
				}
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion() != null) {
					evt.getRegion().setUserRegion(true);
					evt.getRegion().addROIListener(roiListenerLeft);
					
					if (reselect.isChecked()) {
						createNewRegion();
					}
					
				}
				
			}
		};
	}
	
	private IROIListener getROIListenerToRight() {

		return new IROIListener.Stub() {
						
			@Override
			public void roiDragged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
				
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
			}
		};
	}
	
	private IROIListener getROIListenerLeft() {
		return new IROIListener.Stub() {
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				
			}
		};
	}
	
	protected void updateRight(IRegion r, IROI rb) {
		
		leftJob.profile(r, rb);
	}
	
	protected void updateLeft(IRegion r, IROI rb) {

		rightJob.profile(r,rb);
	}
	
	private void updateImage(final IPlottingSystem plot, final IDataset image, final List<IDataset> axes) {
		
		plot.updatePlot2D(image, axes, null);
		
	}
	
	private void updateTrace(final IPlottingSystem plot, final IDataset axis, final IDataset data, final boolean update, final IRegion region) {

		if (update) {
			plot.updatePlot1D(axis,Arrays.asList(new IDataset[] {data}), null);
			plot.repaint();	
		} else {
			final List<ITrace> traceOut = plot.createPlot1D(axis,Arrays.asList(new IDataset[] {data}), null);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					for (ITrace trace : traceOut) {
						trace.setUserObject(region);
						if (trace instanceof ILineTrace){
							region.setRegionColor(((ILineTrace)trace).getTraceColor());
						}
					}
				}
			});
		}
	}
	
	private void updateTrace(final IPlottingSystem plot, final IDataset axis, final IDataset data) {

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				Collection<ITrace> traces = plot.getTraces(ILineTrace.class);

				List<IDataset> datasets = convertFrom2DToListOf1D(axis, data);

				if (traces == null || traces.isEmpty()) {
					plot.createPlot1D(axis, datasets, null);
					return;
				}
				int i = 0;
				for (ITrace trace : traces) {
					if (i < datasets.size()) ((ILineTrace)trace).setData(axis, datasets.get(i));
					else plot.removeTrace(trace);
					i++;
				}

				if (i >= datasets.size()) {
					plot.repaint();
					return;
				}

				List<IDataset> subdatasets = new ArrayList<IDataset>(datasets.size() - i);

				for (; i < datasets.size(); ++i) {
					subdatasets.add(datasets.get(i));
				}

				plot.createPlot1D(axis, subdatasets, null);
				plot.repaint();
			}
		});
	}
	
	private List<IDataset> convertFrom2DToListOf1D(IDataset axis, IDataset data) {
		
		int[] dataShape = data.getShape();
		
		List<IDataset> datasets = new ArrayList<IDataset>(dataShape[0]);
		Slice[] slices = new Slice[2];
		slices[0] = new Slice(0,1,1);
		
		for (int i = 0; i < dataShape[0]; i++) {
			slices[0].setStart(i);
			slices[0].setStop(i+1);
			
			IDataset out = data.getSlice(slices);
			
			out.setName("trace_" + i);
			
			datasets.add(out);
		}
		
		return datasets;
		
	}

	private class HyperDeligateJob extends Job {
		
		private IRegion currentRegion;
		private IROI currentROI;
		private IPlottingSystem plot;
		private ILazyDataset data;
		private List<AbstractDataset> axes;
		private int[] order;
		private Slice[] slices;
		private IDatasetROIReducer reducer;
		
		
		public HyperDeligateJob(String name,
				IPlottingSystem plot,
				ILazyDataset data,
				List<AbstractDataset> axes,
				Slice[] slices,
				int[] order,
				IDatasetROIReducer reducer) {
			
			super(name);
			this.plot = plot;
			this.data = data;
			this.axes = axes;
			this.order = order;
			this.slices = slices;
			this.reducer = reducer;
			setSystem(false);
			setUser(false);
		}
		
		public void profile(IRegion r, IROI rb) {
			this.currentRegion = r;
			this.currentROI    = rb;
	        
          	schedule();		
		}
		
		public IDatasetROIReducer getReducer() {
			return reducer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				IDataset output = this.reducer.reduce(data, axes, currentROI, slices, order);
				List<IDataset> outputAxes = this.reducer.getAxes();

				if (!this.reducer.isOutput1D()) {
					output.setName("Image");
					updateImage(plot,output,outputAxes);
				} else {
					
					IDataset axis = null;
					
					if (outputAxes != null && !outputAxes.isEmpty()) {
						axis = outputAxes.get(0);
					}
					
					if (output.getRank() == 1) {
						Collection<ITrace> traces = plot.getTraces();
						for (ITrace trace : traces) {
							Object uo = trace.getUserObject();
							if (uo == currentRegion) {
								output.setName(trace.getName());
								updateTrace(plot,axis,output,true,currentRegion);
								return Status.OK_STATUS;
							}
						}

						String name = TraceUtils.getUniqueTrace("trace", plot, (String[])null);
						output.setName(name);
						
						updateTrace(plot,axis,output,false,currentRegion);
					} else {
						updateTrace(plot,axis,output);
					}
					
				}

				return Status.OK_STATUS;
			} catch (Throwable ne) {
				return Status.CANCEL_STATUS;
			}
		}
	}
}
