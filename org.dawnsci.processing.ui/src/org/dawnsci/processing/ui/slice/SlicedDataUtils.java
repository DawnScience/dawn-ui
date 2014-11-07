package org.dawnsci.processing.ui.slice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.MaskMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.metadata.AxesMetadataImpl;

public class SlicedDataUtils {

	public static AxesMetadata createAxisMetadata(String path, ILazyDataset ds, Map<Integer, String> axesNames) throws Exception {
		
		int rank = ds.getRank();
		
		AxesMetadataImpl axMeta = new AxesMetadataImpl(rank);
		for (Integer key : axesNames.keySet()) {
			String axesName = axesNames.get(key);
			IDataHolder dataHolder = LoaderFactory.getData(path);
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
			
			if (lazyAx != null) axMeta.setAxis(key-1, new ILazyDataset[] {lazyAx});
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
			if (lz0 != null) x = lz0.getSlice().squeeze();
			if (lz1 != null) y = lz1.getSlice().squeeze();
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
					((IImageTrace)t).setMask(m);
					if (!system.isDisposed())system.repaint();
				}
			});
				
			
		} else if (data.getRank() == 1) {
			system.clear();
			system.updatePlot1D(x,Arrays.asList(new IDataset[]{data}),null);
		}
		
	}
	
}
