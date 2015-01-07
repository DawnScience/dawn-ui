package org.dawnsci.processing.ui.slice;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.MaskMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;

public class SlicedDataUtils {

	private static ILoaderService lservice;
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}

	public static AxesMetadata createAxisMetadata(String path, ILazyDataset ds, Map<Integer, String> axesNames) throws Exception {
		
		int rank = ds.getRank();
		
		AxesMetadataImpl axMeta = new AxesMetadataImpl(rank);
		for (Integer key : axesNames.keySet()) {
			String axesName = axesNames.get(key);
			IDataHolder dataHolder = lservice.getData(path, new IMonitor.Stub());
			ILazyDataset lazyAx = dataHolder.getLazyDataset(axesName);
			if (ds == lazyAx) throw new IllegalArgumentException("Axes metadata should not contain original dataset!");
			if (lazyAx != null && lazyAx.getRank() != rank) {
				lazyAx = lazyAx.getSlice();
				int[] shape = new int[rank];
				Arrays.fill(shape, 1);
				if (lazyAx.getRank() != 0) {
					shape[key-1]= lazyAx.getShape()[0];
				} 
				
				lazyAx.setShape(shape);
			}
			
			if (lazyAx != null) axMeta.setAxis(key-1, lazyAx);
			else axMeta.setAxis(key-1, new ILazyDataset[1]);
		}
		
		return axMeta;
	}
	
	public static void plotDataWithMetadata(IDataset data, final IPlottingSystem system, int[] dataDims) throws Exception {
		
		IDataset x = null;
		IDataset y = null;
		IDataset mask = null;
		
		data = data.getSliceView().squeeze();
		
		List<AxesMetadata> amd = data.getMetadata(AxesMetadata.class);
		List<MaskMetadata> mmd = data.getMetadata(MaskMetadata.class);
		
		if (amd != null && !amd.isEmpty()) {
			AxesMetadata am = amd.get(0);
			ILazyDataset[] axes = am.getAxes();
			ILazyDataset lz0 = axes[0];
			ILazyDataset lz1 = null;
			if (data.getRank() > 1) lz1 = axes[1];
			if (lz0 != null){
//				lz0.clearMetadata(null);
				x = lz0.getSlice().squeeze();
			}
			if (lz1 != null) {
//				lz1.clearMetadata(null);
				y = lz1.getSlice().squeeze();
			}
		}
		
		if (mmd != null && !mmd.isEmpty()) {
			mask = mmd.get(0).getMask().getSlice().squeeze();
		}
		
		if (data.getRank() == 2) {
			if (!system.is2D()) system.clear();
			
			final ITrace t = system.updatePlot2D(data, Arrays.asList(new IDataset[]{y,x}), null);
				
			final IDataset m = mask;

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if (t == null) return;
					((IImageTrace)t).setMask(m);
					if (!system.isDisposed())system.repaint();
				}
			});
				
			
		} else if (data.getRank() == 1) {
			system.clear();
			system.updatePlot1D(x,Arrays.asList(new IDataset[]{data}),null);
		}
		
	}
	
	public static Map<String, int[]> getDatasetInfo(String path, ConversionScheme scheme) {
		IMetadata meta;
		final Map<String, int[]>     names  = new HashMap<String, int[]>();
		try {
			meta = lservice.getMetadata(path, null);
		} catch (Exception e) {
			return names;
		}
        
        if (meta!=null && !meta.getDataNames().isEmpty()){
        	for (String name : meta.getDataShapes().keySet()) {
        		int[] shape = meta.getDataShapes().get(name);
        		if (shape != null) {
        			//squeeze to get usable rank
        			int[] ss = AbstractDataset.squeezeShape(shape, false);
        			if (scheme==null || scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 
        		} else {
        			//null shape is a bad sign
        			names.clear();
        			break;
        		}
        	}
        }
        
        if (names.isEmpty()) {
        	IDataHolder dataHolder;
			try {
				dataHolder = lservice.getData(path, null);
			} catch (Exception e) {
				return names;
			}
        	if (dataHolder!=null) for (String name : dataHolder.getNames()) {
        		if (name.contains("Image Stack")) continue;
        		if (!names.containsKey(name)) {

        			int[] shape = dataHolder.getLazyDataset(name).getShape();
        			int[] ss = AbstractDataset.squeezeShape(shape, false);
        			if (scheme==null || scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 

        		}
        	}
        }
	return sortedByRankThenLength(names);
	}
	
	private static Map<String, int[]> sortedByRankThenLength(Map<String, int[]> map) {
		
		List<Entry<String, int[]>> ll = new LinkedList<Entry<String, int[]>>(map.entrySet());
		
		Collections.sort(ll, new Comparator<Entry<String, int[]>>() {

			@Override
			public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
				int val = Integer.compare(o2.getValue().length, o1.getValue().length);
				
				if (val == 0) val = Integer.compare(o1.getKey().length(), o2.getKey().length());
				
				return val;
			}
		});
		
		Map<String, int[]> lhm = new LinkedHashMap<String, int[]>();
		
		for (Entry<String, int[]> e : ll) lhm.put(e.getKey(), e.getValue());
		
		return lhm;
		
	}
	
}
