package org.dawnsci.isosurface.tool;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.isosurface.SurfaceDisplayer;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
/**
 * 
 * @author nnb55016
 * The Job class for Isovalue visualisation feature
 */
public class IsosurfaceJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
	
	private       IsosurfaceTool      tool;
    private       SurfaceDisplayer    scene;
	private       ILazyDataset        lazyData;

	public IsosurfaceJob(String name, IsosurfaceTool  tool) {
		
		super(name);
		setUser(false);
		setPriority(Job.INTERACTIVE);
		
		this.tool = tool; 
	}

	/**
	 * Call to update when updating the isovalue or
	 * box size.
	 */
	public void compute() {
		compute(null);
	}
	
	/**
	 * Call to update if lazy data changed.
	 * Regenerates the box size and isoValue.
	 * 
	 * @param slice
	 */
	public void compute(ILazyDataset slice) {
		
		this.lazyData = slice;
		cancel();
		schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		

		try {
			setCursor(Cursor.WAIT);
	
			final IOperation<MarchingCubesModel, Surface> generator = tool.getGenerator();
			if (lazyData!=null) {
				
				final MarchingCubesModel model = generator.getModel();
				model.setLazyData(lazyData); // We want to do this task from the thread
			                                 // because it can take a while too
	
			    Display.getDefault().syncExec(new Runnable() {
			    	public void run() {
			    		// We set the estimated values for the slicing which will
			    		// have changed if the lazyData has.
	                    tool.updateUI();
			    	}
			    });
			}
			
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			
			try {
				Surface surface    = generator.execute(null, new ProgressMonitorWrapper(monitor));
				drawSurface(surface);
				
			} catch (UnsupportedOperationException e){
				e.printStackTrace();
				showErrorMessage("The number of vertices has exceeded 1,000,000.", "The surface cannot be rendered. Please increase the box size.");
				return Status.CANCEL_STATUS;
				
			} catch (Exception e) {
				logger.error("Cannot run algorithm "+generator.getClass().getSimpleName(), e);
				return Status.CANCEL_STATUS;
				
			} catch (OutOfMemoryError e){
				e.printStackTrace();
				showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
				return Status.CANCEL_STATUS;
			}
					
			
			return Status.OK_STATUS;
			
		} finally {
			setCursor(Cursor.DEFAULT);
		}
	}

	private void showErrorMessage(final String title, final String message) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	private void drawSurface(final Surface surface) {
		
		Platform.runLater(new Runnable() {			
			public void run() {			
				try {

					if (scene==null){
						Group    root   = new Group();
						MeshView result = new MeshView(surface.createTrangleMesh());
						result.setCursor(Cursor.CROSSHAIR);
						scene = new SurfaceDisplayer(root, result);
						
						final FXCanvas canvas = tool.getCanvas();
						canvas.setScene(scene);
						
					} else {
						scene.updateTransforms();
						TriangleMesh mesh = (TriangleMesh)scene.getIsosurface().getMesh();
						surface.marry(mesh);
						
						final FXCanvas canvas = tool.getCanvas();
						canvas.redraw();
					}			
					
				} catch (OutOfMemoryError e){
					e.printStackTrace();
					showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
				}
			}
		});		
	}

	private void setCursor(final Cursor cursor) {
		if (scene!=null) Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		        // Do some stuff
		         scene.setCursor(cursor);
		    }
		});		
	}


}
