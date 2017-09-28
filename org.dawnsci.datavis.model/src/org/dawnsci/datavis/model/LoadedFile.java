package org.dawnsci.datavis.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDatasetBase;


public class LoadedFile implements IDataObject, IDataFilePackage {

	protected AtomicReference<IDataHolder> dataHolder;
	protected Map<String,DataOptions> dataOptions;
	private boolean selected = false;

	public LoadedFile(IDataHolder dataHolder) {
		this.dataHolder = new AtomicReference<IDataHolder>(dataHolder);		
		dataOptions = new TreeMap<>();
		String[] names = null;
		if (dataHolder.getTree() != null) {
			Map<DataNode, String> uniqueDataNodes = TreeUtils.getUniqueDataNodes(dataHolder.getTree().getGroupNode());
			Collection<String> values = uniqueDataNodes.values();
			names = values.toArray(new String[values.size()]);
//			//Find NX Datas
//			Tree t = dataHolder.getTree();
//			
//			IFindInTree findNXData = new IFindInTree() {
//				
//				@Override
//				public boolean found(NodeLink node) {
//					Node n = node.getDestination();
//					if (n instanceof GroupNode && n.containsAttribute("signal")) {
//						return true;
//					}
//					return false;
//				}
//			};
//			
//			Map<String, NodeLink> found = TreeUtils.treeBreadthFirstSearch(t.getGroupNode(), findNXData, false, null);
//			Tree tree = dataHolder.getTree();
//			for (String key : found.keySet()) {
//				String path = Node.SEPARATOR + key;
//				NodeLink nl = tree.findNodeLink(path);
//				Node dest = nl.getDestination();
//				String signal = dest.getAttribute("signal").getFirstElement();
//				DataOptions d = new DataOptions(path+Node.SEPARATOR+signal, this);
//	
//				dataOptions.add(d);
//			}
//			
		}
		
		if (names == null) names = dataHolder.getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
			if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING && lazyDataset.getSize() != 1) {
				DataOptions d = new DataOptions(n, this);
				dataOptions.put(d.getName(),d);
			}
		}
	}

	public List<DataOptions> getDataOptions() {
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
	
}
