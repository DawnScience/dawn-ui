package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * A class for holding surface trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 * @author fcp94556
 *
 */
public class SurfaceTrace {

	private final String          name;
	private AbstractDataset       data;
	private List<AbstractDataset> axes;
	private List<String>          axesNames;
	private HardwarePlotting      plotter;
	private boolean               active;
	private PaletteData           palette;
	
	public SurfaceTrace(HardwarePlotting plotter, String name) {
		this.plotter = plotter;
		this.name    = name;
	}

	/**
	 * This function updates the color mapping with a ColorMappingUpdate object
	 * @param update
	 */
	public void setPalette(PaletteData palette){
		this.palette = palette;
		if (isActive()) {
			ColourImageData imageData = new ColourImageData(256,1);
			int lastValue=0;
			for (int i = 0; i < imageData.getWidth(); i++){
				int value =  ((255&0xff) << 24)+((palette.colors[i].red&0xff) << 16)+((palette.colors[i].green&0xff) << 8)+(palette.colors[i].blue&0xff);
				if(i==252)
					lastValue = value;
				else if(i==253||i==254||i==255)
					imageData.set(lastValue, i);
				else if(i>=0&&i<252)
					imageData.set(value, i);
			}
			plotter.handleColourCast(imageData, data.min().doubleValue(), data.max().doubleValue());
		}
	}
	
	public void setData(final AbstractDataset data, final List<AbstractDataset> axes) throws Exception {
		this.data = data;
		this.axes = axes;
		if (isActive()) {
			plotter.plot(getData(), createAxisValues(), PlottingMode.SURF2D);
			if (palette!=null) setPalette(palette);
		}
	}

	public String getName() {
		return name;
	}

	public List<AbstractDataset> getAxes() {
		return axes;
	}

	public AbstractDataset getData() {
		return data;
	}

	public boolean isActive() {
		return active;
	}

	protected final void setActive(boolean active) {
		this.active = active;
		if (active&&palette!=null) setPalette(palette);	
	}

	protected List<AxisValues> createAxisValues() {
		
		final AxisValues xAxis = new AxisValues(getLabel(0), axes.get(0));
		final AxisValues yAxis = new AxisValues(getLabel(1), axes.get(1));
		final AxisValues zAxis = new AxisValues(getLabel(2), axes.get(2));
		return Arrays.asList(xAxis, yAxis, zAxis);
	}

	private String getLabel(int i) {
		String label = axesNames!=null ? axesNames.get(i) : null;
		if  (label==null) label = (axes!=null && axes.get(i)!=null) ? axes.get(i).getName() : null;
		return label;
	}

	public List<String> getAxesNames() {
		return axesNames;
	}

	public void setAxesNames(List<String> axesNames) {
		this.axesNames = axesNames;
	}

}
