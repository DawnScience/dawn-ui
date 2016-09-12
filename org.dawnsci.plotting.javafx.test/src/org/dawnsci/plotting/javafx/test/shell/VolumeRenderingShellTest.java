package org.dawnsci.plotting.javafx.test.shell;

import javafx.embed.swt.FXCanvas;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.plotting.javafx.SceneDisplayer;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class VolumeRenderingShellTest {

	MarchingCubes algorithm;

	IDataset dataset;
	MarchingCubesModel model;
	Surface testResult;

	double max, min;

	Group xygroup = new Group();
	Group zygroup = new Group();
	Group xzgroup = new Group();

	private Rotate textXAxisRotate = new Rotate();
	{
		textXAxisRotate.setAxis(new Point3D(1, 0, 0));
	}
	private Rotate textYAxisRotate = new Rotate();
	{
		textYAxisRotate.setAxis(new Point3D(0, 1, 0));
	}
	private Rotate textZAxisRotate = new Rotate();
	{
		textZAxisRotate.setAxis(new Point3D(0, 0, 1));
	}

	private void loadDataset() throws Exception {
		IDataHolder dh = LoaderFactory.getData("files/brain.h5");
		dataset = dh.getLazyDataset("/entry/edf/data").getSlice();
	}

	// simply creates a shell to check javafx fxcanvas still functions
	@Test
	public void shellButtonTest() throws Exception {
		loadDataset();
		// Group group = generateNode(dataset);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
		Group root = new Group();

		SceneDisplayer scene = new SceneDisplayer(root);

//		 scene.addVolume/Trace(group);

		canvas.setScene(scene);
		shell.open();
		
		TimeUnit.SECONDS.sleep(5);
		shell.close();
		display.dispose();

	}
}
