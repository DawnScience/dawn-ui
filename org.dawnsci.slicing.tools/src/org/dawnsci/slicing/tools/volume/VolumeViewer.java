package org.dawnsci.slicing.tools.volume;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite containing 4 plotting systems: one volume and three images,
 * sliced from the X, Y and Z planes.
 * 
 * Axis line regions on the image plots can be used to slice the data
 *
 */
public class VolumeViewer extends Composite {
	
	private static final Logger logger = LoggerFactory.getLogger(VolumeViewer.class);
	
	private IPlottingSystem<Composite> volumePlot;
	private IPlottingSystem<Composite> xPlot;
	private IPlottingSystem<Composite> yPlot;
	private IPlottingSystem<Composite> zPlot;
	
	private Label positionLabel;
 	
	private ILazyDataset lazyDataset;
	private int[] xyzDims;
	private int[] currentCoordinate;
	
	private IRegion yOnXRegion;
	private IRegion zOnXRegion;
	
	private IRegion xOnYRegion;
	private IRegion zOnYRegion;
	
	private IRegion xOnZRegion;
	private IRegion yOnZRegion;
	
	private AtomicBoolean xInUpdate = new AtomicBoolean(false);
	private AtomicBoolean yInUpdate = new AtomicBoolean(false);
	private AtomicBoolean zInUpdate = new AtomicBoolean(false);
	
	private AtomicReference<Runnable> xRunnable = new AtomicReference<Runnable>(null);
	private AtomicReference<Runnable> yRunnable = new AtomicReference<Runnable>(null);
	private AtomicReference<Runnable> zRunnable = new AtomicReference<Runnable>(null);
	
