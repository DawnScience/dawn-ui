package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.UnitMetadata;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

class DataReduction2DToolModel extends DataReduction2DToolObservableModel {

	private static final Logger logger = LoggerFactory.getLogger(DataReduction2DToolModel.class);
	private IImageTrace imageTrace;
	private IDataset[] axes0;
	private IDataset[] axes1;
	private File dataFile;
	private IPlottingSystem<Composite> dataImagePlotting;
	private IPlottingSystem<Composite> spectraPlotting;
	private final List<Integer> deletedIndices = new ArrayList<>();
	private final List<DataReduction2DToolSpectrumDataNode> spectrumDataNodes = new ArrayList<>();
	private final List<String> axesNames = new ArrayList<>();

	private static final double DEFAULT_STACK_OFFSET = 0.1;

	public static final String TRACE_STACK_PROP_NAME = "traceStack";
	private double traceStack = DEFAULT_STACK_OFFSET;
	private DataReduction2DToolSpectraTableComposite spectraTableComposite;
	private DataReduction2DToolSpectraRegionComposite spectraRegionTableComposite;
	private final List<TableColumnData> tableColumnDataList = new ArrayList<>();
	
	enum TableColumnAveragedRegionExportMode {
		FIRST("First"), LAST("Last"), AVERAGE("Average");
		
		private final String label;
		
		private TableColumnAveragedRegionExportMode(String label) {
			this.label = label;
		}
		
		String getLabel() {
			return label;
		}
		
	}
	
	static class TableColumnData {
		private boolean show = true;
		private TableColumnAveragedRegionExportMode exportMode = TableColumnAveragedRegionExportMode.FIRST;	
		private final int columnNumber;
		
		public boolean isShow() {
			return show;
		}
		public void setShow(boolean show) {
			this.show = show;
		}
		public TableColumnAveragedRegionExportMode getExportMode() {
			return exportMode;
		}
		public void setExportMode(TableColumnAveragedRegionExportMode exportMode) {
			this.exportMode = exportMode;
		}
		public TableColumnData(int columnNumber) {
			this.columnNumber = columnNumber;
		}
		public int getColumnNumber() {
			return columnNumber;
		}
	}
	
	public IImageTrace getImageTrace() {
		return imageTrace;
	}

	public void setImageTrace(IImageTrace imageTrace) {
		this.imageTrace = imageTrace;
	}

	public IDataset[] getAxes0() {
		return axes0;
	}

	public void setAxes0(IDataset[] axes0) {
		this.axes0 = axes0;
	}
	
	public IDataset[] getAxes1() {
		return axes1;
	}
	
	public void setAxes1(IDataset[] axes1) {
		this.axes1 = axes1;
	}
	
	public File getDataFile() {
		return dataFile;
	}
	
	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
	
	public IPlottingSystem<Composite> getDataImagePlotting() {
		return dataImagePlotting;
	}
	
	public void setDataImagePlotting(IPlottingSystem<Composite> dataImagePlotting) {
		this.dataImagePlotting = dataImagePlotting;
	}
	
	public IPlottingSystem<Composite> getSpectraPlotting() {
		return spectraPlotting;
	}
	
	public void setSpectraPlotting(IPlottingSystem<Composite> spectraPlotting) {
		this.spectraPlotting = spectraPlotting;
	}
	
	public double getTraceStack() {
		return traceStack;
	}
	
	public void setTraceStack(double traceStack) {
		firePropertyChange(TRACE_STACK_PROP_NAME, this.traceStack, this.traceStack = traceStack);
	}
	
	public List<Integer> getDeletedIndices() {
		return deletedIndices;
	}
	
	public DataReduction2DToolSpectraTableComposite getSpectraTableComposite() {
		return spectraTableComposite;
	}

	public void setSpectraTableComposite(DataReduction2DToolSpectraTableComposite spectraTableComposite) {
		this.spectraTableComposite = spectraTableComposite;
	}

	public List<DataReduction2DToolSpectrumDataNode> getSpectrumDataNodes() {
		return spectrumDataNodes;
	}

	public DataReduction2DToolSpectraRegionComposite getSpectraRegionTableComposite() {
		return spectraRegionTableComposite;
	}

