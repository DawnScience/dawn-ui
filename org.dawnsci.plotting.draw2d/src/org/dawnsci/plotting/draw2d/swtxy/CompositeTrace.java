package org.dawnsci.plotting.draw2d.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;

public class CompositeTrace extends Figure implements ICompositeTrace {

	private String  name;
	private String  dataName;
	private Object  userObject;
	private boolean userTrace;
	private IPlottingSystem plottingSystem;
	private Axis    xAxis;
	private Axis    yAxis;
	private double[] axisRange;
//	private double[] yRange;
	
	private List<ImageTrace> traces;

	public CompositeTrace(String traceName, Axis xAxis, Axis yAxis) {
		this.name = traceName;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		traces = new ArrayList<ImageTrace>(7);
	}

	@Override
	public IDataset getData() {
		if (traces == null || traces.isEmpty()) return null;
		return traces.get(traces.size()-1).getData();
	}

	@Override
	public void add(ITrace trace, int index) throws IllegalArgumentException {
//		yAxis.setTicksIndexBased(false);
//		xAxis.setTicksIndexBased(false);
		
		if (!(trace instanceof ImageTrace)) throw new IllegalArgumentException(trace.getClass()+ " are not supported currently!");
		if (trace.getData() == null) throw new IllegalArgumentException("Please ensure that "+trace.getName() + " trace has data! ");
		if (((ImageTrace)trace).getAxes() == null) throw new IllegalArgumentException("Image traces must have axes in composites");
		
		IDataset x = ((ImageTrace)trace).getAxes().get(0);
		IDataset y = ((ImageTrace)trace).getAxes().get(1);
		yAxis.setTitle(y.getName());
		xAxis.setTitle(x.getName());
		double xmin = x.min().doubleValue();
		double xmax = x.max().doubleValue();
		double ymin = y.min().doubleValue();
		double ymax = y.max().doubleValue();
		
		double xLen = x.getSize();
		double yLen = y.getSize();
		
		double xPix = ((xmax-xmin)/(xLen-1))/2;
		double yPix = ((ymax-ymin)/(yLen-1))/2;
		
		xmin -= xPix;
		ymin -= yPix;
		xmax += xPix;
		ymax += yPix;
		
		if (axisRange == null) {
			axisRange = new double[]{xmin,xmax,ymin,ymax};;
		} else {
			if (xmin < axisRange[0]) axisRange[0] = xmin;
			if (ymin < axisRange[2]) axisRange[2] = ymin;
			if (xmax > axisRange[1]) axisRange[1] = xmax;
			if (ymax > axisRange[3]) axisRange[3] = ymax;
		}
		
//		if (xRange == null) {
//			xRange = new double[]{xmin,xmax};
//			yRange = new double[]{ymin,ymax};
//		} else {
//			if (xmin < xRange[0]) xRange[0] = xmin;
//			if (ymin < yRange[0]) yRange[0] = ymin;
//			if (xmax > xRange[1]) xRange[1] = xmax;
//			if (ymax > yRange[1]) yRange[1] = ymax;
//		}
		
		ImageTrace image = (ImageTrace) trace;
		image.setGlobalRange(axisRange);
		if (index>-1) {
		    traces.add(index, image);
		} else {
			traces.add(image);
		}
		
//		if (xAxis instanceof AspectAxis)((AspectAxis)xAxis).setMaximumRange(axisRange[0], axisRange[1]);
//		if (yAxis instanceof AspectAxis)((AspectAxis)yAxis).setMaximumRange(axisRange[2], axisRange[3]);
		
//		for (ImageTrace t : traces) t.setGlobalRanges(xRange, yRange);
		
		add(image);
//		image.setAxes(((ImageTrace)trace).getAxes(), true);
//		image.setImageOrigin(ImageOrigin.TOP_LEFT);
//		if (traces.size()==1) {
//			image.getXAxis().setRange(axisRange[0], axisRange[1]);
//			image.getYAxis().setRange(axisRange[3], axisRange[2]);
//		}
		performAutoscale();
	}
	
	public void removeImageTraces(){
		for (ImageTrace t : traces) t.remove();
	}
	
	public ImageTrace getTopImageTrace() {
		return traces.get(traces.size()-1);
	}

	public void setPalette(String paletteName) {
		for (IImageTrace t : traces) t.setPalette(paletteName);
	}
	
	public void performAutoscale() {
		if (traces == null || traces.isEmpty())  return;
		
		ImageTrace image = traces.get(0);
			image.getXAxis().setRange(axisRange[0], axisRange[1]);
			image.getYAxis().setRange(axisRange[3], axisRange[2]);
		
	}
	
	public void setBounds(final Rectangle clientArea) {
		super.setBounds(clientArea);

		List<Figure> children = getChildren();
		for (Figure child : children) {
			if (child instanceof ITrace) child.setBounds(clientArea);
		}
	}

	@Override
	public void removeImage(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IDataset> getAxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean is3DTrace() {
		return false;
	}

	@Override
	public int getRank() {
		return 2;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public boolean isUserTrace() {
		return userTrace;
	}

	public void setUserTrace(boolean userTrace) {
		this.userTrace = userTrace;
	}

	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

}
