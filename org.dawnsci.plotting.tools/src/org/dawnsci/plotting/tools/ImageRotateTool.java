/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.MapTo2DUtils;

public class ImageRotateTool extends AbstractToolPage {

	private Logger logger = LoggerFactory.getLogger(ImageRotateTool.class);
	private Spinner angleSpinner;
	private IDataset image;
	private Composite container;
	private IPlottingSystem<Composite> rotatedSystem;
	private ITraceListener traceListener;
	private RotateJob rotationJob;
	private List<IDataset> axes;
	// shape of resulting image (bounding box)
	private boolean hasSameShape = true;
	protected boolean hasAxesRemapped;
	private IRegion xHair, yHair;
	private String xName;
	private String yName;
	private Job remapJob;

	// used to monitor memory consumption
	private MemoryMXBean memoryBean;
	private Action resizeBBox;
	private Action remapAxes;
	private Action resetCrossHair;

	private final static String X_PREFIX = "X Profile";
	private final static String Y_PREFIX = "Y Profile";

	private Color white;

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
			public void traceUpdated(TraceEvent evt) {
				remapAxes(hasAxesRemapped);
			}
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

		memoryBean = ManagementFactory.getMemoryMXBean();
	}

	@Override
	public Control getControl() {
		return container;
	}

	@Override
	public void createControl(Composite parent) {
		white = new Color(Display.getDefault(), 255, 255, 255);
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setBackground(white);

		createActions();

		final IPageSite site = getSite();
		IActionBars actionBars = (site != null) ? site.getActionBars() : null;
		rotatedSystem.createPlotPart(container, getTitle(), actionBars,
				PlotType.IMAGE, getViewPart());
		rotatedSystem.getPlotComposite().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		IImageTrace image = getImageTrace();
		IDataset data = image != null ? image.getData() : null;
		if (data != null) {
			this.image = data;
			axes = getImageTrace().getAxes();
			rotatedSystem.updatePlot2D(data, axes, null);
		}

		Composite angleComp = new Composite(container, SWT.NONE);
		angleComp.setLayout(new GridLayout(2, false));
		angleComp.setBackground(white);
		Label labelAngle = new Label(angleComp, SWT.NONE);
		labelAngle.setText("Rotation angle");
		labelAngle.setBackground(white);
		labelAngle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		angleSpinner = new Spinner(angleComp, SWT.BORDER);
		angleSpinner.setDigits(1);
		angleSpinner.setToolTipText("Rotates the original image by n degrees around the center of the image");
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

		super.createControl(parent);
	}

	private void createActions() {
		resizeBBox = new Action("Resize Bounding Box", SWT.TOGGLE) {
			@Override
			public void run() {
				hasSameShape = !resizeBBox.isChecked();
				rotate();
			}
		};
		resizeBBox.setToolTipText("Resize the Bounding Box and do not crop the resulting rotated image");
		resizeBBox.setImageDescriptor(Activator.getImageDescriptor("icons/resize_bounding_box.png"));

		remapAxes = new Action("Remap Axes", SWT.TOGGLE) {
			@Override
			public void run() {
				hasAxesRemapped = remapAxes.isChecked();
				remapAxes(hasAxesRemapped);
			}
		};
		remapAxes.setToolTipText("Axes remapping is done so that the X and Y step size are the same");
		remapAxes.setImageDescriptor(Activator.getImageDescriptor("icons/axis_remapping.png"));

		resetCrossHair = new Action("Centre Cross Region") {
			@Override
			public void run() {
				if (rotatedSystem == null || rotatedSystem.getTraces().isEmpty())
					return;
				RectangularROI[] rois = getXYCenteredROIs();
				if (rois[0] != null)
					xHair.setROI(rois[0]);
				if (rois[1] != null)
					yHair.setROI(rois[1]);
			}
		};
		resetCrossHair.setToolTipText("Reset the Vertical and Horizontal ROIs positions to the center of the image");
		resetCrossHair.setImageDescriptor(Activator.getImageDescriptor("icons/reset_crosshair_center.png"));

		getSite().getActionBars().getToolBarManager().add(resizeBBox);
		getSite().getActionBars().getToolBarManager().add(resizeBBox);
		getSite().getActionBars().getToolBarManager().add(remapAxes);
		getSite().getActionBars().getToolBarManager().add(resetCrossHair);

		getSite().getActionBars().getMenuManager().add(new Separator());
		getSite().getActionBars().getMenuManager().add(resizeBBox);
		getSite().getActionBars().getMenuManager().add(remapAxes);
		getSite().getActionBars().getMenuManager().add(resetCrossHair);
	}

	protected void remapAxes(boolean hasAxesRemapped) {
		if (hasAxesRemapped) {
			if (remapJob == null) {
				remapJob = new Job("Remapping Axes") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						IImageTrace trace = (IImageTrace) getPlottingSystem().getTraces().iterator().next();
						if (trace == null)
							return Status.CANCEL_STATUS;
						List<IDataset> axes = trace.getAxes();
						if (axes == null)
							return Status.CANCEL_STATUS;
						IDataset xAxis = axes.get(0), yAxis = axes.get(1);
						if (xAxis == null || yAxis == null)
							return Status.CANCEL_STATUS;
						double[] xRange = new double[] { xAxis.min(true).doubleValue(),
								xAxis.max(true).doubleValue() }, yRange = new double[] {
								yAxis.min(true).doubleValue(), yAxis.max(true).doubleValue() };
						double xRangeValue = (xRange[1] - xRange[0]);
						double yRangeValue = (yRange[1] - yRange[0]);
						int[] shape = trace.getData().getShape();
						double xStep = xRangeValue / shape[1];
						double yStep = yRangeValue / shape[0];
						double newStep = xStep;
						if (yStep < xStep) {
							newStep = yStep;
						}
						int xNumber = (int) (xRangeValue / newStep);
						int yNumber = (int) (yRangeValue / newStep);
						List<Dataset> meshAxes = DatasetUtils.meshGrid(
								DatasetUtils.convertToDataset(yAxis),
								DatasetUtils.convertToDataset(xAxis));

						// Check for ratio between X and Y axes
						if ((xNumber > yNumber && ((xNumber/yNumber) > 10)) 
								|| (yNumber > xNumber && ((yNumber/xNumber) > 10))) {
							final String message = "The ratio between the X and Y Axis of the dataset is too big to apply an axes remapping.";
							logger.debug(message);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog
											.openWarning(Display.getDefault().getActiveShell(),
													"Warning", message);
								}
							});
							return Status.CANCEL_STATUS;
						}
						// Check for image resulting size and increase the step if too big
						while(isImageTooBig(xNumber * yNumber)) {
							newStep *= 2;
							xNumber = (int) (xRangeValue / newStep);
							yNumber = (int) (yRangeValue / newStep);
						}

						image = MapTo2DUtils.remap2Dto2DSplitting(
								trace.getData(), meshAxes.get(0),
								meshAxes.get(1), yRange, yNumber, xRange,
								xNumber);

						image = DatasetUtils.transpose(image);
						ImageRotateTool.this.axes = new ArrayList<IDataset>();
						ImageRotateTool.this.axes.add(DatasetFactory.createRange(DoubleDataset.class, xRange[0], xRange[1], newStep));//  meshAxes.get(0));
						ImageRotateTool.this.axes.add(DatasetFactory.createRange(DoubleDataset.class, yRange[0], yRange[1], newStep));
						image.setName(getImageTrace().getDataName());
						// run the rotation job
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								rotate();
							}
						});
						return Status.OK_STATUS;
					}
				};
			}
			if (remapJob.getState() == Job.RUNNING)
				remapJob.cancel();
			remapJob.schedule();
		} else {
			image = getImageTrace().getData();
			this.axes = getImageTrace().getAxes();
			image.setName(getImageTrace().getDataName());
			rotate();
		}
	}

	private boolean isImageTooBig(long dataSize) {
		dataSize *= 8 + 4; // evaluate the potential size of resulting dataset by multiplying
							// the datasize by 8 (8bytes for each item of the
							// dataset are used for doubles, floats on 64bit
							// machines) + 4 to count for the header of the
							// array, and put some restriction on the image size
							// not to consume of memory of the heap
		MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
		long maxMemory = heapUsage.getMax();
		long usedMemory = heapUsage.getUsed();
		long leftMemory = maxMemory - usedMemory;
		return dataSize >= leftMemory;
	}

	private void rotate() {
		Double angle = getSpinnerAngle();
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
			double xVal = trace.getData().getShape()[1] / 2;
			double yVal = trace.getData().getShape()[0] / 2;
			if (regions != null && !regions.isEmpty()) {
				Object[] regionsArray = regions.toArray();
				for (Object regionObj : regionsArray) {
					IRegion region = (IRegion)regionObj;
					if (region.getName().equals(xName)) {
						xRoi = (RectangularROI) region.getROI();
						xRoi.setPoint(new double[] {xVal, 0});
					}
					if (region.getName().equals(yName)) {
						yRoi = (RectangularROI) region.getROI();
						yRoi.setPoint(new double[] {0, yVal});
					}
				}
			} else {
				xRoi = new RectangularROI(xVal, 0,
						200, 200, 0);
				xRoi.setName(xName);
				yRoi = new RectangularROI(0, yVal,
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
				this.xName = RegionUtils.getUniqueName(Y_PREFIX, rotatedSystem);
				this.xHair = rotatedSystem.createRegion(xName, IRegion.RegionType.XAXIS_LINE);
				if (rois[0] != null)
					xHair.setROI(rois[0]);
				addRegion("Updating x cross hair", xHair);
			}
			if (yHair == null
					|| rotatedSystem.getRegion(yHair.getName()) == null) {
				this.yName = RegionUtils.getUniqueName(X_PREFIX, getPlottingSystem());
				this.yHair = rotatedSystem.createRegion(yName, IRegion.RegionType.YAXIS_LINE);
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
		rotatedSystem.setFocus();
	}

	/**
	 * Add the trace listener and plot initial data
	 */
	@Override
	public void activate() {
		deactivate();

		super.activate();

		if (getPlottingSystem() != null) {
			getPlottingSystem().addTraceListener(traceListener);
			if (getImageTrace() != null) {
				image = getImageTrace().getData();
				axes = getImageTrace().getAxes();
			}
		}
		createRegions();
		if (xHair!=null) {
			xHair.setVisible(true);
		}
		if (yHair!=null) {
			yHair.setVisible(true);
		}
	}

	/**
	 * remove the trace listener to avoid unneeded event triggering
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
	}

	@Override
	public void dispose() {
		if (rotatedSystem != null && !rotatedSystem.isDisposed())
			rotatedSystem.dispose();
		rotatedSystem = null;
		if (white != null)
			white.dispose();
		super.dispose();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (rotatedSystem.getAdapter(clazz) != null) {
			return rotatedSystem.getAdapter(clazz);
		}
		return super.getAdapter(clazz);
	}

	@Override
	public IPlottingSystem<Composite> getToolPlottingSystem() {
		return rotatedSystem;
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
				IDataset rotated = ServiceProvider.getService(IImageTransform.class).rotate(image, angle, hasSameShape);
				rotated.setName("rotated-" + image.getName());
				rotatedSystem.updatePlot2D(rotated, axes, monitor);
			} catch (Exception e1) {
				logger.error("Error rotating image:" + e1.getMessage());
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}

	}
}
