package org.dawnsci.plotting.javafx.axis.objects;

import javafx.geometry.Point3D;
import javafx.scene.CacheHint;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class LineGroup extends Group
{
	final double TEXTMOD = 2;
	
	private Line line;
	private Pane textPane;
	private Text textLabel;
//	private Label textLabel;
	
	// text rotates
	private Rotate textXAxisRotate = new Rotate();
		{textXAxisRotate.setAxis(new Point3D(1, 0, 0));}
 	private Rotate textYAxisRotate = new Rotate();
 		{textYAxisRotate.setAxis(new Point3D(0, 1, 0));}
 	private Rotate textZAxisRotate = new Rotate();
 		{textZAxisRotate.setAxis(new Point3D(0, 0, 1));}
 		
 	private Rotate invertedSceneXRotate;
 	private Rotate invertedSceneYRotate;
 		
 		
 	
 	// this transforms
	private Translate offset;
	private Rotate rotate;
	
	private double textSize = 10;
	
	public LineGroup(double length, Point3D direction, Point3D offset, String label, double textSize, Rotate invertXRot, Rotate invertYRot, Rotate gridRotate)
	{		
		this.textSize = textSize;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());
		this.line = new Line(length, new Rotate(), new Point3D(0,0,0));
		
		this.invertedSceneXRotate = invertXRot;
		this.invertedSceneYRotate = invertYRot;
		
		this.rotate = new Rotate();
		textPane = new Pane();
		
		Transform inverseGridRotate = null;
		try 
		{
			inverseGridRotate = gridRotate.createInverse();
		} 
		catch (NonInvertibleTransformException e) 
		{
			e.printStackTrace();
		}
		
		
		if (label != null)
		{
			textLabel = createTextLabel(offset.getX(), offset.getY(), label);
			textLabel.setDepthTest(DepthTest.DISABLE);
			textPane.getChildren().add(textLabel);
			textPane.setCacheHint(CacheHint.SCALE_AND_ROTATE);
			textPane.setCache(true);
			textPane.getTransforms().add(new Scale((float)1/TEXTMOD, (float)1/TEXTMOD, (float)1/TEXTMOD));
			textPane.getTransforms().addAll(inverseGridRotate, this.invertedSceneYRotate, this.invertedSceneXRotate);
		}
		
		
		double angle = new Point3D(0, 1, 0).angle(direction);
		Point3D rotateVector = new Point3D(0, 1, 0).crossProduct(direction);
		
		this.rotate = new Rotate(angle, 0, offset.getX(), 0, rotateVector);
		
		this.getChildren().addAll(this.line, this.textPane);
		this.getTransforms().addAll(this.offset, this.rotate);
		
		
	}
	
	// create text label for the grid axis
	private Text createTextLabel(double x, double y, String text) 
	{
		
		// create the text to return
		Text returnText = new Text(text);
		
		// add transforms
		returnText.getTransforms().addAll(
				textXAxisRotate,
				textYAxisRotate, 
				textZAxisRotate, 
				new Translate(
						(textSize*TEXTMOD)/2,
						(textSize*TEXTMOD)/2,
						0));
		
		// scale the text by Mod -> this is to allow for greater resolution
		// javafx handles text funny and it blurs upon zooming
		// i increase the size then scale down to increase the resolution
		returnText.setFont(new Font(textSize*TEXTMOD));
		
		
		return returnText;
	}
		
	public void setRotate(Rotate newRotate)
	{
		this.rotate = newRotate;
	}
	
	public void setHeightExtended(double x)
	{
		this.line.setHeightExtended(x);
	}
	
	public void resetOffset()
	{
		this.line.editOffset(new Translate( 0, 0 ,0));
	}

	public void setMaterial(PhongMaterial phongMaterial)
	{
		this.line.setMaterial(phongMaterial);
	}
	
	public void rewriteLabel(String newLabel)
	{
		this.textLabel.setText(newLabel);
	}
	
	public boolean getTextState()
	{
		if (this.textLabel == null)
		{
			return false;
		}
		return true;
	}
	
	public Translate getOffset()
	{
		return this.offset;
	}
}
