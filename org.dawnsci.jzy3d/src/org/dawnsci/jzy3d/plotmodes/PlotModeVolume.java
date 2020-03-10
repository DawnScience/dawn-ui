package org.dawnsci.jzy3d.plotmodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawnsci.jzy3d.VolumeTraceImpl;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotModeVolume extends AbstractJZY3DImagePlotMode {

	private static final Logger logger = LoggerFactory.getLogger(PlotModeVolume.class);
	
	private static final String[] options = {"X","Y","Z"};
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	protected ITrace createTrace(String name, IPlottingSystem<?> system) {
		return system.createTrace(name,IVolumeTrace.class);
	}

	
	@Override
	public String getName() {
		return "Volume";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IVolumeTrace;
	}
	
	@Override
	public int getMinimumRank() {
		return 3;
	}
	
	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		IDataset[] data = sliceForPlotInner(lz, slice, options, system);
		Dataset d = DatasetUtils.convertToDataset(data[0]);
		int[] pA = getPermutationArray(options);
		d = d.transpose(pA);
		
		d.squeeze();
		
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<>();
			ILazyDataset[] axes = metadata.getAxes();
			if (axes != null) {
				for (ILazyDataset a : axes) {
					ax.add(a == null ? null : a.getSlice().squeeze());
				}
				Collections.reverse(ax);
			}
		}
		
		ITrace trace = null;
		
		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
		d.setName(name);

		trace = createTrace(d.getName(), system);
		trace.setDataName(d.getName());


		setData(trace,d, ax == null ? null : ax.toArray(new IDataset[ax.size()]));
		atomicTrace.set(trace);
		
		return data;
	}
	
	/**
	 *Take options, return array with
     *Z index at lowest position
	 *Y index at mid
     *X index at highest
     *Empty string indices remain unchanged
     *<p>
     *i.e. {"Y","X","","Z"};<p>
     *Z = 3 -> 0,<p>
     *Y = 0 -> 1,<p>
     *X = 1 -> 3,<p>
     *"" = 2 -> 2,<p>
     * returns {3,0,2,1};
     * <p>
	 * Mapping the input to {"Z","Y","X"} while maintaining gaps
	 * @param options
	 * @return
	 */
	protected int[] getPermutationArray(Object[] options) {
		
		int len = options.length;
		
		assert len >= 3;
		
		int[] perm = new int[len];
		
		String[] volop = getOptions();
		
		int[] opPos = new int[volop.length];
		
		int count = 0;
		
		//fill the sliced dims with the index
		//keep track of other dims
		for (int i = 0 ; i < len; i++) {
			if (options[i].toString().isEmpty()) {
				perm[i] = i;
			} else {
				opPos[count++] = i;
			}
		}
		
		//some debug checks
		assert count == 3;//X,Y,Z
		assert count == opPos.length;
		
		//go through other dims and assign X,Y,Z
		for (int p : opPos) {
			String o = options[p].toString();
			if (o.equals(volop[0])) {
				perm[opPos[2]] = p;
			} else if (o.equals(volop[1])){
				perm[opPos[1]] = p;
			} else {
				perm[opPos[0]] = p;
			}
		}
		
		return perm;
	}
	
	private void setData(ITrace trace, IDataset d, IDataset[] axes) {
		if (trace instanceof VolumeTraceImpl) {
			((VolumeTraceImpl)trace).setData(d, axes,d.min(true),d.max(true));
		}
	}
	
	public IDataset[] sliceForPlotInner(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		long t = System.currentTimeMillis();
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
		logger.debug("Slice time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		data.setErrors(null);
		return new IDataset[]{data};
	}


}
