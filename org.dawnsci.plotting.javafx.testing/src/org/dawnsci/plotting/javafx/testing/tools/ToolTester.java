package org.dawnsci.plotting.javafx.testing.tools;

import static org.junit.Assert.assertTrue;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.junit.Test;

public class ToolTester 
{
	@Test
	public void extractEulerAnglersFromMatrix()
	{
		// check X
		Rotate Rotate_X_90 = new Rotate();
		Rotate_X_90.setAxis(new Point3D(1, 0, 0));
		Rotate_X_90.setAngle(90);
		
		Point3D result_X_90 = Vector3DUtil.extractEulerAnglersFromMatrix(Rotate_X_90);
		 
		assertTrue("X value was incorrect",
				result_X_90.getX() == 90 &&
				result_X_90.getY() == 0  &&
				result_X_90.getZ() == 0 	);
		
		
		// check Y
		Rotate Rotate_Y_90 = new Rotate();
		Rotate_Y_90.setAxis(new Point3D(0, 1, 0));
		Rotate_Y_90.setAngle(90);
		
		Point3D result_Y_90 = Vector3DUtil.extractEulerAnglersFromMatrix(Rotate_Y_90);
		 
		assertTrue("Y value was incorrect",
				result_Y_90.getX() == 00 &&
				result_Y_90.getY() == 90 &&
				result_Y_90.getZ() == 00   );
		
		
		// check Z
		Rotate Rotate_Z_90 = new Rotate();
		Rotate_Z_90.setAxis(new Point3D(0, 0, 1));
		Rotate_Z_90.setAngle(90);
		
		Point3D result_Z_90 = Vector3DUtil.extractEulerAnglersFromMatrix(Rotate_Z_90);
		 
		assertTrue("Z value was incorrect",
				result_Z_90.getX() == 00 &&
				result_Z_90.getY() == 00 &&
				result_Z_90.getZ() == 90 	);
			
	}
	
	@Test
	public void applyEclusiveRotation()
	{
		Point3D vector = new Point3D(1, 0, 0);
		
		Rotate rotate1 = new Rotate();
		rotate1.setAxis(new Point3D(0, 1, 0));
		rotate1.setAngle(45);
		
		Rotate rotate2 = new Rotate();
		rotate1.setAxis(new Point3D(0, 0, 1));
		rotate1.setAngle(60);
		
		Point3D resultVector_working = rotate1.transform(vector);
		resultVector_working = rotate2.transform(resultVector_working);
		
		Box testBox = new Box();
		testBox.getTransforms().addAll(rotate1, rotate2);
		
		
		Point3D resultVector_Tested = Vector3DUtil.applyEclusiveRotation(testBox.getTransforms(), vector, false);
		
		
		assertTrue(resultVector_working.equals(resultVector_Tested));
	}
	
	@Test
	public void applyExclusiveTransforms()
	{
		
		Point3D vector = new Point3D(1, 0, 0);
		
		Rotate rotate1 = new Rotate();
		rotate1.setAxis(new Point3D(0, 1, 0));
		rotate1.setAngle(45);
		Point3D resultVector_All = rotate1.transform(vector);
		Point3D resultVector_Rotate = rotate1.transform(vector);
		
		
		Translate translate1 = new Translate(10, 0, 0);
		resultVector_All = translate1.transform(resultVector_All);
		Point3D resultVector_Translate = translate1.transform(vector);
		
		
		Rotate rotate2 = new Rotate();
		rotate2.setAxis(new Point3D(0, 0, 1));
		rotate2.setAngle(60);
		resultVector_All = rotate2.transform(resultVector_All);
		resultVector_Rotate = rotate2.transform(resultVector_Rotate);
		
		
		Translate translate2 = new Translate(0, 12, 0);
		resultVector_All = translate2.transform(resultVector_All);
		resultVector_Translate = translate2.transform(resultVector_Translate);
		
		
		
		Box box = new Box();
		box.getTransforms().addAll(rotate1, translate1 , rotate2, translate2);
		
		Point3D allRotates = Vector3DUtil.applyExclusiveTransforms(box.getTransforms(), vector, Rotate.class);
		Point3D allTranslates = Vector3DUtil.applyExclusiveTransforms(box.getTransforms(), vector, Translate.class);
		
		assertTrue(allRotates.equals(resultVector_Rotate));
		assertTrue(allTranslates.equals(resultVector_Translate));
	}
	
	
}
