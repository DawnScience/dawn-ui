package org.dawnsci.multidimensional.ui.imagecompare;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.datavis.api.ILazyPlotMode;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
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

public class PlotModeImageCompare implements ILazyPlotMode {
	
	private static final Logger logger = LoggerFactory.getLogger(PlotModeImageCompare.class);
	
	private static final String[] options =  new String[]{"X","Y"};
	
	List<ILazyDataset> images = null;
	boolean plotted = false;

	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system)
			throws Exception {
		
		plotted = false;
		
		if (images == null) {
			images = new ArrayList<>();
		}
		
		SliceViewIterator it = new SliceViewIterator(lz, slice, getDataDimensions(options));
		
		while (it.hasNext()) {
			ILazyDataset next = it.next();
			updateName(next.getName(),next, slice, getDataDimensions(options)[0]);
			images.add(next);
			
		}
		
		return null;
	}
	
	private void updateName(String name, ILazyDataset data, SliceND slice, int dataDim){
		data.setName(name);
		
		if (data.getRank() == 1) {
			return;
		}
		
		try {
			
			SliceFromSeriesMetadata sm = data.getFirstMetadata(SliceFromSeriesMetadata.class);
			AxesMetadata m = data.getFirstMetadata(AxesMetadata.class);
			ILazyDataset[] md = m.getAxes();
			StringBuilder builder = new StringBuilder(name);

			builder.append("[");
			Slice[] s = sm.getSliceFromInput();
			
			for (int i = 0 ; i < md.length; i++){
				if (i == dataDim || md[i] == null){
					builder.append(s[i].toString());
				} else {
					
					Dataset d =  DatasetUtils.sliceAndConvertLazyDataset(md[i]);
					
					if (d == null) {
						builder.append(s[i].toString());
					} else {
						
						if (d instanceof StringDataset) {
							builder.append(d.getString());
						} else {
							builder.append(Double.toString(d.getDouble()));
						}
					}
					
				}
				builder.append(",");
			}
			
			builder.deleteCharAt(builder.length()-1);
			builder.append("]");

			data.setName(builder.toString());
		} catch (Exception e) {
			logger.debug("Could not build name");
		}	
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
	}

	@Override
	public String getName() {
		return "Image Compare";
	}

	@Override
	public boolean supportsMultiple() {
		return true;
	}

	@Override
	public int getMinimumRank() {
		return 2;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IImageCompareTrace;
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
	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		
		if (!plotted) {
			IImageCompareTrace t = system.createTrace("Compare Trace", IImageCompareTrace.class);
			t.setImages(images);
			images = null;
			system.addTrace(t);
			plotted = true;
		}
	}

}
