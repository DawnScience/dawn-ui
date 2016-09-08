package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveMappedData extends MappedData implements ILiveData {

	private boolean connected = false;
	private static final Logger logger = LoggerFactory.getLogger(LiveMappedData.class);
	
	public LiveMappedData(String name, IDatasetConnector map, MappedDataBlock parent, String path) {
		super(name, map.getDataset(), parent, path);
	}

	@Override
	public boolean connect() {
		
		try {
			((IDatasetConnector)baseMap).connect();
			((IDatasetConnector)baseMap).addDataListener(new IDataListener() {
				
				@Override
				public void dataChangePerformed(DataEvent evt) {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			logger.error("Could not connect to " + toString());
			return false;
		}
		
		if (parent.connect()) {
			connected = true;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean disconnect() {
		try {
			((IDatasetConnector)baseMap).disconnect();
		} catch (Exception e) {
			logger.error("Could not disconnect from " + toString());
			return false;
		}
		
		if (parent.disconnect()) {
			connected = false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public IDataset getData(){
		
		if (!connected) {			
			try {
				connect();
			} catch (Exception e) {
				return null;
			}
		}

		IDataset ma = null;
		
		try{
			((IDatasetConnector)baseMap).refreshShape();
			ma = baseMap.getSlice();
		} catch (Exception e) {
			//TODO log?
		}
		
		if (ma == null) return null;
		
		ma.setName(this.toString());
		
		if (parent.isTransposed()) ma = DatasetUtils.convertToDataset(ma).transpose();
		
		// TODO This check is probably not required
		if (baseMap.getSize() == 1) return null;
		
		ILazyDataset ly = parent.getYAxis()[0];
		ILazyDataset lx = parent.getXAxis()[0];
		
		((IDatasetConnector)ly).refreshShape();
		((IDatasetConnector)lx).refreshShape();
		
		IDataset x;
		IDataset y;
		try {
			x = lx.getSlice();
			y = ly.getSlice();
		} catch (DatasetException e) {
			e.printStackTrace();
			return null;
		}
		
		if (y.getRank() == 2) {
			SliceND s = new SliceND(y.getShape());
			s.setSlice(1, 0, 1, 1);
			y = y.getSlice(s);
			if (y.getSize() == 1) {
				y.setShape(new int[]{1});
			} else {
				y.squeeze();
			}
			
		}
		
		if (x.getRank() == 2) {
			SliceND s = new SliceND(x.getShape());
			s.setSlice(0, 0, 1, 1);
			x = x.getSlice(s);
			if (x.getSize() == 1) {
				x.setShape(new int[]{1});
			} else {
				x.squeeze();
			}
			
		}
		
		AxesMetadata axm = null;
		try {
			axm = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			axm.addAxis(0, y);
			axm.addAxis(1, x);
		} catch (MetadataException e) {
			logger.error("Could not create axes metdata", e);
		}

		int[] mapShape = ma.getShape();
		SliceND s = new SliceND(mapShape);
		if (mapShape[0] > y.getShape()[0]) s.setSlice(0, 0, y.getShape()[0], 1);
		if (mapShape[1] > x.getShape()[0]) s.setSlice(1, 0, x.getShape()[0], 1);
		IDataset fm = ma.getSlice(s);
		fm.setMetadata(axm);
		setRange(calculateRange(fm));
		map = fm;
		return fm;
	}
	
	
	protected double[] calculateRange(ILazyDataset m){
		
		if (m instanceof IDataset) return super.calculateRange(m);
		
		return null;
	}
}
