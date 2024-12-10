package org.dawnsci.datavis.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.SlicingUtils;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RunningAverage;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataOptionsUtils {
	private DataOptionsUtils() {
	}
	
	private static final Logger logger = LoggerFactory.getLogger(DataOptionsUtils.class);
	
	private static final String AXIS_NAME = "axis_";
	private static final String DATA_NAME = "data";

	public static DataOptions buildView(DataOptions op) {
		
		if (op instanceof DataOptionsDataset dod) {
			return new DataOptionsDatasetSlice(dod, op.getPlottableObject().getNDimensions().buildSliceND());
		}
		
		return new DataOptionsSlice(op, op.getPlottableObject().getNDimensions().buildSliceND());
	}

	public static DataOptions average(DataOptions data, IMonitor monitor) {

		SliceViewIterator iterator = buildIterator(data);

		try {

			IDataset dataset = iterator.next().getSlice();

			RunningAverage rav = new RunningAverage(dataset);

			while (iterator.hasNext()) {
				if (monitor != null) {
					monitor.worked(1);

					if (monitor.isCancelled()) {
						return null;
					}
				}

				dataset = iterator.next().getSlice();
				rav.update(dataset);
			}

			String name = "[average]";

			return buildNewDataOptions(data, rav.getCurrentAverage(), name);

		} catch (DatasetException e) {
			return null;
		}

	}

	public static DataOptions sum(DataOptions data, IMonitor monitor) {


		SliceViewIterator iterator = buildIterator(data);

		try {

			Dataset dataset = DatasetUtils.cast(DoubleDataset.class, iterator.next().getSlice());

			while (iterator.hasNext()) {
				if (monitor != null) {
					monitor.worked(1);

					if (monitor.isCancelled()) {
						return null;
					}
				}

				IDataset  next= iterator.next().getSlice();
				dataset.iadd(next);
			}

			String name = "[sum]";

			return buildNewDataOptions(data, dataset, name);

		} catch (DatasetException e) {
			return null;
		}

	}

	public static int getNumberOfSlices(DataOptions data) {

		NDimensions nDimensions = data.getPlottableObject().getNDimensions();

		int [] dd = nDimensions.getDimensionsWithoutDescription();

		int nSlice = 1;

		for (int i = 0; i < dd.length; i++) {
			nSlice *= nDimensions.getSize(dd[i]);
		}

		return nSlice;
	}

	public static void saveToFile(DataOptions dataOptions, String filePath, INexusFileFactory factory) throws NexusException, DatasetException {
		Dataset data = DatasetUtils.sliceAndConvertLazyDataset(dataOptions.getLazyDataset());
		data.setName(DATA_NAME);
		
		NXdata nxData = NexusNodeFactory.createNXdata();
		nxData.setAttributeSignal(data.getName());
		nxData.setData("data", data);
		
		Dataset[] axesArray = getAxesArray(data, nxData);
		
		nxData.setAttribute(null, NexusConstants.DATA_AXES, DatasetFactory.createFromObject(createAxisAttributeNames(axesArray)));
		
		for (Dataset d : axesArray) {
			if (d != null) nxData.setDataset(d.getName(), d);
		}
		
		NXentry entry = NexusNodeFactory.createNXentry();
		entry.addGroupNode(DATA_NAME, nxData);
		
		try (NexusFile nf = factory.newNexusFile(filePath)) {
			nf.createAndOpenToWrite();
			nf.addNode(Tree.ROOT + "entry1", entry);
		} 
	}
	
	private static String[] createAxisAttributeNames(Dataset[] axes) {
		String[] axat = new String[axes.length];
		
		for (int i = 0; i < axat.length; i++) {
			if (axes[i] != null) {
				axat[i] = axes[i].getName();
			} else {
				axat[i] = NexusConstants.DATA_AXESEMPTY;
			}
		}
		
		return axat;
	}
	
	private static Dataset[] getAxesArray(Dataset data, NXdata nxData) {
		Dataset[] out = new Dataset[data.getRank()];
		AxesMetadata axm = data.getFirstMetadata(AxesMetadata.class);
		
		if (axm == null) return out;
		
		ILazyDataset[] axes = axm.getAxes();
		
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] != null) {
				try {
					Dataset ax = DatasetUtils.sliceAndConvertLazyDataset(axes[i]).squeeze();
					out[i] = sanitiseNameAndAddIndicesAttribute(ax, i, nxData);
				} catch (DatasetException e) {
					logger.error("Could not slice dataset", e);
				}
			}
		}
		
		return out;
	}
	
	private static Dataset sanitiseNameAndAddIndicesAttribute(Dataset data, int pos, NXdata nxData) {
		
		String name = data.getName();
		
		if (name == null || name.isEmpty()) {
			data.setName(AXIS_NAME + pos);
			return data;
		}
		
		name = MetadataPlotUtils.removeSquareBrackets(name);
		
		if (name.startsWith(Node.SEPARATOR)) {
			String[] split = name.split(Node.SEPARATOR);
			name = split[split.length-1];
			
		}
		
		data.setName(name);
		nxData.setAttribute(null, name + NexusConstants.DATA_INDICES_SUFFIX, DatasetFactory.createFromObject(pos));
		
		return data;
		
	}
	
	private static SliceViewIterator buildIterator(DataOptions data) {
		ILazyDataset lazyDataset = data.getLazyDataset().getSliceView();
		lazyDataset.clearMetadata(null);
		NDimensions nDimensions = data.getPlottableObject().getNDimensions();

		return new SliceViewIterator(lazyDataset, null, nDimensions.getDimensionsWithDescription());
	}

	/**
	 * Builds a new {@link DataOptionsDataset}, named by suffixing the original name.
	 * @param input
	 * 				The original {@link DataOptions} object.
	 * @param output
	 * 				The Dataset containing the new data.
	 * @param suffix
	 * 				The suffix to add to the end of suffix.getName().
	 * @return The constructed object.
	 */
	public static DataOptionsDataset buildNewDataOptions(DataOptions input, Dataset output, String suffix) {
		return buildNewDataOptionsWithName(input, output, input.getName() + suffix);
	}

	/**
	 * Builds a new {@link DataOptionsDataset} with a given name.
	 * @param input
	 * 				The original {@link DataOptions} object.
	 * @param output
	 * 				The Dataset containing the new data.
	 * @param name
	 * 			The full name of the new {@link DataOptionsDataset}.
	 * @return The constructed object.
	 */
	public static DataOptionsDataset buildNewDataOptionsWithName(DataOptions input, Dataset output, String name) {
		NDimensions nDimensions = input.getPlottableObject().getNDimensions();
		NDimensions ndc = new NDimensions(nDimensions);
		ndc.updateShape(output.getShape());
		IPlotMode plotMode = input.getPlottableObject().getPlotMode();
		PlottableObject po = new PlottableObject(plotMode, ndc);
		DataOptionsDataset dop = new DataOptionsDataset(name, input.getParent(), output);
		dop.setPlottableObject(po);

		int[] dd = nDimensions.getDimensionsWithDescription();
		//data axes should be set in sum/average
		String[] ax = input.getCurrentAxes();

		if (ax != null) {
			String[] inax = new String[nDimensions.getRank()];

			for (int i = 0; i < nDimensions.getRank(); i++) {
				ndc.setAxis(i, null);
			}

			for (int i = 0; i < dd.length; i++) {
				inax[dd[i]] = ax[dd[i]];
				ndc.setAxis(dd[i], ax[dd[i]]);
			}

			dop.setAxes(inax);
		}
		

		return dop;
	}

	private static final String SEP = Node.SEPARATOR;
	static final String DATA_ENDING = SEP + NexusConstants.DATA_DATA;

	/**
	 * Strip any "/data" ending from name 
	 * @param name
	 * @return stripped name
	 */
	public static String stripDataEnding(String name) {
		if (name.endsWith(DATA_ENDING)) { // remove NXdata's default signal name
			return name.substring(0, name.length() - DATA_ENDING.length());
		}
		return name;
	}

	/**
	 * Shortens dataset path
	 * @param name
	 * @param justLast if true use last segment
	 * @return last segment or last two if they are distinct
	 */
	public static String shortenDatasetPath(String name, boolean justLast) {
		name = stripDataEnding(name);

		int i = name.lastIndexOf(SEP);
		if (i > 0) {
			String last = name.substring(i + 1);
			if (!justLast) {
				int j = name.substring(0, i).lastIndexOf(SEP);
				if (j > 0) {
					String next = name.substring(j + 1, i);
					if (!next.equals(last)) {
						last = name.substring(j + 1);
					}
				}
			}
			name = last;
		}
		return name;
	}

	/**
	 * Map axis shape to data shape
	 * @param d d-th dimension of data
	 * @param aShape axis shape
	 * @param dShape data shape
	 * @return map of index from axis to data dimensions
	 */
	public static int[] mapAxisToDataShape(int d, int[] aShape, int[] dShape) {
		// maps lengths to indexes in data shape
		int dRank = dShape.length;
		Map<Integer, List<Integer>> lengthIndexes = new HashMap<>();
		for (int j = 0; j < dRank; j++) {
			lengthIndexes.computeIfAbsent(dShape[j], n -> new ArrayList<>()).add(j);
		}

		int aRank = aShape.length;
		int[] map = new int[aRank];
		Arrays.fill(map, -1);

		// dShape = [12, 100, 1600, 1600]
		// length indexes = {12: [0], 100: [1], 1600; [2,3]
		// aShape = [12, 100, 1600] => (0,1,2) for d=2
		for (int i = 0; i < aRank; i++) {
			int l = aShape[i];
			List<Integer> idxs = lengthIndexes.get(l);
			if (idxs.size() == 1) {
				map[i] = idxs.getFirst();
				idxs.clear();
			} else if (idxs.contains(d)) {
				// prioritise matching given dimension
				map[i] = d;
				idxs.remove((Integer) d);
			} else {
				map[i] = idxs.getFirst();
				idxs.removeFirst();
			}
		}

		return map;
	}

	/**
	 * Get mapped axis dataset
	 * @param dRank data rank
	 * @param axis dataset
	 * @param map mapping from axis dimension to data
	 * @return mapping view of axis
	 */
	public static ILazyDataset getMappedAxis(int dRank, ILazyDataset axis, int[] map) {
		int[] order = IntStream.range(0, map.length)
		.boxed().sorted(Comparator.comparingInt(i -> map[i])).mapToInt(i -> i).toArray();
		int[] sMap = map.clone();
		Arrays.sort(sMap);
		return reshapeWithNewDims(dRank, axis.getTransposedView(order), sMap);
	}

	/**
	 * Reshape dataset with new dimensions added
	 * @param d dataset
	 * @param dims new dimensions
	 * @return reshaped dataset
	 */
	public static ILazyDataset reshapeWithNewDims(int dRank, ILazyDataset d, int[] map) {
		if (map == null || map.length == 0) {
			return d;
		}

		int[] max = d instanceof IDynamicDataset dynamic ? dynamic.getMaxShape() : null;

		int[][] nShapes = insertNewDims(dRank, d.getShape(), max, map);

		ILazyDataset rLazy = d.getSliceView();
		IDynamicDataset dynamic = rLazy instanceof IDynamicDataset dyn? dyn: null;
		if (dynamic != null) {
			// workaround different rank shape/maxShape bug in LazyDynamicDataset
			if (d.getRank() != dRank) {
				dynamic.setShape(nShapes[0]);
				if (max != null && max.length != dRank) {
					try {
						Field mField = dynamic.getClass().getDeclaredField("maxShape");
						mField.setAccessible(true);
						mField.set(dynamic, nShapes[1]);
						IMetadata md = MetadataFactory.createMetadata(IMetadata.class, Collections.emptyMap());
						md.addDataInfo(SlicingUtils.ORIGINAL_MAX_SHAPE, max);
						dynamic.addMetadata(md);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | MetadataException e) {
						logger.error("Could not work around max shape having different rank", e);
					}
				}
			} else {
				dynamic.resize(nShapes[0]);
				dynamic.setMaxShape(nShapes[1]);
			}
		} else {
			rLazy.setShape(nShapes[0]);
		}
		return rLazy;
	}

	/**
	 * Insert new dimensions into shape and maxShape
	 * @param dRank
	 * @param shape
	 * @param max
	 * @param map
	 * @return expanded shape and maxShape
	 */
	static int[][] insertNewDims(int dRank, int[] shape, int[] max, int... map) {
		int[] nShape = new int[dRank];
		Arrays.fill(nShape, 1);
		int[] mShape = max == null ? null : nShape.clone();

		for (int i = 0; i < map.length; i++) {
			int m = map[i];
			nShape[m] = shape[i];
			if (mShape != null) {
				mShape[m] = max[i];
			}
		}
		return new int[][] {nShape, mShape};
	}
}
