package org.dawnsci.plotting.draw2d.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
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
	
	private List<ImageTrace> traces;

	public CompositeTrace(String traceName, Axis xAxis, Axis yAxis) {
		this.name = traceName;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		traces = new ArrayList<ImageTrace>(7);
	}

	@Override
	public IDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(ITrace trace, int index) throws IllegalArgumentException {

		if (!(trace instanceof ImageTrace)) throw new IllegalArgumentException(trace.getClass()+ " are not supported currently!");
		if (trace.getData() == null) throw new IllegalArgumentException("Please ensure that "+trace.getName() + " trace has data! ");

		ImageTrace image = (ImageTrace) trace;
		if (index>-1) {
		    traces.add(index, image);
		} else {
			traces.add(image);
		}
		add(image);
		
		if (traces.size()==1) image.performAutoscale();
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