	public void setSpectraRegionTableComposite(DataReduction2DToolSpectraRegionComposite spectraRegionTableComposite) {
		this.spectraRegionTableComposite = spectraRegionTableComposite;
	}
	
	public void averageSpectrumAndExport(Display display) throws Exception {
		File nexusFile = getDataFile();
		String dirToStoreReducedFiles = showSaveDirectory(nexusFile, display.getActiveShell());
		if (dirToStoreReducedFiles == null)
			return;
		
		deletedIndices.sort(null);
		List<RangeData> rangeDataList = new ArrayList<>();
		for (DataReduction2DToolSpectraRegionDataNode node : (DataReduction2DToolSpectraRegionDataNode[]) spectraRegionTableComposite.getCheckedRegionSpectraList().toArray(new DataReduction2DToolSpectraRegionDataNode[0])) {
			if (node instanceof DataReduction2DToolAvgSpectraRegionDataNode) {
				rangeDataList.add(new RangeData(getNewIndex(node.getStart().getIndex(), deletedIndices), getNewIndex(node.getEnd().getIndex(), deletedIndices)));
			}
		}
		rangeDataList.sort(null);
		RangeData.assertValidRangeData(rangeDataList);
		String newFilePath = DataReduction2DToolHelper.getUniqueFilenameWithSuffixInDirectory(nexusFile, "reduced", dirToStoreReducedFiles);
		exportToGenericNexusFile(newFilePath, imageTrace, rangeDataList, deletedIndices);
		Map<String,String> props = new HashMap<>();
		props.put("path", newFilePath);
		EventAdmin eventAdmin = ServiceLoader.getEventAdmin();
		eventAdmin.postEvent(new Event("org/dawnsci/events/file/OPEN", props));
	}
	
	private static String showSaveDirectory(File nexusFile, Shell shell) {
		DirectoryDialog dlg = new DirectoryDialog(shell);
		if (nexusFile != null)
			dlg.setFilterPath(nexusFile.getParent());
		dlg.setText("Select a directory to store new data files");
		return dlg.open();
	}
	
	public static class RangeData implements Comparable<RangeData> {
		private final int startIndex;
		private final int endIndex;

