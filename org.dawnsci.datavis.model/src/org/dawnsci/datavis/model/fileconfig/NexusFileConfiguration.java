package org.dawnsci.datavis.model.fileconfig;

import java.util.List;
import java.util.Map;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataStateObject;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NexusConstants;

public class NexusFileConfiguration implements ILoadedFileConfiguration {

	@Override
	public boolean configure(LoadedFile f) {
		if (f.getTree() == null)
			return false;

		Tree tree = f.getTree();

		String prefix = "";
		
		// if the NXS file has correct post-2014 tagging, there should be a default attribute in the root node to
		// identify the default NXentry group. This entry group should contain a default attribute itself containing
		// the name of the default NXdata group within.
		GroupNode rootNode = tree.getGroupNode();
		Attribute defaultRootAttribute = rootNode.getAttribute("default");
		GroupNode defaultDataNode = null;
		if (defaultRootAttribute != null) {
			// get default entry node
			GroupNode defaultEntryNode = rootNode.getGroupNode(defaultRootAttribute.getFirstElement());
			Attribute defaultEntryAttribute = defaultEntryNode.getAttribute("default");
			if (defaultEntryAttribute != null) {
				// get default NXdata group in this entry
				defaultDataNode = defaultEntryNode.getGroupNode(defaultEntryAttribute.getFirstElement());
				prefix = Node.SEPARATOR + defaultRootAttribute.getFirstElement() + Node.SEPARATOR + defaultEntryAttribute.getFirstElement();
			}
		}
		
		IFindInTree findNXData = node -> {
			Node n = node.getDestination();
			if (n.containsAttribute(NexusConstants.DATA_SIGNAL)) {
				if (n.containsAttribute(NexusConstants.NXCLASS) && 
					n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
						return true;
				}

				n = node.getSource();

				if (n.containsAttribute(NexusConstants.NXCLASS) &&
					n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
						return true;
				}
			}
			return false;
		};

		String maxRank = null;
		int max = -1;

		Map<String, NodeLink> found = null;
		
		if (defaultDataNode != null) {
			found = TreeUtils.treeBreadthFirstSearch(defaultDataNode, findNXData, true, null);
		}
		
		if (found == null || found.isEmpty()) {
			found = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), findNXData, false, null);
		}
		
		for (String key : found.keySet()) {
			String path = prefix + Node.SEPARATOR + key;
			NodeLink nl = tree.findNodeLink(path);
			Node dest = nl.getDestination();
			String signal = dest.getAttribute(NexusConstants.DATA_SIGNAL).getFirstElement();
			
			if (signal != null && dest instanceof DataNode) {
				int r = ((DataNode)dest).getRank();
				if (r > max) {
					max = r;
					maxRank = path;
					continue;
				}
			}
			
			if (signal != null && dest instanceof GroupNode) {
				if (!((GroupNode)dest).getNode(signal).isDataNode()) {
					return false;
				}
				DataNode d = ((GroupNode)dest).getDataNode(signal);
				if (d != null) {
					int r = d.getRank();
					if (r > max) {
						max = r;
						maxRank = path+Node.SEPARATOR+signal;
						continue;
					}
				}
			}
			
		}
		
		if (maxRank != null) {
			DataOptions dataOption = f.getDataOption(maxRank);
			if (dataOption != null) {
				dataOption.setSelected(true);
				return true;
			}
		}

		return false;
	}
	
	@Override
	public void setCurrentState(List<DataStateObject> state) {
		//doesn't need state
	}

}
