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


package org.dawnsci.processing.ui.saxs;


import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
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
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;


//@author Tim Snow, adapted from original plug-in set by Tim Spain.


// A processing plugin wizard page to calculate the mean thickness of mineral crystals, for more information see:
//
// P. Fratzl, S. Schreiber and K. Klaushofer, Connective Tissue Research, 1996, 14, 247-254, DOI: 10.3109/03008209609005268
//
public class TParameterWizardPage extends AbstractOperationModelWizardPage implements IOperationSetupWizardPage {

	// Let's set up a logger first
	private static final Logger logger = LoggerFactory.getLogger(TParameterWizardPage.class);
	
	// Then set up our plotting environments
	private IPlottingSystem<Composite> inputPlotSystem;
	private IPlottingSystem<Composite> porodPlotSystem;
	private IPlottingSystem<Composite> kratkyPlotSystem;
	
	// Somewhere for our plotting data to be stored
	private IDataset inputData;
	private Dataset porodData;
	private Dataset porodFit;
	private Dataset kratkyData;
	private Dataset kratkyFit;
	private Dataset xAxis;
	private Label porodGradientLabel;
	
	// As well as the ranges over which to perform our fitting
	private double[] porodRange;
	private double[] kratkyRange;

	// And access to the model!
	private OperationModelViewer modelViewer;

	
	// Followed by some superclass setup
	public TParameterWizardPage() {
		super();
	}
	
	
	// And another for when there's more variables available
	public TParameterWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
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
		inputSash.setLayout(new GridLayout(4, false));
		inputSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				
		SashForm processingSash = new SashForm(container, SWT.VERTICAL);
		processingSash.setLayout(new GridLayout(3, false));
		processingSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Before handling the plots, let's find the ranges for the user interaction areas
		this.plotRangeFinder();

		// Then stick the input plot into the container
		ActionBarWrapper inputActionBar = ActionBarWrapper.createActionBars(inputSash, null);
		inputPlotSystem.createPlotPart(inputSash, "I(q) scatter (Input data)", inputActionBar, PlotType.XY, null);
		inputPlotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.redrawInputPlot();

		// Then stick the kratky plot into the container
		ActionBarWrapper kratkyActionBar = ActionBarWrapper.createActionBars(processingSash, null);
		kratkyPlotSystem.createPlotPart(processingSash, "Kratky Plot (Output data)", kratkyActionBar, PlotType.XY, null);
		kratkyPlotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.redrawKratkyPlot();
		
		// Then stick the porod plot into the container
		ActionBarWrapper porodActionBar = ActionBarWrapper.createActionBars(processingSash, null);
		porodPlotSystem.createPlotPart(processingSash, "Porod Plot", porodActionBar, PlotType.XY, null);
		porodPlotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.redrawPorodPlot();

		// And then the model container
		modelViewer = new OperationModelViewer();
		modelViewer.createPartControl(inputSash);
		modelViewer.setOperation(operation);
		
		// Finally, the gradient should be displayed for data fitting
		porodGradientLabel = new Label(inputSash, SWT.HORIZONTAL);
		// Let's set this label up with some placeholder text
		porodGradientLabel.setText("Porod line gradient is: NaN");
		
		// Set the default GUI weightings
		container.setWeights(new int[]{35, 65});
		inputSash.setWeights(new int[]{5, 70, 25, 5});
		processingSash.setWeights(new int[]{5, 45, 5, 45});