	private static final MathContext PRECISION = new MathContext(6, RoundingMode.HALF_UP);
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public VolumeViewer(Composite parent, int style, IPlottingService plotService) {
		super(parent, style);

		try {
			volumePlot = plotService.createPlottingSystem();
			xPlot = plotService.createPlottingSystem();
			yPlot = plotService.createPlottingSystem();
			zPlot = plotService.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create plotting systems!");
			return;
		}
		
		this.setLayout(new GridLayout(2,true));
		
		volumePlot.createPlotPart(this, "Volume_viewer_volume", (IActionBars)null, PlotType.IMAGE, (IWorkbenchPart)null);
		volumePlot.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		xPlot.createPlotPart(this, "Volume_viewer_x", (IActionBars)null, PlotType.IMAGE, (IWorkbenchPart)null);
		xPlot.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		yPlot.createPlotPart(this, "Volume_viewer_y", (IActionBars)null, PlotType.IMAGE, (IWorkbenchPart)null);
		yPlot.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		zPlot.createPlotPart(this, "Volume_viewer_z", (IActionBars)null, PlotType.IMAGE, (IWorkbenchPart)null);
		zPlot.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		xPlot.setKeepAspect(false);
		yPlot.setKeepAspect(false);
		zPlot.setKeepAspect(false);
		
		try {
			yOnXRegion = xPlot.createRegion("yOnX", RegionType.XAXIS_LINE);
			yOnXRegion.setROI(new XAxisBoxROI());
			yOnXRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			xPlot.addRegion(yOnXRegion);
			yOnXRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (yInUpdate.getAndSet(true)) return;
					IROI roi = evt.getROI();
					
					double x = roi.getPointX();
					double[] point = yOnZRegion.getROI().getPoint();
					yOnZRegion.getROI().setPoint(x,point[1]);
					zPlot.repaint();
					
					updateY((int)x);
					
					yInUpdate.set(false);
					
				}
			});
			
			
			zOnXRegion = xPlot.createRegion("zOnX", RegionType.YAXIS_LINE);
			zOnXRegion.setROI(new YAxisBoxROI());
			xPlot.addRegion(zOnXRegion);
			zOnXRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			zOnXRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (zInUpdate.getAndSet(true)) return;
					IROI roi = evt.getROI();
					
					double y = roi.getPointY();
					
					double[] point = zOnYRegion.getROI().getPoint();
					zOnYRegion.getROI().setPoint(y, point[1]);
					yPlot.repaint();
					
					updateZ((int)y);
					
					zInUpdate.set(false);

				}
			});
			
			
			
			xOnYRegion = yPlot.createRegion("xOnY", RegionType.YAXIS_LINE);
			xOnYRegion.setROI(new YAxisBoxROI());
			xOnYRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			yPlot.addRegion(xOnYRegion);
			xOnYRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (xInUpdate.getAndSet(true)) return;
					IROI roi = evt.getROI();
					double y = roi.getPointY();
					
					double[] point = xOnZRegion.getROI().getPoint();
					xOnZRegion.getROI().setPoint(point[0], y);
					zPlot.repaint();
					
					updateX((int)y);
					
					xInUpdate.set(false);
					
				}
			});
			
			
			
			zOnYRegion = yPlot.createRegion("zOnY", RegionType.XAXIS_LINE);
			zOnYRegion.setROI(new XAxisBoxROI());
			zOnYRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			yPlot.addRegion(zOnYRegion);
			zOnYRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (zInUpdate.getAndSet(true)) return;
					IROI roi = evt.getROI();
					
					double x = roi.getPointX();
					double[] point = zOnXRegion.getROI().getPoint();
					zOnXRegion.getROI().setPoint(point[0], x);
					xPlot.repaint();
					
					updateZ((int)x);
					
					zInUpdate.set(false);
					
				}
			});
			
			xOnZRegion = zPlot.createRegion("xOnZ", RegionType.YAXIS_LINE);
			xOnZRegion.setROI(new YAxisBoxROI());
			xOnZRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			zPlot.addRegion(xOnZRegion);
			xOnZRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (xInUpdate.getAndSet(true)) return;
					
					IROI roi = evt.getROI();
					
					double y = roi.getPointY();
					
					double[] point = xOnYRegion.getROI().getPoint();
					xOnYRegion.getROI().setPoint(point[0], y);
					yPlot.repaint();
					
					updateX((int)y);
					
					xInUpdate.set(false);
					
				}
			});
			
			yOnZRegion = zPlot.createRegion("yOnZ", RegionType.XAXIS_LINE);
			yOnZRegion.setROI(new XAxisBoxROI());
			yOnZRegion.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			zPlot.addRegion(yOnZRegion);
			yOnZRegion.addROIListener(new IROIListener.Stub() {
				
				@Override
				public void roiChanged(ROIEvent evt) {
					if (yInUpdate.getAndSet(true)) return;
					
					IROI roi = evt.getROI();
					
					double x = roi.getPointX();
					
					double[] point = yOnXRegion.getROI().getPoint();
					yOnXRegion.getROI().setPoint(x, point[1]);
					xPlot.repaint();
					
					updateY((int)x);
					
					yInUpdate.set(false);
					
				}
			});
		} catch (Exception e) {
			logger.error("Could not create regions!");
		}
		
		positionLabel = new Label(this, SWT.None);
		positionLabel.setText("Ready");
		positionLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
	}
	
	private String getLabel(Double[] xyz) {
		String x = (xyz != null && xyz[0] != null) ? doubleToStringWithPrecision(xyz[0]) : Integer.toString(currentCoordinate[0]);
		String y = (xyz != null && xyz[1] != null) ? doubleToStringWithPrecision(xyz[1]) : Integer.toString(currentCoordinate[1]);
		String z = (xyz != null && xyz[2] != null) ? doubleToStringWithPrecision(xyz[2]) : Integer.toString(currentCoordinate[2]);
		
		StringBuilder b = new StringBuilder();
		b.append("Z : ");
		b.append(x);
		b.append(", ");
		b.append("Y : ");
		b.append(y);
		b.append(", ");
		b.append("X : ");
		b.append(z);
		
		return b.toString();

	}
	
	private String doubleToStringWithPrecision(double d) {
		BigDecimal bd = BigDecimal.valueOf(d).round(PRECISION).stripTrailingZeros();
		//stop 100 going to 1.0E2
		if (bd.precision() >= 1 && bd.precision() < PRECISION.getPrecision() && bd.scale() < 0 && bd.scale() > (-1*PRECISION.getPrecision())) {
			bd = bd.setScale(0);
		}
		return bd.toString();
	}
	
	private void updateZ(int pos) {
		currentCoordinate[2] = pos;
		zRunnable.set(() -> updateDimension(2,zPlot,false));
		submit(zRunnable);
	}
	
	private void updateDimension(int index, IPlottingSystem<Composite> system, boolean transpose) {
		
		SliceND slicez = new SliceND(lazyDataset.getShape());
		slicez.setSlice(xyzDims[index], currentCoordinate[index], currentCoordinate[index]+1, 1);
		
		try {
			IDataset data = lazyDataset.getSlice(slicez);
			
			final Double[] label = getCoordLabel(data,xyzDims,currentCoordinate);
			
			data = data.squeeze();
			
			if (transpose) {
				data = ((Dataset)data).transpose();
			}
			
			final IDataset d = data;
			
			Display.getDefault().syncExec(() -> {
				system.clearTraces();
				IImageTrace trace = MetadataPlotUtils.buildTrace(d, system);
				system.addTrace(trace);
				positionLabel.setText(getLabel(label));
			});
		} catch (DatasetException e) {
			logger.error("Could not slice data!");
		}
		
	}
	
	private Double[] getCoordLabel(IDataset data, int[] xyzDims2, int[] currentCoordinate2) {
		
		if (data.getFirstMetadata(AxesMetadata.class) == null) {
			return null;
		}
		
		AxesMetadata md = data.getFirstMetadata(AxesMetadata.class);
		if (md.getAxis(xyzDims2[0]) == null && 
				md.getAxis(xyzDims2[1]) == null &&
						md.getAxis(xyzDims2[2]) == null) {
			return null;
		}
		
		SliceND s = new SliceND(data.getShape());
		int[] start = s.getStart();
		int[] stop = s.getStop();
		
		for (int i = 0; i < xyzDims2.length; i++) {
			int sr = start[xyzDims2[i]];
			int sp = stop[xyzDims2[i]];
			
			if (sp-sr == 1) continue;
			
			s.setSlice(xyzDims2[i], currentCoordinate2[xyzDims2[i]], currentCoordinate2[xyzDims2[i]]+1, 1);
			
		}
		
		IDataset slice = data.getSlice(s);
		
		if (slice.getSize() != 1) return null;
		
		
		AxesMetadata mdSlice = slice.getFirstMetadata(AxesMetadata.class);
		
		if (mdSlice == null) return null;
		
		if (mdSlice.getAxis(xyzDims2[0]) == null && 
				mdSlice.getAxis(xyzDims2[1]) == null &&
				mdSlice.getAxis(xyzDims2[2]) == null) {
			return null;
		}

		try {
			Double[] out = new Double[3];
			out[0] = mdSlice.getAxis(xyzDims2[0]) == null ? null : mdSlice.getAxis(xyzDims2[0])[0].getSlice().squeeze().getDouble();
			out[1] = mdSlice.getAxis(xyzDims2[1]) == null ? null : mdSlice.getAxis(xyzDims2[1])[0].getSlice().squeeze().getDouble();
			out[2] = mdSlice.getAxis(xyzDims2[2]) == null ? null : mdSlice.getAxis(xyzDims2[2])[0].getSlice().squeeze().getDouble();
			
			return out;
		} catch (Exception e) {
			logger.error("Could not slice axes!");
		}
		
		return null;
	}

	private void updateX(int pos) {
		currentCoordinate[0] = pos;
		xRunnable.set(() -> updateDimension(0,xPlot,true));
		submit(xRunnable);
	}
	
	private void submit(AtomicReference<Runnable> r) {
		executor.execute(() -> {
			Runnable local = r.getAndSet(null);
			if (local == null) return;
			local.run();
		});
	}
	
	private void updateY(int pos) {
		currentCoordinate[1] = pos;
		yRunnable.set(() -> updateDimension(1,yPlot,false));
		submit(yRunnable);
	}
	
	public void setData(ILazyDataset lazyDataset, int[] xyzDims) {
		this.lazyDataset = lazyDataset;
		this.xyzDims = xyzDims;
		int[] shape = lazyDataset.getShape();
		currentCoordinate = new int[] {shape[xyzDims[0]]/2,shape[xyzDims[1]]/2,shape[xyzDims[2]]/2};
		
		updateDisplay(shape);
	}

	private void updateDisplay(int[] shape) {
		
		xOnYRegion.getROI().setPoint(shape[xyzDims[0]]/2, shape[xyzDims[0]]/2);
		xOnZRegion.getROI().setPoint(shape[xyzDims[0]]/2, shape[xyzDims[0]]/2);
		yOnXRegion.getROI().setPoint(shape[xyzDims[1]]/2, shape[xyzDims[1]]/2);
		yOnZRegion.getROI().setPoint(shape[xyzDims[1]]/2, shape[xyzDims[1]]/2);
		zOnYRegion.getROI().setPoint(shape[xyzDims[2]]/2, shape[xyzDims[2]]/2);
		zOnXRegion.getROI().setPoint(shape[xyzDims[2]]/2, shape[xyzDims[2]]/2);
		
		updateX(currentCoordinate[0]);
		updateY(currentCoordinate[1]);
		updateZ(currentCoordinate[2]);
		
		AtomicReference<Runnable> volumeRun = new AtomicReference<Runnable>(() -> {


			try {
				IVolumeTrace t = volumePlot.createTrace("Volume", IVolumeTrace.class);
				IDataset s = lazyDataset.getSlice();
				
				AxesMetadata metadata = s.getFirstMetadata(AxesMetadata.class);
				List<IDataset> ax = null;
				
				if (metadata != null) {
					ax = new ArrayList<>();
					ILazyDataset[] axes = metadata.getAxes();
					if (axes != null) {
						for (ILazyDataset a : axes) {
							ax.add(a == null ? null : a.getSlice().squeeze());
						}
						Collections.reverse(ax);
					}
				}
				
				t.setData(s, ax == null ? null : ax.toArray(new IDataset[ax.size()]), s.min(true), s.max(true));
				Display.getDefault().asyncExec(()-> volumePlot.addTrace(t));
			} catch (Exception e) {
				logger.error("Could not make volume trace", e);
			}
		});

		submit(volumeRun);
		
	}
	
	public void reset() {
		
		volumePlot.clearTraces();
		xPlot.clearTraces();
		yPlot.clearTraces();
		zPlot.clearTraces();
	}

}
