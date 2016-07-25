package org.dawnsci.mapping.ui.datamodel;


import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromLiveSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IRemoteDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.MetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveMappedDataBlock extends MappedDataBlock implements ILiveData {

	private LiveRemoteAxes axes;
	private boolean connected = true;
	private int port;
	private String host;
	private static final Logger logger = LoggerFactory.getLogger(LiveMappedDataBlock.class);
	
	public LiveMappedDataBlock(String name, IRemoteDataset dataset, int xDim, int yDim, String path, LiveRemoteAxes axes, String host, int port) {
		super(name, dataset, xDim, yDim, path);
		this.axes = axes;
		this.port = port;
		this.host = host;
	}
	
	public ILazyDataset getSpectrum(int x, int y) {
		
		((IRemoteDataset)dataset).refreshShape();
		
		for (IRemoteDataset a : axes.getAxes()) {
			if (a != null) a.refreshShape();
		}
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(yDim,y,y+1,1);
		slice.setSlice(xDim,x,x+1,1);
		
		IDataset slice2;
		try {
			slice2 = dataset.getSlice(slice);
		} catch (DatasetException e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		
		AxesMetadata ax = null;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
			for (int i = 0; i < dataset.getRank(); i++) {
				if (i == xDim || i == yDim || axes.getAxes()[i] == null) continue;
				try {
					ax.setAxis(i, axes.getAxes()[i].getSlice());
				} catch (DatasetException e) {
					logger.error("Could not get data from lazy dataset for axis " + i, e);
				}
			}
		} catch (MetadataException e1) {
			logger.error("Could not create axes metdata", e1);
		}
		slice2.setMetadata(ax);
		MetadataType sslsm = generateSliceMetadata(x,y);
		slice2.setMetadata(sslsm);
		
		return slice2;
	}

	@Override
	public boolean connect(){
		
		connected = true;
		
		try {
			((IRemoteDataset)dataset).connect();
		} catch (Exception e) {
			connected = false;
			logger.error("Could not connect to " + toString());
			return false;
		}
		
		if (connectAxes(true)) {
			connected = true;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean disconnect(){
		
		try {
			((IRemoteDataset)dataset).disconnect();
		} catch (Exception e) {
			logger.error("Could not disconnect from " + toString());
			return false;
		}
		
		if (connectAxes(false)) {
			connected = false;
			return true;
		}
		
		return false;
		
	}
	
	private boolean connectAxes(boolean connect){
		
		boolean success = true;
		
		for (IRemoteDataset a : axes.getAxes()) {
			if (a instanceof IRemoteDataset) {
				
				try {
					if (connect) a.connect();
					else a.disconnect();
				}
				catch (Exception e) {
					logger.error("Error communicating with " + a.getName());
					success = false;
				} 
			}
		}
		
		if (axes.getxAxisForRemapping() != null) {
			try {
				if (connect) axes.getxAxisForRemapping().connect();
				else axes.getxAxisForRemapping().disconnect();
			}
			catch (Exception e) {
				logger.error("Error communicating with " + axes.getxAxisForRemapping().getName());
				success = false;
			} 
		}
		
		return success;
	}
	
	public ILazyDataset[] getXAxis() {
		if (!connected) {			
			connect();

		}
		
		if (!connected) return null;
		
		if (axes.getxAxisForRemapping() != null) return new ILazyDataset[]{axes.getxAxisForRemapping()};
		return new ILazyDataset[]{axes.getAxes()[xDim]};
	}
	
	public ILazyDataset[] getYAxis() {
		if (!connected) {			
			connect();
		}
		
		if (!connected) return null;
		
		return new ILazyDataset[]{axes.getAxes()[yDim]};
	}

	protected double[] calculateRange(ILazyDataset block){
		
		return null;
	}
	
	private MetadataType generateSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromLiveSeriesMetadata(si,sl,host,port,axes.getAxesNames());
	}
}
