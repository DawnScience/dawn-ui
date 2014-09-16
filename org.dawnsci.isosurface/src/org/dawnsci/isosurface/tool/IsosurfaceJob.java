package org.dawnsci.isosurface.tool;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
/**
 * 
 * @author nnb55016
 * The Job class for Isovalue visualisation feature
 */
public class IsosurfaceJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
	private       IIsosurfaceTrace    trace;
	private       IsosurfaceTool      tool;
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
		

		final IPlottingSystem system = tool.getSlicingSystem().getPlottingSystem();
		try {
			system.setDefaultCursor(IPlottingSystem.WAIT_CURSOR);
	
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
				
				IDataset points     = new FloatDataset(surface.getPoints(), surface.getPoints().length);
				IDataset textCoords = new FloatDataset(surface.getTexCoords(), surface.getTexCoords().length);
				IDataset faces      = new IntegerDataset(surface.getFaces(), surface.getFaces().length);
				
				if (trace == null) {
					trace = system.createIsosurfaceTrace("isosurface");
					trace.setData(points, textCoords, faces, null);
				    Display.getDefault().syncExec(new Runnable() {
				    	public void run() {
							system.addTrace(trace); // doing this is not thread safe!
				    	}
				    });
				} else {
					trace.setData(points, textCoords, faces, null);
				}
                
			} catch (UnsupportedOperationException e){
				e.printStackTrace();
				showErrorMessage("The number of vertices has exceeded "+generator.getModel().getVertexLimit(), "The surface cannot be rendered. Please increase the box size.");
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
			system.setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
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


}
