package org.dawnsci.plotting.javafx.testing.shell;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Cylinder;

import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.dawnsci.plotting.javafx.volume.TexturedPlane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import com.sun.javafx.iio.ImageStorage.ImageType;


public class TexturePlaneCreation {
	
	@Test
	public void shellButtonTest() throws Exception
	{
		Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        
        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        Group root = new Group();
        Group isoSurfaceGroup = new Group();
        
        SurfaceDisplayer scene = new SurfaceDisplayer(root, isoSurfaceGroup);
        
        // create the coloured images to distinguish axes
        BufferedImage red = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage green = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blue = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < 100; i++)
        {
        	for (int j = 0; j < 100; j ++)
        	{
        		int redInt = 255;
				redInt = (redInt << 8) + 255;
				redInt = (redInt << 8) + 0;
				redInt = (redInt << 8) + 0;
				
				int greenInt = 255;
				greenInt = (greenInt << 8) + 0;
				greenInt = (greenInt << 8) + 255;
				greenInt = (greenInt << 8) + 0;
				
				int blueInt = 255;
				blueInt = (blueInt << 8) + 0;
				blueInt = (blueInt << 8) + 0;
				blueInt = (blueInt << 8) + 255;
				
        		red.setRGB(i, j, redInt);
        		green.setRGB(i, j, greenInt);
        		blue.setRGB(i, j, blueInt);
        	}
        }
        
        // cylinders are created along the Y axis
        Cylinder yAxis = new Cylinder(2,100);
        
        // x direction = red
        TexturedPlane tp_XDirection = new TexturedPlane(
				new Point2D(0, 0),
				new Point2D(100, 100),
				SwingFXUtils.toFXImage(red, null),
				new Point3D(1, 0, 0));
        
        // y direction = green
        TexturedPlane tp_YDirection = new TexturedPlane(
				new Point2D(0, 0),
				new Point2D(100, 100),
				SwingFXUtils.toFXImage(green, null),
				new Point3D(0, 1, 0));
        tp_YDirection.setTranslateZ(50);
        
        // y direction = blue
        TexturedPlane tp_ZDirection = new TexturedPlane(
				new Point2D(0, 0),
				new Point2D(100, 100),
				SwingFXUtils.toFXImage(blue, null),
				new Point3D(0, 0, 1));
        tp_ZDirection.setTranslateZ(50);
        
        // add the planes to check if they are correctly oriented
        isoSurfaceGroup.getChildren().addAll(yAxis,tp_XDirection, tp_YDirection, tp_ZDirection);
        
        canvas.setScene(scene);
        shell.open();
        
    	while (!shell.isDisposed ()) {
    		
    		if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose();

	}
}
