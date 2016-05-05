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
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import javax.vecmath.Matrix3d;

import org.dawnsci.plotting.javafx.axis.objects.JavaFXProperties;
import org.dawnsci.plotting.javafx.axis.objects.ScaleAxisGroup;
import org.dawnsci.plotting.javafx.axis.objects.SceneObjectGroup;
import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.dawnsci.plotting.javafx.trace.JavafxTrace;
import org.dawnsci.plotting.javafx.trace.isosurface.IsosurfaceTrace;
import org.dawnsci.plotting.javafx.trace.volume.VolumeRender;
import org.dawnsci.plotting.javafx.trace.volume.VolumeTrace;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * 
 * @author nnb55016 The following class creates the scene where the surface is
 *         visualised It is used when running the application inside DAWN
 *
 * @author Joel Ogden
 * 
 */

public class SurfaceDisplayer extends Scene
{		
	final double MOUSE_MOVEMENT_MOD = 1.2;
	
	// camera for the scene
	private PerspectiveCamera perspectiveCamera;
	private PerspectiveCamera parallelCamera;
	private Camera currentCamera;
	
	// the groups for the scene
	private Group lightingGroup;	// holds the isosurfaces
	private Group nonLightingGroup;		// holds the volume renderings
	private Group cameraGroup; 		// holds the camera translation data
	
	private Group root;				// root of the scene graph
	private Group axisNode; 		// holds the axisGroup -> allows the axisGroup to be null without an exception
	private SceneObjectGroup axisObjectGroup;	// hold the axisGroup
	private Group objectGroup;		// holds the objects for the scene
	private Group lightGroup;		// holds the lights for the scene
	private ScaleAxisGroup scaleAxesGroup;
	
	// Scene and camera variables
	private Translate isoGroupOffset;
	private Scale scaleZoom;
	private Rotate rotate;
	
	// mouse variables
	private boolean mousePositionSet = false;
	private double[] oldMousePos = new double[2];
	private double[] newMousePos = new double[2];
	
	// Axis variables
	private Point3D axesMaxLengths;	
			
	/**
	 * 
	 * @param root - the root node for the scene isosurfaceGroup
	 * @param isosurfaceGroup - the node holding the surface objects
	 */
	public SurfaceDisplayer(Group root, Group isosurfaceGroup)
	{
		
		
		// create the scene
		super(root, 1500, 1500, true);
		
		this.root = root;
		this.lightingGroup = isosurfaceGroup;
				
		// set the camera -> the camera will handle some aspects of movement
		// other are within the group -> this is done to simplify rotation
		// calculations
		this.perspectiveCamera = new PerspectiveCamera();	
		this.parallelCamera = new PerspectiveCamera();
		this.parallelCamera.setFieldOfView(0.01);
				
		this.currentCamera = perspectiveCamera;
		
		initialiseCamera();
		initlialiseGroups();
		createScaleAxisGroup();
		createAxisGroup();
		createSceneGraph();
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
		setCamera(currentCamera);
		
		currentCamera.setNearClip(0.1f);
		currentCamera.setFarClip(100_000_000);
		
		updateCameraSceneTransforms();
		
	}
	
	private void initlialiseGroups()
	{
		// initialise/create the groups
		this.nonLightingGroup = new Group();
		this.cameraGroup = new Group();
		this.axisNode = new Group();
		this.objectGroup = new Group();
		this.lightGroup = new Group();
		
	}
	
	// combine the groups into the scene graph root node
	private void createSceneGraph()
	{
		
		// create the scene graph
		this.lightGroup.getChildren().addAll(this.lightingGroup);
		this.objectGroup.getChildren().addAll(axisNode, this.lightGroup, nonLightingGroup);
		this.cameraGroup.getChildren().addAll(this.objectGroup);
		
		// add groups the the root
		this.root.getChildren().addAll(cameraGroup);
		
	}

	private void createScaleAxisGroup()
	{
		this.scaleAxesGroup = new ScaleAxisGroup(new Point3D(50, 50, 50), 5);
	}
	
	private void createAxisGroup()
	{		
		// create and return the new axis
		SceneObjectGroup newAxisObjectGroup =  new SceneObjectGroup();
		
		this.axisObjectGroup = newAxisObjectGroup;
		this.axisNode.getChildren().add(this.axisObjectGroup);
	}
	
	private void setDepthBuffers()
	{
		// disable the depth buffer for the isosurfaces -> depth buffer doesn't behave with transparency
		// enable for the axis node group
		this.lightingGroup.setDepthTest(DepthTest.ENABLE);
		this.axisNode.setDepthTest(DepthTest.ENABLE);
//		this.volumeGroup.setDepthTest(DepthTest.DISABLE);
	
	}
	
