package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.metadata.MetadataUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
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
	
	public String[] getOptions() {
		return options;
	}
	
	public boolean transposeNeeded(Object[] options){
		
		boolean transpose = false;
		for (int i = 0; i < options.length; i++) {
			if (options[i] != null && !((String)options[i]).isEmpty()) {
				if (options[i].equals(getOptions()[1])) {
					transpose = false;
					break;
				} else {
					transpose = true;
					break;
				}
			}
		}
		
		return transpose;
	}
	
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		long t = System.currentTimeMillis();
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
		MetadataUtils.sliceAxesMetadata(data);
		logger.info("Slice time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		data.setErrors(null);
		updateName(lz.getName(),data,slice);
		data.squeeze();
		if (data.getRank() != 2) return null;
		if (transposeNeeded(options)) data = data.getTransposedView(null);
		return new IDataset[]{data};
	}
	
	private void updateName(String name, IDataset data, SliceND slice){
		
		data.setName(name);
		
		if (data.getRank() == 2) {
			return;
		}
		
		try {

//			IDataset[] md =MetadataPlotUtils.getAxesAsIDatasetArray(data);
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
					d.setShape(new int[]{1});
					double val = d.getDouble(0);
					builder.append(Double.toString(val));
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
	
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		long t = System.currentTimeMillis();
		IDataset d = data[0];
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<IDataset>();
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

						if (axis.getRank() != 1) {
							SliceND s = new SliceND(axis.getShape());
							s.setSlice(0, 0, 1, 1);
							axis = axis.getSlice(s).squeeze();
						}
						axis.setName(MetadataPlotUtils.removeSquareBrackets(axis.getName()));
						ax.add(axis);
					}

				}
				
				
//				for (ILazyDataset a : axes) {
//					
//					IDataset axis = a.getSlice().squeeze();
//					
//					if (axis.getRank() == 2) {
//						
//					}
//					
//					ax.add(a == null ? null : a.getSlice().squeeze());
//				}
				Collections.reverse(ax);
			}
		}
		
		IImageTrace trace = null;
		
//		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
//		d.setName(name);
		//deal with updates
		boolean isUpdate = false;
		if (update == null) {
			trace = system.createImageTrace(d.getName());
			trace.setDataName(d.getName());
		} else {
			if (update[0] instanceof IImageTrace) {
				trace = (IImageTrace) update[0];
				isUpdate = true;
			}
			
			for (int i = 0; i < update.length; i++) {
				if (i==0 && update[i] instanceof IImageTrace) {
					continue;
				}
				system.removeTrace(update[i]);
			}
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
		logger.info("Display time " + (System.currentTimeMillis()-t) + " ms");
		
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
		for (int i = 0; i < currentOptions.length && count < 2; i++) {
			if (currentOptions[i] != null && !currentOptions[i].toString().isEmpty() && (options[0].equals(currentOptions[i].toString()) || options[1].equals(currentOptions[i].toString()))) {
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
