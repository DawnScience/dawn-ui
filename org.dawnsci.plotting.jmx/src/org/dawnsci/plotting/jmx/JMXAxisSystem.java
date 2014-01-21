package org.dawnsci.plotting.jmx;

import java.util.List;

import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.axis.IAxisSystem;
import org.dawnsci.plotting.api.axis.IClickListener;
import org.dawnsci.plotting.api.axis.IPositionListener;

/**
 * Class for calling remove plotting system methods from Jython.
 * 
 * @author fcp94556
 *
 */
class JMXAxisSystem extends JMXSystemObject implements IAxisSystem{


	public JMXAxisSystem(String plotName, String hostName, int port) throws Exception {
		super(plotName, hostName, port);
	}

	@Override
	public IAxis createAxis(String title, boolean isYAxis, int side) {
		return 	(IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), 
		                   new String[]{String.class.getName(), boolean.class.getName(), int.class.getName()},
				           title, isYAxis, side);
	}

	@Override
	public IAxis getSelectedYAxis() {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), yAxis);
	}

	@Override
	public IAxis getSelectedXAxis() {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), xAxis);
	}

	@Override
	public void autoscaleAxes() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}


	@Override
	public IAxis removeAxis(IAxis axis) {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), axis);	
	}  

	@SuppressWarnings("unchecked")
	@Override
	public List<IAxis> getAxes() {
		return (List<IAxis>)call(getMethodName(Thread.currentThread().getStackTrace()));	
	}
	
	@Override
	public IAxis getAxis(String name) {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), name);	
	}


	@Override
	public void addPositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IPositionListener.class}, l);
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IPositionListener.class}, l);
	}


	@Override
	public void addClickListener(IClickListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IClickListener.class}, l);
	}

	@Override
	public void removeClickListener(IClickListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IClickListener.class}, l);
	}

}