	private void initialiseTransforms()
	{
		// initialise
		this.isoGroupOffset = new Translate();
		this.scaleZoom = new Scale();
		this.rotate = new Rotate();
		
		this.scaleAxesGroup.getTransforms().addAll();
				
		this.objectGroup.getTransforms().addAll(scaleZoom, isoGroupOffset);
				
		this.cameraGroup.getTransforms().addAll(rotate);
		
	}
	
	private void addLights()
	{
		// create lights for the iso surface
		AmbientLight ambientSurfaceLight = new AmbientLight(new Color(0.3,0.3,0.3,1));
		ambientSurfaceLight.getScope().add(objectGroup);

		this.objectGroup.getChildren().addAll(ambientSurfaceLight);
		
		AmbientLight ambientVolumeLight = new AmbientLight(new Color(1,1,1,1));
		ambientVolumeLight.getScope().add(nonLightingGroup);
		
		this.nonLightingGroup.getChildren().addAll(ambientVolumeLight);
		
		PointLight pointLight = new PointLight(new Color(1, 1, 1, 1));	
		pointLight.getScope().add(lightGroup);
		
		this.lightGroup.getChildren().addAll(pointLight);
		
		
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
										
					// check if left button is pressed
					// rotate if true - ie, rotate on left button drag
					if (me.isPrimaryButtonDown() && !me.isSecondaryButtonDown())
					{
						Point3D arcOldBallMousePositon = findArcballMousePosition(
								oldMousePos[0]-(getWidth()/2),
								oldMousePos[1]-(getHeight()/2));
						
						Point3D arcNewBallMousePositon = findArcballMousePosition(
																newMousePos[0]-(getWidth()/2),
																newMousePos[1]-(getHeight()/2));
												
						Point3D rotationAxis = arcNewBallMousePositon.crossProduct(arcOldBallMousePositon);
						
						double rotationAngle = arcOldBallMousePositon.angle(arcNewBallMousePositon);
						
						rotateCameraArcball(rotationAxis, rotationAngle);
					}
					
					if (me.isMiddleButtonDown())
					{
						moveObjects(mouseDelta[0]*MOUSE_MOVEMENT_MOD, mouseDelta[1]*MOUSE_MOVEMENT_MOD);
					}
					
					// zoom if right button is pressed
					if (me.isSecondaryButtonDown() && me.isPrimaryButtonDown())
					{
						zoom(mouseDelta[1]);
					}
				}
			}
		});
				
		// on mouse scroll zoom the camera
		setOnScroll(new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle(ScrollEvent event)
			{
				zoom(event.getDeltaY());
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
				updateCameraSceneTransforms();
			}
		});
		
		this.widthProperty().addListener(listener);
		this.heightProperty().addListener(listener);
		
	}
	
	/*
	 * non initialisers
	 */
	
	/**
	 * Find the position of the mouse on the arcball
	 * @param x
	 * @param y
	 * @return the x,y,z positions of the arc ball
	 */
	private Point3D findArcballMousePosition(double x, double y)
	{
		// equation:
		// a = sqrt(r^2 - (b-pb)^2 - (c-pc)^2) + pa
		// pa, pb, pc = 0
		// :. a = sqrt((r^2) - (b^2) - (c^2) )
		// where a,b,c can equal x,y,z interchangeably
		
		// r = (width/2)^2 + (height/2)^2
		double r_Squared = Math.pow((getWidth()/2),2) + Math.pow((getHeight()/2),2); 
		
		double z = Math.sqrt(r_Squared - Math.pow(x, 2) - Math.pow(y, 2));
		
		if (Math.abs(- Math.pow(x, 2) - Math.pow(y, 2)) > r_Squared)
			z = 0;
		
		return new Point3D(x, y, z);
		
	}
	
	private void rotateCameraArcball(Point3D rotationAxis, double newAngle)
	{
		Rotate appliedRotate = new Rotate(newAngle, new Point3D(rotationAxis.getX(), rotationAxis.getY(), -rotationAxis.getZ()));
				
		Matrix3d appliedMatrix = new Matrix3d(
				appliedRotate.getMxx(), appliedRotate.getMxy(), appliedRotate.getMxz(),
				appliedRotate.getMyx(), appliedRotate.getMyy(), appliedRotate.getMyz(),
				appliedRotate.getMzx(), appliedRotate.getMzy(), appliedRotate.getMzz());
		
		Matrix3d currentRotationMatrix = new Matrix3d(
				rotate.getMxx(), rotate.getMxy(), rotate.getMxz(),
				rotate.getMyx(), rotate.getMyy(), rotate.getMyz(),
				rotate.getMzx(), rotate.getMzy(), rotate.getMzz());
		
		appliedMatrix.mul(currentRotationMatrix);
		
		Rotate newRotate= Vector3DUtil.matrixToRotate(appliedMatrix);
		
		rotate.setAxis(newRotate.getAxis());
		rotate.setAngle(newRotate.getAngle());
		
		updateCameraSceneTransforms();
	}
	
	private void moveObjects(double deltaX, double deltaY)
	{
		Point3D dir = Vector3DUtil.applyEclusiveRotation(
				cameraGroup.getTransforms(), 
				new Point3D(deltaX, deltaY,0), 
				true);
								
		isoGroupOffset.setX(isoGroupOffset.getX() + dir.getX() );
		isoGroupOffset.setY(isoGroupOffset.getY() + dir.getY() );
		isoGroupOffset.setZ(isoGroupOffset.getZ() + dir.getZ() );
	}
	
	private void zoom(double amount)
	{
		double delta = ((((amount * MOUSE_MOVEMENT_MOD)/10)) * 0.05);
		
		scaleZoom.setX(Math.abs(scaleZoom.getX() * (1 + delta)));
		scaleZoom.setY(Math.abs(scaleZoom.getY() * (1 + delta)));
		scaleZoom.setZ(Math.abs(scaleZoom.getZ() * (1 + delta)));		
	}
	
	public void resetSceneTransforms()
	{
		scaleZoom.setX(1);
		scaleZoom.setY(1);
		scaleZoom.setZ(1);
		
		isoGroupOffset.setX(0);
		isoGroupOffset.setY(0);
		isoGroupOffset.setZ(0);
		
		rotate.setAngle(0);
						
		updateCameraSceneTransforms();
		centraliseObjectGroup();
	}
		
	private Point3D findMidPointOfBounds(Bounds bounds)
	{		
		final Translate offsetInverse = new Translate(
							bounds.getMinX() + (bounds.getWidth() / 2),                     
							bounds.getMinY() + (bounds.getHeight()/ 2), 
							bounds.getMinZ() + (bounds.getDepth() / 2)
							).createInverse(); 
		
		return new Point3D(
				offsetInverse.getX(),
				offsetInverse.getY(),
				offsetInverse.getZ());
	}
		
	private void updateCameraSceneTransforms()
	{
		this.currentCamera.setTranslateX(-this.getWidth() / 2);           
		this.currentCamera.setTranslateY(-this.getHeight()/ 2);           
		this.currentCamera.setTranslateZ(-0);
	}
	
	private void centraliseObjectGroup()
	{
		Point3D midPoint = findMidPointOfBounds(objectGroup.getBoundsInLocal());
		
		isoGroupOffset.setX(midPoint.getX() ); 
		isoGroupOffset.setY(midPoint.getY() ); 
		isoGroupOffset.setZ(midPoint.getZ() ); 
	}
	
	/*
	 * public 
	 */
	
	public void dispose()
	{
		
		this.setOnMousePressed(null);
		this.setOnMouseDragged(null);
		this.setOnScroll(null);
		
		if (lightingGroup != null)
			lightingGroup.setOnMouseMoved(null);
	}
	
	public Group getIsosurfaceGroup()
	{
		return lightingGroup;
	}
	
	public void addTrace(JavafxTrace trace)
	{
		// isosurfaces require a specific lighting group
		if (trace instanceof IsosurfaceTrace)
			this.lightingGroup.getChildren().add(trace.getNode());
		else
			this.nonLightingGroup.getChildren().add(trace.getNode());
		
	}
	
	public void removeNode(Node removeNode)
	{
		if (lightingGroup.getChildren().contains(removeNode))
		{
			lightingGroup.getChildren().remove(removeNode);
		}
		
		if (nonLightingGroup.getChildren().contains(removeNode))
		{
			nonLightingGroup.getChildren().remove(removeNode);
		}
	}
		
	public void setAxesData(List<IDataset> axesData)
	{
		
		
		Point3D axisLength = new Point3D(
				axesData.get(0).getSize(), 
				axesData.get(1).getSize(),
				axesData.get(2).getSize());
		
		this.axisObjectGroup.setAxes(axisLength, axesData);
		this.axisObjectGroup.setBoundingBox(axisLength);
		
		centraliseObjectGroup();
	}

	public void setAxisGridVisibility(boolean visibility)
	{
		axisObjectGroup.setAllVisible(visibility);
	}

	public void setBoundingBoxVisibility(boolean visibility) 
	{
		axisObjectGroup.setBoundingBoxVisibility(visibility);
	}
	
	public void setScaleAxesVisibility(boolean visibility)
	{
		// do nothing at the moment
	}
	
	public void setCameraType(int cameraType)
	{
		switch (cameraType)
		{
			case (JavaFXProperties.CameraProperties.PERSPECTIVE_CAMERA):
			{
				this.currentCamera = this.perspectiveCamera;
				break;
			}
			case (JavaFXProperties.CameraProperties.PARALLEL_CAMERA):
			{
				this.currentCamera = this.parallelCamera;
				break;
			}
		}
		
		initialiseCamera();
	}
	
	
	
	
	
}
