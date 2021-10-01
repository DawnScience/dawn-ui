package org.dawnsci.datavis.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.LazyDynamicDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;
import uk.ac.diamond.scisoft.analysis.io.Utils;

/**
 * Class to represent a data file loaded in the IFileController
 *
 */
public class LoadedFile implements IDataObject, IDataFilePackage {

	private static final String INTERPRETATION = "interpretation";

	private static final Logger logger = LoggerFactory.getLogger(LoadedFile.class);

	protected AtomicReference<IDataHolder> dataHolder;
	protected Map<String, DataOptions> dataOptions;
	protected Map<String, DataOptions> virtualDataOptions;
	protected Map<String, ILazyDataset> possibleLabels;
	protected boolean onlySignals = false;
	protected Map<String, NexusSignal> signals;
	private boolean selected = false;
	private String labelName = "";
	private Dataset labelValue;

	private final Collection<String> allProcesses;

	public LoadedFile(IDataHolder dataHolder) {
		this.dataHolder = new AtomicReference<>(dataHolder.clone());
		signals = new LinkedHashMap<>();
		dataOptions = new LinkedHashMap<>();
		virtualDataOptions = new LinkedHashMap<>();
		possibleLabels = new TreeMap<>();
		String[] names = null;
		if (dataHolder.getTree() != null) {
			try {
				Map<DataNode, String> uniqueDataNodes = getUniqueDataNodes(dataHolder.getTree().getGroupNode());
				Collection<String> values = uniqueDataNodes.values();
				names = new String[values.size()];
				int count = 0;
				for (String v : values) {
					names[count++] = Tree.ROOT + v;
				}
				
				for (Entry<DataNode,String> e : uniqueDataNodes.entrySet()) {
					if (e.getKey().containsAttribute(INTERPRETATION) && 
							e.getKey().getAttribute(INTERPRETATION).getFirstElement().equals("rgba-image")) {
						e.toString();
						
						ILazyDataset d = e.getKey().getDataset();
						
						int[] shape = d.getShape();
						
						if (shape.length >=3 && d instanceof LazyDynamicDataset && (shape[shape.length-1] == 3 || shape[shape.length-3] == 3)) {
							RGBLazyDynamicDataset rgbl = RGBLazyDynamicDataset.buildFromLazyDataset((LazyDynamicDataset)d);
							virtualDataOptions.put(e.getValue() + "RGB", new DataOptionsDataset(e.getValue() + "RGB", this, rgbl));
						}
					}
				}
				
			} catch ( Exception e) {
				logger.error("Could not get unique nodes",e);
				this.signals = new LinkedHashMap<>();
			}

		}

		Map<Integer, String> processes = new TreeMap<>();
		if (names == null) names = dataHolder.getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
			if (lazyDataset == null) {
				if (signals.containsKey(n)) {
					signals.remove(n);
				}
				continue;
			}

			// parse for processing results
			Matcher m = PROCESS_REGEX.matcher(n);
			String shortName = null;
			String process = null;
			if (m.matches()) {
				int ordinal = Integer.parseInt(m.group(1));
				process = m.group(2);
				shortName = m.group(3);
				if (shortName.endsWith(DATA)) {
					shortName = shortName.substring(0, shortName.length() - DATA.length());
				}
				String p = processes.get(ordinal);
				if (p == null) {
					processes.put(ordinal, process);
				} else if (p.equals(process)) {
					p = process; // use same string
				} else {
					logger.error("Process ({}) for data '{}' has wrong name: {} ({} expected)", ordinal, shortName, process, p);
				}
			} else if (n.endsWith(RESULT_SUFFIX)) {
				process = RESULT;
				processes.put(-1, RESULT);
			}

			boolean notString = !lazyDataset.getElementClass().equals(String.class);
			if (notString && (lazyDataset.getSize() != 1 || signals.containsKey(n))) {
				NexusSignal s = signals.getOrDefault(n, null);
				DataOptions d = new DataOptions(n, this, s);
				d.setShortName(shortName);
				d.setProcess(process);
				dataOptions.put(d.getName(),d);
			} else {
				if (!notString && signals.containsKey(n)) {
					signals.remove(n);
				}
			}

			if (lazyDataset.getSize() == 1) {
				possibleLabels.put(n, lazyDataset);
			}
		}

