package org.dawnsci.plotting.javafx.testing.shell;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
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
	
	
	
	private Node generateNode()
	{		
		Group group = new Group();
		IDataset data = dataset.getSlice();
		
		for (int z = 0; z < 100; z++)
		{
			BufferedImage bi = new BufferedImage(dataset.getShape()[0], dataset.getShape()[1],BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < dataset.getShape()[1]; y++)
			{
				for (int x = 0; x < dataset.getShape()[0]; x++)
				{
					int argb = 255;
					argb = (argb << 8) + 0;
					argb = (argb << 8) + 255;
					argb = (argb << 8) + 0;
					bi.setRGB(x, y, argb);
				}
			}
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0), 
					new Point2D(100, 100), 
					new Image(getClass().getResourceAsStream("unnamed.png")));
			newPlane.setTranslateZ(z);
			group.getChildren().add(newPlane);
		}
		
		
		return group;
	}
		
	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception
	{
		
		loadDataset();
		Node Node = generateNode();
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
