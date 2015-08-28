package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;

public class MappedFileFactory {

	
	public static MappedDataFile getMappedDataFile(String path, MappedFileDescription description, IMonitor monitor) {
		
		List<String> blockNames = description.getBlockNames();
		MappedDataFile file = new MappedDataFile(path);
		
		for (String name : blockNames) {
			if (monitor != null) {
				if (monitor.isCancelled()) return null;
				monitor.subTask(name);
			}
			MappedDataBlock block = setUpBlock(path, name, description);
			file.addMapObject(name, block);
			List<String> maps = description.getMapNames(name);
			for (String map : maps){
				if (monitor != null) {
					if (monitor.isCancelled()) return null;
					monitor.subTask(map);
				}
				MappedData m = setUpMap(path, map,block, description);
				file.addMapObject(map, m);
			}
			if (monitor != null) monitor.worked(1);
		}
		
		return file;
	}
	
	private static MappedDataBlock setUpBlock(String path, String blockName, MappedFileDescription description) {
		
		ILoaderService lService = LocalServiceManager.getLoaderService();
		MappedDataBlock block = null;
		
		List<String> axesNames = description.getBlockAxes(blockName);
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(blockName);
			AxesMetadata axm = checkAndBuildAxesMetadata(axesNames, path, description);
			lz.setMetadata(axm);
			block = new MappedDataBlock(blockName, lz);
		} catch (Exception e) {
			
		}
		
		return block;
	}
	
	private static MappedData setUpMap(String path, String mapName, MappedDataBlock block, MappedFileDescription description) {
		
		String xAxis = description.getxAxisName();
		String yAxis = description.getyAxisName();
		
		ILoaderService lService = LocalServiceManager.getLoaderService();
		MappedData map = null;
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(mapName);
			IDataset d = lz.getSlice();
			while (d.getRank() > 2) {
				d = ((Dataset)d).sum(d.getRank()-1);
			}
			
			List<String> names = null;
			
			if (description.isRemappingRequired()) {
				names = Arrays.asList(new String[]{yAxis});
			} else {
				names =  Arrays.asList(new String[]{yAxis,xAxis});
			}
			
			AxesMetadata axm = checkAndBuildAxesMetadata(names, path, description);
			d.setMetadata(axm);
			if (description.isRemappingRequired()) map = new ReMappedData(mapName, d, block);
			else map = new MappedData(mapName, d, block);
		} catch (Exception e) {
			
		}
		return map;
	}
	
	private static AxesMetadata checkAndBuildAxesMetadata(List<String> axes, String path, MappedFileDescription description) {
		
		AxesMetadataImpl axm = null; 
		ILoaderService lService = LocalServiceManager.getLoaderService();
		
		
		try {
			axm = new AxesMetadataImpl(axes.size());
			for (int i = 0; i < axes.size(); i++) {
				if (axes.get(i) == null) continue;
				ILazyDataset lz = lService.getData(path, null).getLazyDataset(axes.get(i));
				int[] ss = lz.getShape();
				
				if (ss.length == 1) {
					axm.addAxis(i, lz);
					
					String second = getSecondaryIfRequired(axes.get(i), description);
					if (second != null) axm.addAxis(i, lService.getData(path, null).getLazyDataset(second));
					
				} else {
					//approximate 2D with 1D, should be done int the map/mapobjects
					int[] start = new int[ss.length];
					int[] stop = start.clone();
					Arrays.fill(stop, 1);
					int[] step = stop.clone();
					SliceND slice = new SliceND(ss,start,stop,step);
					slice.setSlice(i, 0, ss[i], 1);
					ILazyDataset s = lz.getSlice(slice).squeeze();
					s.setName(lz.getName());
					axm.addAxis(i, s);
				}
				
				
			}
			
		} catch (Exception e) {
			axm = null;
		}
		
		return axm;
		
	}
	
	private static String getSecondaryIfRequired(String name, MappedFileDescription description) {
		if (!description.isRemappingRequired()) return null;
		if (!name.equals(description.getyAxisName())) return null;
		return description.getxAxisName();
	}
	
}
