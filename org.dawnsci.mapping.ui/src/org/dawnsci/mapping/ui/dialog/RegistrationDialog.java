package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.mapping.ui.MappingUtils;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LinearAlgebra;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RegistrationDialog extends Dialog {

	private IDataset image;
	private IDataset map;
	private IDataset registered;
	private MappedDataArea area;
	private IPlottingSystem systemImage;
	private IPlottingSystem systemMap;
	private IPlottingSystem systemComposite;
	private IRegion[] mapPoints = new IRegion[3];
	private IRegion[] imagePoints = new IRegion[3];
	int count = 0;
	
	public RegistrationDialog(Shell parentShell, IDataset map, IDataset image) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.map = map;
		this.image = image.getSliceView();
		this.image.clearMetadata(AxesMetadata.class);
		
		try {
			systemMap = PlottingFactory.createPlottingSystem();
			systemImage = PlottingFactory.createPlottingSystem();
			systemComposite = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		container.setLayout(new GridLayout(1, true));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite main = new Composite(container, SWT.FILL);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(3, true));
		ActionBarWrapper actionBarMap = ActionBarWrapper.createActionBars(main, null);
		ActionBarWrapper actionBarImage = ActionBarWrapper.createActionBars(main, null);
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(main, null);
		systemMap.createPlotPart(main, "Map Plot", actionBarMap, PlotType.IMAGE, null);
		systemMap.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		systemMap.setTitle("Map");
		systemMap.setShowIntensity(false);
		systemMap.getSelectedXAxis().setVisible(false);
		systemMap.getSelectedYAxis().setVisible(false);

		
		systemImage.createPlotPart(main, "Image Plot", actionBarImage, PlotType.IMAGE, null);
		systemImage.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		systemImage.setTitle("Image");
		systemImage.setShowIntensity(false);
		systemImage.getSelectedXAxis().setVisible(false);
		systemImage.getSelectedYAxis().setVisible(false);

		
		systemComposite.createPlotPart(main, "Composite Plot", actionBarComposite, PlotType.IMAGE, null);
		systemComposite.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		try {
			MappingUtils.plotDataWithMetadata(map, systemMap, null);
//			((IPaletteTrace)systemMap.getTraces(IImageTrace.class).iterator().next()).setPalette("Gray Scale");
			image.setName("Image");
			MappingUtils.plotDataWithMetadata(image, systemImage, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		doInitialMapping();
		
		return container;
	}
	
	private void doInitialMapping(){
		
		IDataset[] ax = MappingUtils.getAxesFromMetadata(map);
		
		Assert.isNotNull(ax);
		double mapX = map.getShape()[1];
		double mapY =  map.getShape()[0];
		
		double imX = image.getShape()[1];
		double imY =  image.getShape()[0];
		
		double[] xValsMap = new double[]{mapX/3., mapX/2., mapX-mapX/3};
		double[] yValsMap = new double[]{mapY/3., mapY-mapY/3,mapY/3.};
		double[] xValsImage = new double[]{imX/3., imX/2., imX-imX/3};
		double[] yValsImage = new double[]{imY/3., imY-imY/3,imY/3.};
		
		
		
		try {
			
			for (int i = 0; i < 3; i++) {
				
				Color c = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				if (i == 1) c = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
				if (i == 2) c = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
				
				final IRegion point1 = systemMap.createRegion("Point" +i, RegionType.POINT);
				point1.setRegionColor(c);
				mapPoints[i] = point1;
				point1.setROI(new PointROI(xValsMap[i],yValsMap[i]));
				point1.addROIListener(new IROIListener() {
					
					@Override
					public void roiSelected(ROIEvent evt) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void roiDragged(ROIEvent evt) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void roiChanged(ROIEvent evt) {
						update();
					}
				});
				systemMap.addRegion(point1);
			}
			
			for (int i = 0; i < 3; i++) {
				Color c = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				if (i == 1) c = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
				if (i == 2) c = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
				
				final IRegion point1 = systemImage.createRegion("Point" +i, RegionType.POINT);
				point1.setRegionColor(c);
				imagePoints[i] = point1;
				point1.setROI(new PointROI(xValsImage[i],yValsImage[i]));
				point1.addROIListener(new IROIListener() {
					
					@Override
					public void roiSelected(ROIEvent evt) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void roiDragged(ROIEvent evt) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void roiChanged(ROIEvent evt) {
						update();
					}
				});
				systemImage.addRegion(point1);
			}
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		update();
		
		
	}
	
	public IDataset getRegisteredImage(){
		return registered;
		
	}
	
	private void update() {
		Dataset v = buildDataset(mapPoints);
		Dataset x = buildDataset(imagePoints);
		Dataset trans = LinearAlgebra.solve(x, v);

		double tX = trans.getDouble(2,0);
		double tY = trans.getDouble(2,1);
		
		double sX = Math.hypot(trans.getDouble(0,0), trans.getDouble(0,1));
		double sY = Math.hypot(trans.getDouble(1,0), trans.getDouble(1,1));
		
		double r = Math.toDegrees(Math.atan(trans.getDouble(0,1)/trans.getDouble(1,1)));
		
		int[] shape = image.getShape();
		
		Dataset xR = DatasetFactory.createRange(shape[1], Dataset.FLOAT64);
		Dataset yR = DatasetFactory.createRange(shape[0], Dataset.FLOAT64);
		
		xR.imultiply(sX);
		xR.iadd(tX);
		
		yR.imultiply(sY);
		yR.iadd(tY);
		
		IDataset im = ((RGBDataset)image).getSliceView();
		registered = image.getSliceView();
		AxesMetadataImpl ax = new AxesMetadataImpl(2);
		ax.addAxis(0, yR);
		ax.addAxis(1, xR);
		im.addMetadata(ax);
		registered.addMetadata(ax);
		systemComposite.reset();

		ICompositeTrace comp = this.systemComposite.createCompositeTrace("composite1"+count++);

		comp.add(MappingUtils.buildTrace(im, systemComposite),0);
		IImageTrace buildTrace = MappingUtils.buildTrace(map, systemComposite);
		buildTrace.setAlpha(90);
		comp.add(MappingUtils.buildTrace(map, systemComposite,120),1);
		systemComposite.addTrace(comp);
		
	}
	
	private Dataset buildDataset(IRegion[] regions) {


		try {
			Dataset mat = DatasetFactory.ones(new int[]{3, 3},Dataset.FLOAT64);
			int[] pos = new int[2];

			for (int i = 0; i < 3 ; i++) {
				pos[0] = i;
				pos[1] = 0;
				double[] val;
				val = regions[i].getCoordinateSystem().getValueAxisLocation(regions[i].getROI().getPoint());
				mat.set(val[0], pos);
				pos[1] = 1;
				mat.set(val[1], pos);
			}
			return mat;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return null;

	}
	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Image Registration");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

}
