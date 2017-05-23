package org.dawnsci.datavis.live;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class LiveLoadedFile extends LoadedFile implements IRefreshable {

	private boolean live = true;
	private boolean finished = false;
	private String host;
	private int port;
	
	private final static Logger logger = LoggerFactory.getLogger(LiveLoadedFile.class);
	
	public LiveLoadedFile(String path, String host, int port) {
		this(createDataHolder(path,host,port));
		this.host = host;
		this.port = port;
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
		
		if (map == null) return dh;
		
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
		
		dh.setMetadata(md);
		
		dh.setTree(tree);
		
		return dh;
	}
	
	private void updateDataHolder() {

		IRemoteData rd = ServiceManager.getRemoteDatasetService().createRemoteData(host, port);
		String path = getFilePath();
		rd.setPath(path);
		Map<String, Object> map = null;
		try {
			map = rd.getTree();
		} catch (Exception e) {
			logger.warn("Could not get remote NeXus tree from {}!", path);
		}
		
		if (map == null) return;
		
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
			String name = "/"+s;
			
			if (dataOptions.containsKey(name)) continue;
			
			IDatasetConnector r = ServiceManager.getRemoteDatasetService().createRemoteDataset(host, port);
			r.setPath(path);
			r.setDatasetName(name);
			IDynamicDataset dataset = (IDynamicDataset)r.getDataset();
			dataset.refreshShape();
			dataset.setName(name);
			dataHolder.get().addDataset(name, dataset);
			long[] maxShape = ((DataNode)result.get(s).getDestination()).getMaxShape();
			int[] max = new int[maxShape.length];
			for (int i = 0; i < maxShape.length; i++) max[i] = (int)maxShape[i];
			dataHolder.get().getMetadata().addDataInfo(name, max);
			
			if (((LazyDatasetBase)dataset).getDType() != Dataset.STRING) {
				DataOptions d = new DataOptions(name, this);
				dataOptions.put(d.getName(),d);
			}
			
			dataHolder.get().setTree(tree);
			
		}
	}
	
	private void updateOptionsNonLiveDataHolder() {

		String[] names = dataHolder.get().getNames();
		for (String n : names) {
			
			if (dataOptions.containsKey(n)) continue;
			
			ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
			if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING) {
				DataOptions d = new DataOptions(n, this);
				dataOptions.put(d.getName(),d);
			}
		}
	}
	

	@Override
	public void refresh() {
		if (!live) return;
		
		if (finished) {
			locallyReload();
			return;
		}
		
		if (dataHolder.get().getList().isEmpty()){
			String path = dataHolder.get().getFilePath();
			dataHolder.set(createDataHolder(path, host, port));
			if (dataHolder.get().getList().isEmpty()) return;
		}
		
		
		if (dataOptions.isEmpty()) {
			String[] names = dataHolder.get().getNames();
			for (String n : names) {
				ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
				if (lazyDataset instanceof IDynamicDataset) {
					((IDynamicDataset)lazyDataset).refreshShape();
				}
				if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING) {
					DataOptions d = new DataOptions(n, this);
					dataOptions.put(d.getName(),d);
				}
			}
			
			return;
		} else {
			updateDataHolder();
		}
		
		String[] names = dataHolder.get().getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
			if (lazyDataset instanceof IDynamicDataset) {
				((IDynamicDataset)lazyDataset).refreshShape();
			}
		}
		
		
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
		
		finished = true;
		
		ILoaderService service = ServiceManager.getILoaderService();
		try {
			String path = getFilePath();
			IDataHolder tmp = service.getData(path, null);
			
			if (tmp == null || tmp.getTree() == null || tmp.getNames() == null) {
				logger.error("Scan finished but local reload not ready for {}!", path);
				return;
			}
			
			dataHolder.set(tmp);
			
			if (dataOptions.isEmpty()) {
				String[] names = dataHolder.get().getNames();
				for (String n : names) {
					ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
					if (lazyDataset instanceof IDynamicDataset) {
						((IDynamicDataset)lazyDataset).refreshShape();
					}
					if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING) {
						DataOptions d = new DataOptions(n, this);
						dataOptions.put(d.getName(),d);
					}
				}
				
				return;
			} else {
				updateOptionsNonLiveDataHolder();
			}
			
			dataOptions.values().stream().forEach(o -> o.setAxes(null));
			IDataHolder dh = dataHolder.get();
			Set<String> keySet = dataOptions.keySet();
			for (String k : keySet) {
				if (!dh.contains(k)) {
					dataOptions.remove(k);
				}
			}
			
		} catch (Exception e) {
			logger.error("Could not locally load data!");
		}
		live = false;
		
	}

	@Override
	public boolean isLive() {
		return live;
	}

	@Override
	public boolean hasFinished() {
		return finished;
	}
	
}
