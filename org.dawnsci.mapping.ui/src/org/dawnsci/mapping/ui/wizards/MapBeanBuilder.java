package org.dawnsci.mapping.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.MappingUtils;
import org.dawnsci.mapping.ui.datamodel.AssociatedImageBean;
import org.dawnsci.mapping.ui.datamodel.AssociatedImageStackBean;
import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.StringDataset;

public class MapBeanBuilder {

	private static final String ENTRY_RESULT = "/entry/result/data";
	private static final String PROCESSED_RESULT = "/processed/result/data";
	
	
	public static MappedDataFileBean buildBean(Tree tree, String xName, String yName){
		return buildBean(tree, new NXDataFinderWithAxes(xName, yName), new String[] {xName,yName});
	}
	
	public static MappedDataFileBean buildBean(Tree tree) {
		IFindInTree finder = new NXDataFinder();
		return buildBean(tree, finder, null);
	}
	
	public static MappedDataFileBean buildBean(Tree tree, IFindInTree finder, String[] names) {
		
		MappedDataFileBean bean = new MappedDataFileBean();
		
		
		List<DataInfo> datasets = buildDataStructures(tree,finder,names,bean);
		
		if (datasets.isEmpty() && !bean.getImages().isEmpty()) return bean;
		
		populateData(bean, datasets, names);
		
		if (bean.checkValid()) return bean;
		
		return null;
	}
	
	private static String findOtherAxis(Node n, String[] names, String[] axNames, boolean useSet) {
		
		
		String suffix;
		
		if (useSet) {
			suffix = NexusConstants.DATA_AXESSET_SUFFIX + NexusConstants.DATA_INDICES_SUFFIX;	
		} else {
			suffix = NexusConstants.DATA_INDICES_SUFFIX;
		}
		
		Iterator<? extends Attribute> it = n.getAttributeIterator();
		while (it.hasNext()) {
			Attribute next = it.next();
			String name = next.getName();
			if (name.startsWith(names[0]) && name.endsWith(suffix)) {
				Dataset v = DatasetUtils.convertToDataset(next.getValue());
				int index = Integer.parseInt(v.getString());
				axNames[index] = names[1] + name.substring(names[0].length(), name.length()-8);
				return names[0] + name.substring(names[0].length(), name.length()-8);
			}
		}
		
		return null;
	}
	
