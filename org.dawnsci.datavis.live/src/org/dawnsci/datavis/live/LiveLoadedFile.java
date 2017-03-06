package org.dawnsci.datavis.live;

import java.util.Map;

import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.metadata.Metadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class LiveLoadedFile extends LoadedFile {

	public LiveLoadedFile(String path, String host, int port) {
		this(createDataHolder(path,host,port));
	}

	private LiveLoadedFile(IDataHolder dh) {
		super(dh);
	}

	private static IDataHolder createDataHolder(String path, String host, int port) {
		DataHolder dh = new DataHolder();
		
		IRemoteData rd = ServiceManager.getRemoteDatasetService().createRemoteData(host, port);
		Map<String, Object> map = null;
		try {
			map = rd.getTree();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (map == null) return null;
		
		Tree tree = TreeToMapUtils.mapToTree(map, path);
		IFindInTree finder = new IFindInTree() {
			
			@Override
			public boolean found(NodeLink node) {
				Node d = node.getDestination();
				if (d instanceof DataNode) {
					return true;
				}
				return false;
			}
		};
		Map<String, NodeLink> result = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), finder, false, null);
		
		for (String s : result.keySet()) {
			IDatasetConnector r = ServiceManager.getRemoteDatasetService().createRemoteDataset(host, port);
			r.setPath(path);
			r.setDatasetName(s);
			dh.addDataset(s, r.getDataset());
		}
		
		Metadata md = new Metadata();
//		md.addDataInfo(name, shape);
		
		dh.setMetadata(md);
		
		
		return dh;
	}
	
}
