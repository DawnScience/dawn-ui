package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
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

public class PlotModeXY implements IPlotMode {

	private static final String[] options =  new String[]{"X"};
	private long count = 0;
	
	private final static Logger logger = LoggerFactory.getLogger(PlotModeXY.class);

	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Line";
	}
	
	@Override
	public boolean supportsMultiple(){
		return true;
	}

	@Override
	public int getMinimumRank() {
		return 1;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ILineTrace;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		
		long t = System.currentTimeMillis();
		IDataset allData = lz.getSlice(slice);
		logger.info("Slice time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		
		SliceViewIterator it = new SliceViewIterator(allData, null, getDataDimensions(options));
		
		int total = it.getTotal();
		IDataset[] all = new IDataset[total];
		int count = 0;
		while (it.hasNext()) {
			ILazyDataset next = it.next();
			Dataset d = DatasetUtils.sliceAndConvertLazyDataset(next);
			updateName(lz.getName(), d, slice, getDataDimensions(options)[0]);
			all[count++] = d.squeeze();
			
		}
		return all;
	}
	
	private void updateName(String name, IDataset data, SliceND slice, int dataDim){
		data.setName(name);
		
		if (data.getRank() == 1) {
			return;
		}
		
		try {
			
			SliceFromSeriesMetadata sm = data.getFirstMetadata(SliceFromSeriesMetadata.class);
			IDataset[] md =MetadataPlotUtils.getAxesAsIDatasetArray(data);
			if (md == null) return;

			StringBuilder builder = new StringBuilder(name);

			builder.append("[");
			SliceND combined = SliceNDUtils.getRootSlice(slice, new SliceND(slice.getShape(),sm.getSliceFromInput()));
			Slice[] s = combined.convertToSlice();
			
			for (int i = 0 ; i < md.length; i++){
				IDataset d = md[i];
				if (d == null || i == dataDim){
					builder.append(s[i].toString());
				} else {
					if (d.getSize() == 1) d.setShape(new int[]{1});
					
					if (d instanceof StringDataset) {
						builder.append(d.getString(0));
					} else {
						double val = DatasetUtils.convertToDataset(d).getElementDoubleAbs(0);
						builder.append(Double.toString(val));
					}
					
					
				}
				builder.append(",");
			}
			
			builder.deleteCharAt(builder.length()-1);
			builder.append("]");
			builder.append("[");
			builder.append(combined.toString());
			builder.append("]");
			data.setName(builder.toString());
		} catch (Exception e) {
			logger.error("Could not build name");
		}
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
		
		renameUpdates(update, system);
		int count = 0;
		for (IDataset d : data) {
			createSingleTrace(d, system, userObject, (update == null || count >= update.length) ? null:update[count++]);
		}
		if (update != null) for (; count < update.length; count++) {
			system.removeTrace(update[count]);
		}
		
		system.repaint();
	}
	
	private void renameUpdates(ITrace[] update, IPlottingSystem<?> system) {
		
		if (update == null) return;
		
		for (ITrace t : update) {
			String name = "totally_amazing_unique_name_" + count++;
			t.setName(name);
//			try {
//				system.renameTrace(t, name);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	private void createSingleTrace(IDataset data, IPlottingSystem<?> system, Object userObject, ITrace update) throws DatasetException {
		
		if (update != null && !(update instanceof ILineTrace)) system.removeTrace(update);
		
		AxesMetadata metadata = data.getFirstMetadata(AxesMetadata.class);
		IDataset ax = null;
		
		String axName = null;
		
		if (metadata != null) {
			ILazyDataset[] axes = metadata.getAxes();
			if (axes.length == 1 && axes[0] != null) {
				ax = axes[0].getSlice();
				String name = MetadataPlotUtils.removeSquareBrackets(ax.getName());
				ax.setName(MetadataPlotUtils.removeSquareBrackets(name));
				axName = name;
			}
			
		}
		
		ILineTrace trace = null;
		boolean canUpdate = false;
		if (update instanceof ILineTrace) {
			canUpdate = true;
//			String name = "totally_amazing_unique_name_" + count++;
			update.setName(data.getName());
			try {
//				system.renameTrace(update, data.getName());
				trace = (ILineTrace)update;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			trace  = system.createLineTrace(data.getName());
		}
		trace.setDataName(data.getName());
		trace.setData(ax, data);
		trace.setUserObject(userObject);
		if (!canUpdate)system.addTrace(trace);
		
		if (axName != null) system.getSelectedXAxis().setTitle(axName);
	}

	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		int[] dataDims = new int[1];
		for (int i = 0; i < currentOptions.length; i++) {
			if (PlotModeXY.options[0].equals(currentOptions[i])){
				dataDims[0] = i;
				break;
			}
		}
		
		return dataDims;
	}
}
