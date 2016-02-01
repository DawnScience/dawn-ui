package org.dawnsci.plotting.javafx.testing.shell;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.dawnsci.plotting.javafx.axis.objects.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class VolumeRenderingShellTester {
	
	MarchingCubes algorithm;
	
	ILazyDataset dataset;
	MarchingCubesModel model;
	Surface testResult;
	
	Group xygroup = new Group();
	Group zygroup = new Group();
	Group xzgroup = new Group();

	private Rotate textXAxisRotate = new Rotate();
	{textXAxisRotate.setAxis(new Point3D(1, 0, 0));}
	private Rotate textYAxisRotate = new Rotate();
	{textYAxisRotate.setAxis(new Point3D(0, 1, 0));}
	private Rotate textZAxisRotate = new Rotate();
	{textZAxisRotate.setAxis(new Point3D(0, 0, 1));}
	
	private void loadDataset() throws Exception
	{
		
		IDataHolder dh = LoaderFactory.getData("files/brain.h5");
		dataset = dh.getLazyDataset("/entry/edf/data");
		
		algorithm = new MarchingCubes();
				
		model = new MarchingCubesModel();
		model.setLazyData(dataset);
		model.setBoxSize(new int[]{3,3,3});
		model.setIsovalue(1800d);
		model.setVertexLimit(Integer.MAX_VALUE);
		
		algorithm.setModel(model);
		
		// execute the algorithmA	
		testResult = algorithm.execute(null, null);
	}
	
	private int[][][] createDataset(Image newImage, int xlength, int ylength, int zlength)
	{
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		SwingFXUtils.fromFXImage(newImage,image);
	    
		int[][][] dataset = new int[xlength][ylength][zlength];
		for (int z = 0; z < zlength; z++)
		{
			for (int y = 0; y < ylength; y ++)
			{
				for (int x= 0; x < xlength; x++)
				{
					dataset[x][y][z] = image.getRGB(x, y);
				}
			}
		}
		
		return dataset;
	}
	
	
	private Node generateNode(int[][][] dataset)
	{		
		Group results = new Group();
		
		for (int z = 0; z < 100; z++)
		{
			BufferedImage bi = new BufferedImage(100, 100,BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 100; y++)
			{
				for (int x = 0; x < 100; x++)
				{
					int argb = dataset[x][y][z];
					bi.setRGB(x, y, argb);
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(100, 100),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			xygroup.getChildren().add(newPlane);
		}
		
		for (int z = 0; z < 100; z++)
		{
			BufferedImage bi = new BufferedImage(100, 100,BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 100; y++)
			{
				for (int x = 0; x < 100; x++)
				{
					int argb = dataset[z][y][x];
					bi.setRGB(x, y, argb);
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(100, 100),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			zygroup.getChildren().add(newPlane);
		}
		zygroup.getTransforms().addAll(
				new Rotate(90, new Point3D(0, 1, 0)),
				new Translate(-100,0,0));
		
		
		for (int z = 0; z < 100; z++)
		{
			BufferedImage bi = new BufferedImage(100, 100,BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 100; y++)
			{
				for (int x = 0; x < 100; x++)
				{
					int argb = dataset[x][z][y];
					bi.setRGB(x, y, argb);
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(100, 100),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			xzgroup.getChildren().add(newPlane);
		}
		xzgroup.getTransforms().addAll(
				new Rotate(-90, new Point3D(1, 0, 0)),
				new Translate(0,-100,0));
		
		results.getChildren().addAll(xygroup, zygroup, xzgroup);
		
		generateRotateEvent(results);
				
		return results;
	}
			
	private void generateRotateEvent(Node node) {
		
		

		node.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			System.out.println("------------");
			Point3D defaultAngle = new Point3D(0, 0, 1);

			Point3D angles = Vector3DUtil.extractEulerAnglersFromMatrix(newT);
			
			textXAxisRotate.setAngle(angles.getX());
			textXAxisRotate.setAngle(angles.getY());
			textXAxisRotate.setAngle(angles.getZ());
			
			defaultAngle = textXAxisRotate.transform(defaultAngle);
			defaultAngle = textYAxisRotate.transform(defaultAngle);
			defaultAngle = textZAxisRotate.transform(defaultAngle);
	        System.out.println("defaultAngle: " + defaultAngle.toString());
			
			//defaultAngle = newT.transform(defaultAngle);
					
			
			
			
			System.out.println("angle: " + angles);
			
	        Point3D opacity = new Point3D(
	        		(new Point3D(1, 0, 0).angle(defaultAngle)% 90), // xy
	        		(new Point3D(0, 1, 0).angle(defaultAngle)% 90), // yz
	        		(new Point3D(0, 0, 1).angle(defaultAngle)% 90));// zx
	        
	        System.out.println("opacity: " + opacity.toString());
	        System.out.println("opacity " );
		
		});
	}

	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception
	{
		
		loadDataset();
		Node Node = generateNode(createDataset(new Image(getClass().getResourceAsStream("unnamed.png")),100,100,100));
		Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        
        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        Group root = new Group();
        Group isoSurfaceGroup = new Group();
        
        Scene scene = new SurfaceDisplayer(root, isoSurfaceGroup);
        
        isoSurfaceGroup.getChildren().add(Node);
        
        canvas.setScene(scene);
        shell.open();
        
    	while (!shell.isDisposed ()) {
    		
    		if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose();

	}
}