		// Set the controls
		setControl(parent);
	}
	
	
	private void redrawInputPlot() {
		// The plot view for the input data
		inputPlotSystem.setTitle("Input data");
		MetadataPlotUtils.plotDataWithMetadata(this.inputData, inputPlotSystem);
	}
		
	
	private void redrawPorodPlot() {
		// The plot view for the Porod plot of the data and the fit
		porodPlotSystem.setTitle("Porod Plot");
		MetadataPlotUtils.plotDataWithMetadata(this.porodFit, porodPlotSystem);
		MetadataPlotUtils.plotDataWithMetadata(this.porodData, porodPlotSystem, false);
		// Now we'll draw the interactive UI
		if (porodPlotSystem.getRegion("porodRange") == null) {
			try {
				// Create the region and it's limits
				IRegion porodSelectionRegion = porodPlotSystem.createRegion("porodRange", RegionType.XAXIS);
				IROI roi = new XAxisBoxROI(this.porodRange[0], 0, this.porodRange[1] - this.porodRange[0], 0, 0);
				porodSelectionRegion.setROI(roi);
				// Add the region
				porodPlotSystem.addRegion(porodSelectionRegion);
				// Now we'll create a new listener for this plot region
				porodSelectionRegion.addROIListener(new IROIListener.Stub() {
					@Override
					public void roiChanged(ROIEvent userEvent) {
						// Get the event
						IRectangularROI roi = userEvent.getROI().getBounds();
						// Put the new bounds into the kratkyRange
						porodRange[0] = roi.getPointX();
						porodRange[1] = roi.getLength(0) + roi.getPointX();
						// Set them in the model, if we can
						try {
							model.set("porodRange", porodRange);
						} catch (Exception kratkyError) {
							logger.warn("Couldn't set the Porod region from the Wizard: ", kratkyError);
						}
						
						// Set this in the model viewer and update
						modelViewer.setModel(model);
						regionChange();
						
						//porodGradientLabel.setText("Porod Gradient is: ");
						porodFit.getDouble(0);
					}
				});
			} catch (Exception regionError) {
				// Catching any errors as they come
				logger.warn("Error creating user area for selection on input data: ", regionError);
			}
		}
	}
	
	
	private void redrawKratkyPlot() {
		// The plot view for the Kratky plot of the data and the fit
		kratkyPlotSystem.setTitle("Kratky Plot");
		MetadataPlotUtils.plotDataWithMetadata(this.kratkyFit, kratkyPlotSystem);
		MetadataPlotUtils.plotDataWithMetadata(this.kratkyData, kratkyPlotSystem, false);
		// Now we'll draw the interactive UI
		if (kratkyPlotSystem.getRegion("kratkyRange") == null) {
			try {
				IRegion kratkySelectionRegion = kratkyPlotSystem.createRegion("kratkyRange", RegionType.XAXIS);
				IROI roi = new XAxisBoxROI(this.kratkyRange[0], 0, this.kratkyRange[1] - this.kratkyRange[0], 0, 0);
				kratkySelectionRegion.setROI(roi);
				kratkyPlotSystem.addRegion(kratkySelectionRegion);
				// Now we'll create a new listener for this plot region
				kratkySelectionRegion.addROIListener(new IROIListener.Stub() {
					@Override
					public void roiChanged(ROIEvent userEvent) {
						// Get the event
						IRectangularROI roi = userEvent.getROI().getBounds();
						// Put the new bounds into the kratkyRange
						kratkyRange[0] = roi.getPointX();
						kratkyRange[1] = roi.getLength(0) + roi.getPointX();
						// Set them in the model, if we can
						try {
							model.set("kratkyRange", kratkyRange);
						} catch (Exception kratkyError) {
							logger.warn("Couldn't set the Kratky region from the Wizard: ", kratkyError);
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
	
	
	private void plotRangeFinder() {
		// Get all the declared fields in the model
		List<Field> modelFields = Arrays.asList(this.model.getClass().getDeclaredFields());
		// And loop through them
		for (int loopIter = 0; loopIter < modelFields.size(); loopIter ++) {
			// Extracting the name of the field to start with
			String fieldName = modelFields.get(loopIter).getName();
			// Then perform a specific action if the field is either the porod or kratky range field
			switch (fieldName) {
			case "porodRange":
				// Extract the Porod range from the model
				this.porodRange = this.modelRangeExtractor(fieldName);
				break;
			case "kratkyRange":
				// Extract the Kratky range from the model
				this.kratkyRange = this.modelRangeExtractor(fieldName);
				break;
			default:
				// For all of the other field types we'll just break
				break;
			}
		}
	}
		
		
	private double[] modelRangeExtractor (String fieldName) {
		// Create a home for the range
		double[] range = null;
		try {
				// Try to fetch the field object
				Object genericFieldObject;
				genericFieldObject = model.get(fieldName);
				// And if it's not empty
				if (genericFieldObject != null) {
					// Get the range
					range = (double[]) genericFieldObject;
				} else {
					range = new double[]{this.xAxis.min(true).doubleValue(), this.xAxis.max(true).doubleValue()};
				}
			} catch (Exception modelReadException) {
				// Handling errors if they arise
				logger.error("An error occured retrieving the model object: ", modelReadException);
			}
		// Returning the result
		return range;
	}
	
	
	@Override
	protected void update() {
		// First, let's run the operation
		IMonitor tempMonitor = new IMonitor.Stub();
		OperationData outputData = operation.execute(id.getData(), tempMonitor);
		// Then access the return data
		Serializable[] outputAuxillaryData = outputData.getAuxData();
		// Putting it all in the appropriate datasets
		this.inputData = outputData.getData();
		this.porodData = (Dataset) outputAuxillaryData[0];
		this.porodFit = (Dataset) outputAuxillaryData[1];
		this.kratkyData = (Dataset) outputAuxillaryData[2];
		this.kratkyFit = (Dataset) outputAuxillaryData[3];
		this.xAxis = (Dataset) outputAuxillaryData[4];
		
		// It is useful for the operator to know what the Porod gradient is as, for these calculations
		// it should be 4, so let's give them this information
		String porodGradient = String.format("%.2f", ((Double) outputAuxillaryData[6]));
		
		if (this.porodGradientLabel != null) {
			this.porodGradientLabel.setText("Porod gradient is: " + porodGradient);

		}
		
		this.plotRangeFinder();

		// If we're doing this for the first time, i.e. startup, we should populate the wizard
		if (inputPlotSystem == null || porodPlotSystem == null || kratkyPlotSystem == null) {
			// Initialise the plotting system
			try {
				this.inputPlotSystem = PlottingFactory.createPlottingSystem();
				this.porodPlotSystem = PlottingFactory.createPlottingSystem();
				this.kratkyPlotSystem = PlottingFactory.createPlottingSystem();
			} catch (Exception plotSystemError) {
				logger.error("A plot system error has occured: ", plotSystemError.getMessage());
			}
		} else {
			// Otherwise we'll simply re-run the calculation and replot the data
			this.redrawInputPlot();
			this.redrawPorodPlot();
			this.redrawKratkyPlot();
		}
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		this.update();
	}
	
	
	public void regionChange() {
		update();
	}
	
	
	public IOperationModel getModel() {
		return model;
	}
	
	
	IOperation getOperation() {
		return operation;
	}
}
