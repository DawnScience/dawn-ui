package org.dawnsci.mapping.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.CipherInputStream;

import org.dawnsci.mapping.ui.datamodel.AssociatedImageBean;
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
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.StringDataset;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

public class MapBeanBuilder {

	public static MappedDataFileBean buildBean(Tree tree, String xName, String yName){
		return buildBean(tree, new NXDataFinderWithAxes(xName, yName), new String[] {xName,yName});
	}
	
	public static MappedDataFileBean buildBean(Tree tree) {
		IFindInTree finder = new NXDataFinder();
		return buildBean(tree, finder, null);
	}
	
	public static MappedDataFileBean buildBean(Tree tree, IFindInTree finder, String[] names) {
		
		GroupNode groupNode = tree.getGroupNode();

		Map<String,NodeLink> nodes = TreeUtils.treeBreadthFirstSearch(groupNode, finder, false, null);

		List<String> images= new ArrayList<String>();
//		String highestRanking = null;
//		int highestRank = 0;
		List<DataInfo> datasets = new ArrayList<DataInfo>();

		MappedDataFileBean bean = new MappedDataFileBean();

		for (Entry<String, NodeLink> entry : nodes.entrySet()) {
			NodeLink value = entry.getValue();
			Node n = value.getDestination();
			if (!(n instanceof GroupNode)) continue;
			String att = n.getAttribute("signal").getFirstElement();
			DataNode dataNode = ((GroupNode)n).getDataNode(att);
			if (dataNode.containsAttribute("interpretation") && dataNode.getAttribute("interpretation").getFirstElement().equals("rgba-image")){
				images.add(entry.getKey());
				continue;
			}
			
//			Attribute a = n.getAttribute("axes");
//			IDataset axes = a.getValue();
			
			int rank = dataNode.getRank();
			int squeezedRank = rank;
			
			if (dataNode.getMaxShape() != null) {
				squeezedRank = getSqueezedRank(dataNode.getMaxShape());
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
					}
					
					else continue;
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
				Iterator<? extends Attribute> it = n.getAttributeIterator();
				while (it.hasNext()) {
					Attribute next = it.next();
					String name = next.getName();
					if(name.startsWith(names[0]) && name.endsWith(NexusTreeUtils.NX_AXES_SET+NexusTreeUtils.NX_INDICES_SUFFIX)) {
						IDataset v = next.getValue();
						String string = v.getString();
						int index = Integer.parseInt(v.getString());
						axNames[index] = names[1] + name.substring(names[0].length(), name.length()-8);
						remap = names[0] + name.substring(names[0].length(), name.length()-8);
						break;
						
					}
				}
			}

			DataInfo dataInfo = new DataInfo(Node.SEPARATOR+entry.getKey(), att , axNames, squeezedRank);
			if (remap != null) dataInfo.xAxisForRemapping = remap;

			datasets.add(dataInfo);
		}

		for (String name : images) populateImage(bean, name, nodes.get(name));
		
		if (datasets.isEmpty() && !bean.getImages().isEmpty()) return bean;
		
		List<String> remappingAxesList = null;
		
		for (DataInfo d : datasets) {
		
			if (d.xAxisForRemapping == null) {
				//spiral case
				d.toString();
				NodeLink nl = nodes.get(d.parent.substring(1));
				remappingAxesList = new ArrayList<String>();
				Node n = nl.getDestination();
				Iterator<? extends Attribute> it = n.getAttributeIterator();
				while (it.hasNext()) {
					Attribute next = it.next();
					IDataset value = next.getValue();
					
					if (next.getName().endsWith("_demand_indices")) {
						String name = next.getName();
						name = name.substring(0, name.length()-8);
						remappingAxesList.add(name);
					}
				}
			};
			
		}
		
		if (remappingAxesList != null && remappingAxesList.size() >=2) {
			remappingAxesList.toString();
			for (DataInfo d : datasets) {
				NodeLink nl = nodes.get(d.parent.substring(1));
				Node n = nl.getDestination();
				Attribute attribute = n.getAttribute(remappingAxesList.get(0)+ "_indices");
				if (attribute != null){
					d.axes[0] = remappingAxesList.get(1);
					d.xAxisForRemapping = remappingAxesList.get(0);
				}
			}
		}
		
		populateData(bean, datasets, names);
		
		if (bean.checkValid()) return bean;
		
		return null;
	}
	
	private static int getSqueezedRank(long[] maxShape) {
		int r = 0;
		
		for (long i : maxShape) if (i != 1) r++;
		
		return r;
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
		
		if (minRank < 1) return;
		
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
				xDim = slow ? 1 : d.axes.length -2;
				yDim = slow ? 0 : d.axes.length -1;
			}
			
			if (d.rank == minRank && startCount != 1) continue;
			
			if (d.rank == 1 && startCount == 1) {
				xDim = 0;
				yDim = 0;
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
		
		it = infoList.iterator();
		
		while (it.hasNext()) {
			DataInfo d = it.next();
			MapBean b = new MapBean();
			b.setName(d.getFullName());
			b.setParent(bean.getBlocks().get(0).getName());
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
	
	private static class NXDataFinder implements IFindInTree {
		@Override
		public boolean found(NodeLink node) {
			Node n = node.getDestination();
			if (n.containsAttribute("signal") && n.containsAttribute("NX_class") && n.getAttribute("NX_class").getFirstElement().equals(NexusTreeUtils.NX_DATA)) {
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
			if (n.containsAttribute("signal") && n.containsAttribute("NX_class") && n.getAttribute("NX_class").getFirstElement().equals(NexusTreeUtils.NX_DATA)) {
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
				if(name.startsWith(xName) && name.endsWith(NexusTreeUtils.NX_AXES_SET+NexusTreeUtils.NX_INDICES_SUFFIX)) {
					foundx = true;
					IDataset value = next.getValue();
					if (index == null) {
						String string = value.getString();
						index = Integer.parseInt(value.getString());
					} else {
						int i = Integer.parseInt(value.getString());
						if (i != index) {
							return false;
						}
					}
				}
				
				if(name.startsWith(yName) && name.endsWith(NexusTreeUtils.NX_AXES_SET+NexusTreeUtils.NX_INDICES_SUFFIX)) {
					foundy = true;
					IDataset value = next.getValue();
					if (index == null) {
						index = Integer.parseInt(value.getString());
					} else {
						int i = Integer.parseInt(value.getString());
						if (i != index) {
							return false;
						}
					}
				}
			}
			
			needsRemapping = foundx && foundy && index != null;
			
			return foundx && foundy && index != null;
			
		}
		
		private boolean containedInAxes(Node n, String xName, String yName){
			Attribute at = n.getAttribute(NexusTreeUtils.NX_AXES);
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
