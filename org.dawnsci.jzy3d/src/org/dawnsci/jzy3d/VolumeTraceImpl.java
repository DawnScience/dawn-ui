package org.dawnsci.jzy3d;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.jzy3d.volume.Texture3D;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.jzy3d.colors.ColorMapper;
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
	
	private int downsampling = 1;
	
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
		
		bean.setMin(min);
		bean.setMax(max);

		setPaletteData(getPaletteData());
		
		this.data = data;
		int z = data.getShape()[0];
		int y = data.getShape()[1];
		int x = data.getShape()[2];
		
		FloatDataset v = DatasetUtils.cast(FloatDataset.class, data);
		
		if (axes != null) {
			xAxis = axes[0];
			yAxis = axes[1];
			zAxis = axes[2];
		}
		
		float xmin = 0;
		float xmax = x-1;
		float ymin = 0;
		float ymax = y-1;
		float zmin = 0;
		float zmax = z-1;
		
		if (xAxis != null) {
			xmin = xAxis.min(true).floatValue();
			xmax = xAxis.max(true).floatValue();
		}
		
		if (yAxis != null) {
			ymin = yAxis.min(true).floatValue();
			ymax = yAxis.max(true).floatValue();
		}
		
		if (zAxis != null) {
			zmin = zAxis.min(true).floatValue();
			zmax = zAxis.max(true).floatValue();
		}

		
		if (colorMapper != null) {
			colorMapper.setMin(min.floatValue());
			colorMapper.setMax(max.floatValue());
		}
		
		int[] shape = data.getShape();
		float[] dataf = v.getData();
		
		ByteBuffer buffer = GLBuffers.newDirectByteBuffer(dataf.length*4);
		
		for (int i = 0; i < dataf.length; i++) {
			buffer.putFloat(dataf[i]);
		}
		
		volume = new Texture3D(buffer, shape,min.floatValue(),max.floatValue(),colorMapper,new BoundingBox3d(xmin,xmax,ymin,ymax,zmin,zmax));
		((Texture3D)volume).setDownsampling(downsampling);
	}

	@Override
	protected void setColorMap(ColorMapper mapper) {
		if (volume != null) {
			((Texture3D)volume).setMin(mapper.getMin());
			((Texture3D)volume).setMax(mapper.getMax());
			((Texture3D)volume).setColorMapper(mapper);
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

	@Override
	public void setDownsampling(int downsample) {
		downsampling = downsample;
		
	}


}
