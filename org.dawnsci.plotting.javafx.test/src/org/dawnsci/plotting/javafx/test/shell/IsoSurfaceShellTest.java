package org.dawnsci.plotting.javafx.test.shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SceneDisplayer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class IsoSurfaceShellTest {

	MarchingCubes algorithm;

	ILazyDataset dataset;
	MarchingCubesModel model;
	Surface testResult;
	IProgressMonitor monitor;

	private void loadDataset() throws Exception {

		IDataHolder dh = LoaderFactory.getData("files/brain.h5");
		dataset = dh.getLazyDataset("/entry/edf/data");

		model = new MarchingCubesModel(dataset, null, 1800, new int[] { 3, 3, 3 }, new int[] { 1, 1, 1 }, 1, "traceID");

		algorithm = new MarchingCubes(model);

		monitor = new IProgressMonitor() {
			@Override
			public void worked(int work) {
			}

			@Override
			public void subTask(String name) {
			}

			@Override
			public void setTaskName(String name) {
			}

			@Override
			public void setCanceled(boolean value) {
			}

			@Override
			public boolean isCanceled() {
				return false;
			}

			@Override
			public void internalWorked(double work) {
			}

			@Override
			public void done() {
			}

			@Override
			public void beginTask(String name, int totalWork) {
			}
		};

		// execute the algorithmA
		testResult = algorithm.execute(monitor);
	}

	private MeshView generateMesh() {

		MeshView result = new MeshView(
				createTrangleMesh(testResult.getPoints(), testResult.getTexCoords(), testResult.getFaces()));

		PhongMaterial material;
		Color color = new Color(Color.GOLDENROD.getRed(), Color.GOLDENROD.getGreen(), Color.GOLDENROD.getBlue(), 0.1);
		material = new PhongMaterial(color);

		result.setMaterial(material);

		result.setOpacity(0.1d);

		result.setDepthTest(DepthTest.ENABLE);

		return result;

	}

	private Mesh createTrangleMesh(float[] points, float[] textCoords, int[] faces) {

		final TriangleMesh mesh = new TriangleMesh();

		if (points != null && textCoords != null && faces != null) {
			mesh.getPoints().setAll(points);
			mesh.getTexCoords().setAll(textCoords);
			mesh.getFaces().setAll(faces);
		}

		return mesh;
	}

	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception {

		loadDataset();
		MeshView mesh = generateMesh();

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
//		Group root = new Group();
		Group isoSurfaceGroup = new Group();

		Scene scene = new SceneDisplayer(isoSurfaceGroup);

		isoSurfaceGroup.getChildren().add(mesh);

		PhongMaterial mat = new PhongMaterial(new Color(1, 0, 0, 0.5));
		mesh.setMaterial(mat);
		
		canvas.setScene(scene);
		
		shell.open();
		
		TimeUnit.SECONDS.sleep(5);
		shell.close();
		display.dispose();

	}
}
