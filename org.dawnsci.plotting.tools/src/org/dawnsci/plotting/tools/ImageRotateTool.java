/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.MouseListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.MapTo2DUtils;

public class ImageRotateTool extends AbstractToolPage implements IROIListener, MouseListener{

	private Logger logger = LoggerFactory.getLogger(ImageRotateTool.class);
	private Spinner angleSpinner;
	private static IImageTransform transformer;
	private IDataset image;
	private Composite container;
	private double angle;
	private IPlottingSystem rotatedSystem;
	private ITraceListener traceListener;
	private RotateJob rotationJob;
	private List<IDataset> axes;
	private IRegion xHair, yHair;
	private String xName;
	private String yName;
	private final static String X_PREFIX = "X Profile";
	private final static String Y_PREFIX = "Y Profile";
	// shape of resulting image (bounding box)
	private boolean hasSameShape = true;

	public ImageRotateTool() {
		super();
		try {
			rotatedSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
		// Connect to the trace listener to deal with new images coming in
		traceListener = new ITraceListener.Stub() {
			@Override
			public void tracesAdded(TraceEvent evt) {
				if (!isActive())
					return;
				if (getImageTrace() != null) {
					IDataset data = getImageTrace().getData();
					if (data != null) {
						image = data;
						axes = getImageTrace().getAxes();
					}
				}
				logger.trace("tracelistener firing");
			}
		};
		rotationJob = new RotateJob();
		rotationJob.setPriority(Job.INTERACTIVE);

	}

	/**
	 * Injected by OSGI
	 * @param it
	 */
	public static void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	@Override
	public Control getControl() {
		return container;
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		Composite angleComp = new Composite(container, SWT.NONE | SWT.TOP);
		angleComp.setLayout(new GridLayout(4, false));

		Label labelAngle = new Label(angleComp, SWT.NONE);
		labelAngle.setText("Rotation angle");
		labelAngle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		angleSpinner = new Spinner(angleComp, SWT.BORDER);
		angleSpinner.setDigits(1);
		angleSpinner.setToolTipText("Rotates the original image by n degrees");
		angleSpinner.setSelection(0);
		angleSpinner.setMinimum(-3600);
		angleSpinner.setMaximum(3600);
		angleSpinner.setIncrement(5);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false); 
		gridData.widthHint = 50;
		angleSpinner.setLayoutData(gridData);
		angleSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				rotate();
			}
		});

		Button centreROIButton = new Button(angleComp, SWT.PUSH);
		centreROIButton.setText("Centre");
		centreROIButton.setToolTipText("Centres the Vertical and Horizontal ROIs positions");
		centreROIButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rotatedSystem == null || rotatedSystem.getTraces().isEmpty())
					return;
				RectangularROI[] rois = getXYCenteredROIs();
				if (rois[0] != null)
					xHair.setROI(rois[0]);
				if (rois[1] != null)
					yHair.setROI(rois[1]);
			}
		});

		final Button resizeBBoxButton = new Button(angleComp, SWT.CHECK);
		resizeBBoxButton.setText("Resize Bounding Box");
		resizeBBoxButton.setToolTipText("");
		resizeBBoxButton.setSelection(false);
		resizeBBoxButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				hasSameShape = !resizeBBoxButton.getSelection();
				rotate();
			}
		});
		
		final IPageSite site = getSite();
		IActionBars actionBars = (site != null) ? site.getActionBars() : null;

		rotatedSystem.createPlotPart(container, getTitle(), actionBars, PlotType.IMAGE, null);
		rotatedSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		IImageTrace image = getImageTrace();
		IDataset data = image != null ? image.getData() : null;
		if (data !=  null) {
			this.image = data;
			axes = getImageTrace().getAxes();
			rotatedSystem.updatePlot2D(data, axes, null);
		}
	}

	private void rotate() {
		angle = getSpinnerAngle();
		if (rotationJob == null) {
			rotationJob = new RotateJob();
			rotationJob.setPriority(Job.INTERACTIVE);
		}
		if (rotationJob.getState() == Job.RUNNING)
			rotationJob.cancel();
		rotationJob.setAngle(angle);
		rotationJob.schedule();
	}

	private double getSpinnerAngle() {
		int selection = angleSpinner.getSelection();
		int digits = angleSpinner.getDigits();
		return (selection / Math.pow(10, digits));
	}

	private RectangularROI[] getXYCenteredROIs() {
		IImageTrace trace = (IImageTrace) rotatedSystem.getTraces().iterator().next();
		Collection<IRegion> regions = rotatedSystem.getRegions();
		RectangularROI xRoi = null, yRoi = null; 
		if (trace != null) {
			if (regions != null && !regions.isEmpty()) {
				Object[] regionsArray = regions.toArray();
				for (Object regionObj : regionsArray) {
					IRegion region = (IRegion)regionObj;
					if (region.getName().equals(xName)) {
						xRoi = (RectangularROI) region.getROI();
						xRoi.setPoint(new double[] {trace.getData().getShape()[0] / 2, 0});
					}
					if (region.getName().equals(yName)) {
						yRoi = (RectangularROI) region.getROI();
						yRoi.setPoint(new double[] {0, trace.getData().getShape()[1] / 2});
					}
				}
			} else {
				xRoi = new RectangularROI(trace.getData().getShape()[0] / 2, 0,
						200, 200, 0);
				xRoi.setName(xName);
				yRoi = new RectangularROI(0, trace.getData().getShape()[1] / 2,
						200, 200, 0);
				yRoi.setName(yName);
			}
		}
		return new RectangularROI[] { xRoi, yRoi };
	}

	private void createRegions() {
		if (rotatedSystem == null || rotatedSystem.getTraces().isEmpty())
			return;
		RectangularROI[] rois = getXYCenteredROIs();
		try {
			if (xHair == null
					|| rotatedSystem.getRegion(xHair.getName()) == null) {
				this.xName = RegionUtils.getUniqueName(Y_PREFIX,
						rotatedSystem);
				this.xHair = rotatedSystem.createRegion(xName,
						IRegion.RegionType.XAXIS_LINE);
				if (rois[0] != null)
					xHair.setROI(rois[0]);
				addRegion("Updating x cross hair", xHair);
			}

			if (yHair == null
					|| rotatedSystem.getRegion(yHair.getName()) == null) {
				this.yName = RegionUtils.getUniqueName(X_PREFIX,
						getPlottingSystem());
				this.yHair = rotatedSystem.createRegion(yName,
						IRegion.RegionType.YAXIS_LINE);
				if (rois[1] != null)
					yHair.setROI(rois[1]);
				addRegion("Updating x cross hair", yHair);
			}
		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private void addRegion(String jobName, IRegion region) {
		region.setVisible(false);
		region.setMobile(true);
		region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change it!
		rotatedSystem.addRegion(region);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Add the trace listener and plot initial data
	 */
	@Override
	public void activate() {
		deactivate();
		
		logger.debug("ImageRotateTool: activate ", this.hashCode() );
		super.activate();
		
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			if (getImageTrace() != null) {
				image = getImageTrace().getData();
				axes = getImageTrace().getAxes();
			}
		}
		createRegions();
		if (xHair!=null) {
			if (!isActive()) xHair.addMouseListener(this);
			xHair.setVisible(true);
			xHair.addROIListener(this);
		}
		if (yHair!=null) {
			yHair.setVisible(true);
			yHair.addROIListener(this);
		}
	}

	/**
	 * remove the trace listener to avoid unneeded event triggering
	 */
	@Override
	public void deactivate() {
		logger.trace("ImageRotateTool: deactivate ", this.hashCode() );
		super.deactivate();

		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
	}

	class RotateJob extends Job {

		private double angle = 0;

		public RotateJob() {
			super("Image Rotation Job");
		}

		public void setAngle(double angle) {
			this.angle = angle;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
//				List<IAxis> axes = rotatedSystem.getAxes();
//				IAxis xAxis = axes.get(0), yAxis = axes.get(1);
				IDataset rotated = transformer.rotate(image, angle, hasSameShape);
//				double[] xRange = new double[]{xAxis.getLower(), xAxis.getUpper()}, 
//						yRange = new double[]{yAxis.getUpper(), yAxis.getLower()};
//				IImageTrace trace = (IImageTrace)rotatedSystem.getTraces().iterator().next();
//				List<IDataset> axesD = trace.getAxes();
//				IDataset xO = axesD.get(0), yO = axesD.get(1);
//				int xNumber = (int)xAxis.getUpper(), yNumber = (int)yAxis.getLower();
//				rotated = MapTo2DUtils.remap2Dto2DSplitting(rotated, xO, yO, xRange, xNumber, yRange, yNumber);
				rotated.setName("rotated-" + image.getName());
				rotatedSystem.updatePlot2D(rotated, ImageRotateTool.this.axes, monitor);
			} catch (Exception e1) {
				logger.error("Error rotating image:" + e1.getMessage());
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
		
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		System.out.println();
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(
			org.eclipse.dawnsci.plotting.api.region.MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(
			org.eclipse.dawnsci.plotting.api.region.MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDoubleClicked(
			org.eclipse.dawnsci.plotting.api.region.MouseEvent me) {
		// TODO Auto-generated method stub
		
	}
}
