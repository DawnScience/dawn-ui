package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;

public class MappedFileFactory {

	
	public static MappedDataFile getMappedDataFile(String path, MappedFileDescription description) {
		
		List<String> blockNames = description.getBlockNames();
		MappedDataFile file = new MappedDataFile(path);
		
		for (String name : blockNames) {
			MappedDataBlock block = setUpBlock(path, name, description.getBlockAxes(name));
			file.addMapObject(name, block);
			List<String> maps = description.getMapNames(name);
			for (String map : maps){
				MappedData m = setUpMap(path, map,block, description.getxAxisName(), description.getyAxisName());
				file.addMapObject(map, m);
			}
		}
		
		return file;
	}
	
	private static MappedDataBlock setUpBlock(String path, String blockName, List<String> axesNames) {
		
		ILoaderService lService = LocalServiceManager.getLoaderService();
		MappedDataBlock block = null;
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(blockName);
			AxesMetadata axm = checkAndBuildAxesMetadata(axesNames, path);
			lz.setMetadata(axm);
			block = new MappedDataBlock(blockName, lz);
		} catch (Exception e) {
			
		}
		
		return block;
	}
	
	private static MappedData setUpMap(String path, String mapName, MappedDataBlock block, String xAxis, String yAxis) {
		ILoaderService lService = LocalServiceManager.getLoaderService();
		MappedData map = null;
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(mapName);
			IDataset d = lz.getSlice();
			while (d.getRank() > 2) {
				d = ((Dataset)d).sum(d.getRank()-1);
			}
			AxesMetadata axm = checkAndBuildAxesMetadata(Arrays.asList(new String[]{yAxis,xAxis}), path);
			d.setMetadata(axm);
			map = new MappedData(mapName, d, block);
		} catch (Exception e) {
			
		}
		return map;
	}
	
	private static AxesMetadata checkAndBuildAxesMetadata(List<String> axes, String path) {
		
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
				} else {
					//approximate 2D with 1D
					int[] start = new int[ss.length];
					int[] stop = start.clone();
					Arrays.fill(stop, 1);
					int[] step = stop.clone();
					SliceND slice = new SliceND(ss,start,stop,step);
					IDataset s1 = lz.getSlice();
					slice.setSlice(i, 0, ss[i], 1);
					ILazyDataset s = lz.getSlice(slice).squeeze();
					axm.addAxis(i, s);
				}
				
				
			}
			
		} catch (Exception e) {
			axm = null;
		}
		
		return axm;
		
	}
	
}
