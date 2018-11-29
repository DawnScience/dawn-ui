package org.dawnsci.datavis.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

/**
 * Class to represent a data file loaded in the IFileController
 *
 */
public class LoadedFile implements IDataObject, IDataFilePackage {

	private static final Logger logger = LoggerFactory.getLogger(LoadedFile.class);
	
	protected AtomicReference<IDataHolder> dataHolder;
	protected Map<String,DataOptions> dataOptions;
	protected Map<String, ILazyDataset> possibleLabels;
	protected boolean onlySignals = false;
	protected Set<String> signals;
	private boolean selected = false;
	private String labelName = "";
	private String label = "";

	public LoadedFile(IDataHolder dataHolder) {
		this.dataHolder = new AtomicReference<>(dataHolder.clone());
		this.signals = new LinkedHashSet<>();
		dataOptions = new LinkedHashMap<>();
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
			} catch ( Exception e) {
				logger.error("Could not get unique nodes",e);
				this.signals = new HashSet<>();
			}
			
		}
		
		if (names == null) names = dataHolder.getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
			if (lazyDataset == null) {
				if (signals.contains(n)) {
					signals.remove(n);
				}
				continue;
			}

			boolean notString = !lazyDataset.getElementClass().equals(String.class);
			if (notString && (lazyDataset.getSize() != 1 || signals.contains(n))) {
				DataOptions d = new DataOptions(n, this);
				dataOptions.put(d.getName(),d);
			} else {
				if (!notString && signals.contains(n)) {
					signals.remove(n);
				}
			}

			if (lazyDataset.getSize() == 1) {
				possibleLabels.put(n, lazyDataset);
			}
		}
	}

	public List<DataOptions> getDataOptions() {
		return getDataOptions(onlySignals && !signals.isEmpty());
	}

	public List<DataOptions> getDataOptions(boolean signalsOnly) {
		if (signalsOnly) {
			if (signals.isEmpty()) {
				return Collections.emptyList();
			}
			return signals.stream().filter(Objects::nonNull).map(s -> dataOptions.get(s)).filter(Objects::nonNull).collect(Collectors.toList());
		}
		return new ArrayList<>(dataOptions.values());
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
	
	public String getFilePath() {
		return dataHolder.get().getFilePath();
	}
	
	public ILazyDataset getLazyDataset(String name){
		return dataHolder.get().getLazyDataset(name);
	}
	
	public Tree getTree() {
		return dataHolder.get().getTree();
	}
	
	public Map<String, int[]> getDataShapes(){
		
		IDataHolder dh = dataHolder.get();
		
		//use metadata if possible
		if (dh.getMetadata() != null && dh.getMetadata().getDataShapes() != null) {
			Map<String, int[]> ds = dataHolder.get().getMetadata().getDataShapes();
			ds = new HashMap<>(ds);
			for (String s : ds.keySet()) {
				if (ds.get(s) == null) {
					ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(s);
					if (lazyDataset != null) ds.put(s, lazyDataset.getShape());
				}
			}
			
			return ds;
		} else {
			String[] ds = dataHolder.get().getNames();
			Map<String, int[]> dsmap = new HashMap<>();
			for (String s : ds) {

				ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(s);
				if (lazyDataset != null) dsmap.put(s, lazyDataset.getShape());

			}

			return dsmap;
		}
		
	}
	
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
				Node s = node.getSource();
				
				boolean nxData = NexusTreeUtils.isNXClass(s, NexusConstants.DATA);
				
				if (d != null && d instanceof DataNode) {
					
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
			if (d instanceof DataNode) {
				out.put((DataNode)d, e.getKey());
			}
			
			Node s = e.getValue().getSource();
			
			if (NexusTreeUtils.isNXClass(s, NexusConstants.DATA)) {
				String name = NexusTreeUtils.getFirstString(s.getAttribute(NexusConstants.DATA_SIGNAL));
				String key = e.getKey();
				if (name != null) {
					if (key.equals(name) || key.endsWith(Node.SEPARATOR + name)) {
						signals.add(Tree.ROOT + key);
						ILazyDataset lz = NexusTreeUtils.getAugmentedSignalDataset((GroupNode)s);
						if (lz != null) {
							dataHolder.get().addDataset(Tree.ROOT + name, lz);
						}
					}
				} else if (d.containsAttribute(NexusConstants.DATA_SIGNAL)) {
					signals.add(Tree.ROOT + key);
					ILazyDataset lz = NexusTreeUtils.getAugmentedSignalDataset((GroupNode)s);
					if (lz != null) {
						dataHolder.get().addDataset(Tree.ROOT + key, lz);
					}
				}
			}
		}
		
		return out;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabelName() {
		return labelName;
	}
	
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
			this.label = "";
			return;
		}
		this.labelName = labelName;
		Dataset d = getLabelValue();
		label = d == null ? "" : d.getString();
	}

	public Dataset getLabelValue() {
		return getLabelValue(labelName);
	}

	public Dataset getLabelValue(String labelName) {
		if (possibleLabels.containsKey(labelName)) {
			ILazyDataset l = possibleLabels.get(labelName);
			
			try {
				Dataset slice = DatasetUtils.sliceAndConvertLazyDataset(l);
				return slice.squeeze();
			} catch (Exception e) {
				logger.error("Could not read label {}", labelName,e);
			}
		} else {
			try {
				Serializable metaValue = dataHolder.get().getMetadata().getMetaValue(labelName);
				return metaValue == null ? null : DatasetFactory.createFromObject(metaValue.toString());
				
			} catch (MetadataException e) {
				return null;
			}
		}
		return null;
	}

	public boolean isSignal(String name) {
		return signals.contains(name);
	}

	public boolean isOnlySignals() {
		return onlySignals;
	}

	public void setOnlySignals(boolean onlySignals) {
		this.onlySignals = onlySignals;
	}
}
