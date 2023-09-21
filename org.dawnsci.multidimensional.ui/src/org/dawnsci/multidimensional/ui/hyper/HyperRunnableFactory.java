package org.dawnsci.multidimensional.ui.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Factory to build Runnables which wrap the slicing and reduction
 * in IDatasetROIReducer and sending the data to display
 *
 */
public class HyperRunnableFactory {

	private IPlottingSystem<Composite> plot;
	private HyperBean bean;
	private IDatasetROIReducer reducer;

	public HyperRunnableFactory(IPlottingSystem<Composite> plot,
			HyperBean bean,
			IDatasetROIReducer reducer) {

		this.plot = plot;
		this.bean = bean;
		this.reducer = reducer;
	}
	
	public boolean isSupported(RegionType type) {
		return reducer.getSupportedRegionType().contains(type);
	}
	
	public void updateData(HyperBean b) {
		this.bean = b;
	}

	public Runnable createRunnable(IRegion r, IROI rb, boolean invertY) {
		return new HyperRunnable(plot,bean,reducer,r,rb, invertY);
	}


	private class HyperRunnable implements Runnable {

		private IPlottingSystem<Composite> plot;
		private HyperBean bean;
		private IDatasetROIReducer reducer;
		private IRegion region;
		private IROI roi;
		private boolean invertYAxis = false;

		public HyperRunnable(IPlottingSystem<Composite> plot,
				HyperBean bean,
				IDatasetROIReducer reducer, IRegion region, IROI roi, boolean invertY) {

			this.plot = plot;
			this.bean = bean;
			this.reducer = reducer;
			this.region = region;
			this.roi = roi;
			this.invertYAxis = invertY;

		}

		@Override
		public void run() {
			try {

				IDataset output = this.reducer.reduce(bean.data, bean.axes, roi, bean.slices, bean.order);
				if (output==null) return;

				List<IDataset> outputAxes = this.reducer.getAxes();

				if (!this.reducer.isOutput1D()) {
					if (output.getName() == null || output.getName().isEmpty()) {
						output.setName("Image");
					}

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							updateImage(plot,output,outputAxes);
						}});

					
					
				} else {

					IDataset axis = null;

					if (outputAxes != null && !outputAxes.isEmpty()) {
						axis = outputAxes.get(0);
					}

					if (output.getRank() == 1) {
						Collection<ITrace> traces = plot.getTraces();
						for (ITrace trace : traces) {
							Object uo = trace.getUserObject();
							if (uo == region) {
								output.setName(trace.getName());
								updateTrace(plot,axis,output,true,region);
								return;
							}
						}

						String name = TraceUtils.getUniqueTrace("trace", plot, (String[])null);
						output.setName(name);

						updateTrace(plot,axis,output,false,region);
					} else {
						updateTrace(plot,axis,output);
					}

				}

			} catch (Throwable ne) {

			}

		}

		private void updateTrace(final IPlottingSystem<Composite> plot, final IDataset axis, final IDataset data) {

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					
					plot.clear();
					plot.getAxes().clear();

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

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						
						for (ITrace trace : traceOut) {
							trace.setUserObject(region);
							if (trace instanceof ILineTrace){
								region.setRegionColor(((ILineTrace)trace).getTraceColor());
								//use name listener to update color
								String name = region.getName();
								plot.renameRegion(region, name + " Colour");
								plot.renameRegion(region, name);
							}
						}
					}
				});
			}
		}

		private void updateImage(final IPlottingSystem<Composite> plot, final IDataset image, final List<IDataset> axes) {

			plot.clear();
			plot.getAxes().clear();
			IImageTrace trace = plot.createImageTrace(image.getName());
			trace.setData(image, axes, false);
			plot.addTrace(trace);
			plot.getSelectedYAxis().setInverted(invertYAxis);
			plot.repaint();

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
}

