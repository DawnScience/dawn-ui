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

	
	public static MappedDataFile getMappedDataFile(String path, AssociatedImage image) {
		
		MappedDataFile file = new MappedDataFile(path);
		file.addMapObject(image.toString(), image);
		
		return file;
		
	}
	
	public static MappedDataFile getMappedDataFile(String path, MappedDataFileBean bean, IMonitor monitor) {
		
		MappedDataFile file = new MappedDataFile(path);
		
		for (MappedBlockBean b : bean.getBlocks()) {
			String name = b.getName();
			if (monitor != null) {
				if (monitor.isCancelled()) return null;
				monitor.subTask(name);
			}
			MappedDataBlock block = setUpBlock(path, name, b);
			file.addMapObject(name, block);

			if (monitor != null) monitor.worked(1);
		}
		
		for (MapBean b : bean.getMaps()) {
			if (monitor != null) {
				if (monitor.isCancelled()) return null;
				monitor.subTask(b.getName());
				MappedDataBlock block = file.getDataBlockMap().get(b.getParent());
				MappedData m = setUpMap(path, b.getName(),block);
				file.addMapObject(b.getName(), m);
			}
		}
		
//		for (String map : maps){
//			
//			MappedData m = setUpMap(path, map,block, description);
//			file.addMapObject(map, m);
//		}
		
		return file;
	}
	
	
	private static MappedDataBlock setUpBlock(String path, String blockName, MappedBlockBean bean) {
		
		ILoaderService lService = LocalServiceManager.getLoaderService();
		MappedDataBlock block = null;
		
		List<String> axesNames = Arrays.asList(bean.getAxes());
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(blockName);
			AxesMetadata axm = checkAndBuildAxesMetadata(axesNames, path, bean);
			lz.setMetadata(axm);
			block = new MappedDataBlock(blockName, lz, bean.getxDim(), bean.getyDim());
		} catch (Exception e) {
			
		}
		
		return block;
	}
	
		
	private static MappedData setUpMap(String path, String mapName, MappedDataBlock block) {
		ILoaderService lService = LocalServiceManager.getLoaderService();
		
		try {
			ILazyDataset lz = lService.getData(path, null).getLazyDataset(mapName);
			IDataset d = lz.getSlice();
			while (d.getRank() > 2) {
				d = ((Dataset)d).sum(d.getRank()-1);
			}

			ILazyDataset[] xAxis = block.getXAxis();
			ILazyDataset[] yAxis = block.getYAxis();
			
			ILazyDataset[] yView = new ILazyDataset[yAxis.length];
			ILazyDataset[] xView = new ILazyDataset[xAxis.length];
			
			for (int i = 0; i < xAxis.length; i++) xView[i] = xAxis[i] == null ? null : xAxis[i].getSliceView().squeezeEnds();
			for (int i = 0; i < yAxis.length; i++) yView[i] = yAxis[i] == null ? null : yAxis[i].getSliceView().squeezeEnds();

			if (block.isRemappingRequired() && d.getRank() == 1) {
				AxesMetadataImpl ax = new AxesMetadataImpl(1);
				ax.setAxis(0, xView);

				d.setMetadata(ax);
				return new ReMappedData(mapName, d, block);
			}

			AxesMetadataImpl ax = new AxesMetadataImpl(2);
			ax.setAxis(0,yView);
			ax.setAxis(1, xView);


			d.setMetadata(ax);

			return new MappedData(mapName,d,block);
		}
		catch (Exception e) {
			//FIXME log
		}
		return null;

	}
	
	private static AxesMetadata checkAndBuildAxesMetadata(List<String> axes, String path, MappedBlockBean bean) {
		
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
					
					String second = null;
					if (bean.getxDim() == i && bean.getxAxisForRemapping() != null) second = bean.getxAxisForRemapping();
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
	
	
}
