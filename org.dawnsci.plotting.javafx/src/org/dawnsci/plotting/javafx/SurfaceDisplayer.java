/*
 * Copyright ( 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx;

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.TransformChangedEvent;
import javafx.scene.transform.Translate;

import org.dawnsci.plotting.javafx.axis.objects.BoundingBox;
import org.dawnsci.plotting.javafx.axis.objects.ScaleAxisGroup;
import org.dawnsci.plotting.javafx.axis.objects.SceneObjectGroup;
import org.dawnsci.plotting.javafx.axis.objects.Vector3DUtil;
import org.dawnsci.plotting.javafx.trace.FXIsosurfaceTrace;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * 
 * @author nnb55016 The following class creates the scene where the surface is
 *         visualised It is used when running the application inside DAWN
 *
 * 
 * @author Joel Ogden
 * 
 */

public class SurfaceDisplayer extends Scene
{		
	// camera for the scene
	private PerspectiveCamera camera;
	
	// the groups for the scene
	private Group isosurfaceGroup;	// holds the isosurfaces
	private Group cameraGroup; 		// holds the camera translation data
	
	private Group axisNode; 		// holds the axisGroup -> allows the axisGroup to be null without an exception
	private SceneObjectGroup axisObjectGroup;	// hold the axisGroup
	private Group objectGroup;		// holds the objects for the scene
	private Group lightGroup;		// holds the lights for the scene
	private ScaleAxisGroup scaleAxesGroup;
	
	// Scene and camera variables
	private Translate sceneOffset;
	private Translate isoGroupOffset;
	private Scale scale = new Scale();
	private Rotate alignedXRotate = new Rotate();
	{alignedXRotate.setAxis(new Point3D(1,0,0));};
	private Rotate alignedYRotate = new Rotate();
	{alignedYRotate.setAxis(new Point3D(0,1,0));};
	private double zoom = 100;
	
	// mouse variables
	private boolean mousePositionSet = false;
	private double[] oldMousePos = new double[2];
	private double[] newMousePos = new double[2];
	
