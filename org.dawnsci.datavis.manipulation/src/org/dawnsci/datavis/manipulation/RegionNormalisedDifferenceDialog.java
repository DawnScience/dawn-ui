package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.datavis.api.IXYData;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionNormalisedDifferenceDialog extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(RegionNormalisedDifferenceDialog.class);
	
	private IXYData first;
	private IXYData second;
	
	private double scale = 1;
	private Color[] colors;
	
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
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IPlottingService plottingService = bundleContext.getService(
				 						   bundleContext.getServiceReference(IPlottingService.class));
		
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
		super.createButtonsForButtonBar(parent);
		Button button = getButton(IDialogConstants.OK_ID);
		button.setText("Save and Close");
		setButtonLayoutData(button);
	
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
		IDataset dif = calculateDifference();
		dif.setName("XMCD");
		
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