		public RangeData(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getEndIndex() {
			return endIndex;
		}

		@Override
		public String toString() {
			return startIndex + ":" + endIndex;
		}

		public static RangeData[] toRangeDataList(String commaSepString) throws Exception {
			String[] rangesStr = commaSepString.split(",");
			RangeData[] rangeData = new RangeData[rangesStr.length];
			for (int i = 0; i < rangesStr.length; i++) {
				String[] rangeStartEnd = rangesStr[i].split(":");
				rangeData[i] = new RangeData(Integer.parseInt(rangeStartEnd[0]), Integer.parseInt(rangeStartEnd[1]));
			}
			Arrays.sort(rangeData);
			assertValidRangeData(rangeData);
			return rangeData;
		}

		public static void assertValidRangeData(RangeData[] rangeData) throws Exception {
			if (rangeData.length <= 1)
				return;
			for (int index = 0 ; index < rangeData.length - 1 ; index++) {
				if (rangeData[index].startIndex >= rangeData[index + 1].startIndex || 
					rangeData[index].endIndex >= rangeData[index + 1].startIndex) {
					throw new Exception("Invalid region data found: ensure there is no overlap!");
				}
			}
			
			return;
		}
		
		public static void assertValidRangeData(List<RangeData> rangeData) throws Exception {
			assertValidRangeData(rangeData.toArray(new RangeData[rangeData.size()]));
		}
		
		@Override
		public int compareTo(RangeData o) {
			if (startIndex == o.startIndex) {
				if (endIndex == o.endIndex) {
					return 0;
				} else {
					return endIndex - o.endIndex;
				}
			} else {
				return startIndex - o.startIndex;
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			return compareTo((RangeData) obj) == 0 ? true : false;
		}
	}
	
	public static int getNewIndex(int oldIndex, List<Integer> deletedIndices) {
		if (deletedIndices == null || deletedIndices.isEmpty())
			return oldIndex;
		
		int newIndex = oldIndex;
		for (int deletedIndex : deletedIndices) {
			if (deletedIndex < oldIndex)
				newIndex--;
			else
				break;
		}
		if (getOldIndex(newIndex, deletedIndices) != oldIndex)
			logger.debug("wrong index!!! {} vs {}", getOldIndex(newIndex, deletedIndices), oldIndex);
		return newIndex;
	}
	
	public static int getOldIndex(int newIndex, List<Integer> deletedIndices) {
		
		if (deletedIndices == null || deletedIndices.isEmpty())
			return newIndex;
		
		int oldIndex = newIndex;
		for (int deletedIndex : deletedIndices) {
			if (deletedIndex <= oldIndex)
				oldIndex++;
			else
				break;
		}
		
		return oldIndex;
	}

	private Dataset[] getReducedAxes(ILazyDataset[] rawAxes, List<RangeData> rangeDataList, List<Integer> deletedIndices) {
		List<Dataset> rv = new ArrayList<>();
		int counter = 0;
		
		for (ILazyDataset axis : rawAxes) {
			if (axis == null)
				continue;
			TableColumnData tableColumnData = tableColumnDataList.get(counter++);
			if (tableColumnData.show) {
				try {
					rv.add(applyRangesAndDeletionsToDataset(tableColumnData.getExportMode(), DatasetUtils.sliceAndConvertLazyDataset(axis), rangeDataList, deletedIndices));
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		return rv.toArray(new Dataset[rv.size()]);
	}
	
	private void exportToGenericNexusFile(String newFilePath, IImageTrace trace, List<RangeData> rangeDataList, List<Integer> deletedIndices) throws Exception {
		// generate main dataset
		Dataset rawData = DatasetUtils.convertToDataset(trace.getData());
		Dataset reducedData = null;
	
		AxesMetadata firstAxesMetadata = getFirstAxesMetadata(trace);
		
		ILazyDataset[] rawAxesX = firstAxesMetadata.getAxis(0);
		ILazyDataset[] rawAxesY = firstAxesMetadata.getAxis(1);
		
		reducedData = applyRangesAndDeletionsToDataset(TableColumnAveragedRegionExportMode.AVERAGE, rawData, rangeDataList, deletedIndices);
		Dataset[] reducedAxesX = getReducedAxes(rawAxesX, rangeDataList, deletedIndices); 
		Dataset[] reducedAxesY = Arrays.stream(rawAxesY).map(lazy -> {
				try {
					Dataset dataset = DatasetUtils.sliceAndConvertLazyDataset(lazy);
					String newName = MetadataPlotUtils.removeSquareBrackets(dataset.getName());
					newName = newName.substring(newName.lastIndexOf('/') + 1);
					dataset.setName(newName);
					return dataset;
				} catch (DatasetException e) {
					return null;
				}
			}).toArray(Dataset[]::new);

		// data is ready -> prepare to write to file
		try (NexusFile file = new NexusFileHDF5(newFilePath)) {
			file.createAndOpenToWrite();
			GroupNode rootNode = TreeFactory.createGroupNode(0);
			rootNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ROOT));
			DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currentDate = new Date();
			rootNode.addAttribute(TreeFactory.createAttribute("file_name", newFilePath));
			rootNode.addAttribute(TreeFactory.createAttribute("file_time", dateFormatter.format(currentDate)));
			rootNode.addAttribute(TreeFactory.createAttribute("producer", "DAWN - Data Reduction 2D Tool"));
			rootNode.addAttribute(TreeFactory.createAttribute("default", "entry1"));

			GroupNode entryNode = TreeFactory.createGroupNode(0);
			entryNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			entryNode.addAttribute(TreeFactory.createAttribute("default", reducedData.getName() + "-reduced"));
			
			GroupNode dataNode = TreeFactory.createGroupNode(0);
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, NexusConstants.DATA_DATA));
			
			// primary axes
			Dataset primaryAxisX = ObjectUtils.firstNonNull(reducedAxesX);
			Dataset primaryAxisY = ObjectUtils.firstNonNull(reducedAxesY);
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, new String[]{
					primaryAxisX != null ? primaryAxisX.getName() : ".",
					primaryAxisY != null ? primaryAxisY.getName() : "."
			}));
			
			for (Dataset axisX : reducedAxesX) {
				if (axisX == null)
					continue;
				dataNode.addAttribute(TreeFactory.createAttribute(axisX.getName() + NexusConstants.DATA_INDICES_SUFFIX, Long.valueOf(0)));
				dataNode.addDataNode(axisX.getName(), NexusTreeUtils.createDataNode(axisX.getName(), axisX.squeeze(), getDatasetUnitName(axisX)));
			}
			
