/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.util.io.IOUtils;
import org.dawnsci.common.widgets.radio.RadioGroupWidget;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.function.MapToRotatedCartesian;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.richbeans.widgets.content.FileContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.optimize.ApachePolynomial;

/**
 * Tool to remap ARPES data
 * @author wqk87977
 *
 */
public class ImageARPESRemappingProcessTool extends ImageProcessingTool {

	private final Logger logger = LoggerFactory.getLogger(ImageARPESRemappingProcessTool.class);

	private Spinner smoothingSpinner;
	private int smoothLevel = 1;

	private enum NormaliseType{
		ROI, FERMI;
	}

	@SuppressWarnings("unused")
	private NormaliseType type = NormaliseType.ROI;

	private Text inputLocation;
	private String inputFile;
	private Button inputBrowse;
	private Dataset correctedData;
	private List<IDataset> correctedAxes;
	private Dataset energyMap;
	private Dataset angleMap;
	private Text photonEnergyText;
	private Text workFunctionText;
	private Label workFunctionLabel;
	private Label photonEnergyLabel;
	private Double photonEnergy;
	private Double workFunction;
	private Label angleOffsetLabel;
	private Text angleOffsetText;
	private Label energyOffsetLabel;
	private Text energyOffsetText;
	protected Double energyOffset;
	protected Double angleOffset;

	public ImageARPESRemappingProcessTool() {
	}

	@Override
	protected void configureSelectionPlottingSystem(IPlottingSystem<Composite> plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void configureReviewPlottingSystem(IPlottingSystem<Composite> plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createControlComposite(Composite parent) {
		try {
			Group radioGroupNorm = new Group(parent, SWT.NONE);
			radioGroupNorm.setLayout(new GridLayout(1, false));
			radioGroupNorm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			radioGroupNorm.setText("Normalisation type");
			RadioGroupWidget normRadios = new RadioGroupWidget(radioGroupNorm);
			normRadios.setActions(createNormActions());

			Composite auxComp = new Composite(radioGroupNorm, SWT.NONE);
			auxComp.setLayout(new GridLayout(1, false));
			auxComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			createInputField(auxComp);

			Composite smoothingComp = new Composite(parent, SWT.NONE);
			smoothingComp.setLayout(new GridLayout(2, false));
			Label smoothingLabel = new Label(smoothingComp, SWT.NONE);
			smoothingLabel.setText("Smoothing:");
			smoothingSpinner = new Spinner(smoothingComp, SWT.BORDER);
			smoothingSpinner.setMinimum(1);
			smoothingSpinner.setMaximum(Integer.MAX_VALUE);
			smoothingSpinner.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					smoothLevel = smoothingSpinner.getSelection();
					updateProfiles(false);
				}
			});
		} catch (Exception e) {
			logger.error("Could not create controls", e);
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		//region = (IRegion)evt.getSource();
		//updateProfiles(region, evt.getROI(), false);
	}
	
	@Override
	public void roiChanged(ROIEvent evt) {
		region = (IRegion)evt.getSource();
		updateProfiles(region, region.getROI(), false);
	}
	
	private List<Action> createNormActions(){
		List<Action> radioActions = new ArrayList< Action>();
		Action roiOnlyAction = new Action() {
			@Override
			public void run() {
				type = NormaliseType.ROI;
				updateProfiles(true);
				setInputFieldEnabled(false);
			}
		};
		roiOnlyAction.setText("None");
		roiOnlyAction.setToolTipText("ROI only");

		Action fermiRemapAction = new Action() {
			@Override
			public void run() {
				type = NormaliseType.FERMI;
				updateProfiles(true);
				setInputFieldEnabled(true);
			}
		};
		fermiRemapAction.setText("ROI normalisation");
		fermiRemapAction.setToolTipText("ROI with Fermi Edge Compensation");

		radioActions.add(roiOnlyAction);
		radioActions.add(fermiRemapAction);
		return radioActions;
	}

