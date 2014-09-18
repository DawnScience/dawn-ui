package org.dawnsci.spectrum.ui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DatasetManager {
	
	Collection<String> names;
	Map<Integer, List<String>> possibleAxisDatasets;
	Map<Integer, List<String>> datasetMap;
	
	private DatasetManager(Collection<String> names, Map<Integer, List<String>> possibleAxisDatasets ,Map<Integer, List<String>> datasetMap) {
		this.possibleAxisDatasets = possibleAxisDatasets;
		this.datasetMap = datasetMap;
		this.names = names;
	}
	
	public static DatasetManager create(String path) {
		
		try {
			
			//should work, doesn't
			//IMetadata meta = LoaderFactory.getMetaData(path, null);
			IDataHolder dh = LoaderFactory.getData(path);
			IMetadata meta =dh.getMetadata();

			
			if (meta != null) {
				return getDatasetManager(meta);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			IDataHolder dh = LoaderFactory.getData(path);
			if (dh != null) {
				return getDatasetManager(dh);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public List<String> getPossibleAxisDatasets(int size) {
		if (!possibleAxisDatasets.containsKey(size)) return null;
		
		return possibleAxisDatasets.get(size);
	}
	
	public List<String> getPossibleAxisDatasets(){
		List<String> output = new ArrayList<String>();
		Collection<List<String>> vals = possibleAxisDatasets.values();
		for (List<String> list : vals) {
			for (String name : list) {
				output.add(name);
			}
		}
		
		return output;
	}
	
	public List<String> getAllowedDatasets(int size) {
		if (!datasetMap.containsKey(size)) return null;
		
		return datasetMap.get(size);
	}
	
	public Collection<String> getDatasetNames() {
		return names;
	}
	
	private static DatasetManager getDatasetManager(IMetadata meta) {
		
		Map<Integer, List<String>> axis = new HashMap<Integer, List<String>>();
		Map<Integer, List<String>> all = new HashMap<Integer, List<String>>();
		
		Map<String, int[]> shapeMap = meta.getDataShapes();
		
		for (String name : shapeMap.keySet()) {
			
			int[] shape = shapeMap.get(name);
			
			if (shape == null) {
				try {
					IDataHolder dh = LoaderFactory.getData(meta.getFilePath());
					shape = dh.getLazyDataset(name).getShape();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}
			
			if (shape == null) continue;
			
			updateDatasetMap(all,name,shape);
			
			int size = getSuitableAxisSize(shape);
			
			if (size != 0) {
				updateMap(axis,name,size);
			}
		}
		
		return new DatasetManager(shapeMap.keySet(), axis, all);
	}
	
	private static DatasetManager getDatasetManager(IDataHolder dh) {
//		
		String[] names = dh.getNames();
		Map<Integer, List<String>> axis = new HashMap<Integer, List<String>>();
		Map<Integer, List<String>> all = new HashMap<Integer, List<String>>();
		
		for (String name : names) {
			ILazyDataset ds = dh.getLazyDataset(name);
			
			updateDatasetMap(all,name,ds.getShape());
			
			int size = getSuitableAxisSize(ds.getShape());
			if (size != 0) {
				updateMap(axis,name,size);
			}
		}
		
		return new DatasetManager(Arrays.asList(names), axis, all);
	}
	
	private static int getSuitableAxisSize(int[] shape) {
		
		if (shape.length == 1 && shape[0] > 1) {
			return shape[0];
		}
		
		int max = 0;
		int nGreaterThanOne = 0;
		
		for (int i = 0; i < shape.length; i++) {
			if (shape[i] > 1) {
				nGreaterThanOne++;
				if (shape[i] > max) {
					max = shape[i];
				}
			}
		}
		
		if (max <=1 ) return 0;
		
		if (nGreaterThanOne > 1) return 0;
		
		return max;
	}
	
	private static void updateDatasetMap(Map<Integer, List<String>> map, String name, int[] shape) {
		
		for (int i = 0; i < shape.length ; i++) {
			if (shape[i] > 1) {
				updateMap(map, name, shape[i]);
			}
		}
		
	}
	
	private static void updateMap(Map<Integer, List<String>> map, String name, int size) {
		if (map.containsKey(size)) {
			map.get(size).add(name);
		} else {
			List<String> list = new ArrayList<String>();
			list.add(name);
			map.put(size, list);
		}
	}
	
}
