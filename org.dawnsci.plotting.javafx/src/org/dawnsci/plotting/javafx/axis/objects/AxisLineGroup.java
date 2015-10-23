package org.dawnsci.plotting.javafx.axis.objects;

import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class AxisLineGroup extends Group
{
	private AxisLine line;
	private Pane textPane;
	private Text textLabel;
	
	// text rotates
	private Rotate textZAxisRotate = new Rotate();
 		{textZAxisRotate.setAxis(new Point3D(0, 0, 1));}
 	private Rotate textYAxisRotate = new Rotate();
 		{textYAxisRotate.setAxis(new Point3D(0, 1, 0));}
 	
 	// this transforms
	private Translate offset;
	private Rotate rotate;
	
	public AxisLineGroup(double length, Point3D direction, Point3D offset, String label)
	{
		
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());
		this.line = new AxisLine(length, new Rotate(), new Point3D(0,0,0));
		
		
		this.rotate = new Rotate();		
		
		textPane = new Pane();
		if (label != null)
		{
			textLabel = createTextLabel(offset.getX(), offset.getY(), label);
			textLabel.setDepthTest(DepthTest.DISABLE);
			textPane.getChildren().add(textLabel);
			textPane.getTransforms().add(new Scale(0.1,0.1,0.1));
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
		final int textSize = 10;
		final int textSizeMod = 10;
				
		// create the text to return
		Text returnText = new Text(text);
		textZAxisRotate.setAngle(-90);
		textYAxisRotate.setAngle(0);
		
		// add transforms
		returnText.getTransforms().addAll(
				textZAxisRotate, 
				textYAxisRotate, 
				new Translate(
						(textSize*textSizeMod)/2,
						(textSize*textSizeMod)/2,
						0));
		
		// scale the text by Mod -> this is to allow for greater resolution
		// javafx handles text funny and it blurs upon zooming
		// i increase the size then scale down to increase the resolution
		returnText.setFont(new Font(textSize*textSizeMod));
		
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
