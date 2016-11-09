package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedFileFactory {

	private final static Logger logger = LoggerFactory.getLogger(MappedFileFactory.class);
	
	public static MappedDataFile getMappedDataFile(String path, AssociatedImage image) {
		
		MappedDataFile file = new MappedDataFile(path, (LiveDataBean)null);
		file.addMapObject(image.toString(), image);
		
		return file;
		
	}
	
	public static MappedDataFile getMappedDataFile(String path, MappedDataFileBean bean, IMonitor monitor) {
		
		MappedDataFile file = new MappedDataFile(path, bean);
		
		for (MappedBlockBean b : bean.getBlocks()) {
			String name = b.getName();
			if (monitor != null) {
				if (monitor.isCancelled()) return null;
				monitor.subTask(name);
			}
			MappedDataBlock block = setUpBlock(path, name, b, bean.getLiveBean(),bean.getScanRank());
			file.addMapObject(name, block);

			if (monitor != null) monitor.worked(1);
		}
		
		for (MapBean b : bean.getMaps()) {

				if (monitor != null && monitor.isCancelled()) return null;
				if (monitor != null) monitor.subTask(b.getName());
				MappedDataBlock block = file.getDataBlockMap().get(b.getParent());
				AbstractMapData m = setUpMap(path, b.getName(),block, bean.getLiveBean());
				if (bean.getLiveBean() == null) m.getData().setName(m.toString());
 				file.addMapObject(b.getName(), m);
		}
		
		for (AssociatedImageBean b : bean.getImages()) {
			AssociatedImage im = getAssociatedImage(path,b);
			file.addMapObject(im.getLongName(), im);
		}
		
		return file;
	}
	
	
	private static MappedDataBlock setUpBlock(String path, String blockName, MappedBlockBean bean, LiveDataBean live, int scanRank) {
		MappedDataBlock block = null;
		
		MapScanDimensions msd = new MapScanDimensions(bean.getxDim(), bean.getyDim(), scanRank);
		
		List<String> axesNames = Arrays.asList(bean.getAxes());
		if (live != null) {
			IDatasetConnector lz = getRemoteDataset(path,blockName,live);
			LiveRemoteAxes remoteAxes = getRemoteAxes(axesNames, path, bean, live);
			block = new MappedDataBlock(blockName, lz, msd,path, remoteAxes, live.getHost(),live.getPort());
			return block;
		}
		
		
		try {
			ILazyDataset lz = getLazyDataset(path,blockName);
			lz.clearMetadata(AxesMetadata.class);
			AxesMetadata axm = checkAndBuildAxesMetadata(axesNames, path, bean);
			lz.setMetadata(axm);
			block = new MappedDataBlock(blockName, lz,path,msd);
		} catch (Exception e) {
			
		}
		
		return block;
	}
	
		
	private static AbstractMapData setUpMap(String path, String mapName, MappedDataBlock block, LiveDataBean live) {
		
		if (live != null && block.isLive()) {
			
			if (block.isRemappingRequired()) {
				return new ReMappedData(mapName, getRemoteDataset(path,mapName,live), block, path);
			}
			
			return new MappedData(mapName, getRemoteDataset(path,mapName,live),block, path);
		}
		
		
		try {
			ILazyDataset lz = getLazyDataset(path,mapName);
			
			lz.clearMetadata(AxesMetadata.class);

			block.getMapDims().setMapAxes(block.getLazy(), lz);
			
			IDataset d = DatasetUtils.sliceAndConvertLazyDataset(lz);
			
			if (block.isTransposed()) {
				Dataset ds = DatasetUtils.convertToDataset(d);
				d = ds.transpose();
				
			}

			return new MappedData(mapName,d,block,path);
		}
		catch (Exception e) {
			logger.error("Could not build map", e);
		}
		return null;

	}
	
	private static AssociatedImage getAssociatedImage(String path, AssociatedImageBean b) {
		try {
			Dataset d = DatasetUtils.sliceAndConvertLazyDataset(getLazyDataset(path, b.getName()));
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ax.addAxis(0,getLazyDataset(path, b.getAxes()[0]));
			ax.addAxis(1,getLazyDataset(path, b.getAxes()[1]));
			
			if (d.getRank() == 3) {
				
				RGBDataset ds = (RGBDataset) DatasetUtils.createCompoundDataset(Dataset.RGB, d.getSlice(new Slice(0,1,1),null,null).squeeze(), d.getSlice(new Slice(1,2,1),null,null).squeeze(), d.getSlice(new Slice(2,3,1),null,null).squeeze());
				ds.addMetadata(ax);
				return new AssociatedImage(b.getName(), ds, path);
			} else if (d.getRank() == 2) {
				RGBDataset ds = (RGBDataset) DatasetUtils.createCompoundDataset(Dataset.RGB, d);
				ds.addMetadata(ax);
				return new AssociatedImage(b.getName(), ds, path);
				
			}
			
		} catch (Exception e) {
			logger.error("Error loading image",e);
		}
		return null;
		
	}
	
	private static LiveRemoteAxes getRemoteAxes(List<String> axes, String path, MappedBlockBean bean, LiveDataBean live) {
		IDatasetConnector[] r = new IDatasetConnector[axes.size()];
		String[] axesNames = new String[axes.size()];
		
		for (int i = 0; i < axes.size(); i++) {
			String s = axes.get(i);
			axesNames[i] = s;
			if (s != null) {
					r[i] = getRemoteDataset(path, s, live);
					if (r[i] != null) r[i].setDatasetName(s);
			}

		}
		
		
		
		LiveRemoteAxes lra = new LiveRemoteAxes(r, axesNames,live.getHost(),live.getPort());
		
		String s = bean.getxAxisForRemapping();
		if (s != null){
			IDatasetConnector rd = getRemoteDataset(path, s, live);
			rd.setDatasetName(s);
				lra.setxAxisForRemapping(rd);
				lra.setxAxisForRemappingName(s);
		}
		
		return lra;
	}
	
	private static AxesMetadata checkAndBuildAxesMetadata(List<String> axes, String path, MappedBlockBean bean) {
		
		AxesMetadata axm = null; 
		
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, axes.size());
			for (int i = 0; i < axes.size(); i++) {
				if (axes.get(i) == null) continue;
				ILazyDataset lz = getLazyDataset(path, axes.get(i));
				lz.setName(axes.get(i));
				int[] ss = lz.getShape();
				
				if (ss.length == 1) {
					axm.addAxis(i, lz);
					
					String second = null;
					if (bean.getxDim() == i && bean.getxAxisForRemapping() != null) second = bean.getxAxisForRemapping();
					if (second != null) axm.addAxis(i, getLazyDataset(path, second));
					
				} else {
					//approximate 2D with 1D, should be done int the map/mapobjects
					IDataset ds = lz.getSlice();
					double min = ds.min(true).doubleValue();
					double max = ds.max(true).doubleValue();
					ILazyDataset s = DatasetFactory.createLinearSpace(min, max, ss[i], Dataset.FLOAT64);
					
//					int[] start = new int[ss.length];
//					int[] stop = start.clone();
//					Arrays.fill(stop, 1);
//					int[] step = stop.clone();
//					SliceND slice = new SliceND(ss,start,stop,step);
//					slice.setSlice(i, 0, ss[i], 1);
//					ILazyDataset s = lz.getSlice(slice).squeeze();
					s.setName(axes.get(i));
					axm.addAxis(i, s);
				}
				
				
			}
			
		} catch (Exception e) {
			axm = null;
		}
		
		return axm;
		
	}
	
	private static ILazyDataset getLazyDataset(String path, String name, LiveDataBean lb) throws Exception {
		if (lb == null) {
			ILoaderService lService = LocalServiceManager.getLoaderService();
			ILazyDataset lazyDataset = lService.getData(path, null).getLazyDataset(name);
			lazyDataset.clearMetadata(null);
			return lazyDataset;
		} 
		
		return getRemoteDataset(path, name, lb).getDataset();
	}
	
	private static ILazyDataset getLazyDataset(String path, String name) throws Exception {
		return getLazyDataset(path, name, null);
	}
	
	private static IDatasetConnector getRemoteDataset(String path, String name, LiveDataBean lb) {
		IRemoteDatasetService rds = LocalServiceManager.getRemoteDatasetService();
		IDatasetConnector remote = rds.createRemoteDataset(lb.getHost(), lb.getPort());
		remote.setPath(path);
		remote.setDatasetName(name);
		
		return remote;
	}
	
	
}
