/*-
 * Copyright 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.processing.ui.roiprofile;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.processing.ui.model.AbstractOperationModelWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationDataForDisplay;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.operations.roiprofile.PolygonIntegrationModel;

public class PolygonIntegrationModelWizardPage extends AbstractOperationModelWizardPage implements IROIListener {

	private static final Logger logger = LoggerFactory.getLogger(PolygonIntegrationModelWizardPage.class);
	private static final String POLYGON = "Polygon";
	
	private IPlottingSystem<Composite> plottingSystem;
	private Label sumLabel;
	
	private Job update = new Job("Integrating polygon") {
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (id != null) {
					Display.getDefault().syncExec(() -> sumLabel.setText("Sum: calculating..."));
					od = operation.execute(id.getData(), new ProgressMonitorWrapper(monitor));
					Serializable[] auxData = od.getAuxData();
					double sum = DatasetFactory.createFromObject(auxData[0]).getDouble();
					if (od instanceof OperationDataForDisplay) {
						Display.getDefault().syncExec(() -> {
							sumLabel.setText(String.format("Sum: %g", sum));
							((IImageTrace) trace).setMask(((OperationDataForDisplay)od).getDisplayData()[0]);
						});
					}
					
				}
			} catch (OperationCanceledException e) {
				// ignore
				Display.getDefault().syncExec(() -> sumLabel.setText(""));
				return Status.CANCEL_STATUS;
			} catch (Exception e) {
				Display.getDefault().syncExec(() -> sumLabel.setText(String.format("%s", e.getMessage())));
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	};
	
	private ITrace trace;

	public PolygonIntegrationModelWizardPage() {
		super();
		
	}

	public PolygonIntegrationModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		super(operation);
	}
	
	@Override
	public void createControl(Composite parent) {
		IDataset inputData = id.getData().clone().squeezeEnds();
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem(Composite.class);
			plottingSystem.createPlotPart(container, "Polygon Region of Interest", null, PlotType.IMAGE, null);
			plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			plottingSystem.setKeepAspect(false);
			AxesMetadata axesMetadata = inputData.getFirstMetadata(AxesMetadata.class);
			List<Dataset> axesMetadataList = Arrays.stream(axesMetadata.getAxes()).map(lz -> {
				try {
					return DatasetUtils.sliceAndConvertLazyDataset(lz).squeezeEnds();
				} catch (Exception e) {
					return null;
				}
			}).collect(Collectors.toList());
			if (axesMetadataList.stream().noneMatch(Objects::nonNull))
				axesMetadataList = null;
			trace = plottingSystem.createPlot2D(inputData, axesMetadataList, null);
			
			Button clearRegionButton = new Button(container, SWT.PUSH);
			clearRegionButton.setText("Clear region");
			clearRegionButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			sumLabel = new Label(container, SWT.NONE);
			sumLabel.setText("Sum: 0.0");
			sumLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			sumLabel.setAlignment(SWT.LEFT);
			
			IRegion region = plottingSystem.createRegion(POLYGON, RegionType.POLYGON);
			region.addROIListener(this);
			region.setUserRegion(false);
			PolygonIntegrationModel polygonIntegrationModel = (PolygonIntegrationModel) getModel();
			IROI regionOfInterest = polygonIntegrationModel.getRegionOfInterest();
			if (regionOfInterest != null) {
				region.setROI(regionOfInterest);
				plottingSystem.addRegion(region);
			}
			
			clearRegionButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					final PolygonIntegrationModel polygonIntegrationModel = (PolygonIntegrationModel) getModel();
					IRegion tempRegion = plottingSystem.getRegion(POLYGON);
					if (tempRegion != null) {
						tempRegion.removeROIListener(PolygonIntegrationModelWizardPage.this);
						polygonIntegrationModel.setRegionOfInterest(null);
						plottingSystem.removeRegion(tempRegion);
					}
					try {
						tempRegion = plottingSystem.createRegion(POLYGON, RegionType.POLYGON);
					} catch (Exception e1) {
						logger.error("Could not create region!", e1);
						return;
					}
					Display.getDefault().syncExec(() -> ((IImageTrace) trace).setMask(null));
					tempRegion.addROIListener(PolygonIntegrationModelWizardPage.this);
					tempRegion.setUserRegion(false);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// do nothing
				}
			});
		} catch (Exception e) {
			logger.error("Could not create plotting system", e);
		}
		
		setControl(container);
	}

	private void updateModel(ROIEvent evt) {
		IROI roi = evt.getROI();
		PolygonIntegrationModel polygonIntegrationModel = (PolygonIntegrationModel) getModel();
		polygonIntegrationModel.setRegionOfInterest(roi);
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		updateModel(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		updateModel(evt);
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// this shouldnt trigger a recalculation
	}
	
	@Override
	protected void update() {
		logger.debug("Running update");
		update.cancel();
		update.schedule();
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		if (plottingSystem != null && !plottingSystem.isDisposed()) {
			IRegion region = plottingSystem.getRegion(POLYGON);
			if (region != null) {
				region.removeROIListener(this);
				plottingSystem.removeRegion(region);
			}
			plottingSystem.dispose();
		}
	}
	
}
