/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.window;

import java.util.Collection;

import org.dawb.common.ui.util.DisplayUtils;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.dawnsci.plotting.tools.window.WindowTool.WindowJob;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to create the composite with spinners to control the Window tool
 * @author wqk87977
 *
 */
public class RegionControlWindow {

	private static final Logger logger = LoggerFactory.getLogger(RegionControlWindow.class);

	private Composite parent;
	private Spinner spnStartX;
	private Spinner spnStartY;
	private Spinner spnWidth;
	private Spinner spnHeight;
	private Button btnOverwriteAspect;
	private Spinner spnXAspect;
	private Spinner spnYAspect;
	private Button btnApplyClipping;
	private Spinner spnLowerClipping;
	private Spinner spnUpperClipping;
	private IPlottingSystem<Composite> windowSystem;
	private SelectionAdapter selectionListener;
	private IPlottingSystem<?> plottingSystem;
	private boolean isOverwriteAspect;
	private boolean isApplyClipping;

	public RegionControlWindow(Composite parent, 
			final IPlottingSystem<?> plottingSystem, 
			final IPlottingSystem<Composite> windowSystem, 
			final WindowJob windowJob) {
		this.parent = parent;
		this.plottingSystem = plottingSystem;
		this.windowSystem = windowSystem;
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOverwriteAspect = btnOverwriteAspect.getSelection();
				isApplyClipping = btnApplyClipping.getSelection();

				int startPosX = spnStartX.getSelection();
				int startPosY = spnStartY.getSelection();
				int width = spnWidth.getSelection();
				int height = spnHeight.getSelection();
				if (startPosX + width > spnWidth.getMaximum()) {
					width = spnWidth.getMaximum() - startPosX;
				}
				if (startPosY + height > spnHeight.getMaximum()) {
					height = spnHeight.getMaximum() - startPosY;
				}
				IRegion region = windowSystem.getRegion("Window");
				RectangularROI rroi = new RectangularROI(startPosX, startPosY, width, height, 0);

				if (!e.getSource().equals(btnOverwriteAspect) && !e.getSource().equals(btnApplyClipping)) {
					if (region != null)
						region.setROI(rroi);
				} else if (e.getSource().equals(btnOverwriteAspect)) {
					spnXAspect.setEnabled(isOverwriteAspect);
					spnYAspect.setEnabled(isOverwriteAspect);
				} else if (e.getSource().equals(btnApplyClipping)) {
					spnLowerClipping.setEnabled(isApplyClipping);
					spnUpperClipping.setEnabled(isApplyClipping);
				}
				SurfacePlotROI sroi = createSurfacePlotROI(width, height, true);
				windowJob.schedule(sroi, true);
			}
		};
	}

	/**
	 * Gets the trace or the first trace if there are more than one.
	 * @return
	 */
	protected ITrace getTrace() {
		if (plottingSystem == null) return null;

		final Collection<ITrace> traces = plottingSystem.getTraces();
		if (traces==null || traces.size()==0) return null;
		return traces.iterator().next();
	}

	protected ISurfaceTrace getSurfaceTrace() {
		final ITrace trace = getTrace();
		return trace instanceof ISurfaceTrace ? (ISurfaceTrace)trace : null;
	}

	public boolean isOverwriteAspect(){
		return isOverwriteAspect;
	}

	public boolean isApplyClipping() {
		return isApplyClipping;
	}

	public void setApplyClipping(boolean isApplyClipping) {
		this.isApplyClipping = isApplyClipping;
	}

	public int getXAspectRatio() {
		return spnXAspect.getSelection();
	}

	public int getYAspectRatio() {
		return spnYAspect.getSelection();
	}

	public int getLowerClipping() {
		return spnLowerClipping.getSelection();
	}

	public int getUpperClipping() {
		return spnUpperClipping.getSelection();
	}

	public Composite createRegionControl(String title, IPageSite site, IViewPart viewPart, ImageDescriptor imageDescriptor) {
		Composite windowComposite = new Composite(parent, SWT.NONE);
		windowComposite.setLayout(new FillLayout(SWT.VERTICAL));

		final Action reselect = new Action("Add ROI", imageDescriptor) {
			public void run() {
				createSurfaceRegion("Window", false);
			}
		};
		site.getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
		site.getActionBars().getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
		site.getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));

		windowSystem.createPlotPart(windowComposite, title, site.getActionBars(), PlotType.IMAGE, viewPart);

		int xStartPt = 0;
		int yStartPt = 0;
		final ITrace trace = getTrace();
		int xSize = 0, ySize = 0;
		if (trace != null && !(trace instanceof ILineStackTrace) && trace.getData() != null && trace.getData().getShape().length > 1) {
			xSize = trace.getData().getShape()[1];
			ySize = trace.getData().getShape()[0];
		} else {
			xSize = 1000;
			ySize = 1000;
		}

		Composite bottomComposite = new Composite(windowComposite,SWT.NONE | SWT.BORDER);
		bottomComposite.setLayout(new GridLayout(1, false));
		bottomComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite spinnersComp = new Composite(bottomComposite, SWT.NONE);
		spinnersComp.setLayout(new GridLayout(4, false));
		spinnersComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		Label lblStartX = new Label(spinnersComp, SWT.RIGHT);
		lblStartX.setText("Start X:");
		
		spnStartX = new Spinner(spinnersComp, SWT.BORDER);
		spnStartX.setMinimum(0);
		spnStartX.setMaximum(xSize);
		spnStartX.setSize(62, 18);
		spnStartX.addSelectionListener(selectionListener);

		Label lblStartY = new Label(spinnersComp, SWT.RIGHT);
		lblStartY.setText("Start Y:");

		spnStartY = new Spinner(spinnersComp, SWT.BORDER);
		spnStartY.setMinimum(0);
		spnStartY.setMaximum(ySize);
		spnStartY.setSize(62, 18);
		spnStartY.addSelectionListener(selectionListener);

		Label lblEndX = new Label(spinnersComp, SWT.RIGHT);
		lblEndX.setText("Width:");

		spnWidth = new Spinner(spinnersComp, SWT.BORDER);
		spnWidth.setMinimum(0);
		spnWidth.setMaximum(xSize);
		spnWidth.setSize(62, 18);
		spnWidth.addSelectionListener(selectionListener);

		Label lblEndY = new Label(spinnersComp, SWT.RIGHT);
		lblEndY.setText("Height:");

		spnHeight = new Spinner(spinnersComp, SWT.BORDER);
		spnHeight.setSize(62, 18);
		spnHeight.setMinimum(0);
		spnHeight.setMaximum(ySize);
		spnHeight.addSelectionListener(selectionListener);

		setSpinnerValues(xStartPt, yStartPt, xSize, ySize);

		Composite aspectComp = new Composite(bottomComposite, SWT.NONE); 
		aspectComp.setLayout(new GridLayout(4, false));
		aspectComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnOverwriteAspect = new Button(aspectComp,SWT.CHECK);
		btnOverwriteAspect.setText("Override Aspect-Ratio");
		btnOverwriteAspect.addSelectionListener(selectionListener);

		spnXAspect = new Spinner(aspectComp,SWT.BORDER);
		spnXAspect.setEnabled(false);
		spnXAspect.setMinimum(1);
		spnXAspect.setMaximum(10);
		spnXAspect.setSelection(1);
		spnXAspect.setIncrement(1);
		spnXAspect.addSelectionListener(selectionListener);

		Label lblDelimiter = new Label(aspectComp,SWT.NONE);
		lblDelimiter.setText(":");

		spnYAspect = new Spinner(aspectComp,SWT.BORDER);
		spnYAspect.setEnabled(false);
		spnYAspect.setMinimum(1);
		spnYAspect.setMaximum(10);
		spnYAspect.setSelection(1);
		spnYAspect.setIncrement(1);
		spnYAspect.addSelectionListener(selectionListener);

		btnApplyClipping = new Button(aspectComp,SWT.CHECK);
		btnApplyClipping.setText("Apply clipping");
		btnApplyClipping.setToolTipText("Upper - Lower >= 10000");
		btnApplyClipping.addSelectionListener(selectionListener);

		spnLowerClipping = new Spinner(aspectComp,SWT.BORDER);
		spnLowerClipping.setEnabled(false);
		spnLowerClipping.setMinimum(0);
		spnLowerClipping.setMaximum(Integer.MAX_VALUE);
		spnLowerClipping.setSelection(0);
		spnLowerClipping.setIncrement(100);
		spnLowerClipping.setToolTipText("Lower");
		spnLowerClipping.addSelectionListener(selectionListener);

		lblDelimiter = new Label(aspectComp,SWT.NONE);
		lblDelimiter.setText(":");

		spnUpperClipping = new Spinner(aspectComp,SWT.BORDER);
		spnUpperClipping.setEnabled(false);
		spnUpperClipping.setMinimum(0);
		spnUpperClipping.setMaximum(Integer.MAX_VALUE);
		spnUpperClipping.setSelection(100000);
		spnUpperClipping.setIncrement(100);
		spnUpperClipping.setToolTipText("Upper");
		spnUpperClipping.addSelectionListener(selectionListener);

		return windowComposite;
	}

	public boolean isControlReady() {
		if (spnStartX == null || spnStartY == null || spnWidth == null
				|| spnHeight == null || spnXAspect == null || spnYAspect == null
				|| btnOverwriteAspect == null || btnApplyClipping == null
				|| spnLowerClipping == null || spnUpperClipping == null)
			return false;
		return true;
	}

	public void createSurfaceRegion(String regionName, boolean isDrag) {
		IRegion region = windowSystem.getRegion(regionName);
		//create Region
		try {
			if (region == null) {
				region = windowSystem.createRegion(regionName, RegionType.BOX);

				ISurfaceTrace surface = getSurfaceTrace();
				IROI window = surface != null ? surface.getWindow() : null;
				if (window == null) {
					int height = surface.getData().getShape()[0];
					int width = surface.getData().getShape()[1];
					SurfacePlotROI sroi = createSurfacePlotROI(width, height, isDrag);
					region.setROI(sroi);
				} else {
					region.setROI(window);
				}

				windowSystem.addRegion(region);
			}
		} catch (Exception e) {
			logger.debug("Cannot create region for surface!", e);
		}
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public SurfacePlotROI createSurfacePlotROI(int width, int height, boolean isDrag) {
		int xAspectRatio = 0, yAspectRatio = 0, binShape = 1, samplingMode = 0, 
				lowerClipping = 0, upperClipping = 100000;
		if (isOverwriteAspect) {
			xAspectRatio = getXAspectRatio();
			yAspectRatio = getYAspectRatio();
		}
		if (isApplyClipping) {
			lowerClipping = getLowerClipping();
			upperClipping = getUpperClipping();
		}
		binShape = PlottingUtils.getBinShape(width, height, isDrag);
		if (binShape != 1) {
			// DownsampleMode.MEAN = 2
			samplingMode = 2;
		}
		SurfacePlotROI sroi = new SurfacePlotROI(spnStartX.getSelection(), 
				spnStartY.getSelection(), 
				spnStartX.getSelection() + spnWidth.getSelection(), 
				spnStartY.getSelection() + spnHeight.getSelection(), 
				samplingMode, samplingMode, 
				xAspectRatio, yAspectRatio);
		sroi.setLengths(width, height);
		sroi.setXBinShape(binShape);
		sroi.setYBinShape(binShape);
		sroi.setLowerClipping(lowerClipping);
		sroi.setUpperClipping(upperClipping);
		sroi.setIsClippingApplied(isApplyClipping);
		return sroi;
	}

	/**
	 * Set the spinner values
	 * @param startX start position in x dimension
	 * @param startY start position in y dimension
	 * @param width
	 * @param height
	 */
	protected void setSpinnerValues(final int startX, 
								 final int startY, 
								 final int width, 
								 final int height) {
		DisplayUtils.asyncExec(parent, new Runnable() {
			@Override
			public void run() {
				spnStartX.setSelection(startX);
				spnStartY.setSelection(startY);
				spnWidth.setSelection(width);
				spnHeight.setSelection(height);
			}
		});
	}

	public void addSelectionListener() {
		if (spnStartX != null && !spnStartX.isDisposed())
			spnStartX.addSelectionListener(selectionListener);
		if (spnStartY != null && !spnStartY.isDisposed())
			spnStartY.addSelectionListener(selectionListener);
		if (spnWidth != null && !spnWidth.isDisposed())
			spnWidth.addSelectionListener(selectionListener);
		if (spnHeight != null && !spnHeight.isDisposed())
			spnHeight.addSelectionListener(selectionListener);
	}

	public void removeSelectionListener() {
		if (spnStartX != null && !spnStartX.isDisposed())
			spnStartX.removeSelectionListener(selectionListener);
		if (spnStartY != null && !spnStartY.isDisposed())
			spnStartY.removeSelectionListener(selectionListener);
		if (spnWidth != null && !spnWidth.isDisposed())
			spnWidth.removeSelectionListener(selectionListener);
		if (spnHeight != null && !spnHeight.isDisposed())
			spnHeight.removeSelectionListener(selectionListener);
	}
}
