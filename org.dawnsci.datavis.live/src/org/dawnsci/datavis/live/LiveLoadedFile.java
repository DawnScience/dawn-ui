package org.dawnsci.datavis.live;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.NXCite;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.dataset.LazyDatasetBase;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.january.metadata.Metadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

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
	
	@Override
	public List<DataOptions> getDataOptions() {
		
		return new ArrayList<>(dataOptions.values());
	}

	private static IDataHolder createDataHolder(String path, String host, int port) {
		DataHolder dh = new DataHolder();
		dh.setFilePath(path);
		Tree tree = getTree(path,host,port);
		
		if (tree == null) return dh;
		
		Map<String, NodeLink> result = findNodes(tree);
		
		if (result == null) return dh;
		dh.setMetadata(new Metadata());
		fillDataHolder(dh, result, tree, null,null, path, host, port);
		
		return dh;
	}
	
	private static void fillDataHolder(IDataHolder dh, Map<String, NodeLink> result, Tree tree, Map<String, DataOptions> options, LoadedFile file, String path, String host, int port) {
		IMetadata md = dh.getMetadata();
		
		Map<String, String[]> axesMap = new HashMap<>();
		
		for (Entry<String, NodeLink> s : result.entrySet()) {
			
			String name = "/"+s.getKey();
			
			if (options != null && options.containsKey(name)) continue;
			
			IDynamicDataset dataset = buildDataset(s.getKey(),path,host,port);
			
			long[] maxShape = ((DataNode)s.getValue().getDestination()).getMaxShape();
			
			if (maxShape.length == 0 || allOnes(maxShape)) {
				continue;
			}
			
			addDataset(dataset,maxShape,dh,md);
			
			if ( options != null && ((LazyDatasetBase)dataset).getDType() != Dataset.STRING) {
				DataOptions d = new DataOptions(name, file);
				options.put(d.getName(),d);
			}
			
			String[] split = name.split("/");
			
			String shortName = split[split.length-1];
			
			String[] axes = getAxes(dataset,s.getValue(), shortName);
			if (axes != null) axesMap.put(name, axes);
		
		}
		
		dh.setMetadata(md);
		
		dh.setTree(tree);
		
		try {
			buildAxes(axesMap, dh);
		} catch (MetadataException e) {
			logger.error("Could not build AxesMetadata!", e);
		}
	}
	
	private static void buildAxes(Map<String, String[]> map, IDataHolder dh) throws MetadataException {
		
		for (Entry<String, String[]> entry : map.entrySet()) {
			String name = entry.getKey();
			if (dh.contains(name)){
				int[] shape = dh.getLazyDataset(name).getShape();
				String[] names = entry.getValue();
				AxesMetadata m = null;
				int index = name.lastIndexOf("/");
				
				for (int i = 0 ; (i < shape.length && i < names.length) ; i++) {
					
					String fullName = name.substring(0, index+1) + names[i];
					if (dh.contains(fullName)){
						if (m == null) m = MetadataFactory.createMetadata(AxesMetadata.class, shape.length);
						ILazyDataset ax = dh.getLazyDataset(fullName);
						ax.setName(names[i]);
						m.addAxis(i, dh.getLazyDataset(fullName));
					}
					
				}
			
				if (m != null) {
					ILazyDataset lazyDataset = dh.getLazyDataset(name);
					lazyDataset.addMetadata(m);
				}
			}
		}
		
	}
	
	private static String[] getAxes(IDynamicDataset d, NodeLink l, String name){
		Node s = l.getSource();

		if (s instanceof GroupNode) {
			GroupNode g = (GroupNode)s;
			if (getNXClass(g).equals(NexusTreeUtils.NX_DATA)) {
				String signal = getNXSignal(g);
				if (signal != null && signal.equals(name)) {
					return getNXaxes(g);
				}
			}
		}
		
		return null;
	}
	
	private static String getNXClass(GroupNode g){
		Attribute attribute = g.getAttribute(NexusTreeUtils.NX_CLASS);
		if (attribute != null) return attribute.getFirstElement();
		return "";
	}
	
	private static String getNXSignal(GroupNode g){
		Attribute attribute = g.getAttribute(NexusTreeUtils.NX_SIGNAL);
		if (attribute != null) return attribute.getFirstElement();
		return null;
	}
	
	private static String[] getNXaxes(GroupNode g){
		Attribute attribute = g.getAttribute(NexusTreeUtils.NX_AXES);
		if (attribute == null) return null;
		IDataset value = attribute.getValue();
		if (value instanceof StringDataset) {
			return ((StringDataset)value).getData();
		}
		return null;
	}
	
	
	private static Tree getTree(String path, String host, int port){
		IRemoteData rd = ServiceManager.getRemoteDatasetService().createRemoteData(host, port);
		rd.setPath(path);
		Map<String, Object> map = null;
		try {
			map = rd.getTree();
		} catch (Exception e) {
			logger.error("Error reading tree",e);
		}
		
		if (map == null) return null;
		
		return TreeToMapUtils.mapToTree(map, path);
	}
	
	private static Map<String, NodeLink> findNodes(Tree tree){

		IFindInTree finder = new IFindInTree() {
			
			@Override
			public boolean found(NodeLink node) {
				Node d = node.getDestination();
				return d instanceof DataNode;
			}
		};
		return TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), finder, false, null);

	}
	
	
	private static void addDataset(IDynamicDataset dataset, long[] maxShape, IDataHolder dh, IMetadata md){
		String name = dataset.getName();
		
		dh.addDataset(name, dataset);
		int[] max = new int[maxShape.length];
		for (int i = 0; i < maxShape.length; i++) max[i] = (int)maxShape[i];
		md.addDataInfo(name, max);
	}
	
	private static IDynamicDataset buildDataset(String s, String path, String host, int port){
		String name = "/"+s;
		IDatasetConnector r = ServiceManager.getRemoteDatasetService().createRemoteDataset(host, port);
		r.setPath(path);
		r.setDatasetName(name);
		IDynamicDataset dataset = (IDynamicDataset)r.getDataset();
		dataset.refreshShape();
		dataset.setName(name);
		return dataset;
	}
	
	private static boolean allOnes(long[] shape) {
		for (long l : shape) {
			if (l != 1) return false;
		}
		
		return true;
	}
	
	private static boolean allOnes(int[] shape) {
		for (int l : shape) {
			if (l != 1) return false;
		}
		
		return true;
	}
	
	private void updateDataHolder() {

		String path = getFilePath();
		
		Tree tree = getTree(path, host, port);
		
		if (tree == null) return;
		
		Map<String, NodeLink> result = findNodes(tree);
		
		fillDataHolder(dataHolder.get(), result, tree, dataOptions, this, path, host, port);
		
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
	
	private void addNonStringDataset(ILazyDataset lazyDataset, String name){
		if (lazyDataset instanceof IDynamicDataset) {
			try {
				((IDynamicDataset)lazyDataset).refreshShape();
				AxesMetadata ax = lazyDataset.getFirstMetadata(AxesMetadata.class);
				if (ax != null) {
					int[] refresh = ax.refresh(lazyDataset.getShape());
					((IDynamicDataset) lazyDataset).resize(refresh);
				}


			}catch (Exception e) {
				logger.error("Could not refresh dataset:" + lazyDataset.getName(),e);
			}
		}
		
		int[] shape = lazyDataset.getShape();
		
		if (shape.length == 0 || allOnes(shape)) return;
		
		if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING) {
			
			DataOptions d = new DataOptions(name, this);
			dataOptions.put(d.getName(),d);
		}
	}
	
	private void refreshAllDatasets(){
		IDataHolder dh = dataHolder.get();
		String[] names = dh.getNames();
		for (String n : names) {
			try {
				ILazyDataset lazyDataset = dh.getLazyDataset(n);
				if (lazyDataset instanceof IDynamicDataset) {
					((IDynamicDataset)lazyDataset).refreshShape();
					AxesMetadata ax = lazyDataset.getFirstMetadata(AxesMetadata.class);
					if (ax != null) {
						int[] refresh = ax.refresh(lazyDataset.getShape());
						((IDynamicDataset) lazyDataset).resize(refresh);
					}
				}
			}catch (Exception e) {
				logger.error("Could not refresh dataset: " + n,e);
			}
		}
	}

	private void updateDataOptions() {
		DataOptions[] array = dataOptions.values().stream()
				.filter(d -> d.getPlottableObject() != null && d.getPlottableObject().getNDimensions() != null).toArray(size ->new DataOptions[size]);

		for (DataOptions o : array) {
			NDimensions nDimensions = o.getPlottableObject().getNDimensions();
			o.setAxes(null);
			int[] shape = o.getLazyDataset().getShape();
			if (nDimensions.getRank() != shape.length) {
				o.setPlottableObject(null);
			} else {
				nDimensions.updateShape(shape);
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
				addNonStringDataset(lazyDataset, n);
			}
			return;
		} else {
			updateDataHolder();
		}
		
		refreshAllDatasets();
		updateDataOptions();
		
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
				logger.debug("Data options empty on local reload");
				String[] names = dataHolder.get().getNames();
				for (String n : names) {
					ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
					addNonStringDataset(lazyDataset, n);
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
			logger.error("Could not locally load data!",e);
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
