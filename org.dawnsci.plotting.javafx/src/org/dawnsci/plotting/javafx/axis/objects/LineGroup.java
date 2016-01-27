package org.dawnsci.plotting.javafx.axis.objects;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class LineGroup extends Group
{
	
	private Line line;
	private Group textPane;
	private Text textLabel;
	
	// text rotates
	private Rotate textXAxisRotate = new Rotate();
		{textXAxisRotate.setAxis(new Point3D(1, 0, 0));}
 	private Rotate textYAxisRotate = new Rotate();
 		{textYAxisRotate.setAxis(new Point3D(0, 1, 0));}
 	private Rotate textZAxisRotate = new Rotate();
 		{textZAxisRotate.setAxis(new Point3D(0, 0, 1));}
 	 	
 	// this transforms
	private Translate offset;
	private Translate textOffset;
	private Translate translateTextPane;
	private Rotate rotate;
	
	private double textSize = 10;
	
	public LineGroup(double length, Point3D direction, Point3D offset, String label, double textSize, Rotate gridRotate)
	{		
		this.textSize = textSize;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());
		this.line = new Line(length, new Rotate(), new Point3D(0,0,0));
		
		this.rotate = new Rotate();
		textPane = new Group();
		
		if (label != null)
		{
			textLabel = createTextLabel(label);
			
			textLabel.setFontSmoothingType(FontSmoothingType.LCD);
			textPane.getChildren().add(textLabel);
			
			setTextTransforms();

			textLabel.getTransforms().addAll(translateTextPane);
			textLabel.getTransforms().addAll(
					new Translate(0,-10,0),
					textXAxisRotate,
					textYAxisRotate, 
					textZAxisRotate);
			
			textOffset = new Translate();
			
			textPane.getTransforms().addAll(textOffset);
			
		}
		
		
		double angle = new Point3D(0, 1, 0).angle(direction);
		Point3D rotateVector = new Point3D(0, 1, 0).crossProduct(direction);
		
		this.rotate = new Rotate(angle, 0, offset.getX(), 0, rotateVector);
		
		this.getChildren().addAll(this.line, this.textPane);
		this.getTransforms().addAll(this.offset, this.rotate);
				
		textPane.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Point3D angles = Vector3DUtil.extractEulerAnglersFromMatrix(newT);
                        
            textXAxisRotate.setAngle(-angles.getX());
            textYAxisRotate.setAngle(-angles.getY());
            textZAxisRotate.setAngle(-angles.getZ());
            
        });		
	}
	
	
	private void setTextTransforms()
	{
		
		double width = textLabel.getBoundsInLocal().getWidth();
		double height= textLabel.getBoundsInLocal().getHeight();	
		double depth = textLabel.getBoundsInLocal().getDepth();	
		
		// Don't even ask why this is the mid point of the text... I don't understand myself.
		Point3D midPoint = new Point3D(-(width/(2.5f))+4, height/4, 0);
		
		textXAxisRotate.setPivotX(-midPoint.getX());
		textXAxisRotate.setPivotY(-midPoint.getY());
		
		textYAxisRotate.setPivotX(-midPoint.getX());
		textYAxisRotate.setPivotY(-midPoint.getY());
		
		textZAxisRotate.setPivotX(-midPoint.getX());
		textZAxisRotate.setPivotY(-midPoint.getY());	
		
		Rotate rotate = new Rotate(-90,new Point3D(0, 0, 1));
		rotate.setPivotX(width);
		rotate.setPivotY(-height/4);

//		translateTextPane = new Translate(0,0,0);
		translateTextPane = new Translate(midPoint.getX(), midPoint.getY(), midPoint.getZ());
	}
	
	// create text label for the grid axis
	private Text createTextLabel(String text) 
	{
		// create the text to return
		Text returnText = new Text(text);
		returnText.setText(text);
		returnText.setFont(new Font(textSize));
		
		return returnText;
	}
		
	public void setRotate(Rotate newRotate)
	{
		this.rotate = newRotate;
	}
	
	public void setTextOffset(Point3D translate)
	{
		if (this.textOffset != null)
		{
			this.textOffset.setX(translate.getX());
			this.textOffset.setY(translate.getY());
			this.textOffset.setZ(translate.getZ());
		}
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
		setTextTransforms();
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
