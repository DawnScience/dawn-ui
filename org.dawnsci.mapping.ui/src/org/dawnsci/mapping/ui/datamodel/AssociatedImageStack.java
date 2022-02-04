package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssociatedImageStack implements PlottableMapObject {
	
	private String name;
	private String shortName;
	private String path;
	private String nameSuffix;
	private ILazyDataset stack;
	private IDataset image;
	private double[] range;
	private boolean plotted;
	
	private MapScanDimensions mapDims;
	
	private static final Logger logger = LoggerFactory.getLogger(AssociatedImageStack.class);
	
	
	public AssociatedImageStack(String name, ILazyDataset stack, String path) {
		this.name = name;
		this.stack = stack;
		
		int rank = stack.getRank();
		
		this.mapDims = new MapScanDimensions(rank-1, rank-2, rank);
		
		mapDims.updateNonXYScanSlice(0, stack.getShape()[0]/2);
		buildImage();

		this.range = calculateRange(stack);
		this.path = path;
		this.shortName = MappingUtils.getShortName(name);
	}
	
	private void buildImage() {
		SliceND s = mapDims.getMapSlice(stack);

		try {
			IDataset im = stack.getSlice(s);
			buildSuffix(s, im.getFirstMetadata(AxesMetadata.class));
		    image = im.squeeze();
			
		} catch (DatasetException e) {
			logger.error("Could not build image from stack", e);
		}
		
	}
	
	protected void buildSuffix(SliceND slice, AxesMetadata m) {
		
		try {
			ILazyDataset[] md = m.getAxes();
			if (md == null) return;

			StringBuilder builder = new StringBuilder(" ");

			builder.append("[");
			Slice[] s = slice.convertToSlice();
			int[] shape = slice.getShape();
			for (int i = 0 ; i < md.length; i++){
				
				if (md[i] == null || shape[i] != 1) {
					builder.append(s[i].toString());
					builder.append(",");
					continue;
				}
				
				
				IDataset d = md[i].getSlice();
				if (d == null || d.getSize() != 1){
					builder.append(s[i].toString());
				} else {
					d.squeeze();
					double val = d.getDouble();
					builder.append(Double.toString(val));
				}
				builder.append(",");
			}
			
			builder.setCharAt(builder.length()-1,']');
			
			nameSuffix = builder.toString();
		} catch (Exception e) {
			logger.warn("Could not build name suffix", e);
		}
		
	}
	

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}

	@Override
	public double[] getRange() {
		return range;
	}

	@Override
	public String getLongName() {
		return path + " : " + name;
	}

	@Override
	public IDataset getMap() {
		if (image != null) return image;
		
		buildImage();
		
		return image;
	}

	@Override
	public boolean isLive() {
		return false;
	}
	
	public int getNImages() {
		return stack.getShape()[0];
	}
	
	public int getCurrentImageNumber() {
		return mapDims.getNonXYScanSlice(0);
	}
	
	public void setCurrentImageNumber(int n) {
		image = null;
		mapDims.updateNonXYScanSlice(0, n);

	}

	@Override
	public void update() {
	}

	@Override
	public int getTransparency() {
		return 255;
	}

	@Override
	public void setColorRange(double[] range) {

	}

	@Override
	public double[] getColorRange() {
		return null;
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		// no spectrum
		return null;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isPlotted() {
		return plotted;
	}

	@Override
	public void setPlotted(boolean plot) {
		this.plotted = plot;

	}
	
	private double[] calculateRange(ILazyDataset block){
		
		if (block == null) return null;
		
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(block);
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		return MappingUtils.calculateRangeFromAxes(new IDataset[]{ax[yDim],ax[xDim]});
	}
	
	@Override
	public String toString() {
		return shortName + nameSuffix;
	}

	@Override
	public void setTransparency(int transparency) {
		// unsupported for now
		
	}

}
