package org.dawnsci.isosurface;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

/**
 * @author nnb55016
 * The following class creates the scene where the surface is visualised 
 * It is used when running the application inside DAWN
 */
public class SurfaceDisplayer extends Scene{
	
	private final SurfaceTransformations cam;
	private final SurfaceTransformations camOffset;
	
	private MeshView isosurface;
	private double surfaceX = 400; 
	private double surfaceY = 520;
	private double surfaceZ = 0;
	private double surfaceScaleX = 1;
	private double surfaceScaleY = 1;
	private double surfaceScaleZ = 1;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    /**
     * Constructor for the class:
     * @param root: The Group which is the parent of the surface
     * @param isosurface: the mesh of the surface visualised which is initialised in the IsosufaceJob class
     */
	public SurfaceDisplayer(Group root, MeshView isosurface){
		super(root, 700, 700, true);

		this.isosurface = isosurface;
		
		cam       = new SurfaceTransformations();
		camOffset = new SurfaceTransformations();

		setCamera(new PerspectiveCamera());
		

		camOffset.getChildren().add(cam);
	
		cam.getChildren().add(surfaceGenerator());
			
		cam.p.setX(this.getWidth()/2.0);
        cam.ip.setX(-this.getWidth()/2.0);
        cam.p.setY(this.getHeight()/2.0);
        cam.ip.setY(-this.getHeight()/2.0);
        
        frameCam(this);
        
        root.getChildren().addAll(camOffset);
        
        actions();
       
	}


	public void dispose() {
		
		this.setOnMousePressed(null);
		this.setOnMouseDragged(null);
		this.setOnScroll(null);
		
		if (isosurface!=null) isosurface.setOnMouseMoved(null);
		if (cam!=null)        cam.getChildren().removeAll();
		if (camOffset!=null)  camOffset.getChildren().removeAll();
		
	}
	
	/**
	 * Method for visualising the surface from the mesh view
	 * @return the group that contains the surface
	 */

	private Group surfaceGenerator(){
        Group group = new Group();
		
		isosurface.setMaterial(new PhongMaterial(Color.GOLDENROD));
		isosurface.setCullFace(CullFace.NONE);
        isosurface.setScaleX(surfaceScaleX);
        isosurface.setScaleY(surfaceScaleY);
        isosurface.setScaleZ(surfaceScaleZ);
        
        isosurface.setTranslateX(surfaceX);
        isosurface.setTranslateY(surfaceY);
        isosurface.setTranslateZ(surfaceZ);
        
        group.getChildren().add(isosurface);
		
        return group;
	}

/**
 * The following four methods( frameCam, setCamTranslate, setCamPivot, setCamOffsets) have been taken
 * from the source code from the Rotation tutorial of JavaFX written by Oracle
 * The original code can be found at:  http://docs.oracle.com/javafx/2/transformations/rotation.htm
 */
	private void frameCam(Scene scene) {
	        setCamOffset(camOffset, scene);
	        setCamPivot(cam);
	        setCamTranslate(cam);

	}
	
    public void setCamTranslate(final SurfaceTransformations cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        cam.translation.setX(-pivotX);
        cam.translation.setY(-pivotY);
            
    }
    
	public void setCamPivot(final SurfaceTransformations cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
        cam.p.setX(pivotX);
        cam.p.setY(pivotY);
        cam.p.setZ(pivotZ);
        cam.ip.setX(-pivotX);
        cam.ip.setY(-pivotY);
        cam.ip.setZ(-pivotZ);
        
    }
	
	public void setCamOffset(final SurfaceTransformations camOffset, final Scene scene) {
        double width = scene.getWidth();
        double height = scene.getHeight();
        camOffset.translation.setX(width/2.0);
        camOffset.translation.setY(height/2.0);
    }
	
	/**
	 * The actions() method has all the listeners for the events of the transformations of the surface
	 */
	public void actions(){
		this.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mousePosX = me.getX();
                mousePosY = me.getY();
            }
        });
		
		this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getX();
                mousePosY = me.getY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;
                if (me.isControlDown() && me.isPrimaryButtonDown()) {
                    double rzAngle = cam.zRotation.getAngle()%360;
                    cam.zRotation.setAngle(rzAngle - mouseDeltaX);
                }
                else if (!me.isControlDown() && me.isPrimaryButtonDown()) {
                	
                    double ryAngle = cam.yRotation.getAngle()%360;
                    cam.yRotation.setAngle(ryAngle - mouseDeltaX);           
                    
                    double rxAngle = cam.xRotation.getAngle()%360;
                    cam.xRotation.setAngle(rxAngle + mouseDeltaY);          
                    
                }

                else if (me.isMiddleButtonDown()) {
                    double tx = cam.translation.getX();
                    double ty = cam.translation.getY();
                    cam.translation.setX(tx + mouseDeltaX);
                    cam.translation.setY(ty + mouseDeltaY);
                }                
            }
        });
		
		this.setOnScroll(new EventHandler<ScrollEvent>() {
        	@Override public void handle(ScrollEvent event){
        		cam.scaling.setX(cam.scaling.getX() + event.getDeltaY()/800);
        		cam.scaling.setY(cam.scaling.getY() + event.getDeltaY()/800);    
        		cam.scaling.setZ(cam.scaling.getZ() + event.getDeltaY()/800);
        	}
        });
		
		getCurrentCoordinates();
	}

	/**
	 * The updateTransforms() method records the current coordinates and dimensions of the surface
	 * This method is called before the surface is updated either by changing the isovalue or by 
	 * changing the box size so that the new surface has the same location and size
	 */
	public void updateTransforms(){
		surfaceX = isosurface.getTranslateX();
		surfaceY = isosurface.getTranslateY();
		surfaceZ = isosurface.getTranslateZ();
		
		surfaceScaleX = isosurface.getScaleX();
		surfaceScaleY = isosurface.getScaleY();
		surfaceScaleZ = isosurface.getScaleZ();
	}
	
	public void setIsosurface(MeshView isosurface) {
		this.isosurface = isosurface;
	}

	/**
	 * Whenever the isovalue or the box size are changed this method is called to update the GUI
	 */
	public void updateSurface() {
		cam.getChildren().remove(0);
		cam.getChildren().add(surfaceGenerator());
		getCurrentCoordinates();
	}
	
	/**
	 * The getCurrentCoordinates() method is a compensation for the lack of axes
	 * It prints  the coordinates of the point that the mouse points to 
	 * 
	 * Three text boxes should be added to the user interface so that the user can see the coordinates
	 */
	public void getCurrentCoordinates(){
        isosurface.setOnMouseMoved(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				
				double temporaryX = event.getX();
				double temporaryY = event.getY();
				double temporaryZ = event.getZ();
				
//				System.out.println("point: " + temporaryX + ", " + temporaryY + ", " + temporaryZ);
				
			}
        	
        });
	}


}