	// Axis variables
	private Point3D axesMaxLengths;	
	private EventHandler<MouseEvent> scaleEvent = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent me)
		{
			
			Object obj = me.getSource();
			final Point3D scaleDir = Vector3DUtil.exclusiveTransforms(
								((Cylinder)obj).getTransforms(),
								new Point3D(0, -1, 0),
								Rotate.class);
			
			final Point3D actualDir = new Point3D(0, 1, 0);
						
			Transform localToSceneTransforms = ((Cylinder)obj).getLocalToSceneTransform();
			Point3D screenPoint = localToSceneTransforms.transform(actualDir);			
			Point3D screenPoint2 = localToSceneTransforms.transform(actualDir.multiply(2));
			
			Point3D sceneMouseOffset = screenPoint.subtract(screenPoint2);
			// sceneMouseOffset = new Point3D(sceneMouseOffset.getX(), -sceneMouseOffset.getY(), sceneMouseOffset.getZ());
						
			// mouseState = MOUSE_SCALE;
			
			// set old values
			oldMousePos[0] = newMousePos[0];
			oldMousePos[1] = newMousePos[1];
			// find new values of mouse pos
			newMousePos[0] = me.getSceneX();
			newMousePos[1] = me.getSceneY();
			
			
			Point2D  mouseScaleDir = new Point2D(sceneMouseOffset.getX(), sceneMouseOffset.getY());
			
			final double[] mouseDelta = {
					newMousePos[0] - oldMousePos[0], 
					newMousePos[1] - oldMousePos[1]};
			
			final double mouseMovementMod = ((zoom + 1000) * 0.001f) + 0.1f;
			
			updateScale(mouseDelta, mouseMovementMod, mouseScaleDir, scaleDir);
			
		}
	};
		
	/**
	 * 
	 * @param root - the root node for the scene isosurfaceGroup
	 * @param isosurfaceGroup - the node holding the surface objects
	 */
	public SurfaceDisplayer(Group root, Group isosurfaceGroup)
	{
		// create the scene
		super(root, 1500, 1500, true);
		this.isosurfaceGroup = isosurfaceGroup;
		
		// set the camera -> the camera will handle some aspects of movement
		// other are within the group -> this is done to simplify rotation
		// calculations
		this.camera = new PerspectiveCamera();
		setCamera(camera); 
				
		initialiseCamera();
		initlialiseGroups();
		createScaleAxisGroup();
		createAxisGroup();
		createSceneGraph(root);
		setDepthBuffers();
		initialiseTransforms();
		addLights();
		
		// add the listeners for scene camera movement
		addListeners();
		
	}
	/*
	 * private
	 */
	
	// could be potentially redundant
	private void initialiseCamera()
	{
		// add the initial transforms
		camera.getTransforms().addAll(new Translate(0, 0, -zoom));
		camera.setNearClip(0.00001f);
		camera.setFarClip(100_000);
		
	}
	
	private void initlialiseGroups()
	{	
		// initialise/create the groups
		this.cameraGroup = new Group();
		this.axisNode = new Group();
		this.objectGroup = new Group();
		this.lightGroup = new Group();
	}
	
	// combine the groups into the scene graph root node
	private void createSceneGraph(Group root)
	{
		
		// create the scene graph
		this.lightGroup.getChildren().addAll(this.isosurfaceGroup);
		this.objectGroup.getChildren().addAll(axisNode, this.lightGroup);
		this.cameraGroup.getChildren().addAll(this.objectGroup);
		
		// add groups the the root
		root.getChildren().addAll(cameraGroup);
		
	}

	private void createScaleAxisGroup()
	{
		this.scaleAxesGroup = new ScaleAxisGroup(new Point3D(50, 50, 50), 5);
	}
	
	private void createAxisGroup()
	{		
		// create and return the new axis
		SceneObjectGroup newAxisObjectGroup =  new SceneObjectGroup(scaleEvent);
		
		scaleAxesGroup.setAxisEventListener(scaleEvent); //!! look into re-organising
		
		this.axisObjectGroup = newAxisObjectGroup;
		this.axisNode.getChildren().add(this.axisObjectGroup);
		
	}
	
	private void setDepthBuffers()
	{
		// disable the depth buffer for the isosurfaces -> depth buffer doesn't behave with transparency
		// enable for the axis node group
		this.isosurfaceGroup.setDepthTest(DepthTest.ENABLE);
		this.axisNode.setDepthTest(DepthTest.ENABLE);
	
	}
	
	private void initialiseTransforms()
	{
		// initialise
		this.sceneOffset = new Translate();
		this.isoGroupOffset = new Translate();

		// add to groups
		this.cameraGroup.getTransforms().addAll(sceneOffset);
		this.cameraGroup.getTransforms().addAll(alignedXRotate, alignedYRotate);

		this.scaleAxesGroup.getTransforms().addAll();
		
		this.objectGroup.getTransforms().addAll(isoGroupOffset);
		
		this.isosurfaceGroup.getTransforms().addAll(scale);
		
		// update transforms
		updateTransforms();
	}
	
	private void addLights()
	{
		// create lights for the iso surface
		AmbientLight ambientSurfaceLight = new AmbientLight(new Color(0.3, 0.3, 0.3, 1));
//		ambientSurfaceLight.getScope().add(lightGroup);
		
		PointLight pointLight = new PointLight(new Color(1, 1, 1, 1));	
		pointLight.getScope().add(lightGroup);
		
		this.lightGroup.getChildren().addAll(ambientSurfaceLight, pointLight);
	}
	
	// add the listeners
	// !! re-organise
	private void addListeners()
	{
		/*
		 * scene Mouse listeners
		 */
		
		// on click, reset mouse position info - ie reset delta
		setOnMousePressed(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent me)
			{
				oldMousePos[0] = (float) me.getSceneX();
				oldMousePos[1] = (float) me.getSceneY();
				
				newMousePos[0] = (float) me.getSceneX();
				newMousePos[1] = (float) me.getSceneY();
				
				// linux doesn't always call the events in the expected order
				mousePositionSet = true;
			}
		});
		
		setOnMouseReleased(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent me)
			{
				
				oldMousePos[0] = newMousePos[0];
				oldMousePos[1] = newMousePos[1];
				
				mousePositionSet = false;
			}
		});
		
		// on mouse drag change the camera state
		setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent me)
			{
				if (mousePositionSet)
				{
					// set old values
					oldMousePos[0] = newMousePos[0];
					oldMousePos[1] = newMousePos[1];
					// find new values of mouse pos
					newMousePos[0] = me.getSceneX();
					newMousePos[1] = me.getSceneY();
					
					// find offset from last tick - ie delta
					final double[] mouseDelta = {
							newMousePos[0] - oldMousePos[0], 
							newMousePos[1] - oldMousePos[1]};
					
					final double mouseMovementMod = ((zoom + 1000) * 0.001f) + 0.1f;
					
					// check if left button is pressed
					// rotate if true - ie, rotate on left button drag
					if (me.isPrimaryButtonDown() && !me.isSecondaryButtonDown())
					{
						alignedXRotate.setAngle(alignedXRotate.getAngle() + mouseDelta[1] * 0.65f);
						alignedYRotate.setAngle(alignedYRotate.getAngle() - mouseDelta[0] * 0.65f);
					}
					
					// zoom
					if (me.isMiddleButtonDown())
					{
						Point3D XYTranslationDirection = alignedXRotate.transform(mouseDelta[0]*mouseMovementMod, mouseDelta[1]*mouseMovementMod,0);
						XYTranslationDirection = alignedYRotate.transform(XYTranslationDirection);
												
						
						isoGroupOffset.setX(isoGroupOffset.getX() + XYTranslationDirection.getX() );
						isoGroupOffset.setY(isoGroupOffset.getY() + XYTranslationDirection.getY() );
						isoGroupOffset.setZ(isoGroupOffset.getZ() - XYTranslationDirection.getZ() );
					}
					
					// zoom if right button is pressed
					if (me.isSecondaryButtonDown() && me.isPrimaryButtonDown())
					{
						zoom += (-mouseDelta[1] * mouseMovementMod);
						
					}
					camera.getTransforms().setAll(new Translate(0, 0, -zoom));
					// camera.getTransforms().clear();
				}
			}
		});
		
		// on mouse scroll zoom the camera
		setOnScroll(new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle(ScrollEvent event)
			{
				
				zoom += event.getDeltaY() * 0.2f;
				camera.getTransforms().setAll(new Translate(0, 0, -zoom));
			}
		});
		
		/*
		 * scene resize listeners
		 */
		
		// on resize reset the camera scene offsets
		InvalidationListener listener = (new InvalidationListener()
		{
			@Override
			public void invalidated(Observable arg0)
			{
				updateSceneTransforms();
			}
		});
		
		this.widthProperty().addListener(listener);
		this.heightProperty().addListener(listener);
		
		/*
		 * misc listeners -> transform changes etc.
		 */
		
		// on transform change
		EventHandler<TransformChangedEvent> tranformChange = new EventHandler<TransformChangedEvent>()
		{
			@Override
			public void handle(TransformChangedEvent arg0)
			{
				// !! do nothing for now -> might be used to orientate text
			}
		};
		
		alignedXRotate.setOnTransformChanged(tranformChange);
		alignedYRotate.setOnTransformChanged(tranformChange);
		
		// on scale change
		EventHandler<TransformChangedEvent> scaleChanged = new EventHandler<TransformChangedEvent>()
		{
			@Override
			public void handle(TransformChangedEvent arg0)
			{
				updateAxisSize(axesMaxLengths);
			}
		};
		
		scale.setOnTransformChanged(scaleChanged);
		
	}
	
	/*
	 * non initialisers
	 */
	
	private void updateAxisSize(Point3D maxLength) 
	{
		axisObjectGroup.checkScale(this.scale.transform(maxLength), this.zoom);
	}
	
	private void updateScale(double[] mouseDelta, double mouseMovementMod, Point2D mouseScaleDir, Point3D scaleDir) 
	{
		Point3D mouseDelta3D = new Point3D(mouseDelta[0], mouseDelta[1], 0);
		
		Point3D mouseScalarDir3D = new Point3D(mouseScaleDir.getX(), mouseScaleDir.getY(), 0);
		
		double scalar = Vector3DUtil.getScaleAcrossProjectedVector(mouseScalarDir3D, mouseDelta3D);
									
		scaleDir.normalize();
		scale.setX(scale.getX() + (scalar * (0.65f * (0.005 * scaleDir.getX()))));
		if (scale.getX() < 0 )
		{
			scale.setX(0);
		}
		
		scale.setY(scale.getY() + (scalar * (0.65f * (0.005 * scaleDir.getY()))));
		if (scale.getY() < 0 )
		{
			scale.setY(0);
		}
		
		scale.setZ(scale.getZ() + (scalar * (0.65f * (0.005 * scaleDir.getZ()))));
		if (scale.getZ() < 0 )
		{
			scale.setZ(0);
		}
	}
	
	private double calculateTickSeperation(IDataset dataSet)
	{
		double offset = dataSet.getDouble(0);
		if (dataSet.getSize() > 0)
		{
			offset = dataSet.getDouble(1) - dataSet.getDouble(0);
			for (int i = 1; i < dataSet.getSize(); i++)
			{
				if ((dataSet.getDouble(i) - dataSet.getDouble(i-1)) != offset)
				{
					offset = (dataSet.getDouble(i) - dataSet.getDouble(i-1));
					System.err.println("Axis Ticks inconsistant");
				}
			}
		}
		return offset;
	}
	
	private void updateSceneTransforms()
	{
		this.sceneOffset.setX(this.getWidth() / 2);
		this.sceneOffset.setY(this.getHeight()/ 2);
		this.sceneOffset.setZ(0);
	}
	
	
	/*
	 * public 
	 */
	
	public void dispose()
	{
		
		this.setOnMousePressed(null);
		this.setOnMouseDragged(null);
		this.setOnScroll(null);
		
		if (isosurfaceGroup != null)
			isosurfaceGroup.setOnMouseMoved(null);
		
	}
	
	public Group getIsosurfaceGroup()
	{
		return isosurfaceGroup;
	}
	
	public void addTrace(FXIsosurfaceTrace trace)
	{
		// if the first trace create the axes using the trace.axes data
		// all of this data is irrelevant as it get reset when a surface is added
		// this is extremely inefficient but works well enough I don't plan to fix yet
		if (axisObjectGroup.getChildren().size() <= 0)
		{
			Point3D maxLengths = new Point3D(
					trace.getAxes().get(0).getFloat(0),
					trace.getAxes().get(0).getFloat(1),
					trace.getAxes().get(0).getFloat(2));
			
			Point3D seperationValue = new Point3D(
											calculateTickSeperation(trace.getAxes().get(1)), 
											calculateTickSeperation(trace.getAxes().get(2)), 
											calculateTickSeperation(trace.getAxes().get(3)));
						
			double maxSeperation = Vector3DUtil.getMaximumValue(seperationValue);
			Point3D tickSeperationXYZ = new Point3D(maxSeperation, maxSeperation, maxSeperation);
			
			axisObjectGroup.createAxes(
					maxLengths,
					maxSeperation*0.08f, 
					tickSeperationXYZ);
			
			this.axisObjectGroup.createBoundingBox(maxLengths);
		}
		
		// add the mesh the the scene graph
		this.isosurfaceGroup.getChildren().add(trace.getIsoSurface());
	}
	
	public void removeSurface(Node removeNode)
	{
		isosurfaceGroup.getChildren().remove(removeNode);
	}
	
	
	public void updateTransforms()
	{
		updateSceneTransforms();
		
		final Bounds isoGroupOffsetBounds = this.objectGroup.getBoundsInLocal();
		
		final Translate offsetInverse = new Translate(
							isoGroupOffsetBounds.getMinX() + (isoGroupOffsetBounds.getWidth() / 2),                     
							isoGroupOffsetBounds.getMinY() + (isoGroupOffsetBounds.getHeight() / 2), 
							isoGroupOffsetBounds.getMinZ() + (isoGroupOffsetBounds.getDepth() / 2)
							).createInverse(); 
		
		isoGroupOffset.setX(offsetInverse.getX());
		isoGroupOffset.setY(offsetInverse.getY());
		isoGroupOffset.setZ(offsetInverse.getZ());
		
	}
	
	
	
	public void setAxesData(List<IDataset> axesData)
	{
		// set the initial data size
		this.axesMaxLengths = new Point3D(
									axesData.get(0).getFloat(0),
									axesData.get(0).getFloat(1),
									axesData.get(0).getFloat(2));
		
		Point3D seperationValue = new Point3D(
										calculateTickSeperation(axesData.get(1)), 
										calculateTickSeperation(axesData.get(2)), 
										calculateTickSeperation(axesData.get(3)));
		
		double maxSeperation = Vector3DUtil.getMaximumValue(seperationValue);
		
		axisObjectGroup.SetTickSeparationXYZ(new Point3D(maxSeperation, maxSeperation, maxSeperation));		
		
		this.axisObjectGroup.setAxisLimitMax(axesMaxLengths);
		updateAxisSize(this.axesMaxLengths);
		
	}

	public void flipAxisGridVisibility()
	{
		axisObjectGroup.flipXGridVisible();
		axisObjectGroup.flipYGridVisible();
		axisObjectGroup.flipZGridVisible();
	}

	public void flipBoundingBoxVisibility() 
	{
		axisObjectGroup.flipBoundingBoxVisibility();
	}
	
	public void flipScaleAxesVisibility()
	{
		scaleAxesGroup.flipVisibility();
	}
	
	
	
	
	
	
}
