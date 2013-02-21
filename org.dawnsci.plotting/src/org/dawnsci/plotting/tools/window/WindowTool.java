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

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.ISurfaceTrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * A tool which has one box region for configuring the region
 * which defines the window of a 3D plot.
 * 
 * TODO Add aspect ratio controls like the old windowing tool used to have.
 * 
 * @author fcp94556
 *
 */
public class WindowTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(WindowTool.class);
	
	private AbstractPlottingSystem windowSystem;
	private IRegionListener        regionListener;
	private IROIListener           roiListener;
	private ITraceListener         traceListener;
	private WindowJob              windowJob;
 
	public WindowTool() {
		try {
			this.windowSystem  = PlottingFactory.createPlottingSystem();
			this.windowJob     = new WindowJob();
			
			this.traceListener = new ITraceListener.Stub() {
				protected void update(TraceEvent evt) {
					ISurfaceTrace trace = getSurfaceTrace();
					if (trace!=null) {
						updateWindowPlot(trace);
					} else {
						windowSystem.clear();
					}
				}
			};
			
			this.roiListener = new IROIListener.Stub() {
				public void update(ROIEvent evt) {
					windowJob.schedule(evt.getROI());
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
	
	
	protected void updateWindowPlot(ISurfaceTrace trace) {
		AbstractDataset data = trace.getData();
		List<AbstractDataset> axes = trace.getAxes();
		if (axes!=null) axes = Arrays.asList(axes.get(0), axes.get(1));
		windowSystem.updatePlot2D(data, axes, null);		
	}


	@Override
	public void activate() {
		super.activate();
		
		if (getPlottingSystem()!=null) {
			
			getPlottingSystem().addTraceListener(traceListener);
			
		}
		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
			
			final ISurfaceTrace surface = getSurfaceTrace();
			if (surface!=null) updateWindowPlot(surface);

			windowSystem.addRegionListener(regionListener);
			
			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.addROIListener(roiListener);
		}
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
	}


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_3D;
	}

	@Override
	public void createControl(Composite parent) {
		
		windowSystem.createPlotPart(parent, getTitle(), getSite().getActionBars(), PlotType.IMAGE, getPart());
		
		try {
			final ISurfaceTrace surface = getSurfaceTrace();
			if (surface!=null) {
							
			    final IRegion region = windowSystem.createRegion("Window", RegionType.BOX);
			    region.setROI(surface.getWindow());
			    windowSystem.addRegion(region);
			}
			
		} catch (Exception e) {
			logger.error("Cannot create region for surface!", e);
		}
		

	}

	@Override
	public Control getControl() {
		return windowSystem.getPlotComposite();
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
		if (windowSystem!=null) windowSystem.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (windowSystem!=null) windowSystem.dispose();
		windowSystem = null;
	}
	
	
	private class WindowJob extends Job {

		private ROIBase window;

		public WindowJob() {
			super("Window");
			setPriority(Job.INTERACTIVE);
			setUser(false);
			setSystem(true);
		}
		
		protected void schedule(ROIBase window) {
			cancel();
			this.window = window;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			final ISurfaceTrace surface = getSurfaceTrace();
			if (surface!=null) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						surface.setWindow(window);
					}
				});
			}
			return Status.OK_STATUS;
		}
		
	}

}
