package org.dawnsci.plotting.javafx;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import javax.vecmath.Matrix3d;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

public class ArcBall {

	final double MOUSE_MOVEMENT_MOD = 1.2;
	
	// arcBall info
	private double size;
	private Point2D position;
	
	// reaction objects
	private Rotate rotate;
	private Translate translate;
	private Scale scaleZoom;
	
	public ArcBall(Point2D windowSize)
	{
		setSize(windowSize);
				
		rotate = new Rotate();
		translate = new Translate();
		scaleZoom = new Scale();
	}
	
	public void rotateArcBall(Point2D startPoint, Point2D endPoint)
	{
		startPoint = startPoint.subtract(this.position); 
		endPoint= endPoint.subtract(this.position); 
		
		Point3D arcOldBallMousePositon = findArcballMousePosition(
												startPoint.getX(),
												startPoint.getY());
		
		Point3D arcNewBallMousePositon = findArcballMousePosition(
												endPoint.getX(),   
												endPoint.getY());  
								
		Point3D rotationAxis = arcNewBallMousePositon.crossProduct(arcOldBallMousePositon);
		
		double rotationAngle = arcOldBallMousePositon.angle(arcNewBallMousePositon);
		
		rotateCameraArcball(rotationAxis, rotationAngle);
	}
	
	public void move(Point2D deltaXY)
	{
		deltaXY.multiply(MOUSE_MOVEMENT_MOD);
		try 
		{
			Point3D direction = rotate.createInverse().transform(new Point3D(deltaXY.getX(), deltaXY.getY(),0));
			
			translate.setX(translate.getX() + direction.getX() );
			translate.setY(translate.getY() + direction.getY() );
			translate.setZ(translate.getZ() + direction.getZ() );
		} 
		catch (NonInvertibleTransformException e) 
		{
			e.printStackTrace();
		}
								
		
	}
	
	public void zoom(double amount)
	{
		double delta = ((((amount * MOUSE_MOVEMENT_MOD)/10)) * 0.05);
		
		scaleZoom.setX(Math.abs(scaleZoom.getX() * (1 + delta)));
		scaleZoom.setY(Math.abs(scaleZoom.getY() * (1 + delta)));
		scaleZoom.setZ(Math.abs(scaleZoom.getZ() * (1 + delta)));		
	}
	
	private Point3D findArcballMousePosition(double x, double y)
	{
		double z = Math.sqrt(Math.pow(size, 2) - Math.pow(x, 2) - Math.pow(y, 2));
		
		if (Math.abs(- Math.pow(x, 2) - Math.pow(y, 2)) > Math.pow(size, 2))
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
	}
	
	
	// getters and setters
	public void setTranslate(Point3D newPoint){
		
		this.translate.setX(newPoint.getX() ); 
		this.translate.setY(newPoint.getY() ); 
		this.translate.setZ(newPoint.getZ() ); 
		
	}
	
	public void resetTransforms()
	{
		scaleZoom.setX(1);
		scaleZoom.setY(1);
		scaleZoom.setZ(1);
		
		translate.setX(0);
		translate.setY(0);
		translate.setZ(0);
		
		rotate.setAngle(0);
	}
	
	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public Rotate getRotate() {
		return rotate;
	}

	public void setRotate(Rotate rotate) {
		this.rotate = rotate;
	}

	public Translate getTranslate() {
		return translate;
	}

	public void setTranslate(Translate translate) {
		this.translate = translate;
	}
	
	public void setSize(Point2D windowSize){
		// equation:
		// a = sqrt(r^2 - (b-pb)^2 - (c-pc)^2) + pa
		// pa, pb, pc = 0
		// :. a = sqrt((r^2) - (b^2) - (c^2) )
		// where a,b,c can equal x,y,z interchangeably
		
		// rr = (width/2)^2 + (height/2)^2 + (0/2)^2
		// radius = Sqrt(rr) = r
		double rr = Math.pow((windowSize.getX()/2),2) + Math.pow((windowSize.getY()/2),2); 
		this.size = Math.sqrt(rr);
		
		this.position = windowSize.multiply(0.5f);
	}

	public Scale getScaleZoom() {
		return scaleZoom;
	}

	public void setScaleZoom(Scale scaleZoom) {
		this.scaleZoom = scaleZoom;
	}
	
	
}
