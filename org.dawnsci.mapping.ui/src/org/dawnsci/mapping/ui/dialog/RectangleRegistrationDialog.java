package org.dawnsci.mapping.ui.dialog;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.dataset.impl.function.MapToRotatedCartesian;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LinearAlgebra;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RectangleRegistrationDialog extends Dialog {
	private IDataset image;
	private IDataset map;
	private IDataset registered;
	private IPlottingSystem<Composite> systemMap;
	private IPlottingSystem<Composite> systemImage;
	private IPlottingSystem<Composite> systemComposite;
	private IRegion box;
	private IRegion[] mapPoints = new IRegion[4];
	private IRegion[] imagePoints = new IRegion[4];
	int count = 0;
	private Scale scale;
	private Button method;
	
	private final static Logger logger = LoggerFactory.getLogger(RectangleRegistrationDialog.class);
	
	public RectangleRegistrationDialog(Shell parentShell, IDataset map, IDataset image) {
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
			logger.error("Could not create plotting systems!", e);
		}
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		container.setLayout(new GridLayout(1, true));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite c = new Composite(container, SWT.None);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout());
		final SashForm main = new SashForm(c, SWT.None);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(3, true));
		createPlotAndActionBar(systemMap, main, "Map Plot");
		systemMap.setTitle("Map");
		systemMap.setShowIntensity(false);
		systemMap.getSelectedXAxis().setVisible(false);
		systemMap.getSelectedYAxis().setVisible(false);
		
		
		createPlotAndActionBar(systemImage, main, "Image Plot");
		
//		systemImage.createPlotPart(main, "Image Plot", actionBarImage, PlotType.IMAGE, null);
//		systemImage.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		systemImage.setTitle("Image");
		systemImage.setShowIntensity(false);
		systemImage.getSelectedXAxis().setVisible(false);
		systemImage.getSelectedYAxis().setVisible(false);

		
		Composite c1 = createPlotAndActionBar(systemComposite, main, "Composite Plot");
		scale = new Scale(c1, SWT.None);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		scale.setMinimum(20);
		scale.setMaximum(255);
		scale.setSelection(255);
		scale.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IImageTrace trace = (IImageTrace)systemComposite.getTrace("map");
				if (trace != null) trace.setAlpha(scale.getSelection());
				systemComposite.repaint();
			}
		});
