package org.dawnsci.plotting.javafx.axis.objects;

import org.dawnsci.plotting.javafx.objects.Line3D;
import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class TickGroup extends Group
{
	
	private Line3D line;
	private Group textPane;
	private Text textLabel;
		
	private Rotate labelCorrectionRotate;
 	 	
 	// this transforms
	private Translate offset;
	private Translate textOffset;
	private Translate pivotPoint_TextPane;
	private Translate translate_TextPane;
	private Rotate rotate;
	
	private double textSize;
	
	public TickGroup(double length, Point3D direction, Point3D offset, String label, double textSize, Rotate gridRotate)
	{		
		super();
		this.textSize = textSize;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());
		this.line = new Line3D(length, new Rotate(), new Point3D(0,0,0));
		
		this.rotate = new Rotate();
		this.textPane = new Group();
		this.pivotPoint_TextPane = new Translate(0,0,0);
		this.translate_TextPane = new Translate(0,0,0);
		this.textOffset = new Translate(0,0,0);
		
		labelCorrectionRotate  = new Rotate();
		
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
					labelCorrectionRotate);
			
			
		}
		
		this.rotate = Vector3DUtil.alignVector(new Point3D(0, 1, 0), direction);
		
		this.getChildren().addAll(this.line, this.textPane);
		this.getTransforms().addAll(this.offset, this.rotate);
				
		addUpdateListener();
	}
	
	private void addUpdateListener()
	{
		this.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Rotate axisAngleRotate = Vector3DUtil.matrixToRotate(newT);
			
			try {
				axisAngleRotate = (Rotate) axisAngleRotate.createInverse();
			} catch (Exception e) {
				e.printStackTrace();
			}

			labelCorrectionRotate.setAxis(axisAngleRotate.getAxis());
			labelCorrectionRotate.setAngle(axisAngleRotate.getAngle());

		});
	}
	
	private void setTextTransforms()
	{
		double width = textLabel.getBoundsInLocal().getWidth();
		double height= textLabel.getBoundsInLocal().getHeight();
		double depth = textLabel.getBoundsInLocal().getDepth();	// should be zero as it is only a plane
		
		// Don't even ask why this is the mid point of the text... I don't understand myself.
		Point3D midPoint = new Point3D((width/2), -(height*0.65)/2, depth/2);
		
		labelCorrectionRotate.setPivotX(midPoint.getX());
		labelCorrectionRotate.setPivotY(midPoint.getY());
		labelCorrectionRotate.setPivotZ(midPoint.getZ());
		
		Rotate rotate = new Rotate(-90,new Point3D(0, 0, 1));
		rotate.setPivotX(width);
		rotate.setPivotY(-height/4);
		
		pivotPoint_TextPane.setX(-midPoint.getX());
		pivotPoint_TextPane.setY(-midPoint.getY());
		pivotPoint_TextPane.setZ(-midPoint.getZ());
				
		double maxOffset = Vector3DUtil.getMaximumValue(midPoint);
		
		this.translate_TextPane.setY(-maxOffset * 1.5);
		
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
	
	public void setTextPivot(Point3D pivot)
	{
		labelCorrectionRotate.setPivotX(pivot.getX());
		labelCorrectionRotate.setPivotY(pivot.getY());
		labelCorrectionRotate.setPivotZ(pivot.getZ());
	}
	
	/**
	 * set the text offset along the tick mark
	 * @param translate
	 */
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
	
	public void setTextVisibility(boolean visibility)
	{
		textPane.setVisible(visibility);;
	}
	
}
