package org.dawnsci.datavis.live;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.dawnsci.datavis.model.PlottableObject;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.dataset.LazyDatasetBase;
import org.eclipse.january.metadata.Metadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class LiveLoadedFile extends LoadedFile implements IRefreshable {

	private boolean live = true;
	
	public LiveLoadedFile(String path, String host, int port) {
		this(createDataHolder(path,host,port));
	}

	private LiveLoadedFile(IDataHolder dh) {
		super(dh);
	}
	
	public List<DataOptions> getDataOptions() {
		
		return new ArrayList<>(dataOptions.values());
	}

	private static IDataHolder createDataHolder(String path, String host, int port) {
		DataHolder dh = new DataHolder();
		dh.setFilePath(path);
		
		IRemoteData rd = ServiceManager.getRemoteDatasetService().createRemoteData(host, port);
		rd.setPath(path);
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
		
		Metadata md = new Metadata();
		
		for (String s : result.keySet()) {
			String name = "/"+s;
			IDatasetConnector r = ServiceManager.getRemoteDatasetService().createRemoteDataset(host, port);
			r.setPath(path);
			r.setDatasetName(name);
			IDynamicDataset dataset = (IDynamicDataset)r.getDataset();
			dataset.refreshShape();
			dataset.setName(name);
			dh.addDataset(name, dataset);
			long[] maxShape = ((DataNode)result.get(s).getDestination()).getMaxShape();
			int[] max = new int[maxShape.length];
			for (int i = 0; i < maxShape.length; i++) max[i] = (int)maxShape[i];
			md.addDataInfo(name, max);
			
		}
		
		
//		md.addDataInfo(name, shape);
		
		dh.setMetadata(md);
		
		
		return dh;
	}

	@Override
	public void refresh() {
		if (!live) return;
		
		if (dataOptions.isEmpty()) {
			String[] names = dataHolder.getNames();
			for (String n : names) {
				ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
				((IDynamicDataset)lazyDataset).refreshShape();
				if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING) {
					DataOptions d = new DataOptions(n, this);
					dataOptions.put(d.getName(),d);
				}
			}
			
			return;
		}
		
		String[] names = dataHolder.getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
			((IDynamicDataset)lazyDataset).refreshShape();
		}
		
//		dataOptions.values().stream().map(d -> d.getLazyDataset())
//		.filter(IDynamicDataset.class::isInstance)
//		.map(IDynamicDataset.class::cast)
//		.forEach(d -> d.refreshShape());
		
		DataOptions[] array = dataOptions.values().stream()
		.filter(d -> d.getPlottableObject() != null && d.getPlottableObject().getNDimensions() != null).toArray(size ->new DataOptions[size]);
		
		for (DataOptions o : array) {
			NDimensions nDimensions = o.getPlottableObject().getNDimensions();
			o.setAxes(null);
			int[] shape = o.getLazyDataset().getShape();
			nDimensions.updateShape(shape);
		}
		
	}

	@Override
	public void locallyReload() {
		ILoaderService service = ServiceManager.getILoaderService();
		try {
			dataHolder = service.getData(getFilePath(), null);
			dataOptions.values().stream().forEach(o -> o.setAxes(null));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		live = false;
		
	}
	
}