//		systemComposite.createPlotPart(main, "Composite Plot", actionBarComposite, PlotType.IMAGE, null);
//		systemComposite.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		main.setWeights(new int[]{0,50,50});
		image.setName("Image");
		systemComposite.setShowIntensity(false);
		MetadataPlotUtils.plotDataWithMetadata(image, systemImage);
		MetadataPlotUtils.plotDataWithMetadata(map, systemMap);
		
		Composite buttons = new Composite(c, SWT.None);
		buttons.setLayout(new GridLayout(3, false));
		method = new Button(buttons, SWT.TOGGLE);
		method.setText("Rectangle");
		
		new Label(buttons, SWT.None).setText("Angle: ");
		final FloatSpinner angle = new FloatSpinner(buttons,SWT.NONE);
		angle.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		angle.setIncrement(0.1);
		angle.setDouble(0);
		angle.setPrecision(1);
		angle.setMaximum(360);
		angle.setMinimum(-360);
		angle.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (box != null){
					IROI roi = box.getROI();
					if (roi instanceof RectangularROI) {
						((RectangularROI)roi).setAngleDegrees(angle.getDouble());
						update();
					}
				}
				
			}
		});
		
		
		method.addSelectionListener(new SelectionAdapter() {

			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (method.getSelection()){
					method.setText("4-Point");
					main.setWeights(new int[]{30,30,40});
					switchMode(false);
					angle.setEnabled(false);
				} else {
					method.setText("Rectangle");
					main.setWeights(new int[]{0,50,50});
					switchMode(true);
					angle.setEnabled(true);
				}
				
			}
			
		});
		
		
		
		doInitialMappingPoints();
		doInitialMapping();
		switchMode(true);
		buttons.redraw();
		return container;
	}
	
	private Composite createPlotAndActionBar(IPlottingSystem<Composite> system, Composite comp, String name) {
		Composite c = new Composite(comp, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout());
		ActionBarWrapper actionBarImage = ActionBarWrapper.createActionBars(c, null);
		system.createPlotPart(c, name, actionBarImage, PlotType.IMAGE, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return c;
	}
	
	private void doInitialMapping(){
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(map);
		
		Assert.isNotNull(ax);

		try {
			box = systemImage.createRegion("Scan Region", RegionType.BOX);
			box.setROI(new RectangularROI(10, 0));
			box.addROIListener(new IROIListener.Stub() {


				@Override
				public void roiChanged(ROIEvent evt) {
					IROI roi = evt.getROI();
					sanitizeROI(roi, image.getShape());
					systemImage.repaint(false);
					
					RectangleRegistrationDialog.this.update();
				}
			});
			systemImage.addRegion(box);


			update();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	private void switchMode(boolean rectangle) {
		if (rectangle) {
			for (IRegion reg : imagePoints) {
				reg.setVisible(false);
			}
			box.setVisible(true);
			update();
		} else {
			box.setVisible(false);
			for (IRegion reg : imagePoints) {
				reg.setVisible(true);
			}
			updatePoints();
		}
	}
	
	private void doInitialMappingPoints(){
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(map);
		
		Assert.isNotNull(ax);
		double mapX = map.getShape()[1];
		double mapY =  map.getShape()[0];
		
		double imX = image.getShape()[1];
		double imY =  image.getShape()[0];
		
		double[] xValsMap = new double[]{mapX/3., mapX/2., mapX-mapX/3,mapX/2};
		double[] yValsMap = new double[]{mapY/3., mapY-mapY/3,mapY/3.,mapY/2};
		double[] xValsImage = new double[]{imX/3., imX/2., imX-imX/3,imX/2};
		double[] yValsImage = new double[]{imY/3., imY-imY/3,imY/3.,imY/2};
		
		try {
			for (int i = 0; i < 4; i++) {

				Color c = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				if (i == 1) c = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
				if (i == 2) c = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
				if (i == 3) c = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);

				final IRegion point1 = systemMap.createRegion("Point" +i, RegionType.POINT);
				point1.setRegionColor(c);
				mapPoints[i] = point1;
				point1.setROI(new PointROI(xValsMap[i],yValsMap[i]));
				point1.addROIListener(new IROIListener.Stub() {


					@Override
					public void roiChanged(ROIEvent evt) {
						IROI roi = evt.getROI();
						sanitizeROI(roi, map.getShape());
						systemMap.repaint(false);

						RectangleRegistrationDialog.this.updatePoints();
					}
				});
				systemMap.addRegion(point1);
			}

			for (int i = 0; i < 4; i++) {
				Color c = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				if (i == 1) c = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
				if (i == 2) c = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
				if (i == 3) c = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);

				final IRegion point1 = systemImage.createRegion("Point" +i, RegionType.POINT);
				point1.setRegionColor(c);
				imagePoints[i] = point1;
				point1.setROI(new PointROI(xValsImage[i],yValsImage[i]));
				point1.addROIListener(new IROIListener.Stub() {

					@Override
					public void roiChanged(ROIEvent evt) {
						IROI roi = evt.getROI();
						sanitizeROI(roi, image.getShape());
						systemImage.repaint(false);
						
						RectangleRegistrationDialog.this.updatePoints();
					}
				});
				systemImage.addRegion(point1);
			}
		} catch (Exception e) {
			logger.error("Could not create Regions",e);
		}



		updatePoints();
		
		
	}
	
	public IDataset getRegisteredImage(){
		return registered;
		
	}
	
	private void sanitizeROI(IROI roi, int[] shape) {
		
		double[] point = roi.getPoint();
		
		
		if (point[0] >= shape[1]) point[0] = shape[1]-1;
		if (point[0] < 0) point[0] = 0;
		if (point[1] < 0) point[1] = 0;
		if (point[1] >= shape[0]) point[1] = shape[0]-1;
		
		roi.setPoint(point);
	}
	
	
	private void update() {
		Dataset v = buildDataset(map);//map
		Dataset x = buildDataset((RectangularROI)box.getROI());//image
		if (x == null || v == null) return;
		Dataset trans = LinearAlgebra.solveSVD(x, v);

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
		
		MapToRotatedCartesian mrc = new MapToRotatedCartesian(0, 0, shape[1], shape[0], r*-1);
		
		IDataset im;
		
		if (image instanceof RGBDataset) {
			
			RGBDataset rgb = (RGBDataset)image;
			im = DatasetUtils.createCompoundDataset(RGBDataset.class, mrc.value(rgb.getRedView()).get(0),
								mrc.value(rgb.getGreenView()).get(0),
								mrc.value(rgb.getBlueView()).get(0));
			
			
		} else {
			List<Dataset> value = mrc.value(image);
			im = value.get(0);
		}
		
		logger.debug("XOffset: {}, YOffset: {}, XScale {}, YScale {},",tX,tY,sX,sY);
		
		registered = im;
		try {
			AxesMetadata mmd = map.getFirstMetadata(AxesMetadata.class);
			String n0 = mmd.getAxis(0)[0].getName();
			String n1 = mmd.getAxis(1)[0].getName();

			String[] split = n0.split(Node.SEPARATOR);
			yR.setName(split[split.length-1]);
			
			split = n1.split(Node.SEPARATOR);
			xR.setName(split[split.length-1]);
			
			AxesMetadata ax;
			ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ax.addAxis(0, yR);
			ax.addAxis(1, xR);
			im.addMetadata(ax);
			registered.addMetadata(ax);
		} catch (MetadataException e) {
			logger.error("Could not create axes metadata", e);
		}
		systemComposite.clear();
		double[] range = MappingUtils.getGlobalRange(im,map);

		IImageTrace image = MetadataPlotUtils.buildTrace("image",im, systemComposite);
		image.setGlobalRange(range);
		IImageTrace mapim = MetadataPlotUtils.buildTrace("map", map, systemComposite,scale.getSelection());
		mapim.setGlobalRange(range);
		systemComposite.addTrace(image);
		systemComposite.addTrace(mapim);
		
	}
	
	
	private void updatePoints() {
		Dataset v = buildDataset(mapPoints);
		Dataset x = buildDataset(imagePoints);
		if (x == null || v == null) return;
		Dataset trans = LinearAlgebra.solveSVD(x, v);

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
		
		MapToRotatedCartesian mrc = new MapToRotatedCartesian(0, 0, shape[1], shape[0], r*-1);
		
		IDataset im;
		
		if (image instanceof RGBDataset) {
			
			RGBDataset rgb = (RGBDataset)image;
			im = DatasetUtils.createCompoundDataset(Dataset.RGB, mrc.value(rgb.getRedView()).get(0),
								mrc.value(rgb.getGreenView()).get(0),
								mrc.value(rgb.getBlueView()).get(0));
			
			
		} else {
			List<Dataset> value = mrc.value(image);
			im = value.get(0);
		}
		
		logger.debug("XOffset: {}, YOffset: {}, XScale {}, YScale {},",tX,tY,sX,sY);
		
		registered = im;
		AxesMetadata ax = null;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ax.addAxis(0, yR);
			ax.addAxis(1, xR);
		} catch (MetadataException e) {
			logger.error("Could not create axes metadata", e);
		}
		im.addMetadata(ax);
		registered.addMetadata(ax);
		systemComposite.clear();
		double[] range = MappingUtils.getGlobalRange(im,map);

		IImageTrace image = MetadataPlotUtils.buildTrace("image",im, systemComposite);
		image.setGlobalRange(range);
		IImageTrace mapim = MetadataPlotUtils.buildTrace("map", map, systemComposite,120);
		mapim.setGlobalRange(range);
		systemComposite.addTrace(image);
		systemComposite.addTrace(mapim);
		
	}
	
	private Dataset buildDataset(IRegion[] regions) {


		try {
			Dataset mat = DatasetFactory.ones(new int[]{4, 3},Dataset.FLOAT64);
			int[] pos = new int[2];

			for (int i = 0; i < 4 ; i++) {
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
			logger.error("Could not get axis location",e);
		}

		return null;

	}
	
	
	private Dataset buildDataset(RectangularROI roi) {

		double[] matb = new double[12];
		Arrays.fill(matb, 1);
		matb[0] = roi.getPoint(0, 0)[0];
		matb[1] = roi.getPoint(0, 0)[1];
		matb[3] = roi.getPoint(0, 1)[0];
		matb[4] = roi.getPoint(0, 1)[1];
		matb[6] = roi.getPoint(1, 0)[0];
		matb[7] = roi.getPoint(1, 0)[1];
		matb[9] = roi.getPoint(1, 1)[0];
		matb[10] = roi.getPoint(1, 1)[1];
		Dataset mat = DatasetFactory.createFromObject(matb);
		mat.setShape(new int[]{4, 3});

		return mat;
	}
	
	private Dataset buildDataset(IDataset map) {

		AxesMetadata md = map.getFirstMetadata(AxesMetadata.class);
		IDataset x;
		try {
			x = md.getAxes()[1].getSlice();
		} catch (DatasetException e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		IDataset y;
		try {
			y = md.getAxes()[0].getSlice();
		} catch (DatasetException e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		double yMax = y.max().doubleValue();
		double yMin = y.min().doubleValue();
		double[] matb = new double[12];
		Arrays.fill(matb, 1);
		matb[0] = xMin;
		matb[1] = yMin;
		matb[3] = xMin;
		matb[4] = yMax;
		matb[6] = xMax;
		matb[7] = yMin;
		matb[9] = xMax;
		matb[10] = yMax;
		Dataset mat = DatasetFactory.createFromObject(matb);
		mat.setShape(new int[]{4, 3});

		return mat;
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
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
	
	@Override
	public boolean close() {
		if (systemMap != null && !systemMap.isDisposed()) systemMap.dispose();
		if (systemImage != null && !systemImage.isDisposed()) systemImage.dispose();
		if (systemComposite != null && !systemComposite.isDisposed()) systemComposite.dispose();
		return super.close();
	}
}

