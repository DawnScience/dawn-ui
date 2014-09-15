package org.dawnsci.plotting.javafx;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Cursor;

import org.dawnsci.plotting.javafx.trace.FXIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * TODO The implementation of this plotter viewer is not complete.
 * 
 * @author fcp94556
 *
 */
public class FXPlotViewer extends IPlottingSystemViewer.Stub {

	private FXCanvas canvas;
	private ITrace   currentTrace;
	
	/**
	 * Call to create plotting
	 * @param parent
	 * @param initialMode may be null
	 */
	public void createControl(final Composite parent) {
        this.canvas = new FXCanvas(parent, SWT.NONE);
	} 
	
	public void updatePlottingRole(PlotType type) {
		// TODO
	}

	public Composite getControl() {
		return canvas;
	}

	/**
	 * Thread safe
	 */
	public void setDefaultCursor(final int cursorFlag) {
		
		if (Platform.isFxApplicationThread()) {
			setCursor(cursorFlag);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setCursor(cursorFlag);
				}
			});
		}
	}
	
	private void setCursor(int cursorFlag) {
		if (canvas.getScene()==null) return;
		
		Cursor cursor = Cursor.DEFAULT;
		if (cursorFlag==IPlottingSystem.CROSS_CURSOR) cursor = Cursor.CROSSHAIR;
		if (cursorFlag==IPlottingSystem.WAIT_CURSOR)  cursor = Cursor.WAIT;
		canvas.getScene().setCursor(cursor);
	}

	public ITrace createTrace(String name, Class<? extends ITrace> clazz) {
		if (IIsosurfaceTrace.class.isAssignableFrom(clazz)) {
			if (name==null || "".equals(name)) throw new RuntimeException("Cannot create trace with no name!");
			return new FXIsosurfaceTrace(this, canvas, name);
		} else {
		    throw new RuntimeException("Trace type not supported "+clazz.getSimpleName());
		}

	}
	
	public boolean addTrace(ITrace trace) {
		
		currentTrace = trace;
		if (trace instanceof IIsosurfaceTrace) {
			FXIsosurfaceTrace itrace = (FXIsosurfaceTrace)trace;
			if (itrace.getData()==null) throw new RuntimeException("Trace has no data "+trace.getName());
			itrace.create();
			
		} else {
		    throw new RuntimeException("Trace type not supported "+trace.getClass().getSimpleName());
		}
		return true;

	}

	/**
	 * 
	 * @param type
	 * @return true if this viewer deals with this plot type.
	 */
	public boolean isPlotTypeSupported(PlotType type){
		switch(type) {
		case ISOSURFACE:
		    return true;
		default:
			return false;
		}
	}
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		if (IIsosurfaceTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
}
