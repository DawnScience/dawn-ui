package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.SlicingUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotModeImage implements IPlotModeColored {

	private static final Logger logger = LoggerFactory.getLogger(PlotModeImage.class);
	
	private static final String[] options =  new String[]{"X","Y"};
	
	protected Number[] minMax;
	private double[] range;

	@Override
	public String[] getOptions() {
		return options;
	}
	
	public boolean transposeNeeded(Object[] options){
		String opt = getOptions()[1];
		for (Object o : options) {
			if (o instanceof String s && !s.isEmpty()) {
				return !s.equals(opt);
			}
		}

		return false;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		long t = System.currentTimeMillis();
		Dataset data = SlicingUtils.sliceWithAxesMetadata(lz, slice);
		logger.debug("Slice time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice, lz.getName());
		data.setErrors(null);
		updateName(system.getSelectedXAxis(), lz.getName(),data,slice);
		data.squeeze();
		if (data.getRank() != 2) return null;
		if (transposeNeeded(options)) data = data.getTransposedView();
		return new IDataset[]{data};
	}
	
	private void updateName(IAxis xAxis, String name, IDataset data, SliceND slice){
		
		data.setName(name);
		
		if (data.getRank() == 2) {
			return;
		}
		
		try {
			AxesMetadata m = data.getFirstMetadata(AxesMetadata.class);
			if (m == null) return;
			ILazyDataset[] md = m.getAxes();
			if (md == null) return;

			StringBuilder builder = new StringBuilder(name);

			builder.append("[");
			Slice[] s = slice.convertToSlice();
			int[] shape = slice.getShape();
			for (int i = 0 ; i < md.length; i++){
				
				if (md[i] == null || shape[i] != 1) {
					builder.append(s[i].toString());
					builder.append(",");
					continue;
				}
				
				
				IDataset d = md[i].getSlice();
				if (d == null || d.getSize() != 1){
					builder.append(s[i].toString());
				} else {
					d.setShape(1);
					double val = d.getDouble(0);
					builder.append(xAxis.format(val, PLOT_DATA_NUMBER_EXTRA_PRECISION));
				}
				builder.append(",");
			}
			
			builder.deleteCharAt(builder.length()-1);
			builder.append("]");
			data.setName(builder.toString());
		} catch (Exception e) {
			logger.error("Could not build name");
		}
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		long t = System.currentTimeMillis();
		IDataset d = data[0];
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<>();
			ILazyDataset[] axes = metadata.getAxes();
			if (axes != null) {
				
				if (axes[0] == null) {
					ax.add(null);
				} else {
					IDataset axis = axes[0].getSlice().squeeze();
					
					if (axis instanceof StringDataset) {
						ax.add(null);
					} else {
						if (axis.getRank() != 1) {
							SliceND s = new SliceND(axis.getShape());
							s.setSlice(1, 0, 1, 1);
							axis = axis.getSlice(s).squeeze();
						}
						axis.setName(MetadataPlotUtils.removeSquareBrackets(axis.getName()));
						ax.add(axis);
					}
				}
				if (axes[1] == null) {
					ax.add(null);
				} else {
					IDataset axis = axes[1].getSlice().squeeze();

					if (axis instanceof StringDataset) {
						ax.add(null);
					} else {
						int lastDim = axis.getRank() - 1;
						if (lastDim > 1) { // TODO check if this is actually used
							SliceND s = new SliceND(axis.getShape());
							for (int i = 0; i < lastDim; i++) {
								s.setSlice(i, 0, 1, 1);
							}
							axis = axis.getSlice(s).squeeze();
						}
						axis.setName(MetadataPlotUtils.removeSquareBrackets(axis.getName()));
						ax.add(axis);
					}

				}

				Collections.reverse(ax);
			}
		}
		
		IImageTrace trace = null;
		
		boolean isUpdate = false;
		if (update == null) { // TODO it seems that update is always null(!) so we should refactor the code
			trace = system.createImageTrace(d.getName());
			trace.setDataName(d.getName());
		} else {
			if (update[0] instanceof IImageTrace iTrace) {
				trace = iTrace;
				isUpdate = true;
			} else {
				system.removeTrace(update[0]);
			}
			
			for (int i = 1; i < update.length; i++) {
				system.removeTrace(update[i]);
			}
		}
		
		if (trace == null) {
			logger.warn("Updates missing");
			return;
		}

		trace.setData(d, ax, false);
		trace.setUserObject(userObject);
		if (minMax != null) {
			trace.setRescaleHistogram(false);
			ImageServiceBean imageServiceBean = trace.getImageServiceBean();
			imageServiceBean.setMin(minMax[0]);
			imageServiceBean.setMax(minMax[1]);
		}
		
		system.setTitle(d.getName());
		if (!isUpdate)system.addTrace(trace);
		
		if (range != null && system.getSelectedXAxis() != null && system.getSelectedYAxis() != null) {
			system.getSelectedXAxis().setRange(range[0], range[1]);
			system.getSelectedYAxis().setRange(range[2], range[3]);
		}
		logger.debug("Display time {}ms", System.currentTimeMillis() - t);
	}

	@Override
	public String getName() {
		return "Image";
	}
	
	@Override
	public boolean supportsMultiple(){
		return false;
	}

	@Override
	public int getMinimumRank() {
		return 2;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IImageTrace;
	}
	
	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		int[] dataDims = new int[2];
		int count = 0;
		String[] ops = getOptions();
		for (int i = 0; i < currentOptions.length && count < 2; i++) {
			if (currentOptions[i] != null && !currentOptions[i].toString().isEmpty() && (ops[0].equals(currentOptions[i].toString()) || ops[1].equals(currentOptions[i].toString()))) {
				dataDims[count++] = i;
			}
		}
		return dataDims;
	}

	@Override
	public void setMinMax(Number[] minMax) {
		this.minMax = minMax;
	}

	@Override
	public void setAxesRange(double[] range) {
		this.range = range;
		
	}
	
}
