package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedFileFactory {
	
	private MappedFileFactory() {
		//static only
	}

	private static final Logger logger = LoggerFactory.getLogger(MappedFileFactory.class);
	
	public static MappedDataFile getMappedDataFile(String path, MappedDataFileBean bean, IMonitor monitor, IDataHolder dataHolder) {
		
		if (dataHolder == null) {
			return null;
		}
		
		
		MappedDataFile file = new MappedDataFile(path, bean, dataHolder);
		
		for (MappedBlockBean b : bean.getBlocks()) {
			String name = b.getName();
			if (monitor != null) {
				if (monitor.isCancelled()) return null;
				monitor.subTask(name);
			}
			
			try {
				MappedDataBlock block = setUpBlock(path, name, b, bean.getLiveBean(),bean.getScanRank(), dataHolder,file);
				file.addMapObject(name, block);
			} catch (Exception e) {
				logger.error("Could not build block named " + name, e);
			}
			

			if (monitor != null) monitor.worked(1);
		}
		
		for (MapBean b : bean.getMaps()) {

			if (monitor != null && monitor.isCancelled()) return null;
			if (monitor != null) monitor.subTask(b.getName());
			MappedDataBlock block = file.getDataBlockMap().get(b.getParent());

			if (block == null && !file.getDataBlockMap().isEmpty()) {
				block = file.getDataBlockMap().values().iterator().next();
			}

			AbstractMapData m = setUpMap(path, b.getName(),block, bean.getLiveBean(),dataHolder);

			if (bean.getLiveBean() == null && m != null) {
				m.getData().setName(m.toString());
			}

			file.addMapObject(b.getName(), m);
		}
		
		for (AssociatedImageBean b : bean.getImages()) {
			AssociatedImage im = getAssociatedImage(path,b, dataHolder);
			file.addMapObject(im.getLongName(), im);
		}
		
		
		for (AssociatedImageStackBean b : bean.getImageStacks()) {
			AssociatedImageStack im = getAssociatedImageStack(path,b, dataHolder);
			file.addMapObject(im.getLongName(), im);
		}
		
		return file;
	}
	
	
	private static MappedDataBlock setUpBlock(String path, String blockName, MappedBlockBean bean, LiveDataBean live, int scanRank, IDataHolder dataHolder, MappedDataFile file) {
		MappedDataBlock block = null;
		
		MapScanDimensions msd = new MapScanDimensions(bean.getxDim(), bean.getyDim(), scanRank);
		
		List<String> axesNames = Arrays.asList(bean.getAxes());

		ILazyDataset lz = dataHolder.getLazyDataset(blockName);
		lz.clearMetadata(AxesMetadata.class);
		AxesMetadata axm = checkAndBuildAxesMetadata(axesNames, bean, dataHolder);
		lz.setMetadata(axm);
		block = new MappedDataBlock(blockName, lz,path,msd,file,live != null);

		return block;
	}
	
		
	private static AbstractMapData setUpMap(String path, String mapName, MappedDataBlock block, LiveDataBean live, IDataHolder dataHolder) {
		
		boolean isLive = (live != null && block.isLive());
		

			ILazyDataset lz =dataHolder.getLazyDataset(mapName);
			
			lz.clearMetadata(AxesMetadata.class);

			if (!isLive) {

				try {
				//hack for old i18
				if (block.getScanRank() < lz.getRank()) {
					Dataset d = DatasetUtils.sliceAndConvertLazyDataset(lz);
					d= d.sum(d.getRank()-1);
					lz = d;
				}

				block.getMapDims().setMapAxes(block.getLazy(), lz);

				lz = DatasetUtils.sliceAndConvertLazyDataset(lz);

				if (block.isTransposed()) {
					Dataset ds = DatasetUtils.convertToDataset((IDataset)lz);
					lz = ds.transpose();

				}
				
				} catch (Exception e) {
					logger.error("Could not slice map",e);
					return null;
				}
			} 
			
			if (block.isRemappingRequired()) {
				return new ReMappedData(mapName,lz,block,path, isLive);
			}

			return new MappedData(mapName,lz,block,path, isLive);
		

	}
	
	private static AssociatedImage getAssociatedImage(String path, AssociatedImageBean b, IDataHolder dataHolder) {
		try {
			Dataset d = DatasetUtils.sliceAndConvertLazyDataset(dataHolder.getLazyDataset(b.getName()));
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ax.addAxis(0,dataHolder.getLazyDataset(b.getAxes()[0]));
			ax.addAxis(1,dataHolder.getLazyDataset(b.getAxes()[1]));
			
			if (d.getRank() == 3) {
				d.clearMetadata(AxesMetadata.class);
				RGBDataset ds = createRGBDataset(d, d.getShape()[2] == 3);
				ds.addMetadata(ax);
				return new AssociatedImage(b.getName(), ds, path);
			} else if (d.getRank() == 2) {
				RGBDataset ds = DatasetUtils.createCompoundDataset(RGBDataset.class, d);
				ds.addMetadata(ax);
				return new AssociatedImage(b.getName(), ds, path);
				
			}
			
		} catch (Exception e) {
			logger.error("Error loading image",e);
		}
		return null;
		
	}
	
	private static AssociatedImageStack getAssociatedImageStack(String path, AssociatedImageStackBean b, IDataHolder dataHolder) {
		try {
			
			ILazyDataset lz = dataHolder.getLazyDataset(b.getName());
			
			int rank = lz.getRank();
			int[] shape = lz.getShape();
			
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, rank);
			
			String[] axes = b.getAxes();
			
			for (int i = 0; i < axes.length; i++) {
				if (i >= (rank - 2) || axes[i] == null) {
					ax.setAxis(i, DatasetFactory.createRange(shape[i]));
				} else {
					ILazyDataset lzax = dataHolder.getLazyDataset(axes[i]);
					ax.setAxis(i, lzax);
				}
			}
			
			lz.setMetadata(ax);
			
			return new AssociatedImageStack(b.getName(), lz, path);

		} catch (Exception e) {
			logger.error("Error loading image stack",e);
		}
		return null;
		
	}
	
	private static RGBDataset createRGBDataset(Dataset d, boolean interleaved) {
	
		if (interleaved) {
			
			CompoundDataset cd = DatasetUtils.createCompoundDatasetFromLastAxis(d, true);
			
			return RGBDataset.createFromCompoundDataset(cd);
		}

		return DatasetUtils.createCompoundDataset(RGBDataset.class, new Dataset[] {
				d.getSlice(new Slice(0,1,1),null,null).squeeze(),
				d.getSlice(new Slice(1,2,1),null,null).squeeze(),
				d.getSlice(new Slice(2,3,1),null,null).squeeze()});	
	}
	
	private static AxesMetadata checkAndBuildAxesMetadata(List<String> axes, MappedBlockBean bean, IDataHolder dataHolder) {
		
		AxesMetadata axm = null; 
		
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, axes.size());
			for (int i = 0; i < axes.size(); i++) {
				if (axes.get(i) == null)  continue;
				ILazyDataset lz = dataHolder.getLazyDataset(axes.get(i));
				lz.setName(axes.get(i));
				
				int rank = lz.getRank();
				
				if (lz instanceof IDynamicDataset) {
					long[] maxShape = Arrays.stream(((IDynamicDataset)lz).getMaxShape()).asLongStream().toArray();
					rank = MappingUtils.getSqueezedRank(maxShape);
				}
				
				if (rank == 1) {
					axm.addAxis(i, lz);
					
					String second = null;
					if (bean.getxDim() == i && bean.getxAxisForRemapping() != null) second = bean.getxAxisForRemapping();
					if (second != null) {
						ILazyDataset l =  dataHolder.getLazyDataset(second);
						l.setName(second);
						axm.addAxis(i, l);
					}
					
				} else {
					//approximate 2D with 1D, should be done int the map/mapobjects
					IDataset ds = lz.getSlice();
					double min = ds.min(true).doubleValue();
					double max = ds.max(true).doubleValue();
					ILazyDataset s = DatasetFactory.createLinearSpace(DoubleDataset.class,min, max, lz.getShape()[i]);
					s.setName(axes.get(i));
					axm.addAxis(i, s);
				}
				
				
			}
			
		} catch (Exception e) {
			axm = null;
		}
		
		return axm;
		
	}
}
