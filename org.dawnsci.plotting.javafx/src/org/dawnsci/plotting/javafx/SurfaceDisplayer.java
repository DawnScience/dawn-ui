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

import javafx.animation.AnimationTimer;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.TransformChangedEvent;
import javafx.scene.transform.Translate;

import org.dawnsci.plotting.javafx.axis.objects.AxisGroup;
import org.dawnsci.plotting.javafx.axis.objects.ScaleAxisGroup;
import org.dawnsci.plotting.javafx.axis.objects.Vector3DUtil;
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
	// finals
	private final int MOUSE_CAM_ROTATE = 0;
	private final int MOUSE_SCALE = 1;
	
	// camera for the scene
	private PerspectiveCamera camera;
	
	// the groups for the scene
	private Group isosurfaceGroup;	// holds the isosurfaces
	private Group cameraGroup; 		// holds the camera translation data
	
	private Group axisNode; 		// holds the axisGroup -> allows the axisGroup to be null without an exception
	private AxisGroup axisGroup;	// hold the axisGroup
	private Group objectGroup;		// holds the objects for the scene
	private Group lightGroup;		// holds the lights for the scene
	private ScaleAxisGroup scaleAxesGroup;
	
	// the saved offset/ rotation data
	private Translate sceneOffset;
	private Translate isoGroupOffset;
	private Scale scale = new Scale();
	private Rotate alignedXRotate = new Rotate();
	{alignedXRotate.setAxis(new Point3D(1,0,0));};
	private Rotate alignedYRotate = new Rotate();
	{alignedYRotate.setAxis(new Point3D(0,1,0));};
	
	private int mouseState = MOUSE_CAM_ROTATE; // !!
	private Point3D scaleDir = new Point3D(1, 1, 1);
	private Point2D mouseScaleDir = new Point2D(1, 1);
	
	private boolean mousePressed = false; // added to stop a bug where onDrag would be called before onPress
	
	/**
	 * Axes hacking
	 */
	
	private Point3D axesMaxLengths;
	private double tickSeperator_REMOVE = 10;
	
	/**
	 * 
	 */
	
	private EventHandler<MouseEvent> scaleEvent = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent ME)
		{
			
			Object obj = ME.getSource();
			final Point3D dir = Vector3DUtil.exclusiveTransforms(
								((Cylinder)obj).getTransforms(),
								new Point3D(0, -1, 0),
								Rotate.class);
			
			scaleDir = dir;
			
			final Point3D actualDir = new Point3D(0, 1, 0);
						
			Transform localToSceneTransforms = ((Cylinder)obj).getLocalToSceneTransform();
			Point3D screenPoint = localToSceneTransforms.transform(actualDir);			
			Point3D screenPoint2 = localToSceneTransforms.transform(actualDir.multiply(2));
			
			Point3D sceneMouseOffset = screenPoint.subtract(screenPoint2);
			// sceneMouseOffset = new Point3D(sceneMouseOffset.getX(), -sceneMouseOffset.getY(), sceneMouseOffset.getZ());
						
			mouseState = MOUSE_SCALE;
			
			mouseScaleDir = new Point2D(sceneMouseOffset.getX(), sceneMouseOffset.getY());
			
		}
		
	};
		
	private double[] oldMousePos = new double[2];
	private double[] newMousePos = new double[2];
	private double zoom = 100;
	
	/**
	 * 
	 * debugging
	 * 
	 */
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0 ;
    private boolean arrayFilled = false ;
	
	
	/*
	 * root - the root node for the scene isosurfaceGroup - the isosurface group
	 * node within the scene graph
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
		
		/*
		 *  debugging
		 */
		
		final Label label = new Label();
        AnimationTimer frameRateMeter = new AnimationTimer() {

            @Override
            public void handle(long now) {
                long oldFrameTime = frameTimes[frameTimeIndex] ;
                frameTimes[frameTimeIndex] = now ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                if (arrayFilled) {
                    long elapsedNanos = now - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                    double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
                    label.setText(String.format("Current frame rate: %.3f", frameRate));
                }
            }
        };

        frameRateMeter.start();

		root.getChildren().add(label);
		
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
		camera.setFarClip(100000);
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
		this.objectGroup.getChildren().addAll(this.lightGroup, axisNode);
		this.cameraGroup.getChildren().addAll(scaleAxesGroup, this.objectGroup);
		
		// add groups the the root
		root.getChildren().addAll(cameraGroup);
		
	}

	private void createScaleAxisGroup()
	{
		this.scaleAxesGroup = new ScaleAxisGroup(new Point3D(50, 50, 50), 5);
	}
	
	private void createAxisGroup()
	{
		// find the length of each axis
		final Point3D xyzLength = new Point3D(100, 100, 100);
		
		// set the thickness of the axis
		// this is arbitrary
		final double size = 3;
				
		// create and return the new axis
		AxisGroup newAxisGroup =  new AxisGroup(
				new Point3D(0,0,0), 
				new Point3D(xyzLength.getX(),xyzLength.getY(),xyzLength.getZ()), 
				size, 
				new Point3D(10,10,10));
		
		scaleAxesGroup.setAxisEventListener(scaleEvent); //!! look into re-organising
		
		this.axisGroup = newAxisGroup;
		this.axisNode.getChildren().add(this.axisGroup);
		
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
		PointLight pointLight = new PointLight(new Color(1, 1, 1, 1));		
		this.lightGroup.getChildren().addAll(ambientSurfaceLight, pointLight);
		
		// create lights for the axes
		AmbientLight ambientAxisLight = new AmbientLight(Color.WHITE);
		ambientAxisLight.getScope().add(this.axisGroup);
		// !! this.axisGroup.getChildren().addAll(ambientAxisLight);
		
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
				
				mousePressed = true;
				
				oldMousePos[0] = (float) me.getSceneX();
				oldMousePos[1] = (float) me.getSceneY();
				newMousePos[0] = (float) me.getSceneX();
				newMousePos[1] = (float) me.getSceneY();
				
			}
		});
		
		// on mouse drag change the camera state
		setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			
			@Override
			public void handle(MouseEvent me)
			{				
				// when testing I found the mouseDragged event was sometimes called prior to the mousePressed event.
				// This will result in the mouse positions not being reset.
				// Added mousePressed to stop this event unless the mousePos has been reset.
				if (mousePressed)
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
					
					final double mouseMovementMod = 0.5f;
					
					// check if left button is pressed
					// rotate if true - ie, rotate on left button drag
					if (me.isPrimaryButtonDown() && !me.isSecondaryButtonDown())
					{
						// !! remove the switch 
						switch (mouseState)
						{
							case 0:
							{
								alignedXRotate.setAngle(alignedXRotate.getAngle() + mouseDelta[1] * mouseMovementMod);
								alignedYRotate.setAngle(alignedYRotate.getAngle() - mouseDelta[0] * mouseMovementMod);	
								break;
							}
							case 1:
							{
								updateScale(mouseDelta, mouseMovementMod);
								break;
							}
						}
					}
					
					// zoom
					if (me.isMiddleButtonDown())
					{
						
						
						Point3D XYTranslationDirection = alignedXRotate.transform(mouseDelta[0]*mouseMovementMod, mouseDelta[1]*mouseMovementMod,0);
						XYTranslationDirection = alignedYRotate.transform(XYTranslationDirection);
												
						
						isoGroupOffset.setX(isoGroupOffset.getX() + XYTranslationDirection.getX() );
						isoGroupOffset.setY(isoGroupOffset.getY() + XYTranslationDirection.getY() );
						isoGroupOffset.setZ(isoGroupOffset.getZ() - XYTranslationDirection.getZ() );
						
//						isoGroupOffset.setX(isoGroupOffset.getX() + (mouseDelta[0]*mouseMovementMod));
						
						// y is good
//						isoGroupOffset.setY(isoGroupOffset.getY() + (mouseDelta[1]*mouseMovementMod));
						
//						sceneOffset.setX(sceneOffset.getX() + (mouseDelta[0]*mouseMovementMod));
//						sceneOffset.setY(sceneOffset.getY() + (mouseDelta[1]*mouseMovementMod));
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
		
		// reset the mouse state upon release
		// !! consider removing
		setOnMouseReleased(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				mousePressed = false;
				
				mouseState = MOUSE_CAM_ROTATE;
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
				updateTransforms();
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
		axisGroup.checkScale(this.scale.transform(maxLength));  	
		
	}
	
	private void updateScale(double[] mouseDelta, double mouseMovementMod)
	{
		Point3D mouseDelta3D = new Point3D(mouseDelta[0], mouseDelta[1], 0);
		
		Point3D mouseScalarDir3D = new Point3D(mouseScaleDir.getX(), mouseScaleDir.getY(), 0);
		
		double scalar = Vector3DUtil.getScaleAcrossProjectedVector(mouseScalarDir3D, mouseDelta3D);
									
		scaleDir.normalize();
		scale.setX(scale.getX() + (scalar * (mouseMovementMod * (0.005 * scaleDir.getX()))));
		if (scale.getX() < 0 )
		{
			scale.setX(0);
		}
		
		scale.setY(scale.getY() + (scalar * (mouseMovementMod * (0.005 * scaleDir.getY()))));
		if (scale.getY() < 0 )
		{
			scale.setY(0);
		}
		
		scale.setZ(scale.getZ() + (scalar * (mouseMovementMod * (0.005 * scaleDir.getZ()))));
		if (scale.getZ() < 0 )
		{
			scale.setZ(0);
		}
		
		System.out.println(scale);
		
	}
	
	
	// private VBox generateControls()
	// {
	//
	// VBox controls = new VBox();
	// controls.setAlignment(Pos.BOTTOM_RIGHT);
	//
	// positionLabel = new Label("");
	// positionLabel.setAlignment(Pos.BOTTOM_RIGHT);
	// positionLabel.setContentDisplay(ContentDisplay.RIGHT);
	//
	// controls.getChildren().add(positionLabel);
	//
	// return controls;
	// }
	
	
	/*
	 * public 
	 */
	
	public void addRemoveScaleAxes()
	{
		scaleAxesGroup.flipVisibility();
	}
	
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
	
	public void removeSurface(Node removeNode)
	{
		isosurfaceGroup.getChildren().remove(removeNode);
	}
	
	// !!
	public void updateTransforms()
	{
		
		this.sceneOffset.setX(this.getWidth() / 2);
		this.sceneOffset.setY(this.getHeight()/ 2);
		this.sceneOffset.setZ(0);
		
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
	
	public void removeAxisGrid()
	{
		axisGroup.flipXGridVisible();
		axisGroup.flipYGridVisible();
		axisGroup.flipZGridVisible();
	}
	
	public void setAxesData(List<IDataset> axesData)
	{
		this.axesMaxLengths = new Point3D(
										axesData.get(0).getFloat(0),
										axesData.get(0).getFloat(1),
										axesData.get(0).getFloat(2));
		
		setTickSeperator_FORDEMO_REMOVE(axesMaxLengths);
		
		this.axisGroup.setAxisLimitMax(axesMaxLengths);
		updateAxisSize(this.axesMaxLengths);
		
	}
	
	private void setTickSeperator_FORDEMO_REMOVE(Point3D axesMaxLength)
	{
		double maxLength = -999;
		if (axesMaxLength.getX() > maxLength)
		{
			maxLength = axesMaxLength.getX();
		}
		if (axesMaxLength.getY() > maxLength)
		{
			maxLength = axesMaxLength.getY();
		}
		if (axesMaxLength.getZ() > maxLength)
		{
			maxLength = axesMaxLength.getZ();
		}
		
		
		tickSeperator_REMOVE = maxLength/10;
		
		axisGroup.SetTickSeperationXYZ(new Point3D(tickSeperator_REMOVE, tickSeperator_REMOVE, tickSeperator_REMOVE));
		
		
	}
	
	
}