	private static List<DataInfo> buildDataStructures(Tree tree, IFindInTree finder, String[] names, MappedDataFileBean bean) {
		
		GroupNode groupNode = tree.getGroupNode();

		Map<String,NodeLink> nodes = TreeUtils.treeBreadthFirstSearch(groupNode, finder, false, null);

		List<String> images= new ArrayList<String>();
		List<DataInfo> datasets = new ArrayList<DataInfo>();
		
		
		for (Entry<String, NodeLink> entry : nodes.entrySet()) {
			NodeLink value = entry.getValue();
			Node n = value.getDestination();
			if (!(n instanceof GroupNode)) continue;
			String att = n.getAttribute(NexusConstants.DATA_SIGNAL).getFirstElement();
			DataNode dataNode = ((GroupNode)n).getDataNode(att);
			
			if (dataNode == null) {
				//no signal dataset
				continue;
			}
			
			if (dataNode.containsAttribute("interpretation")) {
				String interp = dataNode.getAttribute("interpretation").getFirstElement();
				if (interp.contains("rgb")) {
					images.add(entry.getKey());
					continue;
				}
			}
			
//			Attribute a = n.getAttribute("axes");
//			IDataset axes = a.getValue();
			
			int rank = dataNode.getRank();
			int squeezedRank = rank;
			
			if (dataNode.getMaxShape() != null) {
				squeezedRank = MappingUtils.getSqueezedRank(dataNode.getMaxShape());
			}
			
			String[] axNames = new String[rank];
			Attribute at = n.getAttribute("axes");
			IDataset ad = at.getValue();
			
			if (ad.getSize() != rank) {
				if (ad.getSize() == 1) {
					String string = ad.getString(new int[ad.getRank()]);
					String[] split = string.split(",");
					if (split.length == rank) {
						ad = DatasetFactory.createFromObject(split);
					}else {
						continue;
					}
				}
				
			}
			
			for (int i = 0; i < rank; i++) {
				String s = null;
				if (ad.getRank() == 0) {
					 s = ad.getString();
				} else {
					 s = ad.getString(i);
				}
				
				
				if (s.equals(".") || s.isEmpty()) continue;
				axNames[i] = s;
			}
			
			String remap = null;
			if (finder instanceof NXDataFinderWithAxes && ((NXDataFinderWithAxes)finder).isRemappingRequired() && names != null) {
				remap = findOtherAxis(n, names, axNames, true);
				
				if (remap == null) {
					remap = findOtherAxis(n, names, axNames, false);
				}
			}


			DataInfo dataInfo = new DataInfo(Node.SEPARATOR+entry.getKey(), att , axNames, squeezedRank);
			if (remap != null) dataInfo.xAxisForRemapping = remap;

			datasets.add(dataInfo);
			
			Attribute auxSig = n.getAttribute(NexusConstants.DATA_AUX_SIGNALS);
			
			if (auxSig != null) {
				IDataset auxSigDs = auxSig.getValue();
				GroupNode gn = (GroupNode)n;
				if (auxSigDs instanceof StringDataset) {
					String[] data = ((StringDataset) auxSigDs).getData();
					
					for (String s : data) {
						if (gn.containsDataNode(s)) {
							DataInfo di = new DataInfo(Node.SEPARATOR+entry.getKey(), s , axNames, squeezedRank);
							datasets.add(di);
						}
					}
				}
			}
		}
		
		if (names == null && !datasets.isEmpty()) {
			
			int minRank = Integer.MAX_VALUE;
			DataInfo min = null;
			
			for (DataInfo d : datasets) {
				if (d.rank < minRank) {
					minRank = d.rank;
					min = d;
				}
			}
			
			
			String fullName = min.getFullName();
			NodeLink link = tree.findNodeLink(fullName);
			Node d = link.getSource();
			
			Iterator<? extends Attribute> it = d.getAttributeIterator();
		
			Integer index = null;
			
			Map<Integer,String> nameDimMap = new HashMap<>();
			String name = null;
			Integer indexKey = null;
			while (it.hasNext()) {
				Attribute next = it.next();
				name = next.getName();
				if (name.endsWith(NexusConstants.DATA_AXESSET_SUFFIX + NexusConstants.DATA_INDICES_SUFFIX)) {
					IDataset value = next.getValue();
					if (value.getSize() != 1) continue;
					value.squeeze();
					index = Integer.parseInt(value.getString());
					if (nameDimMap.containsKey(index)) {
						indexKey = index;
						break;
					}
					nameDimMap.put(index, name.substring(0,name.length()-NexusConstants.DATA_INDICES_SUFFIX.length()));
				}
			}
			if (indexKey != null && nameDimMap.containsKey(indexKey)) {
				for (DataInfo di : datasets) {
					di.axes[indexKey] = name.substring(0,name.length()-NexusConstants.DATA_INDICES_SUFFIX.length());
					di.xAxisForRemapping = nameDimMap.get(indexKey);
				}		
			}
		}

		for (String name : images) populateImage(bean, name, nodes.get(name));
		
		return datasets;
		
	}

