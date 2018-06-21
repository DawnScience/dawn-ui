package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class HyperDelegateJob extends Job {

	private IRegion currentRegion;
	private IROI currentROI;
	private IPlottingSystem<Composite> plot;
	private ILazyDataset data;
	private List<IDataset> axes;
	private int[] order;
	private Slice[] slices;
	private IDatasetROIReducer reducer;


	public HyperDelegateJob(String name,
			IPlottingSystem<Composite> plot,
			ILazyDataset data,
			List<IDataset> axes,
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

		cancel(); // Needed for large datasets but makes small ones look less responsive.
		this.currentRegion = r;
		this.currentROI    = rb;

		schedule();		
	}

	public void updateData(ILazyDataset data,
			List<IDataset> axes,
			Slice[] slices,
			int[] order){
		this.data = data;
		this.axes = axes;
		this.order = order;
		this.slices = slices;
	}

	public IDatasetROIReducer getReducer() {
		return reducer;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;

			IDataset output = this.reducer.reduce(data, axes, currentROI, slices, order, new ProgressMonitorWrapper(monitor));
			if (output==null) return Status.CANCEL_STATUS;

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


	private void updateTrace(final IPlottingSystem<Composite> plot, final IDataset axis, final IDataset data) {

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

	private void updateTrace(final IPlottingSystem<Composite> plot, final IDataset axis, final IDataset data, final boolean update, final IRegion region) {

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
							//use name listener to update color
							String name = region.getName();
							plot.renameRegion(region, name + " Color");
							plot.renameRegion(region, name);
						}
					}
				}
			});
		}
	}

	private void updateImage(final IPlottingSystem<Composite> plot, final IDataset image, final List<IDataset> axes) {

		plot.updatePlot2D(image, axes, null);

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

			datasets.add(out.squeeze());
		}

		return datasets;

	}

}
