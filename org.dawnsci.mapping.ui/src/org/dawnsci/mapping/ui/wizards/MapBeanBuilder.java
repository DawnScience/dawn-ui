package org.dawnsci.mapping.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.datamodel.AssociatedImageBean;
import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;

public class MapBeanBuilder {

	public static MappedDataFileBean buildBean(Tree tree) {
		
		GroupNode groupNode = tree.getGroupNode();

		IFindInTree finder = new IFindInTree() {

			@Override
			public boolean found(NodeLink node) {
				Node n = node.getDestination();
				if (n.containsAttribute("signal")) {
					return true;
				}

				return false;
			}
		};

		Map<String,NodeLink> nodes = TreeUtils.treeBreadthFirstSearch(groupNode, finder, false, null);

		List<String> images= new ArrayList<String>();
		String highestRanking = null;
		int highestRank = 0;
		List<DataInfo> datasets = new ArrayList<DataInfo>();

		MappedDataFileBean bean = new MappedDataFileBean();

		for (Entry<String, NodeLink> entry : nodes.entrySet()) {
			NodeLink value = entry.getValue();
			Node n = value.getDestination();
			if (!(n instanceof GroupNode)) return null;
			String att = n.getAttribute("signal").getFirstElement();
			DataNode dataNode = ((GroupNode)n).getDataNode(att);
			if (dataNode.containsAttribute("interpretation") && dataNode.getAttribute("interpretation").getFirstElement().equals("rgba-image")){
				images.add(entry.getKey());
				continue;
			}
			
			Attribute a = n.getAttribute("axes");
			IDataset axes = a.getValue();
			
			int rank = dataNode.getRank();
			String[] axNames = new String[rank];
			Attribute at = n.getAttribute("axes");
			IDataset ad = at.getValue();
			
			if (ad.getSize() != rank) {
				if (ad.getSize() == 1) {
					String string = ad.getString(0);
					String[] split = string.split(",");
					if (split.length == rank) {
						ad = DatasetFactory.createFromObject(split);
					}
					
					else continue;
				}
				
			}
			
			for (int i = 0; i < rank; i++) {
				String s = ad.getString(i);
				if (s.equals(".") || s.isEmpty()) continue;
				axNames[i] = s;
			}
			
			datasets.add(new DataInfo(Node.SEPARATOR+entry.getKey(), att , axNames));
		}

		for (String name : images) populateImage(bean, name, nodes.get(name));
		
		if (datasets.isEmpty() && !bean.getImages().isEmpty()) return bean;
		
		populateData(bean, datasets);
		
		if (bean.checkValid()) return bean;
		
		return null;
	}
	

	
	
	
	private static void populateData(MappedDataFileBean bean, List<DataInfo> infoList) {
		//TODO 1D scans
		int maxRank = 0;
		DataInfo max = null;
		int minRank = Integer.MAX_VALUE;
		DataInfo min = null;
		
		for (DataInfo d : infoList) {
			if (d.axes.length > maxRank) {
				maxRank = d.axes.length;
				max = d;
			}
			if (d.axes.length < minRank) {
				minRank = d.axes.length;
				min = d;
			}
		}
		
		if (maxRank > 4) return;
		
		if (minRank < 2) return;
		
		if (max == null || min == null) return;
		
		boolean slow = isMapSlow(max, min);
		
		//Assume anything above min is block, min are maps
		
		Iterator<DataInfo> it = infoList.iterator();
		
		while (it.hasNext()) {
			DataInfo d = it.next();
			if (d.axes.length == minRank) continue;
			
			MappedBlockBean b = new MappedBlockBean();
			b.setName(d.getFullName());
			b.setAxes(d.getFullAxesNames());
			b.setxDim(slow ? 1 : d.axes.length -2);
			b.setyDim(slow ? 0 : d.axes.length -1);
			b.setRank(d.axes.length);
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
		
		public DataInfo(String parent, String name, String[] axes) {
			this.name = name;
			this.axes = axes;
			this.parent = parent;
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
	
}
