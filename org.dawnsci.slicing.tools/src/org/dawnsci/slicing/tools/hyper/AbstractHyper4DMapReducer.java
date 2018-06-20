package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;

public abstract class AbstractHyper4DMapReducer implements IDatasetROIReducer {

	private final static RegionType TYPE = RegionType.BOX;
	protected List<IDataset> traceAxes;
	
	protected int xDim;
	protected int yDim;
	protected int xOther;
	protected int yOther;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes, IROI roi, Slice[] slices, int[] order,
			IMonitor monitor) throws Exception {
		
		SliceND s = new SliceND(data.getShape(),slices);
		
		int xStart = (int)Math.floor(roi.getPointX());
		int yStart = (int)Math.floor(roi.getPointY());
		
		double[] lengths = ((RectangularROI)roi).getLengths();
		
		int xLength = (int)Math.floor(lengths[0]);
		int yLength = (int)Math.floor(lengths[1]);
		
		s.setSlice(order[xDim], yStart, yStart+yLength, 1);
		s.setSlice(order[yDim], xStart, xStart+xLength, 1);
		
		IDataset slice = data.getSlice(s);
		
		Dataset d = DatasetUtils.convertToDataset(slice);
		Dataset m;
		if (order[xDim] > order[yDim]) {
			m = d.mean(order[xDim], true).mean(order[yDim],true).squeeze();
		} else {
			m = d.mean(order[yDim], true).mean(order[xDim],true).squeeze();
		}
		
		IDataset x = axes.get(xOther).getSlice();
		IDataset y = axes.get(yOther).getSlice();
		
		if (x.getRank() > 1) {
			int skip = order[yOther] == 0 ? 0 : 1;
			SliceND sx = new SliceND(x.getShape());
			for (int i = 0; i < x.getRank(); i++) {
				if (i== skip) {
					continue;
				}
				sx.setSlice(i, new Slice(0,1,1));
			}
			
			x = x.getSlice(sx).squeeze();
		}
		
		if (y.getRank() > 1) {
			int skip = order[yOther] == 0 ? 0 : 1;
			
			SliceND sy = new SliceND(y.getShape());
			for (int i = 0; i < y.getRank(); i++) {
				if (i== skip) {
					continue;
				}
				sy.setSlice(i, new Slice(0,1,1));
			}
			
			y = y.getSlice(sy).squeeze();
		}

		this.traceAxes = new ArrayList<IDataset>();
		
		if (order[yOther] < order[xOther]) {
			m = m.transpose();
		}
		
		this.traceAxes.add(y);
		this.traceAxes.add(x);
		
		return m;
	}
	
	@Override
	public boolean isOutput1D() {
		return false;
	}
	
	
	@Override
	public boolean supportsMultipleRegions() {
		return false;
	}

	@Override
	public List<IDataset> getAxes() {
		return traceAxes;
	}
	
	@Override
	public IROI getInitialROI(List<IDataset> axes, int[] order) {
		int[] x = axes.get(xDim).getShape();
		int[] y = axes.get(yDim).getShape();
		
		int xval = x[0];
		int yval = y[0];
		
		if (y.length > 1) {
			yval = y[1];
		}
		
		return new RectangularROI(yval/10, xval/10, yval/10, xval/10, 0);
	}

	@Override
	public List<RegionType> getSupportedRegionType() {
		return Arrays.asList(TYPE);
	}
}