			for (Dataset axisY : reducedAxesY) {
				if (axisY == null)
					continue;
				dataNode.addAttribute(TreeFactory.createAttribute(axisY.getName() + NexusConstants.DATA_INDICES_SUFFIX, Long.valueOf(1)));
				dataNode.addDataNode(axisY.getName(), NexusTreeUtils.createDataNode(axisY.getName(), axisY.squeeze(), getDatasetUnitName(axisY)));
			}
			
			dataNode.addDataNode("data", NexusTreeUtils.createDataNode(reducedData.getName(), reducedData, getDatasetUnitName(reducedData)));
	
			file.addNode("/", rootNode);
			file.addNode("/entry1", entryNode);
			file.addNode("/entry1/" + reducedData.getName() + "-reduced", dataNode);
		}
	}

	private static String getDatasetUnitName(Dataset dataset) {
		UnitMetadata unitMetaData = dataset.getFirstMetadata(UnitMetadata.class);
		if (unitMetaData != null && unitMetaData.getUnit() != null && unitMetaData.getUnit().getName() != null)
			return unitMetaData.getUnit().getName();
		return "arbitrary";
	}
	
	private static Dataset applyRangesAndDeletionsToDataset(TableColumnAveragedRegionExportMode mode, Dataset rawData, List<RangeData> rangeDataList, List<Integer> deletedIndices) throws Exception {
		Dataset rv = null;
	
		if (rawData.getShape()[0] == 1) {
			rv = rawData;
			String newName = MetadataPlotUtils.removeSquareBrackets(rawData.getName());
			newName = newName.substring(newName.lastIndexOf('/') + 1);
			rv.setName(newName);
			return rv;
		}
			
		int noOfSpectrum = rawData.getShape()[0];
		int noOfChannels = rawData.getShape()[1];
		
		if (rangeDataList.isEmpty() && deletedIndices.isEmpty()) {
			rv = rawData;
		} else if (!rangeDataList.isEmpty()) {
				Dataset dataToAvgAndAdd = DatasetFactory.zeros(rawData.getClass(), 0, noOfChannels);
				int j = 0;
				for (RangeData avgInfo : rangeDataList) {
					Dataset avgDataItem = null;
					switch (mode) {
					case FIRST:
						avgDataItem = getProperSlice(
							rawData,
							new int[]{avgInfo.getStartIndex(), 0},
							new int[]{avgInfo.getStartIndex() + 1, noOfChannels},
							null,
							deletedIndices);
						break;
					case LAST:
						avgDataItem = getProperSlice(
							rawData,
							new int[]{avgInfo.getEndIndex(), 0},
							new int[]{avgInfo.getEndIndex() + 1, noOfChannels},
							null,
							deletedIndices);
						break;
					case AVERAGE:
						avgDataItem = getProperSlice(
							rawData,
							new int[]{avgInfo.getStartIndex(), 0},
							new int[]{avgInfo.getEndIndex() + 1, noOfChannels},
							null,
							deletedIndices)
							.mean(0);
						break;
					default:
						return null;
					}
					avgDataItem.setShape(1, noOfChannels);
					if (avgInfo.getStartIndex() - j > 0) {
						Dataset sliceToAppend = getProperSlice(
								rawData,
								new int[]{j, 0},
								new int[]{avgInfo.getStartIndex(), noOfChannels},
								null,
								deletedIndices);
						dataToAvgAndAdd = DatasetUtils.append(dataToAvgAndAdd, sliceToAppend, 0);
					}
					dataToAvgAndAdd = DatasetUtils.append(dataToAvgAndAdd, avgDataItem, 0);
					j = avgInfo.getEndIndex() + 1;
				}
				if (j < noOfSpectrum) {
					Dataset sliceToAppend = getProperSlice(
							rawData,
							new int[]{j, 0},
							new int[]{noOfSpectrum, noOfChannels},
							null,
							deletedIndices);
					dataToAvgAndAdd = DatasetUtils.append(dataToAvgAndAdd, sliceToAppend, 0);
				}
				rv = dataToAvgAndAdd;
		} else  {
				rv = getProperSlice(rawData, 
					null,
					null,
					null,
					deletedIndices);
		}
	
		String newName = MetadataPlotUtils.removeSquareBrackets(rawData.getName());
		newName = newName.substring(newName.lastIndexOf('/') + 1);
		rv.setName(newName);
		
		return rv;
	}
	
	public static Dataset getProperSlice(Dataset data, int[] start, int[] stop, int[] step, List<Integer> deletedIndices) throws Exception {
		// data must be 2D!
		if (data.getRank() != 2)
			throw new Exception("data must be 2D!");

		// stepping is not allowed right now...
		if (step != null)
			throw new Exception("step must be null!");

		// deal with null's for start and stop
		if (start == null)
			start = new int[]{0, 0};
		
		if (stop == null)
			stop = new int[]{data.getShapeRef()[0], data.getShapeRef()[1]};
		
		final int[] startFinal = start;
		final int[] stopFinal = stop;
		
		// start with zeros
		long badIndices = deletedIndices.stream().filter(index -> index >= startFinal[0] && index < stopFinal[0]).count();
		Dataset rv = DatasetFactory.zeros(data.getClass(), stop[0] - start[0] - (int) badIndices, stop[1] - start[1]);
		
		// there may be a slightly more efficient way to do this, but it will do for now...
		int j = 0;
		for (int i = start[0] ; i < stop[0] ; i++) {
			if (deletedIndices.contains(i))
				continue;
			rv.setSlice(data.getSlice(new int[]{i, start[1]}, new int[]{i + 1, stop[1]}, null), new int[]{j, start[1]}, new int[]{j + 1, stop[1]}, null);
			j++;
		}
		
		return rv;
	}

	public List<String> getAxesNames() {
		return axesNames;
	}

	private static void ensureImageAxisIsPrimary(AxesMetadata full, AxesMetadata trace, int axis) {
		ILazyDataset firstNonNull = ObjectUtils.firstNonNull(trace.getAxis(axis));
		if (firstNonNull == null)
			return;
		String firstNonNullName = firstNonNull.getName();
		firstNonNullName = firstNonNullName.substring(firstNonNullName.lastIndexOf('/') + 1);
		ILazyDataset match = null;
		ILazyDataset[] axes = full.getAxis(axis);
		for (ILazyDataset ds : axes) {
			// match names
			String dsName = ds.getName();
			// compare starting at last slash
			if (dsName.substring(dsName.lastIndexOf('/') + 1).equals(firstNonNullName)) {
				match = ds;
				break;
			}
		}

		if (match != null) {
			int index = ArrayUtils.indexOf(axes, match);
			if (index > 0) {
				ILazyDataset temp = axes[0];
				axes[0] = match;
				axes[index] = temp;
				full.setAxis(axis, axes);
			}
		} else {
			logger.warn("Trace axisX not found in parent dataset!");
		}
	}
	
	public static AxesMetadata getFirstAxesMetadata(IImageTrace trace) {
		AxesMetadata rv = null;
	
		SliceFromSeriesMetadata sliceMetadata = trace.getData().getFirstMetadata(SliceFromSeriesMetadata.class);
		if (sliceMetadata != null) {
			ILoaderService loaderService = ServiceLoader.getLoaderService();
			loaderService.clearSoftReferenceCache(sliceMetadata.getFilePath());
			try {
				IDataset dataset = loaderService.getDataset(sliceMetadata.getFilePath(), sliceMetadata.getDatasetName(), null).getSlice(sliceMetadata.getSliceInfo().getSliceFromInput());
				rv = dataset.getFirstMetadata(AxesMetadata.class);
				// assume that primary axes are those in the trace
				AxesMetadata traceAxesMetadata = trace.getData().getFirstMetadata(AxesMetadata.class);

				ensureImageAxisIsPrimary(rv, traceAxesMetadata, 0);
				ensureImageAxisIsPrimary(rv, traceAxesMetadata, 1);
			} catch (Exception e) {
				rv = trace.getData().getFirstMetadata(AxesMetadata.class);
			}
		} else {
			rv = trace.getData().getFirstMetadata(AxesMetadata.class);
		}
		
		return rv;
	}

	public List<TableColumnData> getTableColumnDataList() {
		return tableColumnDataList;
	}
}