		allProcesses = Collections.unmodifiableCollection(processes.values());
	}

	// capture /processed/(auxiliary|summary)/%d-PROCESS_NAME/DATA_NAME
	static final Pattern PROCESS_REGEX = Pattern.compile("/[^/]+/[^/]+/(\\d+)-([^/]+)/(.+)");
	static final String DATA = "/data";

	/**
	 * name of NXdata group holding processed data
	 */
	public static final String RESULT = "result";
	static final String RESULT_SUFFIX = RESULT + DATA;

	public Collection<String> getProcesses() {
		return allProcesses;
	}

	public List<DataOptions> getDataOptions() {
		return getDataOptions(onlySignals && !signals.isEmpty());
	}

	public List<DataOptions> getDataOptions(boolean signalsOnly) {

		List<DataOptions> out = Collections.emptyList();

		if (signalsOnly) {
			if (signals.isEmpty()) {
				return out;
			}

			out = signals.keySet().stream().filter(Objects::nonNull).map(s -> dataOptions.get(s)).filter(Objects::nonNull).collect(Collectors.toList());

		} else {
			out = new ArrayList<>(dataOptions.values());
		}

		out.addAll(virtualDataOptions.values());
		
		out.stream().forEach(d -> d.setFilterAxes(onlySignals));

		return out;
	}

	public List<DataOptions> getSelectedDataOptions() {
		List<DataOptions> list = dataOptions.values().stream()
				.filter(dOp -> dOp.isSelected())
				.collect(Collectors.toList());
		return list;
	}

	public DataOptions getDataOption(String name) {
		return dataOptions.get(name);
	}

	@Override
	public String getName() {
		File f = new File(dataHolder.get().getFilePath());
		return f.getName();
	}

	public String getParent() {
		File f = new File(dataHolder.get().getFilePath());
		return f.getParent();
	}

	@Override
	public String getFilePath() {
		return dataHolder.get().getFilePath();
	}

	public ILazyDataset getLazyDataset(String name){
		return dataHolder.get().getLazyDataset(name);
	}

	@Override
	public Tree getTree() {
		return dataHolder.get().getTree();
	}

	public Map<String, int[]> getDataShapes(){

		return dataHolder.get().getDatasetShapes();

	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public List<DataOptions> getChecked() {

		List<DataOptions> checked = new ArrayList<>();

		for (DataOptions op : dataOptions.values()) {
			if (op.isSelected()) {
				checked.add(op);
			}
		}

		return checked;
	}

	@Override
	public IDataPackage[] getDataPackages() {
		return dataOptions.values().stream().toArray(size ->new IDataPackage[size]);
	}

	public Map<DataNode,String> getUniqueDataNodes(GroupNode node) {
		Set<DataNode> nodes = new HashSet<>();

		IFindInTree tree = new IFindInTree() {

			@Override
			public boolean found(NodeLink node) {
				Node d = node.getDestination();

				while (d instanceof SymbolicNode) {
					d = ((SymbolicNode) d).getNode();
				}
				if (d instanceof DataNode) {
					boolean nxData = NexusTreeUtils.isNXClass(node.getSource(), NexusConstants.DATA);

					if (nxData || !nodes.contains(d)) {
						nodes.add((DataNode)d);
						return true;
					}
				}
				return false;
			}
		};

		Map<String, NodeLink> results = TreeUtils.treeBreadthFirstSearch(node, tree, false, true, null);

		Map<DataNode, String> out = new LinkedHashMap<DataNode, String>();

		for (Entry<String, NodeLink> e: results.entrySet()) {
			Node d = e.getValue().getDestination();
			while (d instanceof SymbolicNode) {
				d = ((SymbolicNode) d).getNode();
			}
			if (d instanceof DataNode) {
				out.put((DataNode)d, e.getKey());
			}

			GroupNode s = (GroupNode) e.getValue().getSource();

			if (NexusTreeUtils.isNXClass(s, NexusConstants.DATA) && d instanceof DataNode) {
				DataNode dn = (DataNode)d;
				String name = NexusTreeUtils.getFirstString(s.getAttribute(NexusConstants.DATA_SIGNAL));
				String key = e.getKey();
				
				//Only post 2014  NXData Nexus tagging runs through here,
				//NXdata may have many pre-2014 signal tags from GDA
				//and this code does not work for more than one signal in a node
				if ((name != null && (key.equals(name) || key.endsWith(Node.SEPARATOR + name)))) {
					String path = Tree.ROOT + key;
					String nxdatapath = path.substring(0,path.lastIndexOf(Node.SEPARATOR)) + Node.SEPARATOR;
					NexusSignal ns = new NexusSignal(path, dn.getRank());
					buildNexusSignalFromGroup(s, ns, nxdatapath, name);
					signals.put(path,ns);
				}
				//do "something" with pre-2014 tagged
				if (name == null) {
					Attribute attribute = dn.getAttribute(NexusConstants.DATA_SIGNAL);
					if (attribute == null) {
						continue;
					}

					int signal_value = NexusTreeUtils.parseFirstInt(attribute);
					if (signal_value != 1) {
						continue;
					}
					String path = Tree.ROOT + key;
					String nxdatapath = path.substring(0,path.lastIndexOf(Node.SEPARATOR)) + Node.SEPARATOR;
					NexusSignal ns = new NexusSignal(path, dn.getRank());
					buildNexusSignalFromPre2014Group(s, ns, nxdatapath);
					signals.put(path,ns);
				}
			}
		}

		return out;
	}
	
	protected void storeNXData(GroupNode source, DataNode signal, String key) { 

		if (NexusTreeUtils.isNXClass(source, NexusConstants.DATA)) {
			String name = NexusTreeUtils.getFirstString(source.getAttribute(NexusConstants.DATA_SIGNAL));
			//Only post 2014  NXData Nexus tagging runs through here,
			//NXdata may have many pre-2014 signal tags from GDA
			//and this code does not work for more than one signal in a node
			if ((name != null && (key.equals(name) || key.endsWith(Node.SEPARATOR + name)))) {
				String path = Tree.ROOT + key;
				
				String nxdatapath = path.substring(0,path.lastIndexOf(Node.SEPARATOR)) + Node.SEPARATOR;
				NexusSignal ns = new NexusSignal(path, signal.getRank());
				buildNexusSignalFromGroup(source, ns, nxdatapath, name);
				signals.put(path,ns);
			}
		}
	}
	
	private void buildNexusSignalFromGroup(GroupNode gn, NexusSignal signal, String location, String name) {
		
		String[] tmp = NexusTreeUtils.getStringArray(gn.getAttribute(NexusConstants.DATA_AXES));
		if (tmp != null && tmp.length == signal.getRank()) {
			for (int i = 0; i < tmp.length; i++) {
				if (NexusConstants.DATA_AXESEMPTY.equals(tmp[i])) {
					continue;
				}
				if (tmp[i] != null && gn.containsDataNode(tmp[i])) {
					signal.addAxis(i, location + tmp[i]);
				}
			}
		} else {
			logger.debug("Problem with axes attribute");
		}

		Set<String> annotations = gn.getAttributeNames();
		
		for (String s : annotations) {
			if (s.endsWith(NexusConstants.DATA_INDICES_SUFFIX)) {
				String dsName = s.substring(0, s.length()- NexusConstants.DATA_INDICES_SUFFIX.length());
				if (gn.containsDataNode(dsName)) {

					int[] indices = NexusTreeUtils.parseIntArray(gn.getAttribute(s));
					if (indices != null) {
						for (int i = 0; i < indices.length; i++) {
							signal.addAxis(indices[i], location + dsName);
						}
					}
				}
			}
		}
		
		if (gn.containsDataNode(NexusConstants.DATA_ERRORS)) {
			signal.setUncertainties(location + NexusConstants.DATA_ERRORS);
		}
	}
	
	private void buildNexusSignalFromPre2014Group(GroupNode gn, NexusSignal signal, String location) {
		
		Set<String> dnn = gn.getDataNodeNames();
		
		for (String n : dnn) {
			DataNode dataNode = gn.getDataNode(n);
			if (dataNode.containsAttribute(NexusConstants.DATA_AXIS)) {
				Attribute attribute = dataNode.getAttribute(NexusConstants.DATA_AXIS);
				if (attribute != null) {
					int[] intArray = NexusTreeUtils.parseIntArray(attribute);
					if (intArray != null && intArray.length == 1) {
						signal.addAxis(intArray[0]-1, location + n);
					}
				}
			}
		}
	}
	
	public String getLabel() {
		if (labelValue == null) return "";
		return labelValue.getString();
	}

	public void setLabel(String label) {
		labelValue = DatasetFactory.createFromObject(label);
	}

	@Override
	public String getLabelName() {
		return labelName;
	}

	@Override
	public Collection<String> getLabelOptions() {
		if (possibleLabels.isEmpty()) {
			try {
				return dataHolder.get().getMetadata().getMetaNames();
			} catch (MetadataException e) {
				return Collections.emptyList();
			}
		}
		return possibleLabels.keySet();
	}

	public void setLabelName(String labelName) {
		if (labelName == null) {
			this.labelName = "";
			this.labelValue = null;
			return;
		}
		this.labelName = labelName;
		labelValue = getLabelValue(labelName);
	}

	public Dataset getLabelValue() {
		return labelValue;
	}

	private static final String[] LABELS_TO_CONVERT = new String[] { "entry_identifier", };
	private static Dataset convertFromString(String name, Dataset values) {
		if (Arrays.stream(LABELS_TO_CONVERT).anyMatch(n -> name.endsWith(n))) {
			if (!InterfaceUtils.isNumerical(values.getClass())) {
				return DatasetFactory.createFromObject(Utils.parseValue(values.getString()));
			}
		}
		return values;
	}

	@Override
	public Dataset getLabelValue(String labelName) {
		if (possibleLabels.containsKey(labelName)) {
			ILazyDataset l = possibleLabels.get(labelName);

			try {
				Dataset slice = DatasetUtils.sliceAndConvertLazyDataset(l);
				slice.squeeze();
				
				if (slice.getRank() == 0) {
					Dataset v = convertFromString(labelName, slice);
					v.setName(labelName);
					return v;
				}
				logger.warn("Label {} does not have rank 0", labelName);
				return null;
			} catch (Exception e) {
				logger.error("Could not read label {}", labelName,e);
			}
		} else {
			try {
				Serializable metaValue = dataHolder.get().getMetadata().getMetaValue(labelName);
				if (metaValue == null) return null;
				Dataset d = DatasetFactory.createFromObject(metaValue);
				d.squeeze();
				if (d.getRank() == 0) {
					return convertFromString(labelName, d);
				}
				logger.warn("Label {} does not have rank 0", labelName);
				return null;
				
			} catch (MetadataException e) {
				return null;
			}
		}
		return null;
	}

	public boolean isSignal(String name) {
		return signals.containsKey(name);
	}

	public boolean isOnlySignals() {
		return onlySignals;
	}

	public void setOnlySignals(boolean onlySignals) {
		this.onlySignals = onlySignals;
	}

	public void removeVirtualOption(String name) {
		virtualDataOptions.remove(name);
	}

	public void addVirtualOption(DataOptions option) {
		virtualDataOptions.put(option.getName(), option);
	}
}