	private void createInputField(Composite parent){
		Label lb = new Label(parent, SWT.NONE);
		lb.setText("Choose Auxiliary Data Input:");

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		inputLocation = new Text(comp, SWT.BORDER);
		if (inputFile != null)
			inputLocation.setText(inputFile);
		inputLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(inputLocation, 
				new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		inputLocation.setToolTipText("Input file location");
		inputLocation.addSelectionListener(getInputLocationListener());

		//add drop support
		DropTarget dt = new DropTarget(inputLocation, DND.DROP_MOVE
				| DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance() });
		dt.addDropListener(getDropTargetEvent());

		inputBrowse = new Button(comp, SWT.NONE);
		inputBrowse.setText("...");
		inputBrowse.setToolTipText("Choose Auxiliary Data Input");
		inputBrowse.addSelectionListener(getInputBrowseListener());

		setInputFieldEnabled(true);
		
		
		// now add 2 fields for setting angle and energy offfsets
		angleOffsetLabel = new Label(comp, SWT.NONE);
		angleOffsetLabel.setText("Angle Offset");
		angleOffsetText  = new Text(comp, SWT.NONE);
		angleOffsetText.setText("0.0");
		energyOffsetLabel = new Label(comp, SWT.NONE);
		energyOffsetLabel.setText("Energy Offset");
		energyOffsetText  = new Text(comp, SWT.NONE);
		energyOffsetText.setText("0.0");
		
		// now add 2 fields for setting the numbers for Photon Energy and Work Function
		workFunctionLabel = new Label(comp, SWT.NONE);
		workFunctionLabel.setText("Work Function");
		workFunctionText  = new Text(comp, SWT.NONE);
		workFunctionText.setText("2.1");
		photonEnergyLabel = new Label(comp, SWT.NONE);
		photonEnergyLabel.setText("Photon Energy");
		photonEnergyText  = new Text(comp, SWT.NONE);
		photonEnergyText.setText("20.0");
		
	}

	private void setInputFieldEnabled(boolean value){
		inputLocation.setEnabled(value);
		inputBrowse.setEnabled(value);
	}

