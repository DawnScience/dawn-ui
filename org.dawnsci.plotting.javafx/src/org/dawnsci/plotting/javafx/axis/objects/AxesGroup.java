package org.dawnsci.plotting.javafx.axis.objects;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Translate;

/**
 * Class used to hold potentially link objects related to the Axis
 * e.g. if each axis was to have a scale bar or an offset "arrow" 
 * @author uij85458
 *
 */
public class AxesGroup extends Group{
	
	private Grid axisGrid;
	private Cylinder scaleAxis;
	private IDataset axisTickLookUpTable;
	private Point3D axisSize;
	private double textSize;
	
	private Point3D planeNormal;
	
	public AxesGroup(
			Point3D planeNormal,
			IDataset axisTickLookUpTable, 
			Point3D axisLengths)
	{
		// estimate a textSize
		double a = Math.sqrt(axisLengths.getX() * axisLengths.getY() * axisLengths.getZ());
		textSize = a * 0.0025f;
		
		this.axisTickLookUpTable = axisTickLookUpTable;
		this.axisSize = axisLengths;
		this.planeNormal = planeNormal;
		
		generateAxisGrid();
	}
	
	private void generateAxisGrid()
	{
		if (axisGrid != null)
			axisGrid.getChildren().clear();
		
		axisGrid = new Grid(planeNormal, this.axisTickLookUpTable, this.axisSize, this.textSize);
		
		AmbientLight ambientAxisLight = new AmbientLight(JavaFXProperties.ColourProperties.LINE_COLOUR);
		ambientAxisLight.getScope().add(axisGrid);
		axisGrid.getChildren().add(ambientAxisLight);
		
		this.getChildren().addAll(axisGrid);
	}
	
	private Cylinder createScaleBar(Point3D direction, double length, double thickness)
	{
		// generate the cylinder
		// default position is centered on (0,0,0) in direction (0,1,0)
		Cylinder tempBox = new Cylinder(thickness,length * 1.05d);
		
		// rotate the axis to face the right direction
		// in this case the axis
		tempBox.getTransforms().add(Vector3DUtil.alignVector(new Point3D(0,1,0), direction));
		tempBox.getTransforms().add(new Translate(0,(length * 1.05d)/2,0));
		
		// create the material to colour the axis
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor (new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		mat.setSpecularColor(new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		
		// set the material -> ie colour the axis
		tempBox.setMaterial(mat);
		
		return tempBox;
	}

	public void addScaler(Point3D planeNormal, Point3D axisLength, double thickness, EventHandler<MouseEvent> scaleEventHandler)
	{ 
		scaleAxis = createScaleBar(planeNormal, axisLength.getZ(), thickness);
		scaleAxis.setCursor(Cursor.OPEN_HAND);
		scaleAxis.setOnMouseDragged(scaleEventHandler);
		
		this.getChildren().addAll(axisGrid);
	}
	
	public void setTextSize(double textSize)
	{
		this.textSize = textSize;
		generateAxisGrid();
	}
	
}
