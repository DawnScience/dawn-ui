package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
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
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RunningAverage;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataOptionsUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(DataOptionsUtils.class);
	
	private static final String AXIS_NAME = "axis_";
	private static final String DATA_NAME = "data";

	public static DataOptions buildView(DataOptions op) {
		
		if (op instanceof DataOptionsDataset) {
			return new DataOptionsDatasetSlice((DataOptionsDataset)op, op.getPlottableObject().getNDimensions().buildSliceND());
		}
		
		return new DataOptionsSlice(op, op.getPlottableObject().getNDimensions().buildSliceND());
	}

	public static DataOptions average(DataOptions data, IMonitor monitor) {

		SliceViewIterator iterator = buildIterator(data);
		iterator.hasNext();

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
		iterator.hasNext();

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
			nf.addNode(Tree.ROOT + "entry", entry);
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
		String[] ax = input.getPrimaryAxes();

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

	/**
	 * Shortens dataset path
	 * @param name
	 * @param justLast if true use last segment
	 * @return last segment or last two if they are distinct
	 */
	public static String shortenDatasetPath(String name, boolean justLast) {
		int i = name.lastIndexOf("/data");
		if (i > 0) {
			name = name.substring(0, i);
		}
		i = name.lastIndexOf(SEP);
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
}