	private static void populateData(MappedDataFileBean bean, List<DataInfo> infoList, String[] xyNames) {
		
		int startCount = infoList.size();
		//TODO 1D scans
		int maxRank = 0;
		DataInfo max = null;
		int minRank = Integer.MAX_VALUE;
		DataInfo min = null;
		
		for (DataInfo d : infoList) {
			if (d.rank > maxRank) {
				maxRank = d.rank;
				max = d;
			}
			if (d.rank < minRank) {
				minRank = d.rank;
				min = d;
			}
		}
		
		if (max == null || min == null) return;
		
		bean.setScanRank(minRank);
		
		boolean slow = isMapSlow(max, min);
		
		//Assume anything above min is block, min are maps
		
		Iterator<DataInfo> it = infoList.iterator();
		
		while (it.hasNext()) {
			
			int xDim = 1;
			int yDim = 0;
			DataInfo d = it.next();
			
			if (xyNames != null) {
				String[] axes = max.axes;
				
				for (int i = 0; i < axes.length; i++) {
					if (axes[i] != null && axes[i].startsWith(xyNames[0])) xDim = i;
					if (axes[i] != null && axes[i].startsWith(xyNames[1])) yDim = i;
				}
				
			} else {
				xDim = slow ? minRank - 1 : d.axes.length -2;
				yDim = slow ? minRank -2 : d.axes.length -1;
			}
			
			if (d.rank == minRank && startCount != 1 && minRank != maxRank) continue;
			
			if (d.rank == 1 && startCount == 1) {
				xDim = 0;
				yDim = 0;
			}
			
			if (d.xAxisForRemapping != null) {
				xDim = yDim = bean.getScanRank() == 0 ? 0 : bean.getScanRank()-1;
			}
			
			MappedBlockBean b = new MappedBlockBean();
			b.setName(d.getFullName());
			b.setAxes(d.getFullAxesNames());
			b.setxDim(xDim);
			b.setyDim(yDim);
			b.setRank(d.rank);
			b.setxAxisForRemapping(d.xAxisForRemapping == null ? null : d.parent + Node.SEPARATOR + d.xAxisForRemapping);

			it.remove();
			bean.addBlock(b);
		}
		
		
		String name = bean.getBlocks().get(0).getName();
		
		for (MappedBlockBean b : bean.getBlocks()) {
			if (ENTRY_RESULT.equals(b.getName()) || PROCESSED_RESULT.equals(b.getName())) {
				name = b.getName();
			}
		}
		
		it = infoList.iterator();
		
		while (it.hasNext()) {
			DataInfo d = it.next();
			MapBean b = new MapBean();
			b.setName(d.getFullName());
			b.setParent(name);
			bean.addMap(b);
		}
		
	}
	
	private static boolean isMapSlow(DataInfo max, DataInfo min) {
		
		String mx = max.axes[0];
		String mn = min.axes[0];
		if (mx == null || mn == null) return false;
		
		return mx.equals(mn);
		
	}
	
	private static void populateImage(MappedDataFileBean bean, String name, NodeLink link) {
		Node n = link.getDestination();
		
		AssociatedImageBean ab = new AssociatedImageBean();
		ab.setName(Node.SEPARATOR+name+Node.SEPARATOR+n.getAttribute("signal").getFirstElement());
		
		Attribute a = n.getAttribute("axes");
		IDataset axes = a.getValue();
		
		if (axes.getSize() == 3) {
			if (axes.getString(0).equals(".") || axes.getString(0).isEmpty()){
				String x = Node.SEPARATOR+name+Node.SEPARATOR+axes.getString(1);
				String y = Node.SEPARATOR+name+Node.SEPARATOR+axes.getString(2);
				ab.setAxes(new String[]{x,y});
			}
			
			if (axes.getString(2).equals(".") || axes.getString(2).isEmpty()){
				String x = Node.SEPARATOR+name+Node.SEPARATOR+axes.getString(0);
				String y = Node.SEPARATOR+name+Node.SEPARATOR+axes.getString(1);
				ab.setAxes(new String[]{x,y});
			}
		}
		
		if (ab.checkValid()) bean.addImage(ab);

	}
	
	private static class DataInfo {
		
		String parent;
		String name;
		String[] axes;
		int rank;
		String xAxisForRemapping;
		
		public DataInfo(String parent, String name, String[] axes, int rank) {
			this.name = name;
			this.axes = axes;
			this.parent = parent;
			this.rank = rank;
		}
		
		public String getFullName() {
			return parent + Node.SEPARATOR + name;
		}
		
		public String[] getFullAxesNames() {
			String[] full = new String[axes.length];
			for (int i = 0; i < full.length; i++){
				if (axes[i] == null || axes[i].equals(".") || axes[i].isEmpty()) continue;
				full[i] = parent + Node.SEPARATOR + axes[i];
			}
			
			return full;
		}
		
		
	}
	
