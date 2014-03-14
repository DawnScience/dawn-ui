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
package org.dawnsci.plotting.tools.window;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.richbeans.components.scalebox.IntegerBox;
import org.dawnsci.common.richbeans.components.scalebox.NumberBox;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.util.PlottingUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.IPaletteListener;
import org.dawnsci.plotting.api.trace.IPaletteTrace;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.IWindowTrace;
import org.dawnsci.plotting.api.trace.PaletteEvent;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.mihalis.opal.rangeSlider.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * A tool which has one box region for configuring the region
 * which defines the window of a 3D plot.
 * 
 * @author fcp94556
 *
 */
public class WindowTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(WindowTool.class);
	
	private IPlottingSystem        windowSystem;
	private IRegionListener        regionListener;
	private IROIListener           roiListener;
	private ITraceListener         traceListener;
	private IPaletteListener       paletteListener;
	private WindowJob              windowJob;
	private Composite              sliceControl, windowControl, blankComposite;
	private Composite              content;

	private RegionControlWindow regionControlWindow;

	public WindowTool() {
		try {
			this.windowSystem  = PlottingFactory.createPlottingSystem();
			this.windowJob     = new WindowJob();

			this.traceListener = new ITraceListener.Stub() {
				protected void update(TraceEvent evt) {
					ITrace trace = getTrace();
					if (trace!=null) {
						if (trace instanceof ISurfaceTrace)
							updateWindowPlot((ISurfaceTrace)trace);
					} else {
						windowSystem.clear();
					}
				}
			};
			
			this.paletteListener = new IPaletteListener.Stub() {
				@Override
				public void paletteChanged(PaletteEvent evt) {
					try {
					     ITrace trace = windowSystem.getTraces().iterator().next();
					     if (trace instanceof IPaletteTrace) {
					    	 ((IPaletteTrace)trace).setPaletteData(evt.getPaletteData()); 
					     }
					} catch (Exception ne) {
						logger.error("Cannot set new palette.", ne);
					}
				}
			};

			this.roiListener = new IROIListener() {
				@Override
				public void roiDragged(ROIEvent evt) {
					IROI roi = evt.getROI();
					if (roi!=null && roi instanceof RectangularROI){
						SurfacePlotROI sroi = getSurfacePlotROI((RectangularROI)roi, true);
						windowJob.schedule(sroi);
					}
				}

				@Override
				public void roiChanged(ROIEvent evt) {
					IROI roi = evt.getROI();
					if (roi != null && roi instanceof RectangularROI){
						SurfacePlotROI sroi = getSurfacePlotROI((RectangularROI)roi, false);
						windowJob.schedule(sroi);
					}
				}

				@Override
				public void roiSelected(ROIEvent evt) {
					
				}
			};
			
			this.regionListener = new IRegionListener.Stub() {
				@Override
				public void regionAdded(RegionEvent evt) {
					evt.getRegion().addROIListener(roiListener);
				}

				@Override
				public void regionRemoved(RegionEvent evt) {
					evt.getRegion().removeROIListener(roiListener);
				}			
			};

		} catch (Exception e) {
			logger.error("Cannot create a plotting system, something bad happened!", e);
		}
	}

	@Override
	public void createControl(Composite parent) {
		
		this.content = new Composite(parent, SWT.NONE);
		final StackLayout stackLayout = new StackLayout();
		content.setLayout(stackLayout);

		this.regionControlWindow = new RegionControlWindow(content, getPlottingSystem(), windowSystem, windowJob);

		this.windowControl = regionControlWindow.createRegionControl(getTitle(), getSite(), getViewPart(), getImageDescriptor());
        this.sliceControl = createSliceControl();
   
        final ITrace trace = getTrace();
		if (trace instanceof ISurfaceTrace) {
			stackLayout.topControl = windowControl;
		} else if (trace instanceof ILineStackTrace) {
		    stackLayout.topControl = sliceControl;
		}
		
		this.blankComposite = new Composite(content, SWT.BORDER);
        
	}

	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return windowSystem;
	}

	private CLabel      errorLabel;
	private RangeSlider sliceSlider;
	private NumberBox   lowerControl, upperControl;
	private int         lastLower = -1, lastUpper = -1;

	private Composite createSliceControl() {
		Composite sliceControl = new Composite(content, SWT.NONE);
		sliceControl.setLayout(new GridLayout(3, false));
		
		final Label info = new Label(sliceControl, SWT.WRAP);
		info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 2));
		info.setText("Please edit the window of the data, not more than 100 symultaneous plots are allowed in 3D.");
	
		sliceSlider = new RangeSlider(sliceControl, SWT.HORIZONTAL);
		sliceSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2));
		sliceSlider.setMinimum(0);
		sliceSlider.setMaximum(25);
		sliceSlider.setLowerValue(0);
		sliceSlider.setUpperValue(25);
		sliceSlider.setIncrement(1);
		
		GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1);
		gridData.widthHint=130;
		
        lowerControl = new IntegerBox(sliceControl, SWT.NONE);
        lowerControl.setLabel(" Lower    ");
        lowerControl.setLayoutData(gridData);
        lowerControl.setIntegerValue(sliceSlider.getLowerValue());
        lowerControl.setActive(true);
        lowerControl.on();
        lowerControl.addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				sliceSlider.setLowerValue(lowerControl.getIntegerValue());
				final int lower = sliceSlider.getLowerValue();
				final int upper = sliceSlider.getUpperValue();
				if (lower<0 || upper<0)                   return;
				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
				lastLower = lower;
				lastUpper = upper;
			}
		});
 
        upperControl = new IntegerBox(sliceControl, SWT.NONE);
        upperControl.setLabel(" Upper    ");
        upperControl.setLayoutData(gridData);
        upperControl.setIntegerValue(sliceSlider.getUpperValue());
        upperControl.setActive(true);
        upperControl.on();
        upperControl.addValueListener(new ValueAdapter() {			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				sliceSlider.setUpperValue(upperControl.getIntegerValue());
				final int lower = sliceSlider.getLowerValue();
				final int upper = sliceSlider.getUpperValue();
				if (lower<0 || upper<0)                   return;
				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
				lastLower = lower;
				lastUpper = upper;
			}
		});

        upperControl.setMinimum(lowerControl);
        upperControl.setMaximum(25);
        lowerControl.setMinimum(0);
        lowerControl.setMaximum(upperControl);
        
		errorLabel = new CLabel(sliceControl, SWT.WRAP);
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		errorLabel.setText("The slice range is too large.");
		errorLabel.setImage(Activator.getImage("icons/error.png"));
        
		sliceSlider.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if ((e.button & SWT.BUTTON1)==0) {
					final int lower = sliceSlider.getLowerValue();
					final int upper = sliceSlider.getUpperValue();
					if (lower<0 || upper<0)                   return;
					if (lower==lastLower && upper==lastUpper) return;
					
					sliceSlider.setToolTipText("("+sliceSlider.getMinimum()+")  "+lower+" <-> "+upper+"  ("+sliceSlider.getMaximum()+")");
					upperControl.setIntegerValue(upper);
					lowerControl.setIntegerValue(lower);
					updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
					
					lastLower = lower;
					lastUpper = upper;
				}
			}
		});

		return sliceControl;
	}

	protected void updateSliceRange(int lower, int upper, int max, boolean setValue) {
		if (upper-lower>100) {
			GridUtils.setVisible(errorLabel, true);
			errorLabel.getParent().layout();
			return;
		}
		
		GridUtils.setVisible(errorLabel, false);
		errorLabel.getParent().layout();

		if (setValue) { // Send to UI
			sliceSlider.setMaximum(max);
			sliceSlider.setLowerValue(lower);
			sliceSlider.setUpperValue(upper);
			lowerControl.setIntegerValue(lower);
			upperControl.setIntegerValue(upper);
	        upperControl.setMaximum(max);
		} else {        // Send to region
			final LinearROI   roi   = new LinearROI(new double[]{lower,0}, new double[]{upper,0});
			windowJob.schedule(roi);
		}
	
	}

	protected void updateTrace(ITrace trace) {
		if (content == null)
			return;
		if (trace instanceof ISurfaceTrace) {
		    setActionsEnabled(true);
			updateWindowPlot((ISurfaceTrace)trace);
			((ISurfaceTrace)trace).addPaletteListener(paletteListener);
		} else if (trace instanceof ILineStackTrace) {
		    setActionsEnabled(false);
		    updateSlicePlot((ILineStackTrace)trace);
		} else {
			setActionsEnabled(false);
			StackLayout stackLayout = (StackLayout)content.getLayout();
			stackLayout.topControl  = blankComposite;
			content.layout();
		}
	}

	private void setActionsEnabled(boolean enabled) {
		if (getSite() == null)
			return;
		IContributionManager[] mans = new IContributionManager[]{ 
				                             getSite().getActionBars().getToolBarManager(),
				                             getSite().getActionBars().getMenuManager()};
		for (IContributionManager man : mans) {
			IContributionItem[] items = man.getItems();
			for (IContributionItem item : items) {
				item.setVisible(enabled);
			}
			man.update(true);
		}
	}

	private SurfacePlotROI getSurfacePlotROI(RectangularROI rroi, boolean isDrag) {
		int startX = (int)Math.round(rroi.getPointX());
		int startY = (int)Math.round(rroi.getPointY());
		int roiWidth = (int)Math.round(rroi.getLengths()[0]);
		int roiHeight = (int)Math.round(rroi.getLengths()[1]);
		int endX = (int)Math.round(rroi.getEndPoint()[0]);
		int endY = (int)Math.round(rroi.getEndPoint()[1]);
		regionControlWindow.setSpinnerValues(startX, startY, roiWidth, roiHeight);

		int xAspectRatio = 0, yAspectRatio = 0, binShape = 1, samplingMode = 0;
		if (regionControlWindow.isOverwriteAspect()){
			xAspectRatio = regionControlWindow.getXAspectRatio();
			yAspectRatio = regionControlWindow.getYAspectRatio();
		}
		binShape = PlottingUtils.getBinShape(rroi.getLengths()[0], rroi.getLengths()[1], isDrag);

		if (binShape != 1) {
			// DownsampleMode.MEAN = 2
			samplingMode = 2; 
		}
		SurfacePlotROI sroi = new SurfacePlotROI(startX, 
				startY, 
				endX, 
				endY, 
				samplingMode, samplingMode,
				xAspectRatio, 
				yAspectRatio);
		sroi.setXBinShape(binShape);
		sroi.setYBinShape(binShape);
		return sroi;
	}

	protected void updateWindowPlot(ISurfaceTrace trace) {
		AbstractDataset data =  (AbstractDataset)trace.getData();
		List<IDataset> axes = trace.getAxes();
		if (axes!=null) axes = Arrays.asList(axes.get(0), axes.get(1));
		windowSystem.updatePlot2D(data, axes, null);
		if (regionControlWindow != null && regionControlWindow.isControlReady())
			regionControlWindow.createSurfaceRegion("Window", true);
		// manage layout
		if (content != null && content.isDisposed()) return;
		StackLayout stackLayout = (StackLayout)content.getLayout();
		stackLayout.topControl = windowControl;
		content.layout();
	}

	protected void updateSlicePlot(ILineStackTrace trace) {
		
		StackLayout stackLayout = (StackLayout)content.getLayout();
		stackLayout.topControl  = sliceControl;
		
		final LinearROI roi = (LinearROI)trace.getWindow();
		if (roi!=null) {
			final int lower = roi.getIntPoint()[0];
			final int upper = (int)Math.round(((LinearROI)roi).getEndPoint()[0]);
		    updateSliceRange(lower, upper, trace.getStack().length, true);
		}
		content.layout();
	}

	@Override
	public void activate() {
		super.activate();

		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
			final ITrace trace = getTrace();
			if (trace!=null) updateTrace(trace);

			windowSystem.addRegionListener(regionListener);

			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.addROIListener(roiListener);
			windowJob.schedule();
		}
		if (regionControlWindow != null)
			regionControlWindow.addSelectionListener();
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
			
		}
		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
			windowSystem.removeRegionListener(regionListener);
			
			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.removeROIListener(roiListener);
		}
		if (regionControlWindow != null)
			regionControlWindow.removeSelectionListener();
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_3D;
	}

	@Override
	public Control getControl() {
		return content;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return windowSystem;
		} else {
			return super.getAdapter(clazz);
		}
	}

	@Override
	public void setFocus() {
		if (windowSystem!=null && !windowSystem.isDisposed()) windowSystem.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public class WindowJob extends Job {

		private IROI window;

		public WindowJob() {
			super("Window");
			setPriority(Job.INTERACTIVE);
			setUser(false);
			setSystem(true);
		}
		
		protected void schedule(IROI window) {
			cancel();
			this.window = window;
			schedule();
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			final IWindowTrace windowTrace = getWindowTrace();
			if (windowTrace!=null) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (monitor.isCanceled()) return;
						monitor.beginTask("Sending data to plot", 100);
						IStatus result = windowTrace.setWindow(window, monitor);
						if (result == Status.CANCEL_STATUS) return;
						monitor.worked(100);
					}
				});
			} else {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}
}
