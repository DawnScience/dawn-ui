/*
* Copyright 2017 Diamond Light Source Ltd.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.dawnsci.processing.ui.oned;


import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import org.apache.commons.beanutils.BeanUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.model.AbstractOperationModelWizardPage;
import org.dawnsci.processing.ui.model.OperationModelViewer;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import uk.ac.diamond.scisoft.analysis.processing.operations.oned.DigitalFilterOperation;


//@author Tim Snow


public class DigitalFilterWizardPage extends AbstractOperationModelWizardPage implements IOperationSetupWizardPage {

	// Let's set up a logger first
	private static final Logger logger = LoggerFactory.getLogger(DigitalFilterWizardPage.class);
	
	// Then set up our plotting environments
	private IPlottingSystem<Composite> inputPlotSystem;
	private IPlottingSystem<Composite> outputPlotSystem;
	
	// Somewhere for our plotting data to be stored
	private IDataset inputData;
	private IDataset outputData;
	private IDataset xAxis;
	
	// As well as the ranges over which to perform our fitting
	private double peakRange;
	private double peakMin;
	private double peakMax;

	// And access to the model!
	private OperationModelViewer modelViewer;

	
	// Followed by some superclass setup
	public DigitalFilterWizardPage() {
		super();
	}
	
	
	// And another for when there's more variables available
	public DigitalFilterWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		super(operation);
	}

	
	// For initialisation of the wizard
	private void initAbstractOperationModelWizardPage() {
		try {
			// Instantiate a new model
			this.model = operation.getModel().getClass().newInstance();
			// Getting the old model
			this.omodel = operation.getModel();
			// Copying the properties from the old model back to the new one
			BeanUtils.copyProperties(this.model, this.omodel);
			// Then setting the current model for the processing operation
			operation.setModel(this.model);
		} catch (Exception modelError) {
			// Catching errors as we go
			logger.error("Could not instantiate default model! ", modelError);
		}
		if (this.model != null && this.model instanceof AbstractOperationModel) {
			// Now we can add property change listeners to the model, which should trigger updating of the UI
			((AbstractOperationModel)this.model).addPropertyChangeListener(this);
		}
	}
	
	
	// Create the control using an internal method
	@Override
	public void createControl(Composite parent) {
		// Create the dialog
		this.inputData = id.getData();
		this.xAxis = DigitalFilterOperation.xAxisExtractor(this.inputData);
		createDialogArea(parent);
	}
	
	
	// The window to display
	private void createDialogArea(Composite parent) {
		// Set up an overall container for the dialog box complete with layout information
		SashForm container = new SashForm(parent, SWT.HORIZONTAL);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Complete with subhomes for the graphs and model
		SashForm inputSash = new SashForm(container, SWT.VERTICAL);
		inputSash.setLayout(new GridLayout(3, false));
		inputSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				
		SashForm outputSash = new SashForm(container, SWT.VERTICAL);
		outputSash.setLayout(new GridLayout(2, false));
		outputSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Before handling the plots, let's find the ranges for the user interaction areas
		this.peakRangeFinder();

		// Then stick the input plot into the container
		ActionBarWrapper inputActionBar = ActionBarWrapper.createActionBars(inputSash, null);
		inputPlotSystem.createPlotPart(inputSash, "Input data", inputActionBar, PlotType.XY, null);
		inputPlotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.redrawInputPlot();

		// Then stick the kratky plot into the container
		ActionBarWrapper outputActionBar = ActionBarWrapper.createActionBars(outputSash, null);
		outputPlotSystem.createPlotPart(outputSash, "Filtered data", outputActionBar, PlotType.XY, null);
		outputPlotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.redrawOutputPlot();
		
		// And then the model container
		modelViewer = new OperationModelViewer();
		modelViewer.createPartControl(inputSash);
		modelViewer.setOperation(operation);
		
		// Set the default GUI weightings
		container.setWeights(new int[]{35, 65});
		inputSash.setWeights(new int[]{5, 75, 25,});
		outputSash.setWeights(new int[]{5, 95});

		// Set the controls
		setControl(parent);
	}
	
	
	private void redrawInputPlot() {
		// The plot view for the input data
		MetadataPlotUtils.plotDataWithMetadata(this.inputData, inputPlotSystem);
		inputPlotSystem.setTitle("Input data");
	}
		
	
	private void redrawOutputPlot() {
		// The plot view for the Porod plot of the data and the fit
		outputPlotSystem.clear();
		MetadataPlotUtils.plotDataWithMetadata(this.outputData, outputPlotSystem);
		outputPlotSystem.setTitle("Output data");
		// Now we'll draw the interactive UI
		if (inputPlotSystem.getRegion("peakRange") == null) {
			try {
				// Create the region and it's limits
				IRegion peakSelectionRegion = inputPlotSystem.createRegion("peakRange", RegionType.XAXIS);
				IROI roi = new XAxisBoxROI(this.xAxis.min(true).doubleValue(), 0, this.xAxis.max(true).doubleValue(), 0, 0);
				peakSelectionRegion.setROI(roi);
				// Add the region
				inputPlotSystem.addRegion(peakSelectionRegion);
				// Now we'll create a new listener for this plot region
				peakSelectionRegion.addROIListener(new IROIListener.Stub() {
					@Override
					public void roiChanged(ROIEvent userEvent) {
						// Get the event
						IRectangularROI roi = userEvent.getROI().getBounds();
						
						// Put the new bounds into the kratkyRange
						peakRange = roi.getLength(0);
						// Set them in the model, if we can
						try {
							model.set("firstFilterWidth", peakRange);
							model.set("secondFilterWidth", (peakRange / 2));
						} catch (Exception kratkyError) {
							logger.warn("Couldn't set the Porod region from the Wizard: ", kratkyError);
						}
						
						// Set this in the model viewer and update
						modelViewer.setModel(model);
						regionChange();
					}
				});
			} catch (Exception regionError) {
				// Catching any errors as they come
				logger.warn("Error creating user area for selection on input data: ", regionError);
			}
		}
	}
	
	
	private void peakRangeFinder() {
		// Get all the declared fields in the model
		List<Field> modelFields = Arrays.asList(this.model.getClass().getDeclaredFields());
		// And loop through them
		for (int loopIter = 0; loopIter < modelFields.size(); loopIter ++) {
			// Extracting the name of the field to start with
			String fieldName = modelFields.get(loopIter).getName();
			// Then perform a specific action for the firstFilterWidth field
			switch (fieldName) {
			case "firstFilterWidth":
				try {
					
					// Extract the first filter range from the model
					Object peakRangeObject = model.get(fieldName);
					// Then format it according to whether or not it exists
					if (peakRangeObject != null) {
						this.peakRange = (double) peakRangeObject;
					} else {
						this.peakRange = 0;
					} 
				} catch (Exception modelReadException) {
					// Handling errors if they arise
					logger.error("An error occured retrieving the model object: ", modelReadException);
				}
				break;
			default:
				// For all of the other field types we'll just break
				break;
			}
		}
	}
	
	
	@Override
	protected void update() {
		// First, let's run the operation
		IMonitor tempMonitor = new IMonitor.Stub();
		
		OperationData operationData = operation.execute(id.getData(), tempMonitor);
		// Then access the return data
		// Putting it all in the appropriate datasets
		this.outputData = operationData.getData();
		
		this.peakRangeFinder();

		// If we're doing this for the first time, i.e. startup, we should populate the wizard
		if (inputPlotSystem == null || outputPlotSystem == null) {
			// Initialise the plotting system
			try {
				this.inputPlotSystem = PlottingFactory.createPlottingSystem();
				this.outputPlotSystem = PlottingFactory.createPlottingSystem();
				
				// Then do the drawing
				this.redrawInputPlot();
				this.redrawOutputPlot();
			} catch (Exception plotSystemError) {
				logger.error("A plot system error has occured: ", plotSystemError.getMessage());
			}
		} else {
			// Otherwise we'll simply re-run the calculation and replot the data
			this.redrawOutputPlot();
		}
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		this.update();
	}
	
	
	public void regionChange() {
		this.update();
	}
	
	
}
