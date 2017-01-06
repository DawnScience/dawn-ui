package org.dawnsci.plotting.tools.powderlines;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PowderLineTool extends AbstractToolPage {

	private Composite composite; // root Composite of the tool
	private TableViewer lineTableViewer; // TableViewer holding the list of lines
	private ITraceListener tracerListener; // The trace on which the tool listens
	boolean isCoordinateAngle = false; // default to momentum for the independent coordinate
	
	public PowderLineTool() {
		try{
			this.tracerListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent event) {
					if (!(event.getSource() instanceof List<?>)) {
						return;
					}
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void createControl(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		
		// Create the table of lines
		// Create the Actions
		
		activate();
		
		super.createControl(parent);
	}
	
	@Override
	public void setFocus() {
		// set the viewer focus
	}
	
	/**
	 * Activate the tool.
	 */
	public void activate() {
		
		// Add the traceListener
		if (getPlottingSystem() != null) {
			getPlottingSystem().addTraceListener(tracerListener);
		}
		
		super.activate();
	}
	
	/**
	 * Deactivate the tool.
	 */
	public void deactivate() {
		super.deactivate();
		
		// Remove the traceListener
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(tracerListener);
		}
	}
}