	private SelectionListener getInputLocationListener(){
		return new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String path = inputLocation.getText();
				loadAuxiliaryData(path);
//				else {
//					inputLocation.setText(inputFile);
//				}
			}
		};
	}

	private SelectionListener getInputBrowseListener(){
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fChooser = new FileDialog(getSite().getShell());
				fChooser.setText("Choose file to load from");
				fChooser.setFilterPath(inputFile);
				final String path = fChooser.open();
				loadAuxiliaryData(path);
			}
		};
	}
	
	private DropTargetAdapter getDropTargetEvent(){
		return new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				String path = null;
				Object data = event.data;
				if (data instanceof IResource[]) {
					IResource[] res = (IResource[]) data;
					path = res[0].getRawLocation().toOSString();
				} else if (data instanceof File[]) {
					path = ((File[]) data)[0].getAbsolutePath();
				}
				if (path != null) {
					inputLocation.setText(path);
					loadAuxiliaryData(path);
				}
			}
		};
	}

	private void loadAuxiliaryData(String path){
		if (path == null)return;
		if (IOUtils.checkFile(path, true)) {
			inputFile = path;
			auxiliaryData = loadDataset(path);
			if(auxiliaryData == null) return;

			updateProfiles(true);
		}
	}

	@Override
	protected void createSelectionProfile(IImageTrace image, IROI roi, IProgressMonitor monitor) {
		
		logger.debug("Calling the energy remapping update");
		
		correctedData = DatasetUtils.convertToDataset(originalData.clone());
		correctedAxes = originalAxes;
		
		if (auxiliaryData != null) {
			
			Dataset tmpProfile = DatasetUtils.convertToDataset(auxiliaryData.clone());
			
			if(smoothLevel > 1){
				try {
					tmpProfile = ApachePolynomial.getPolynomialSmoothed(DatasetUtils.convertToDataset(originalAxes.get(1)), tmpProfile, smoothLevel, 3);
				} catch (Exception e) {
					logger.error("Could not smooth the plot", e);
				}
			}
			
			IDataset x = originalAxes.get(1);
			List<IDataset> ys = new ArrayList<IDataset>(1);
			ys.add(tmpProfile);
			reviewPlottingSystem.updatePlot1D(x, ys, null);
			
			Dataset newEnergyAxis = Maths.subtract(originalAxes.get(0), auxiliaryData.mean());
			
			Dataset differences = Maths.subtract(auxiliaryData, auxiliaryData.mean());
			
			double meanSteps = (originalAxes.get(0).max().doubleValue()-originalAxes.get(0).min().doubleValue())/(float)originalAxes.get(0).getShape()[0];
			
			Dataset differenceInts = Maths.floor(Maths.divide(differences, meanSteps));
			int[] shape = originalData.getShape();
			correctedData = DatasetFactory.zeros(DoubleDataset.class, shape);
			for(int y = 0; y < shape[0]; y++) {
				int min = Math.max(differenceInts.getInt(y), 0);
				int max = Math.min(shape[1]+differenceInts.getInt(y), shape[1]);
				int ref = 0;
				for(int xx = min; xx < max; xx++) {
					correctedData.set(originalData.getObject(y,xx), y,ref);
					ref++;
				}
			}
			
			//correctedData = InterpolatorUtils.remapOneAxis((Dataset) originalData, 1, (Dataset) tmpProfile, (Dataset) originalAxes.get(0), newEnergyAxis);
			correctedAxes = new ArrayList<IDataset>();
			correctedAxes.add(newEnergyAxis.clone());
			correctedAxes.add(originalAxes.get(1).clone());
		}
		
		selectionPlottingSystem.createPlot2D(correctedData, correctedAxes, null);
		
		
//		Dataset data = (Dataset)image.getData();
//		Dataset ds = data.clone();
//
//		if(originalData == null) return;
//		IDataset currentData = getPlottingSystem().getTraces().iterator().next().getData();
//		//if(!currentData.equals(originalData))
//		//	getPlottingSystem().updatePlot2D(originalData, originalAxes, monitor);
//
//		if(selectionPlottingSystem.getTraces().size()==0){
//			selectionPlottingSystem.updatePlot2D(originalData, originalAxes, monitor);
//		} else {
//			IDataset selectionData = selectionPlottingSystem.getTraces().iterator().next().getData();
//			if(!selectionData.equals(originalData))
//				selectionPlottingSystem.updatePlot2D(originalData, originalAxes, monitor);
//		}
	}

	@Override
	protected void createReviewProfile(IProgressMonitor monitor) {
		
		logger.debug("Calling the ROI update");
		
		photonEnergy = null;
		workFunction = null;
		angleOffset = null;
		energyOffset = null;
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					angleOffset = Double.parseDouble(angleOffsetText.getText());
					energyOffset = Double.parseDouble(energyOffsetText.getText());
					photonEnergy = Double.parseDouble(photonEnergyText.getText());
					workFunction = Double.parseDouble(workFunctionText.getText());
				} catch (Exception e) {
					logger.error("Cannot parse offsets, work function or photon energy", e);
				}
			}
		});
		
		if (photonEnergy == null || workFunction == null || angleOffset == null || energyOffset == null) {
			logger.error("Photon energy, work function angle or energy offset not valid");
			return;
		}
		
		// modify the axes of the main plot if offset
		ArrayList<Dataset> updatedAxes = new ArrayList<Dataset>();
		updatedAxes.add(Maths.add(correctedAxes.get(0),energyOffset));
		updatedAxes.add(Maths.add(correctedAxes.get(1),angleOffset));

		
		selectionPlottingSystem.updatePlot2D(correctedData, updatedAxes, monitor);
		// Now create the full size maps
		energyMap = updatedAxes.get(0);
		energyMap = energyMap.reshape(1,energyMap.getSize());
		energyMap = DatasetUtils.tile(energyMap, correctedData.getShapeRef()[0], 1);
		
		// need to calculate angleRegion here
		angleMap = updatedAxes.get(1);
		angleMap = angleMap.reshape(angleMap.getShapeRef()[0],1);
		angleMap = DatasetUtils.tile(angleMap, correctedData.getShapeRef()[1]);
		
		
		createMomentumDatasets();
	}

	private IDataset loadDataset(final String path) {
		try {
			auxiliaryData = LoaderFactory.getDataSet(path,
					"/entry/calibration/fittedMu/data", null).squeeze();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not load the auxiliary data", e);
		}
		return auxiliaryData;
	}
	
	private void createMomentumDatasets() {
		// now check to see what the offsets are before calculating the conversions
		// TODO these should be read off the GUI
		if (correctedData == null) return;
		

		
		// then get the regions
		IRegion roi = selectionPlottingSystem.getRegion("Processed Region 1");
		MapToRotatedCartesian map = new MapToRotatedCartesian((RectangularROI)roi.getROI());

		Dataset dataRegion = map.value(correctedData).get(0);
		Dataset energyRegion = map.value(energyMap).get(0);
		Dataset angleRegion = map.value(angleMap).get(0);
	
		
		// No calculate the energies
		// TODO could be optimised
		DoubleDataset photonEnergyDS = DatasetFactory.ones(DoubleDataset.class, dataRegion.getShapeRef()).imultiply(photonEnergy);
		DoubleDataset workFunctionDS = DatasetFactory.ones(DoubleDataset.class, dataRegion.getShapeRef()).imultiply(workFunction);
				
		DoubleDataset bindingEnergy = DatasetFactory.ones(DoubleDataset.class, energyRegion.getShapeRef()).imultiply(0);
		
		bindingEnergy.iadd(photonEnergyDS);
		bindingEnergy.isubtract(workFunctionDS);
		bindingEnergy.isubtract(energyRegion);
		
		if (bindingEnergy.min().doubleValue() < 0.0) {
			logger.error("Binding energy is less than zero, aborting");
			return;
		}
		
		//TODO this is an approximate value, should probably be corrected.
		Dataset k = Maths.sqrt(bindingEnergy).imultiply(0.51168);
				
		// Finally calculate k parallel
		Dataset kParallel = Maths.multiply(k, Maths.sin(Maths.toRadians(angleRegion)));
		
		// Caluclate the axis for display
		double bindingEnergyValue = photonEnergy - workFunction;
		double kValue = Math.sqrt(bindingEnergyValue) * 0.51168;
		Dataset kParaAxis = Maths.multiply(Maths.sin(Maths.toRadians(angleRegion.getSlice(new int[] {0,0}, new int[] {angleRegion.getShape()[0],1}, new int[] {1,1}).squeeze())), kValue);
		
		//logger.error("Max and min values are {} and {}", kParallel1D.min(), kParallel1D.max());
		
		//Dataset kParaAxis = kParallel.getSlice(new int[] {0,fermiSurfacePosition}, new int[] {kParallel.getShape()[0],fermiSurfacePosition+1}, new int[] {1,1}).squeeze();
		logger.error("Max and min values are {} and {}", kParaAxis.min(), kParaAxis.max());

		// make axis correction to regrid here
		//double KPStep = kParallel.peakToPeak().doubleValue()/(dataRegion.getShape()[0]-1);
		//Dataset kParaAxis = DatasetFactory.createRange(kParallel.min().doubleValue()+(KPStep), kParallel.max().doubleValue()-(KPStep), KPStep, Dataset.FLOAT64);
				
		// prepare the results
		// Dataset remappedRegion = InterpolatorUtils.remapAxis(dataRegion, 0, kParallel, kParaAxis);
		Dataset remappedRegion = dataRegion;
		ArrayList<IDataset> remappedAxes = new ArrayList<IDataset>();
		kParaAxis.setName("K Parallel (A-1)");
		Dataset energyAxis = energyRegion.getSlice(new int[] {0,0}, new int[] {1,energyRegion.getShapeRef()[1]}, new int[] {1,1}).squeeze();
		energyAxis.imultiply(-1.0);
		energyAxis.setName("Binding Energy (eV)");
		remappedAxes.add(energyAxis);
		remappedAxes.add(kParaAxis);
		
//		userPlotBean.addList("remapped", remappedRegion.clone());
//		userPlotBean.addList("remapped_energy", remappedAxes.get(0).clone());
//		userPlotBean.addList("remapped_k_parallel", remappedAxes.get(1).clone());
//		
//		// items which need to be saved for the batch processing
//		userPlotBean.addRoi("mapping_roi", roi.getROI());
//		userPlotBean.addList("kParallel", kParallel.clone());
//		userPlotBean.addList("kParaAxis", kParaAxis.clone());
//		if (auxiliaryData != null) {
//			userPlotBean.addList("auxiliaryData", auxiliaryData.clone());
//		}
//		
//		userPlotBean.addScalar("photonEnergy", photonEnergy.toString());
//		userPlotBean.addScalar("workFunction", workFunction.toString());
		
		getPlottingSystem().createPlot2D(remappedRegion, remappedAxes , null);
		
	}
}
