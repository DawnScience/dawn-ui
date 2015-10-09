/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import java.util.List;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.DepthTest;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;	
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author fcp94556
 *
 * @Internal
 */
public class FXIsosurfaceTrace extends Image3DTrace implements IIsosurfaceTrace
{
	private MeshView isosurface;
	private Dataset points;
	private Dataset textCoords;
	private Dataset faces;
	
	// !! circular dependency
	private SurfaceDisplayer scene;
	
	private CullFace cullFace = CullFace.NONE;
	private int[] rgb;
	private double opacity = 0.5;
	
	public FXIsosurfaceTrace(IPlottingSystemViewer viewer, SurfaceDisplayer newScene , String traceName)
	{
		super(viewer, traceName);
		this.scene = newScene;
	}
	
	@Override
	public IDataset getData()
	{
		return points;
	}
	
	public void dispose() {
        scene.removeSurface(isosurface);
        super.dispose();
        // remove from scene
	}
	
	@Override
	public void setData(IDataset points, IDataset textCoords, IDataset faces, List<? extends IDataset> axes)
	{
		this.points = (Dataset) points;
		this.textCoords = (Dataset) textCoords;
		this.faces = (Dataset) faces;
		this.axes = (List<IDataset>) axes; // !! add stuff
				
		if (Platform.isFxApplicationThread())
		{
			update();
		}
		else
		{
			Platform.runLater(new Runnable()
			{
				public void run()
				{
					try
					{
						update();
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
						showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
					}
				};
			});
		}
	}
	
	private void showErrorMessage(final String title, final String message)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}
	
	/**
	 * Internal use only.
	 */
	public void create()
	{
		if (Platform.isFxApplicationThread())
		{
			createInternal();
		}
		else
		{
			Platform.runLater(new Runnable()
			{
				public void run()
				{
					try
					{
						createInternal();
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
						showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
					}
				};
			});
		}
	}
	
	private void createInternal()
	{
		MeshView result = new MeshView(createTrangleMesh());
		result.setCursor(Cursor.CROSSHAIR);
		result.setOpacity(opacity);
		
		result.setCullFace(javafx.scene.shape.CullFace.BACK);
		result.setDrawMode(DrawMode.FILL);
		
		
		PhongMaterial material;
		if (rgb == null)
		{
			Color color = new Color(Color.GOLDENROD.getRed(), Color.GOLDENROD.getGreen(), Color.GOLDENROD.getBlue(), opacity);
			material = new PhongMaterial(color);
		}
		else
		{
			Color color = Color.rgb(rgb[0], rgb[1], rgb[2], opacity);
			material = new PhongMaterial(color);
		}
				
		// check if it's needed to remove the depth test
		// 0.99 is arbitrary and will require more testing to determine a better value
		// used to determine whether the object is transparent and :. will need depthTest disabled
		if (opacity > 0.95)
		{
			result.setDepthTest(DepthTest.ENABLE);
		}
		else
		{
			result.setDepthTest(DepthTest.DISABLE);
		}
		
//		Image whiteImage = new Image(getClass().getResourceAsStream("org.dawnsci.plotting.javafx/icons/white.png"));
//		
//		material.setDiffuseMap(whiteImage);
		
		result.setDepthTest(DepthTest.ENABLE);
		
		result.setMaterial(material);
		this.isosurface = result;
	}
	
//	private javafx.scene.shape.CullFace toJavaFX()
//	{
//		switch (cullFace)
//		{
//			case NONE:
//				return javafx.scene.shape.CullFace.NONE;
//			case BACK:
//				return javafx.scene.shape.CullFace.BACK;
//			case FRONT:
//				return javafx.scene.shape.CullFace.FRONT;
//			default:
//				return javafx.scene.shape.CullFace.NONE;
//		}
//	}
	
	private void update()
	{
		PhongMaterial material;
		isosurface.setOpacity(opacity);
		if (rgb == null)
		{
			Color color = new Color(Color.GOLDENROD.getRed(), Color.GOLDENROD.getGreen(), Color.GOLDENROD.getBlue(), opacity);
			material = new PhongMaterial(color);
		}
		else
		{
			Color color = Color.rgb(rgb[0], rgb[1], rgb[2], opacity);
			material = new PhongMaterial(color);
		}
		
		
		
		isosurface.setMaterial(material);
		TriangleMesh mesh = (TriangleMesh) isosurface.getMesh();
		
		if (opacity > 0.95)
		{
			isosurface.setDepthTest(DepthTest.ENABLE);
		}
		else
		{
			isosurface.setDepthTest(DepthTest.DISABLE);
		}
		
		isosurface.setDepthTest(DepthTest.ENABLE);
		
		marry(mesh);
		
		scene.updateTransforms();
	}
	
	private Mesh createTrangleMesh()
	{
		final TriangleMesh mesh = new TriangleMesh();
		marry(mesh);
		
		return mesh;
	}
	
	private void marry(TriangleMesh mesh)
	{
		mesh.getPoints().setAll((float[]) points.getBuffer());
		mesh.getTexCoords().setAll((float[]) textCoords.getBuffer());
		mesh.getFaces().setAll((int[]) faces.getBuffer());
		
	}
	
	@Override
	public CullFace getCullFace()
	{
		return cullFace;
	}
	
	@Override
	public void setCullFace(CullFace culling)
	{
		this.cullFace = culling;
	}
	
	@Override
	public int[] getMaterialRBG()
	{
		return rgb;
	}
	
	@Override
	public double getMaterialOpacity()
	{
		return opacity;
	}
	
	@Override
	public void setMaterial(int red, int green, int blue, double opacity)
	{
		this.rgb = new int[] { red, green, blue };
		this.opacity = opacity;
	}
	
	@Override
	public void setPalette(String paletteName)
	{
		// TODO Auto-generated method stub
	}

	public MeshView getIsoSurface()
	{
		return this.isosurface; 
	}
		
}


