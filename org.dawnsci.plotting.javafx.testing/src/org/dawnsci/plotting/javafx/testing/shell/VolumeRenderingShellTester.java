package org.dawnsci.plotting.javafx.testing.shell;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.dawnsci.plotting.javafx.axis.objects.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class VolumeRenderingShellTester {
	
	MarchingCubes algorithm;
	
	IDataset dataset;
	MarchingCubesModel model;
	Surface testResult;
	
	double max, min;
	
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
		dataset = dh.getLazyDataset("/entry/edf/data").getSlice();
		
		algorithm = new MarchingCubes();
				
		model = new MarchingCubesModel();
		model.setLazyData(dataset);
		model.setBoxSize(new int[]{3,3,3});
		model.setIsovalue(1800d);
		model.setVertexLimit(Integer.MAX_VALUE);
		
		algorithm.setModel(model);
		
		max = dataset.max(true,true).doubleValue();
		
		System.out.println("max = " + max );
		
		// execute the algorithmA	
		testResult = algorithm.execute(null, null);
	}
		
	private Group generateNode(IDataset dataset)
	{		
		Group results = new Group();
		
		for (int z = 0; z < dataset.getShape()[2]; z+=2)
		{
			BufferedImage bi = new BufferedImage(dataset.getShape()[0], dataset.getShape()[1],BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < dataset.getShape()[1]; y++)
			{
				for (int x = 0; x < dataset.getShape()[0]; x++)
				{
					int argb = dataset.getInt(x,y,z);					
					
					if (dataset.getInt(x,y,z) > 1800)
					{
						int rgb = 7;
						rgb = (rgb << 8) + 255;
						rgb = (rgb << 8) + 0;
						rgb = (rgb << 8) + 0;
						
						argb = rgb;
					}
					else
						argb = 0;
					
					bi.setRGB(x, y, argb);
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(dataset.getShape()[0], dataset.getShape()[1]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(-z);
			xygroup.getChildren().add(newPlane);
		}
		
		for (int z = 0; z < dataset.getShape()[0]; z+=2)
		{
			BufferedImage bi = new BufferedImage(dataset.getShape()[2], dataset.getShape()[1],BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < dataset.getShape()[1]; y++)
			{
				for (int x = 0; x < dataset.getShape()[2]; x++)
				{
					int argb = dataset.getInt(z,y,x);
					
					if (dataset.getInt(z,y,x) > 1800)
					{
						int rgb = 7;
						rgb = (rgb << 8) + 255;
						rgb = (rgb << 8) + 0;
						rgb = (rgb << 8) + 0;
						
						argb = rgb;
					}
					else
						argb = 0;
					
					bi.setRGB(x, y, argb);
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(dataset.getShape()[2], dataset.getShape()[1]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			zygroup.getChildren().add(newPlane);
		}
		zygroup.getTransforms().addAll(
				new Rotate(90, new Point3D(0, 1, 0)));
		
		
		for (int z = 0; z < dataset.getShape()[1]; z+=2)
		{
			BufferedImage bi = new BufferedImage(dataset.getShape()[0], dataset.getShape()[2],BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < dataset.getShape()[2]; y++)
			{
				for (int x = 0; x < dataset.getShape()[0]; x++)
				{
					int argb = dataset.getInt(x,z,y);
//					int argb = dataset[x][z][y];
					
					if (dataset.getInt(x,z,y) > 1800)
					{
						int rgb = 7;
						rgb = (rgb << 8) + 255;
						rgb = (rgb << 8) + 0;
						rgb = (rgb << 8) + 0;
						argb = rgb;
					}
					else
						argb = 0;
					
					bi.setRGB(x, y, argb);
					
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(dataset.getShape()[0], dataset.getShape()[2]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			xzgroup.getChildren().add(newPlane);
		}
		xzgroup.getTransforms().addAll(
				new Rotate(-90, new Point3D(1, 0, 0)),
				new Translate(0,0,0));
		
		results.getChildren().addAll(xygroup, zygroup, xzgroup);
		
		generateRotateEvent(results);
		
		return results;
	}
	
	private MeshView generateMesh()
	{
		
		MeshView result = new MeshView(createTrangleMesh(
												testResult.getPoints(),
												testResult.getTexCoords(),
												testResult.getFaces()));
		
		PhongMaterial material;
		Color color = new Color(Color.GOLDENROD.getRed(), Color.GOLDENROD.getGreen(), Color.GOLDENROD.getBlue(), 0.1);
		material = new PhongMaterial(color);
		
		result.setMaterial(material);
		
		result.setOpacity(0.1d);
		
		result.setDepthTest(DepthTest.ENABLE);
		
		return result;
		
	}
	
	private Mesh createTrangleMesh(
						float[] points,
						float[] textCoords,
						int[] faces)
	{
		
		final TriangleMesh mesh = new TriangleMesh();
		
		if (points != null && textCoords != null && faces != null)
		{
			mesh.getPoints().setAll(points);
			mesh.getTexCoords().setAll(textCoords);
			mesh.getFaces().setAll(faces);
		}
		
		return mesh;
	}
	
	
	private void generateRotateEvent(Node node) {
		
		xygroup.setDepthTest(DepthTest.DISABLE);
		zygroup.setDepthTest(DepthTest.DISABLE);
		xzgroup.setDepthTest(DepthTest.DISABLE);
		
		node.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {

			Point3D xAngle = new Point3D(1, 0, 0);

			Point3D angles = Vector3DUtil.extractEulerAnglersFromMatrix(newT);
					
			textXAxisRotate.setAngle(-angles.getX());
			textYAxisRotate.setAngle(-angles.getY());
			textZAxisRotate.setAngle(-angles.getZ());
			
			xAngle = textXAxisRotate.transform(xAngle);
			xAngle = textYAxisRotate.transform(xAngle);
			xAngle = textZAxisRotate.transform(xAngle);
			
//			xygroup.setOpacity(Math.abs(xAngle.getX()));
//	        zygroup.setOpacity(Math.abs(xAngle.getZ()));
//	        xzgroup.setOpacity(Math.abs(xAngle.getY()));
	      
			
//	        for (Node n : xygroup.getChildren())
//	        	if (n instanceof AxisAlignedPlane)
//	        		((AxisAlignedPlane)n).setMaterialOpacity(Math.abs(xAngle.getX()));
//	        
//	        for (Node n : zygroup.getChildren())
//	        	if (n instanceof AxisAlignedPlane)
//	        		((AxisAlignedPlane)n).setMaterialOpacity(Math.abs(xAngle.getZ()));
//	        
//	        for (Node n : zygroup.getChildren())
//	        	if (n instanceof AxisAlignedPlane)
//	        		((AxisAlignedPlane)n).setMaterialOpacity(Math.abs(xAngle.getY()));
//	        
		});
	}

	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception
	{
		
		loadDataset();
		Group group = generateNode(dataset);
		Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        
        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        Group root = new Group();
        Group isoSurfaceGroup = new Group();
        
        SurfaceDisplayer scene = new SurfaceDisplayer(root, isoSurfaceGroup);
        
		MeshView isosurface = generateMesh();
		PhongMaterial mat = new PhongMaterial(new Color(1 ,0 ,0 ,0.5 ));
		isosurface.setMaterial(mat);
        
//        AmbientLight al = new AmbientLight(new Color(0,0,1,1));
//        al.getScope().addAll(xygroup, zygroup, xzgroup);
//        group.getChildren().add(al);
        
        isoSurfaceGroup.getChildren().addAll(isosurface);
        
        scene.addVolumeTrace(group);
        
        canvas.setScene(scene);
        shell.open();
        
    	while (!shell.isDisposed ()) {
    		
    		if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose();

	}
}
