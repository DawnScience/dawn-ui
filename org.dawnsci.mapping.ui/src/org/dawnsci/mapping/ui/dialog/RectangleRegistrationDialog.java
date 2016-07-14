package org.dawnsci.mapping.ui.dialog;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LinearAlgebra;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.function.MapToRotatedCartesian;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RectangleRegistrationDialog extends Dialog {
	private IDataset image;
	private IDataset map;
	private IDataset registered;
	private IPlottingSystem<Composite> systemImage;
	private IPlottingSystem<Composite> systemComposite;
	private IRegion box;
	int count = 0;
	
	private final static Logger logger = LoggerFactory.getLogger(RegistrationDialog.class);
	
	public RectangleRegistrationDialog(Shell parentShell, IDataset map, IDataset image) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.map = map;
		this.image = image.getSliceView();
		this.image.clearMetadata(AxesMetadata.class);
		
		try {
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
		Composite main = new Composite(container, SWT.FILL);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(2, true));
		ActionBarWrapper actionBarImage = ActionBarWrapper.createActionBars(main, null);
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(main, null);

		
		systemImage.createPlotPart(main, "Image Plot", actionBarImage, PlotType.IMAGE, null);
		systemImage.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		systemImage.setTitle("Image");
		systemImage.setShowIntensity(false);
		systemImage.getSelectedXAxis().setVisible(false);
		systemImage.getSelectedYAxis().setVisible(false);

		
		systemComposite.createPlotPart(main, "Composite Plot", actionBarComposite, PlotType.IMAGE, null);
		systemComposite.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		image.setName("Image");
		MetadataPlotUtils.plotDataWithMetadata(image, systemImage);
		
		doInitialMapping();
		
		return container;
	}
	
	private void doInitialMapping(){
		
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
			box = systemImage.createRegion("Scan Region", RegionType.BOX);
			box.setROI(new RectangularROI());
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
			im = new RGBDataset(mrc.value(rgb.getRedView()).get(0),
								mrc.value(rgb.getGreenView()).get(0),
								mrc.value(rgb.getBlueView()).get(0));
			
			
		} else {
			List<Dataset> value = mrc.value(image);
			im = value.get(0);
		}
		
		logger.debug("XOffset: {}, YOffset: {}, XScale {}, YScale {},",tX,tY,sX,sY);
		
		registered = im;
		AxesMetadataImpl ax = new AxesMetadataImpl(2);
		ax.addAxis(0, yR);
		ax.addAxis(1, xR);
		im.addMetadata(ax);
		registered.addMetadata(ax);
		systemComposite.clear();
		double[] range = MappingUtils.getGlobalRange(im,map);

		IImageTrace image = MetadataPlotUtils.buildTrace("image",im, systemComposite);
		image.setGlobalRange(range);
		IImageTrace mapim = MetadataPlotUtils.buildTrace("map", map, systemComposite,180);
		mapim.setGlobalRange(range);
		systemComposite.addTrace(image);
		systemComposite.addTrace(mapim);
		
	}
	
	
	private void update1() {
		
		RectangularROI roi = (RectangularROI)box.getROI();
		
		double r = roi.getAngle();
		double tX = roi.getPointX();
		double tY = roi.getPointY();
		
		AxesMetadata md = map.getFirstMetadata(AxesMetadata.class);
		IDataset x = md.getAxes()[1].getSlice();
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		IDataset y = md.getAxes()[0].getSlice();
		double yMax = y.max().doubleValue();
		double yMin = y.min().doubleValue();
		
		
//		Dataset v = buildDataset(mapPoints);
//		Dataset x = buildDataset(imagePoints);
//		if (x == null || v == null) return;
//		Dataset trans = LinearAlgebra.solveSVD(x, v);
//
//		double tX = trans.getDouble(2,0);
//		double tY = trans.getDouble(2,1);
		
		double cr = Math.cos(r);
		
		double xLen = roi.getLength(0);
		double yLen = roi.getLength(1);
		
		double sX = (xMax-xMin)/xLen;
		double sY = (yMax-yMin)/yLen;
		
//		double sX = Math.hypot(trans.getDouble(0,0), trans.getDouble(0,1));
//		double sY = Math.hypot(trans.getDouble(1,0), trans.getDouble(1,1));
		
//		double r = Math.toDegrees(Math.atan(trans.getDouble(0,1)/trans.getDouble(1,1)));
		
		int[] shape = image.getShape();
		
		Dataset xR = DatasetFactory.createRange(shape[1], Dataset.FLOAT64);
		Dataset yR = DatasetFactory.createRange(shape[0], Dataset.FLOAT64);
		
		double xVal = xMin + (sX*-tX);
		double yVal = yMin+ (sY*-tY);
		
		double yrot = xVal * Math.cos(r) - yVal * Math.sin(r);
	    double xrot = xVal * Math.sin(r) + yVal * Math.cos(r);
		
//		xR.iadd(tX);
		xR.imultiply(sX);
		xR.iadd(xVal);
		
//		yR.iadd(tY);
		yR.imultiply(sY);
		yR.iadd(yVal);
		
		
		MapToRotatedCartesian mrc = new MapToRotatedCartesian(0, 0, shape[1], shape[0], Math.toDegrees(r));
		
		IDataset im;
		
		if (image instanceof RGBDataset) {
			
			RGBDataset rgb = (RGBDataset)image;
			im = new RGBDataset(mrc.value(rgb.getRedView()).get(0),
								mrc.value(rgb.getGreenView()).get(0),
								mrc.value(rgb.getBlueView()).get(0));
			
			
		} else {
			List<Dataset> value = mrc.value(image);
			im = value.get(0);
		}
		
		logger.debug("XOffset: {}, YOffset: {}, XScale {}, YScale {},",tX,tY,sX,sY);
		
		registered = im;
		AxesMetadataImpl ax = new AxesMetadataImpl(2);
		ax.addAxis(0, yR);
		ax.addAxis(1, xR);
		im.addMetadata(ax);
		registered.addMetadata(ax);
		systemComposite.clear();
		double[] range = MappingUtils.getGlobalRange(im,map);

		IImageTrace image = MetadataPlotUtils.buildTrace("image",im, systemComposite);
		image.setGlobalRange(range);
		IImageTrace mapim = MetadataPlotUtils.buildTrace("map", map, systemComposite,180);
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
		IDataset x = md.getAxes()[1].getSlice();
		double xMax = x.max().doubleValue();
		double xMin = x.min().doubleValue();
		IDataset y = md.getAxes()[0].getSlice();
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

}

