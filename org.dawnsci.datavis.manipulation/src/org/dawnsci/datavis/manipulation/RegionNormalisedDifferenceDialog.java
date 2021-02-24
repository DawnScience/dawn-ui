package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.printing.IPrintImageProvider;
import org.dawb.common.ui.printing.PlotPrintPreviewDialog;
import org.dawb.common.ui.printing.PrintSettings;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LinearAlgebra;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.Stats;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionNormalisedDifferenceDialog extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(RegionNormalisedDifferenceDialog.class);
	
	private IXYData first;
	private IXYData second;
	
	private double scale = 1;
	private Color[] colors;
	private RectangularROI linearRoi;
	
	private IPlottingSystem<Composite> regionSystem;
	private IPlottingSystem<Composite> differenceSystem;

	public RegionNormalisedDifferenceDialog(Shell parentShell, IXYData first, IXYData second) {
		super(parentShell);
		
		this.first = first;
		this.second = second;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		IPlottingService plottingService = DataVisManipulationServiceManager.getPlottingService();
		try {
			regionSystem = plottingService.createPlottingSystem();
			differenceSystem = plottingService.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create plotting system!",e);
			return container;
		}
		
		
		regionSystem.createPlotPart(container, "Region Select", null, PlotType.XY, null);
		regionSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		differenceSystem.createPlotPart(container, "Scaled Difference", null, PlotType.XY, null);
		differenceSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		first.getY().setName(first.getFileName() + ":" +first.getDatasetName());
		second.getY().setName(second.getFileName() + ":" +second.getDatasetName());
		regionSystem.createPlot1D(first.getX(), Arrays.asList(first.getY()), null);
		regionSystem.createPlot1D(second.getX(), Arrays.asList(second.getY()), null);
		regionSystem.setTitle("");
		
		Button subtractLinear = new Button(container, SWT.CHECK);
		subtractLinear.setText("Subtract linear fix from XMCD");
		
		try {
			IRegion r = regionSystem.createRegion("Normalisation region", RegionType.XAXIS);
			RectangularROI roi = new RectangularROI();
			roi.setPoint(first.getX().min().doubleValue(), 0.0);
			roi.setLengths(DatasetUtils.convertToDataset(first.getX()).peakToPeak(true).doubleValue()/20,1);
			r.setROI(roi);
			regionSystem.addRegion(r);
			
			r.addROIListener(new IROIListener() {
				
				@Override
				public void roiSelected(ROIEvent evt) {
					// Do nothing here
					
				}
				
				@Override
				public void roiDragged(ROIEvent evt) {
					updateScale((RectangularROI)evt.getROI());
					
				}
				
				@Override
				public void roiChanged(ROIEvent evt) {
					updateScale((RectangularROI)evt.getROI());
				}
			});
			
			subtractLinear.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					if (subtractLinear.getSelection()) {
						IROI roi2 = r.getROI().copy();
						
						try {
							IRegion region = differenceSystem.createRegion("Linear region", RegionType.XAXIS);
							region.setROI(roi2);
							differenceSystem.addRegion(region);
							region.setAlpha(64);
							linearRoi = (RectangularROI)roi2;
							region.addROIListener(new IROIListener() {
								
								@Override
								public void roiSelected(ROIEvent evt) {
									// Do nothing here
									
								}
								
								@Override
								public void roiDragged(ROIEvent evt) {
									//Do nothing here, ideally we would update,
									//but since the data is on the same plot this causes issues
									
								}
								
								@Override
								public void roiChanged(ROIEvent evt) {
									linearRoi = (RectangularROI)evt.getROI().copy();
									updateData();
								}
							});
							
						} catch (Exception e1) {
							logger.error("Error creating region", e);
						}
						updateData();
					} else {
						differenceSystem.clearRegions();
						linearRoi = null;
						updateData();
					}
				}
			});
			
		} catch (Exception e) {
			logger.error("Could not create region on plotting system!",e);
		}
		
		updateData();
		
		return container;
	}
	
	public Dataset getData() {
		
		Dataset d = calculateDifference();
		Dataset sum = cumTrapz(first.getX(),d);
		try {
			AxesMetadata m = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			m.setAxis(0, first.getX());
			m.addAxis(0, sum);
			d.setMetadata(m);

		} catch (MetadataException e) {
			logger.error("Could not create metadata", e);
		}
		
		return d;
		
	}
	
	public String getTemplateName() {
		
		String name = "xmcd";
		
		try {
			
			name = getFileName(second.getFileName()) + "-" + getFileName(first.getFileName());
			
		} catch (Exception e) {
			logger.error("Error building filename",e);
		}
		
		return name;
	}
	
	public String getFileName(String path) {
		String name = new File(path).getName();
		int idx = name.lastIndexOf('.');
		
		if (idx < 1) {
			return name;
		}
		
		return name.substring(0, idx);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button print = createButton(parent, IDialogConstants.NO_ID, "Print...", false);
		
		print.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Seems to be no way that works to just print a composite and contents so...
				PrintSettings settings = new PrintSettings();
				final IPrintImageProvider prov = new IPrintImageProvider() {
					@Override
					public Image getImage(Rectangle size) {
						//Lets combine images of the two systems...
						//Make a half size rectangle for each plot
						Rectangle s2 = new Rectangle(size.x,size.y,size.width-6,size.height/2);
						Image im1 = ((AbstractPlottingSystem)regionSystem).getImage(s2);
						Image im2 = ((AbstractPlottingSystem)differenceSystem).getImage(s2);
						ImageData imd1 = im1.getImageData();
						ImageData imd2 = im2.getImageData();
						//Make the big picture (make it white)
						ImageData combined = new ImageData(size.width, size.height, imd1.depth, imd1.palette);
						int[] fullPixels = new int[size.width* size.height];
						Arrays.fill(fullPixels, 0xFFFFFF);
						combined.setPixels(0, 0, size.width*size.height, fullPixels, 0);
						//fill the pixel array with the plot images.
						int[] pixels = new int[s2.width* s2.height];
						Arrays.fill(pixels, 0xFFFFFF);
						imd1.getPixels(0, 0, s2.width* s2.height, pixels, 0);
						combined.setPixels(0, 0, s2.width* s2.height, pixels,0);
						Arrays.fill(pixels, 0xFFFFFF);
						imd2.getPixels(0, 0, s2.width* s2.height, pixels, 0);
						combined.setPixels(0, s2.height, s2.width* s2.height, pixels,0);
						return new Image(null,combined);

					}
					@Override
					public Rectangle getBounds() {
						Control da = RegionNormalisedDifferenceDialog.this.getDialogArea();
						Rectangle rect = da.getBounds();
						return new Rectangle(rect.x, rect.y, rect.width, rect.height);
					}			
				};
				PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(prov, Display.getDefault(), settings);
				settings=dialog.open();
				
			}

		});
		super.createButtonsForButtonBar(parent);
		Button button = getButton(IDialogConstants.OK_ID);
		button.setText("Save and Close");
		setButtonLayoutData(button);
	
	}
	
	private double[] calculateLinear(RectangularROI roi, Dataset data) {
		int tmpMin = ROISliceUtils.findPositionOfClosestValueInAxis(first.getX(), roi.getPointX());
		int tmpMax = ROISliceUtils.findPositionOfClosestValueInAxis(first.getX(), roi.getPointX()+roi.getLength(0));
		
		if (tmpMin > tmpMax) {
			int tmp = tmpMin;
			tmpMin = tmpMax;
			tmpMax = tmp;
		}
		
		return linearFit(tmpMin, tmpMax);
		
	}
	
	private double[] linearFit(int tmpMin, int tmpMax) {
		//Linear fit across region
		SliceND s = new SliceND(first.getX().getShape());

		s.setSlice(0, tmpMin,tmpMax,1);

		Dataset dif = calculateDifference();

		dif = dif.getSlice(s);
		IDataset r = first.getX().getSlice(s);

		DoubleDataset linear = DatasetFactory.ones(DoubleDataset.class,new int[] {dif.getShape()[0],2});
		s = new SliceND(linear.getShape());
		s.setSlice(1, 1, 2, 1);

		r.setShape(linear.getShape()[0],1);
		linear.setSlice(r, s);

		Dataset result = LinearAlgebra.solveSVD(linear, dif);
		double c = result.getDouble(0);
		double m = result.getDouble(1);

		return new double[] {m,c};
	}
	
	private double[] twoPointLinear(Dataset data, int tmpMin, int tmpMax) {
		//simple two point linear baseline
		//may still be needed
		double x1 = first.getX().getDouble(tmpMin);
		double x2 = first.getX().getDouble(tmpMax);

		double y1 = data.getDouble(tmpMin);
		double y2 = data.getDouble(tmpMax);

		double m = (y2-y1)/(x2-x1);

		double c = y1-m*x1;

		return new double[] {m,c};
	}
	
	private void updateScale(RectangularROI roi) {
		int tmpMin = ROISliceUtils.findPositionOfClosestValueInAxis(first.getX(), roi.getPointX());
		int tmpMax = ROISliceUtils.findPositionOfClosestValueInAxis(first.getX(), roi.getPointX()+roi.getLength(0));
		
		if (tmpMin > tmpMax) {
			int tmp = tmpMin;
			tmpMin = tmpMax;
			tmpMax = tmp;
		}
		
		SliceND s = new SliceND(first.getX().getShape());
		
		s.setSlice(0, tmpMin,tmpMax,1);
		
		Dataset s1 = DatasetUtils.convertToDataset(first.getY().getSlice(s));
		Dataset s2 = DatasetUtils.convertToDataset(second.getY().getSlice(s));
		
		s2.setShape(new int[] {s2.getShape()[0],1});
		
		Dataset result = LinearAlgebra.solveSVD(s2, s1);
		scale = result.getDouble();

		updateData();
	}
	
	private Dataset calculateDifference() {
		return Maths.subtract(Maths.multiply(second.getY(),scale), first.getY());
	}
	
	private void updateData() {
		Dataset dif = calculateDifference();
		dif.setName("XMCD");
		
		if (linearRoi != null) {
			double[] mc = calculateLinear(linearRoi,dif);
			IDataset linear = Maths.multiply(first.getX(),mc[0]).iadd(mc[1]);
			dif.isubtract(linear);
		}
		
		Dataset sum = cumTrapz(first.getX(),dif);
		sum.setName("sum");
		
		Dataset zero = DatasetFactory.zeros(DoubleDataset.class, first.getX().getShape());
		zero.setName("zeroth line");
		
		List<ITrace> t = differenceSystem.updatePlot1D(first.getX(), Arrays.asList(zero,sum,dif),null);
		
		if (colors == null) {
			colors = getColors();
			((ILineTrace)t.get(0)).setTraceColor(colors[0]);
			((ILineTrace)t.get(1)).setTraceColor(colors[1]);
			((ILineTrace)t.get(2)).setTraceColor(colors[2]);
			
			((ILineTrace)t.get(0)).setTraceType(TraceType.DASH_LINE);
		}
		
		differenceSystem.setTitle("");
		differenceSystem.repaint();
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
	
	private Dataset cumTrapz(IDataset x, IDataset y) {
		
		DoubleDataset cum = DatasetFactory.zeros(DoubleDataset.class, x.getShape());
		
		Dataset xd = DatasetUtils.cast(DoubleDataset.class, x);
		Dataset yd = DatasetUtils.cast(DoubleDataset.class, y);
		
		for (int i = 1; i < cum.getSize(); i++) {
			
			double step = xd.getElementDoubleAbs(i) - xd.getElementDoubleAbs(i-1); 
			double signal = yd.getElementDoubleAbs(i) + yd.getElementDoubleAbs(i-1);
			double area = (signal/2)*step;
			cum.setAbs(i, area);
		}
		
		return Stats.cumulativeSum(cum);
	}
	
	private Color[] getColors() {
		return new Color[] {new Color(getShell().getDisplay(), new RGB(100,100,100)),
				new Color(getShell().getDisplay(), new RGB(0,0,0)),
				new Color(getShell().getDisplay(), new RGB(0,200,0))};
	}
	
	
	public boolean close() {
		
		if (colors != null) {
			for (Color c : colors) c.dispose();
		}
		
		return super.close();
	}
	
	@Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("XMCD");
	  }
	
	@Override
	  protected Point getInitialSize() {
	    return new Point(1000, 720);
	  }

}
