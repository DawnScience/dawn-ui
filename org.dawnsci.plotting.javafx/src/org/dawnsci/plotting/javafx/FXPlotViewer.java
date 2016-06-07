/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;

import org.dawnsci.plotting.javafx.axis.objects.JavaFXProperties;
import org.dawnsci.plotting.javafx.trace.JavafxTrace;
import org.dawnsci.plotting.javafx.trace.isosurface.IsosurfaceTrace;
import org.dawnsci.plotting.javafx.trace.plane.PlaneTrace;
import org.dawnsci.plotting.javafx.trace.volume.VolumeTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPlane3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

/**
 * TODO The implementation of this plotter viewer is not complete.
 * 
 * @author Matthew Gerring
 *
 */

/**
 * @author uij85458
 *  // added comments
 * viewer
 * 		include the scene
 * 		holds a trace list
 * 		draws trace list within scene root
 * 
 */
public class FXPlotViewer extends IPlottingSystemViewer.Stub<Composite>
{
	// the scene class -> uses an inherited class to hold the camera translation data
	private SceneDisplayer scene;
	// root node
	private Group root;
	
	// the canvas for drawing -> not sure if this is needed but will keep it for now
	private FXCanvas canvas;
	// not sure what this does currently
	private FXPlotActions plotActions;

	/**
	 * Must have no-argument constructor.
	 */
	public FXPlotViewer() {

	}

	/**
	 * Call to create plotting
	 * 
	 * @param parent
	 * @param initialMode
	 *            may be null
	 */
	public void createControl(final Composite parent)
	{
		// DO NOT REMOVE
		Platform.setImplicitExit(false);
		
		// declare the canvas in memory
		this.canvas = new FXCanvas(parent, SWT.NONE);
		
		// create the root node
		this.root = new Group();
		
		// create the scene -> most of the changes will be done within here
		scene = new SceneDisplayer(root);
		
		// set the scene to the canvas
		this.canvas.setScene(scene);
		
		this.plotActions = new FXPlotActions(this, system);
		this.plotActions.createActions();
		this.system.getActionBars().getToolBarManager().update(true);
		this.system.getActionBars().updateActionBars();

	}

	/**
	 * @param cursorFlag
	 */
	public void setDefaultCursor(final int cursorFlag)	{
		// change the cursor -> does isFxApplicationThread make it thread safe??
		// potentially redundant but makes the code easier to read which is nice!!
		if (Platform.isFxApplicationThread())
		{
			setCursor(cursorFlag);
		}
		else
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					setCursor(cursorFlag);
				}
			});
		}
	}

	// declare the cursor depending on the flag
	private void setCursor(int cursorFlag)
	{
		if (canvas.getScene() == null)
			return;
		
		Cursor cursor = Cursor.DEFAULT;
		if (cursorFlag == IPlottingSystem.CROSS_CURSOR)
			cursor = Cursor.CROSSHAIR;
		if (cursorFlag == IPlottingSystem.WAIT_CURSOR)
			cursor = Cursor.WAIT;
		canvas.getScene().setCursor(cursor);
	}

	public void saveScreenShotOfSceneToFile()
	{
		String fileURL = null;
		
		FileDialog dialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);

		String [] filterExtensions = new String [] {".png"};
		
		dialog.setFilterPath(File.listRoots()[0].getAbsolutePath());
		dialog.setFilterNames(new String[]{".png"});
		dialog.setFilterExtensions (filterExtensions);
		
		// will = null if cancelled
		fileURL = dialog.open();
				
		WritableImage wi = scene.snapshot(null);
		BufferedImage rawImage;
		rawImage = SwingFXUtils.fromFXImage(wi, null);
		
		
		if (fileURL != null)
		{
			try {
				ImageIO.write(rawImage, "png", new File(fileURL));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ITrace createTrace(String name, Class<? extends ITrace> clazz)
	{
		if (name == null || "".equals(name))
			throw new RuntimeException("Cannot create trace with no name!");

		if (IIsosurfaceTrace.class.isAssignableFrom(clazz)) {
			return new IsosurfaceTrace(this, scene, name);
		} else if (IVolumeRenderTrace.class.isAssignableFrom(clazz)) {
			return new VolumeTrace(this, scene, name);
		} else if (IPlane3DTrace.class.isAssignableFrom(clazz)) {
			return new PlaneTrace(this, scene, name);
		} else {
			throw new RuntimeException("Trace type not supported " + clazz.getSimpleName());
		}
	}

	@Override
	public boolean addTrace(ITrace trace)
	{
		if (trace instanceof JavafxTrace)
		{
			// declare the trace from the parameter trace
			JavafxTrace javafxTrace = (JavafxTrace) trace;
			
			// add the trace into the list of current traces
			scene.addTrace(javafxTrace);
			if (javafxTrace.getAxes() != null && javafxTrace.getAxes().size() == 3)
			scene.setAxesData(javafxTrace.getAxes());
		}
		
		else
		{
			throw new RuntimeException("Trace type not supported " + trace.getClass().getSimpleName());
		}
		return true;
		
	}

	public void setScaleAxesVisibility(boolean visibility) {
		scene.setScaleAxesVisibility(visibility);
	}

	public void setAxisGridVisibility(boolean visibility) {
		scene.setAxisGridVisibility(visibility);
	}

	public void setBoundingBoxVisibility(boolean visibility) {
		scene.setBoundingBoxVisibility(visibility);
	}

	/**
	 * @param parallel if true set camera to parallel otherwise set it to perspective
	 */
	public void toggleParallelCamera(boolean parallel)
	{
		if (parallel) {
			scene.setCameraType(JavaFXProperties.CameraProperties.PARALLEL_CAMERA);
		} else {
			scene.setCameraType(JavaFXProperties.CameraProperties.PERSPECTIVE_CAMERA);
		}
	}

	public void resetSceneTransforms() {
		scene.resetSceneTransforms();
	}

	@Override
	public boolean isPlotTypeSupported(PlotType type)
	{
		switch (type) {
		case ISOSURFACE:
			return true;
		case VOLUME:
			return true;
		case PLANE3D:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace)
	{
		if (IIsosurfaceTrace.class.isAssignableFrom(trace)) {
			return true;
		} else if (IVolumeRenderTrace.class.isAssignableFrom(trace)) {
			return true;
		} else if (IPlane3DTrace.class.isAssignableFrom(trace)) {
			return true;
		}

		return false;
	}

	@Override
	public Composite getControl() {
		return canvas;
	}
}