	public static MappedDataFileBean buildPixelImageBean(Tree tree) {
		
		IFindInTree finder = new NXDataFinder();
		MappedDataFileBean bean = new MappedDataFileBean();
		List<DataInfo> datasets = buildDataStructures(tree, finder, null, null);
		
		populatePixelImageData(bean,datasets);
		
		return bean;
	}
	
	
	public static void populatePixelImageData(MappedDataFileBean bean, List<DataInfo> datasets) {
		
		int maxRank = 0;
		
		for (DataInfo d : datasets) {
			if (d.rank > maxRank) {
				maxRank = d.rank;
			}
		}
		
		if (maxRank < 2) return;
		
		for (DataInfo d : datasets) {
			if (d.rank == maxRank) {
				
				AssociatedImageStackBean aisb = new AssociatedImageStackBean();
				aisb.setName(d.getFullName());
				aisb.setAxes(d.getFullAxesNames());			
				bean.addImageStack(aisb);
			}
		}
	}
	
	private static class NXDataFinder implements IFindInTree {
		@Override
		public boolean found(NodeLink node) {
			Node n = node.getDestination();
			if (n.containsAttribute(NexusConstants.DATA_SIGNAL) && n.containsAttribute(NexusConstants.NXCLASS)
					&& n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
				return true;
			}

			return false;
		}
	}
	
	private static class NXDataFinderWithAxes implements IFindInTree {
		
		private String xName;
		private String yName;
		
		boolean needsRemapping = false;
		
		public NXDataFinderWithAxes(String x, String y) {
			this.xName = x;
			this.yName = y;
		}
		
		@Override
		public boolean found(NodeLink node) {
			Node n = node.getDestination();
			if (n.containsAttribute(NexusConstants.DATA_SIGNAL) && n.containsAttribute(NexusConstants.NXCLASS)
					&& n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
				if (containedInAxes(n,xName,yName)) {
					return true;
				}
				
				if (containedInNode(n,xName,yName)) {
					return true;
				}
				
			}

			return false;
		}
		
		public boolean isRemappingRequired() {
			return needsRemapping;
			
		}
		
		private boolean containedInNode(Node n, String xName, String yName){
			
			Iterator<? extends Attribute> it = n.getAttributeIterator();
		
			
			boolean foundx = false;
			boolean foundy = false;
			Integer index = null;
			
			while (it.hasNext()) {
				Attribute next = it.next();
				String name = next.getName();
				if (name.startsWith(xName) && name.endsWith(NexusConstants.DATA_INDICES_SUFFIX)) {
					foundx = true;
					IDataset value = next.getValue();
					if (index == null) {
						index = value.getShape().length == 0 ? Integer.parseInt(value.getString()) : Integer.parseInt(value.getString(0));
					} else {
						int i = value.getShape().length == 0 ? Integer.parseInt(value.getString()) : Integer.parseInt(value.getString(0));
						if (i != index) {
							return false;
						}
					}
				}
				
				if (name.startsWith(yName) && name.endsWith(NexusConstants.DATA_INDICES_SUFFIX)) {
					foundy = true;
					IDataset value = next.getValue();
					if (index == null) {
						index = value.getShape().length == 0  ? Integer.parseInt(value.getString()) : Integer.parseInt(value.getString(0));
					} else {
						int i = value.getShape().length == 0 ? Integer.parseInt(value.getString()) : Integer.parseInt(value.getString(0));
						if (i != index) {
							return false;
						}
					}
				}
			}
			
			if (!foundx && !foundy) {
				return false;
			}
			
			needsRemapping = foundx && foundy && index != null;
			
			return foundx && foundy && index != null;
			
		}
		
		private boolean containedInAxes(Node n, String xName, String yName){
			Attribute at = n.getAttribute(NexusConstants.DATA_AXES);
			if (at == null) return false;
			IDataset ad = at.getValue();
			if (!(ad instanceof StringDataset)) return false;
			String[] axes = ((StringDataset)ad).getData();
			
			boolean containsX = false;
			
			for (String x : axes) {
				if (x.startsWith(xName)) {
					containsX = true;
					break;
				}
			}
			
			if (!containsX) return false;
			
			boolean containsY = false;
			
			for (String y : axes) {
				if (y.startsWith(yName)) {
					containsY = true;
					break;
				}
			}
			
			if (!containsY) return false;
			
			return true;
		}
	}
}
