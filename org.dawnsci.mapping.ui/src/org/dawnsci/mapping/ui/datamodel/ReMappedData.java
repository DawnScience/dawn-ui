package org.dawnsci.mapping.ui.datamodel;

import java.util.List;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.impl.Stats;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.XYImagePixelCache;

public class ReMappedData extends MappedData {

	private IDataset reMapped;
	private IDataset lookup;
	private int[] shape;
	
	public ReMappedData(String name, IDataset map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
		
	}
	
	@Override
	protected double[] calculateRange(IDataset map){
		IDataset[] ax = MappingUtils.getAxesForDimension(map,0);
		double[] r = new double[4];
		r[0] = ax[0].min().doubleValue();
		r[1] = ax[0].max().doubleValue();
		r[2] = ax[1].min().doubleValue();
		r[3] = ax[1].max().doubleValue();
		return r;
	}
	
	public IDataset getMap(){
		
		if (reMapped == null) updateRemappedData(null);
		
		return reMapped;
	}
	
	private void updateRemappedData(int[] shape) {
		
		IDataset[] axes = MappingUtils.getAxesForDimension(map, 0);
		IDataset y = axes[0];
		IDataset x = axes[1];
		
		double yMax = y.max().doubleValue();
		double yMin = y.min().doubleValue();
		
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		
		if (shape == null) {
			double yStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(y.getSize(),Dataset.INT32),(Dataset)y,1)));
			double xStepMed = (double)Stats.median(Maths.abs(Maths.derivative(DatasetFactory.createRange(x.getSize(),Dataset.INT32),(Dataset)x,1)));
			
			int nBinsY = (int)(((yMax-yMin)/yStepMed));
			int nBinsX = (int)(((xMax-xMin)/xStepMed));
			
			this.shape = shape = new int[]{nBinsX, nBinsY};
		}
		
		
		XYImagePixelCache cache = new XYImagePixelCache((Dataset)x,(Dataset)y,new double[]{xMin,xMax},new double[]{yMin,yMax},shape[0],shape[1]);
		
		List<Dataset> data = PixelIntegration.integrate(map, null, cache);
		
		AxesMetadataImpl axm = new AxesMetadataImpl(2);
		axm.addAxis(0, data.get(2));
		axm.addAxis(1, data.get(0));
		reMapped = data.get(1);
		reMapped.addMetadata(axm);
		lookup = data.get(3);
		
	}
	
	public int[] getShape() {
		return shape;
	}
	
	public void setShape(int[] shape){
		this.shape = shape;
		updateRemappedData(shape);
	}
	
	private int[] getIndices(double x, double y) {

		IDataset[] ax = MappingUtils.getAxesFromMetadata(reMapped);

		IDataset yy = ax[0];
		IDataset xx = ax[1];

		Dataset xd = Maths.subtract(xx, x);
		Dataset yd = Maths.subtract(yy, y);
		
		int xi = Maths.abs(xd).argMin();
		int yi = Maths.abs(yd).argMin();

		return new int[]{yi,xi};
	}
	
	@Override
	public IDataset getSpectrum(double x, double y) {
		int[] indices = getIndices(x, y);
		int index = lookup.getInt(indices);
		if (index == -1) return null;
		return parent.getSpectrum(index);
	}

}
