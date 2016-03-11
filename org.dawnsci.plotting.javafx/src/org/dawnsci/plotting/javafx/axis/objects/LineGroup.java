package org.dawnsci.plotting.javafx.axis.objects;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
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
	private Translate pivotPoint_TextPane;
	private Translate translate_TextPane;
	private Rotate rotate;
	
	private double textSize = 10;
	
	public LineGroup(double length, Point3D direction, Point3D offset, String label, double textSize, Rotate gridRotate)
	{		
		super();
		this.textSize = textSize;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());
		this.line = new Line(length, new Rotate(), new Point3D(0,0,0));
		
		this.rotate = new Rotate();
		this.textPane = new Group();
		this.pivotPoint_TextPane = new Translate(0,0,0);
		this.translate_TextPane = new Translate(0,0,0);
		this.textOffset = new Translate(0,0,0);
		
		if (label != null)
		{
			textLabel = createTextLabel(label);
			
			textLabel.setFontSmoothingType(FontSmoothingType.LCD);
			textPane.getChildren().add(textLabel);
			
			setTextTransforms();
			
			textPane.getTransforms().addAll(
					textOffset,
					pivotPoint_TextPane, 
					translate_TextPane,
					textXAxisRotate,
					textYAxisRotate, 
					textZAxisRotate);
			
			
			textPane.getTransforms().addAll();
			
		}
		
		
		double angle = new Point3D(0, 1, 0).angle(direction);
		Point3D rotateVector = new Point3D(0, 1, 0).crossProduct(direction);
		
		this.rotate = new Rotate(angle, 0, offset.getX(), 0, rotateVector);
		
		this.getChildren().addAll(this.line, this.textPane);
		this.getTransforms().addAll(this.offset, this.rotate);
				
		this.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
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
		double depth = textLabel.getBoundsInLocal().getDepth();	// should be zero as it is only a plane
		
		// Don't even ask why this is the mid point of the text... I don't understand myself.
		Point3D midPoint = new Point3D((width/2), -(height*0.65)/2, depth/2);
				
		textXAxisRotate.setPivotX(midPoint.getX());
		textXAxisRotate.setPivotY(midPoint.getY());
		
		textYAxisRotate.setPivotX(midPoint.getX());
		textYAxisRotate.setPivotY(midPoint.getY());
		
		textZAxisRotate.setPivotX(midPoint.getX());
		textZAxisRotate.setPivotY(midPoint.getY());	
		
		Rotate rotate = new Rotate(-90,new Point3D(0, 0, 1));
		rotate.setPivotX(width);
		rotate.setPivotY(-height/4);
		
		pivotPoint_TextPane.setX(-midPoint.getX());
		pivotPoint_TextPane.setY(-midPoint.getY());
		pivotPoint_TextPane.setZ(-midPoint.getZ());
		
		double maxOffset = Vector3DUtil.getMaximumValue(midPoint);

		this.translate_TextPane.setY(-maxOffset);
		this.translate_TextPane.setZ(-maxOffset);
		
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
			
			if (translate.getY() > 0)
			{
				this.translate_TextPane.setY(Math.abs(this.translate_TextPane.getY()) *  1);
			}
			else
			{
				this.translate_TextPane.setY(Math.abs(this.translate_TextPane.getY()) * -1);;
			}
		}
	}
	
	
	public void gridMoved(Point3D translate)
	{
		if (translate.getZ() > 0)
		{
			this.translate_TextPane.setZ(Math.abs(this.translate_TextPane.getZ()) *  1);
		}
		else
		{
			this.translate_TextPane.setZ(Math.abs(this.translate_TextPane.getZ()) * -1);
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
