package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.DataEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataListener;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveMappedData extends MappedData implements ILiveData {

	private boolean connected = false;
	private static final Logger logger = LoggerFactory.getLogger(LiveMappedData.class);
	
	public LiveMappedData(String name, IRemoteDataset map, LiveMappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}

	@Override
	public boolean connect() {
		
		try {
			((IRemoteDataset)baseMap).connect();
			((IRemoteDataset)baseMap).addDataListener(new IDataListener() {
				
				@Override
				public void dataChangePerformed(DataEvent evt) {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			logger.error("Could not connect to " + toString());
			return false;
		}
		
		if (((LiveMappedDataBlock)parent).connect()) {
			connected = true;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean disconnect() {
		try {
			((IRemoteDataset)baseMap).disconnect();
		} catch (Exception e) {
			logger.error("Could not disconnect from " + toString());
			return false;
		}
		
		if (((LiveMappedDataBlock)parent).disconnect()) {
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
			((IRemoteDataset)baseMap).refreshShape();
			ma = baseMap.getSlice();
		} catch (Exception e) {
			//TODO log?
		}
		
		if (ma == null) return null;
		
		if (parent.isTransposed()) ma = DatasetUtils.convertToDataset(ma).transpose();
		
		// TODO This check is probably not required
		if (baseMap.getSize() == 1) return null;
		
		ILazyDataset ly = parent.getYAxis()[0];
		ILazyDataset lx = parent.getXAxis()[0];
		
		((IRemoteDataset)ly).refreshShape();
		((IRemoteDataset)lx).refreshShape();
		
		IDataset y = ly.getSlice();
		IDataset x = lx.getSlice();
		
		if (y.getRank() == 2) {
			SliceND s = new SliceND(y.getShape());
			s.setSlice(1, 0, 1, 1);
			y = y.getSlice(s);
			y.squeeze();
		}
		
		if (x.getRank() == 2) {
			SliceND s = new SliceND(x.getShape());
			s.setSlice(0, 0, 1, 1);
			x = x.getSlice(s);
			x.squeeze();
		}
		
		AxesMetadataImpl axm = new AxesMetadataImpl(2);
		axm.addAxis(0, y);
		axm.addAxis(1, x);
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
