package org.dawnsci.plotting.javafx.trace;

import java.util.List;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * 
 * @author fcp94556
 *
 * @Internal
 */
public class FXIsosurfaceTrace extends Image3DTrace implements IIsosurfaceTrace {

	private FXCanvas           canvas;
	private Dataset            points;
	private Dataset            textCoords;
	private Dataset            faces;
	private SurfaceDisplayer   scene;
	private CullFace cullFace = CullFace.NONE;
	private int[] rgb;
	private double opacity=1.0;
	
	public FXIsosurfaceTrace(IPlottingSystemViewer viewer, 	FXCanvas canvas, String traceName) {
		super(viewer, traceName);
		this.canvas = canvas;
	}
	
	@Override
	public IDataset getData() {
		return points;
	}

	@Override
	public void setData(IDataset points, IDataset textCoords, IDataset faces, List<? extends IDataset> axes) {
		
		this.points     = (Dataset)points;
		this.textCoords = (Dataset)textCoords;
		this.faces      = (Dataset)faces;
		this.axes       = (List<IDataset>)axes;
		
		if (Platform.isFxApplicationThread()) {
			update();
		} else {
			Platform.runLater(new Runnable() {			
				public void run() {			
					try {
						update();
					} catch (OutOfMemoryError e){
						e.printStackTrace();
						showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
					}
				};
			});
		}
	}

	private void showErrorMessage(final String title, final String message) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	/**
	 * Internal use only.
	 */
	public void create() {
		if (Platform.isFxApplicationThread()) {
			createInternal();
		} else {
			Platform.runLater(new Runnable() {			
				public void run() {			
					try {
						createInternal();
					} catch (OutOfMemoryError e){
						e.printStackTrace();
						showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
					}
				};
			});
		}
	}
	
	private void createInternal() {
		
		Group    root   = new Group();
		MeshView result = new MeshView(createTrangleMesh());
		result.setCursor(Cursor.CROSSHAIR);

		Material material;
		if (rgb == null) {
			material = new PhongMaterial(Color.GOLDENROD);
		} else {
			Color color = Color.rgb(rgb[0], rgb[1], rgb[2], opacity);
			material = new PhongMaterial(color); 
		}

		scene = new SurfaceDisplayer(root, result, material, toJavaFX());

		canvas.setScene(scene);
	}

	private javafx.scene.shape.CullFace toJavaFX() {
		switch(cullFace) {
		case NONE:
			return  javafx.scene.shape.CullFace.NONE;
		case BACK:
			return  javafx.scene.shape.CullFace.BACK;
		case FRONT:
			return  javafx.scene.shape.CullFace.FRONT;
		default:
			return javafx.scene.shape.CullFace.NONE;
		}
	}

	private void update() {
		if (scene==null) {
			return;
		} else {
			scene.updateTransforms();
			TriangleMesh mesh = (TriangleMesh)scene.getIsosurface().getMesh();
			marry(mesh);
			
			canvas.redraw();
		}			
	}

	private Mesh createTrangleMesh() {
		final TriangleMesh mesh = new TriangleMesh();
		marry(mesh);
		return mesh;
	}

	private void marry(TriangleMesh mesh) {
		mesh.getPoints().setAll((float[])points.getBuffer());
		mesh.getTexCoords().setAll((float[])textCoords.getBuffer());
		mesh.getFaces().setAll((int[])faces.getBuffer());
	}

	@Override
	public CullFace getCullFace() {
		return cullFace;
	}

	@Override
	public void setCullFace(CullFace culling) {
        this.cullFace = culling;
	}

	@Override
	public int[] getMaterialRBG() {
		return rgb;
	}

	@Override
	public double getMaterialOpacity() {
		return opacity;
	}

	@Override
	public void setMaterial(int red, int green, int blue, double opacity) {
		if (scene!=null) throw new RuntimeException("Changing the material after the surface is created is not implemented yet!");
		this.rgb     = new int[]{red, green, blue};
		this.opacity = opacity;
	}

}
