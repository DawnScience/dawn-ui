package org.dawnsci.jzy3d;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapGrayscale;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;

import com.jogamp.opengl.util.GLBuffers;

public class VolumeTraceImpl extends Abstract2DJZY3DTrace implements IVolumeTrace {

	private AbstractDrawable volume;
	private IDataset data;
	private IDataset xAxis;
	private IDataset yAxis;
	private IDataset zAxis;
	
	protected ColorMapper colorMapper;
	
	
	public VolumeTraceImpl(IPaletteService paletteService, IImageService imageService, String palette) {
		super(paletteService, imageService, palette);
	}

	@Override
	public IDataset getData() {
		return data;
	}

	@Override
	public List<IDataset> getAxes() {
		return Arrays.asList(new IDataset[] {xAxis,yAxis,zAxis});
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setData(IDataset data, IDataset[] axes, Number min, Number max) {
		if (getImageServiceBean() == null) {
			bean = imageService.createBeanFromPreferences();
		}
		
		bean.setImage(data);
		
//		double[] fs = imageService.getFastStatistics(bean);
		
		bean.setMin(min);
		bean.setMax(max);
		
		float maxf = bean.getMax().floatValue();
		float minf = bean.getMin().floatValue();

		setPaletteData(getPaletteData());
		
		this.data = data;
		int x = data.getShape()[0];
		int y = data.getShape()[1];
		int z = data.getShape()[2];
		
		FloatDataset v = DatasetUtils.cast(FloatDataset.class, data);
		float[] xArray = null;
		float[] yArray = null;
		float[] zArray = null;
		
		xAxis = axes[0];
		yAxis = axes[1];
		
		if (axes[0] != null && axes[0].getSize() != data.getShape()[1]) {
			axes[0] = null;
		}
//		
//		if (axes[1] != null && axes[1].getSize() != data.getShape()[0]) {
//			axes[1] = null;
//		}
//
//		xArray = (axes[0] != null) ? DatasetUtils.cast(FloatDataset.class, axes[0]).getData() : getRange(x);
//		yArray = (axes[1] != null) ? DatasetUtils.cast(FloatDataset.class, axes[1]).getData() : getRange(y);
//		float[] zArray = z.getData();
//
//		
		if (colorMapper != null) {
			colorMapper.setMin(min.floatValue());
			colorMapper.setMax(max.floatValue());
		}
		
		int[] shape = data.getShape();
		float[] dataf = v.getData();
		
//		shape = new int[] {3,3,3};
		
//		dataf = new float[] {1,0,2,0,3,0,4,0,5,0,0,0,0,6,0,0,0,0,7,0,8,0,9,0,10,0,11};
//		dataf = new float[] {1,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,1};
		
//		max = 11;
//		min = 1;
//		
//		colorMapper.setMax(11);
//		colorMapper.setMin(0);
//		
		ByteBuffer buffer = GLBuffers.newDirectByteBuffer(dataf.length*4);
//		
		for (int i = 0; i < dataf.length; i++) {
			buffer.putFloat(dataf[i]);
		}
		
//		float diff = max.floatValue() - min.floatValue();
		
		volume = new Texture3D(buffer, shape,min.floatValue(),max.floatValue(),colorMapper,new BoundingBox3d(0.f,x,0.f,y,0.f,z));
		
	}

	@Override
	protected void setColorMap(ColorMapper mapper) {
		if (volume != null) {
			((Texture3D)volume).setMin(mapper.getMin());
			((Texture3D)volume).setMax(mapper.getMax());
			((Texture3D)volume).setMapper(mapper);
		}
		colorMapper = mapper;
	}
	
	public AbstractDrawable getShape(){
		return volume;
	}

	@Override
	protected AbstractDrawable buildShape(float[] x, float[] y, float[] z, ColorMapper mapper) {
		// nothing here
		return null;
	}

	@Override
	protected void configureShape(Shape shape) {
		// nothing here
		
	}


}
