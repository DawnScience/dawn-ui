package org.dawnsci.datavis.model.fileconfig;

import java.util.List;
import java.util.Map;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataStateObject;
import org.dawnsci.datavis.model.LoadedFile;
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
		if (f.getTree() == null) return false;

		Tree tree = f.getTree();

		IFindInTree findNXData = new IFindInTree() {

			@Override
			public boolean found(NodeLink node) {
				Node n = node.getDestination();
				if (n.containsAttribute(NexusConstants.DATA_SIGNAL)) {

					if (n.containsAttribute(NexusConstants.NXCLASS)) {
						if (n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
							return true;
						}
					}

					n = node.getSource();

					if (n.containsAttribute(NexusConstants.NXCLASS)) {
						if (n.getAttribute(NexusConstants.NXCLASS).getFirstElement().equals(NexusConstants.DATA)) {
							return true;
						}
					}

				}

				return false;
			}
		};

		String maxRank = null;
		int max = -1;

		Map<String, NodeLink> found = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), findNXData, false, null);
		for (String key : found.keySet()) {
			String path = Node.SEPARATOR + key;
			NodeLink nl = tree.findNodeLink(path);
			Node dest = nl.getDestination();
			String signal = dest.getAttribute("signal").getFirstElement();
			
			if (signal != null && dest instanceof DataNode) {
				int r = ((DataNode)dest).getRank();
				if (r > max) {
					max = r;
					maxRank = path;
					continue;
				}
			}
			
			if (signal != null && dest instanceof GroupNode) {
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
