package org.dawnsci.plotting.javafx.testing.shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class isoSurfaceShellTester {
	
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
	
	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception
	{
		
		loadDataset();
		MeshView mesh = generateMesh();
		
		Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        
        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        Group root = new Group();
        Group isoSurfaceGroup = new Group();
        
        
        Scene scene = new SurfaceDisplayer(root, isoSurfaceGroup);
        
        isoSurfaceGroup.getChildren().add(mesh);
        
        PhongMaterial mat = new PhongMaterial(new Color(1 ,0 ,0 ,0.5 ));
        mesh.setMaterial(mat);
        
        canvas.setScene(scene);
        shell.open();
        
    	while (!shell.isDisposed ()) {
    		
    		if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose();

	}
}
