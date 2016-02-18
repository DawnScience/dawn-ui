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
	}
	
	
	
	private Group generateNode(IDataset dataset)
	{		
		return new VolumeRender(dataset.getShape(), dataset);
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
                
        scene.addVolumeTrace(group);
        
        canvas.setScene(scene);
        shell.open();
        
    	while (!shell.isDisposed ()) {
    		
    		if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose();

	}
}
