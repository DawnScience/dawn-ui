package org.dawnsci.plotting.javafx.volume;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;

public class VolumeRender extends Group
{
	@SuppressWarnings("unused")
	private ILazyDataset lazySlice;
	
	private double maxValue;
	private double minValue;

	private double maxCulling;
	private double minCulling;
	
	private double opacity;
	private double intensity;
	
	private Group xygroup;
	private Group zygroup;
	private Group zxgroup;
	
	/**
	 * Create a volume render from a dataset.
	 * @param size - the size of the volume
	 * @param dataset - the dataset to be rendered
	 * @param intensityValue - the intensity of each values colour
	 */
	public VolumeRender()
	{
		super();
		
		xygroup = new Group();
		zygroup = new Group();
		zxgroup = new Group();
	}
	
	/**
	 * Create a volume render from buffered image planes
	 * @param xyPlanes - list of xy images
	 * @param yzPlanes - list of yz images
	 * @param zxPlanes - list of xz images
	 * @param xyzObjectSize - the size of the volume render (int[3])
	 */
	
	/*
	 * privates 
	 */
	private void initialise()
	{
		zygroup.getTransforms().addAll(
				new Rotate(120, new Point3D(1, 1, 1)));
		
		zxgroup.getTransforms().addAll(
				new Rotate(-120, new Point3D(1, 1, 1)));
		
		xygroup.setDepthTest(DepthTest.DISABLE);
		zygroup.setDepthTest(DepthTest.DISABLE);
		zxgroup.setDepthTest(DepthTest.DISABLE);
		
		this.getChildren().addAll(xygroup, zygroup, zxgroup);
		
		
	}
	
	private Group createPlanesFromDataSlice(
			final int[]XYZSize, 
			final PaletteService paletteService, 
			final ILazyDataset lazySlice) 
	{
		Group outputGroup = new Group();
				
//		FunctionContainer functionContainer = paletteService.getFunctionContainer("Viridis (blue-green-yellow)");
		FunctionContainer functionContainer = paletteService.getFunctionContainer("Plasma (blue-red-yellow)");
//		FunctionContainer functionContainer = paletteService.getFunctionContainer("Gray Scale");
		
		functionContainer.setInverseRed(true);
		functionContainer.setInverseGreen(true);
		functionContainer.setInverseBlue(true);
		
		LayeredImageTexture textureData = new LayeredImageTexture(
				lazySlice,
				maxValue,
				minValue,
				maxCulling,
				minCulling,
				intensity,
				functionContainer);
		
		LayeredPlaneMesh newPlane = new LayeredPlaneMesh(
				new Point3D(XYZSize[0], XYZSize[1], XYZSize[2]),
				new Point2D(lazySlice.getShape()[0], lazySlice.getShape()[1]),
				textureData,
				new Point3D(0, 0, 1));
		
		double xOffset = (double)((XYZSize[0] / (double)lazySlice.getShape()[0]) / 2);
		double yOffset = (double)((XYZSize[1] / (double)lazySlice.getShape()[1]) / 2);
				
		newPlane.setTranslateX(-xOffset);
		newPlane.setTranslateY(-yOffset);
		
		newPlane.setMaxOpacity(opacity);
		newPlane.setOpacity_Material(1);
		
		outputGroup.getChildren().add(newPlane);
		
		outputGroup.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Rotate worldRotate = Vector3DUtil.matrixToRotate(newT);
			
			Point3D zVector = new Point3D(0, 0, 1);
			try 
			{
				zVector = worldRotate.createInverse().transform(zVector);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			double zAngle = zVector.angle( new Point3D(0, 0, 1));
						
			double opacity = Math.abs(Math.cos(Math.toRadians(zAngle)));
			
			if (opacity < 0.3)
			{
				opacity *= (opacity*2);
				if (opacity < 0.05)
					opacity = 0;
			}
				
			newPlane.setOpacity_Material(Math.abs(opacity));
			newPlane.setColour(new Color(opacity, opacity, opacity, 1));
			
        });
		
		return outputGroup;
	}
		
	private void setGroupColour(Color colour, Group group)
	{
		for (Node n : group.getChildren())
		{
			if (n instanceof LayeredPlaneMesh)
			{
				((LayeredPlaneMesh)n).setColour(colour);
			}
		}
	}
	
	private void setGroupOpacity(double opacity, Group group)
	{
		
		for (Node n : group.getChildren())
		{
			if (n instanceof LayeredPlaneMesh)
			{
				((LayeredPlaneMesh)n).setOpacity_Material(opacity);
			}
		}
	}
	
	/* 
	 * publics
	 * 
	 */
	/**
	 * 
	 * @param size
	 * @param dataset
	 * @param intensity
	 * @param opacity
	 * @param paletteService
	 */
	public void compute(
			final int[] size, 
			final ILazyDataset dataset, 
			final double intensity, 
			final double opacity,
			final PaletteService paletteService,
			final double[] minMaxValue,
			final double[] minMaxCulling)
	{

		this.minValue = minMaxValue[0];
		this.maxValue = minMaxValue[1];

		this.minCulling = minMaxCulling[0];
		this.maxCulling = minMaxCulling[1];
				
		this.intensity = intensity;
		this.opacity =  Math.pow(opacity, 3);
			
		System.out.println(this.opacity);
		
		xygroup.getChildren().clear(); 
		zygroup.getChildren().clear(); 
		zxgroup.getChildren().clear(); 
				
		// generate the planes
		xygroup = createPlanesFromDataSlice(new int[]{size[0], size[1], size[2]}, paletteService, dataset);
		zygroup = createPlanesFromDataSlice(new int[]{size[1], size[2], size[0]}, paletteService, dataset.getTransposedView(1,2,0).getSlice());
		zxgroup = createPlanesFromDataSlice(new int[]{size[2], size[0], size[1]}, paletteService, dataset.getTransposedView(2,0,1).getSlice());
		
		this.setTranslateX((size[0] / dataset.getShape()[0])/2);
		this.setTranslateY((size[1] / dataset.getShape()[1])/2);
		this.setTranslateZ((size[2] / dataset.getShape()[2])/2);
				
		initialise();
				
	}
	
	/**
	 * sets the colour of the volume. IGNORES THE OPACITY.
	 * @param colour - the new colour
	 */
	public void setColour(Color colour)
	{
		setGroupColour(colour, xygroup);
		setGroupColour(colour, zygroup);
		setGroupColour(colour, zxgroup);
	}
	
	/**
	 * sets the opacity of the volume.
	 * @param opacity - the new opacity
	 */
	public void setOpacity_Matrial(double opacity)
	{
		setGroupOpacity(opacity, xygroup);
		setGroupOpacity(opacity, zygroup);
		setGroupOpacity(opacity, zxgroup);
	}
	
}


