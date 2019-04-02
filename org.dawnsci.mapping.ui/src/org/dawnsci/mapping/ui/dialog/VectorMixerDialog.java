/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.dawnsci.mapping.ui.dialog;


import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.VectorMapData;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace.ArrowConfiguration;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace.VectorNormalization;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// @author Tim Snow


// A class to create the Vector graph creation dialog for the mapping perspective in DAWN
public class VectorMixerDialog extends Dialog  {

	// For errors!
	private static Logger logger = LoggerFactory.getLogger(VectorMixerDialog.class);

	// A place to store the data
	private List<Dataset> data;
	private VectorMapData vectorMap;
	private List<AbstractMapData> maps;

	// For the plotting system
	private IPlottingSystem<Composite> plotSystem;
	
	// For the spinner boxes
	private int plotIndex = -1;
	private int vectorDirectionIndex = -1;
	private int vectorMagnitudeIndex = -1;


	// Class initiation
	public VectorMixerDialog(Shell parent, List<AbstractMapData> passedData) throws Exception {
		// Do some setup
		super(parent);
		
		// Check that there has been some data passed
		if (passedData.isEmpty()) {
			throw new Exception("No data is available to visualize in the Vector Mixer dialog.");
		}
		
		// Find out how big the data that's been passed is for the first frame
		int width = passedData.get(0).getMap().getShape()[1];
		int height = passedData.get(0).getMap().getShape()[0];
		
		this.data = new ArrayList<Dataset>();

		// Looping through the subsequent datasets, or frames...
		for (AbstractMapData loopFrame : passedData) {
			// Check that the subsequent data is of the same size
			if (width != loopFrame.getMap().getShape()[1] || height != loopFrame.getMap().getShape()[0]) {
				throw new Exception("Data has not the same size");
			}
			
			// And if it is, convert this to a dataset and stick it in the classes data container
			this.data.add(DatasetUtils.convertToDataset(loopFrame.getMap()));
		}
		
		// Then 
		this.maps = passedData;
		
		// Try to create a plotting system and handle any errors that we might encounter
		try {
			this.plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception plottingError) {
			String error = "Error creating plotting system:" + plottingError.getMessage();
			logger.error("Error creating plotting system:", plottingError);
			throw new Exception(error);
		}
	}


	// Create the dialog window
	@Override
	public Control createDialogArea(Composite parent)  {
		// Set up an overall container for the dialog box complete with layout information
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create a home for the graph to live in
		Composite topPane = new Composite(container, SWT.NONE);
		topPane.setLayout(new GridLayout(1, false));
		topPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Then stick the plot into the container
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(topPane, null);
		plotSystem.createPlotPart(topPane, "Plot", actionBarWrapper, PlotType.IMAGE, null);
		plotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Now for the container for the user options
		Composite bottomPane = new Composite(container, SWT.NONE);
		bottomPane.setLayout(new GridLayout(3, false));
		bottomPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// Generate combo options
		String[] dataNames = new String[maps.size() + 1];
		dataNames[0] = "None";
		for (int i = 0; i < maps.size(); i ++) {
			dataNames[i + 1] = maps.get(i).toString();
		}

		// Create a place for the GUI objects to live
		Composite plotComposite= new Composite(bottomPane, SWT.NONE);
		// Declare the layout
		plotComposite.setLayout(new GridLayout(1, false));
		// Give the combo box a label
		Label plotLabel = new Label(plotComposite, SWT.RIGHT);
		// Set the title
		plotLabel.setText("Plot dataset");
		// Insert the combo box
		final Combo plotCombo = new Combo(plotComposite, SWT.CENTER);
		// Put it in the middle of the composite layout region
		plotCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		// Set the dataset names as populable items within the list
		plotCombo.setItems(dataNames);
		// Select the default of "None"
		plotCombo.select(0);
		// Listen out in case the user should do something to the GUI
		plotCombo.addSelectionListener(new SelectionAdapter() {
			// Handle the event
			@Override
			public void widgetSelected(SelectionEvent event) {
				plotIndex = plotCombo.getSelectionIndex() - 1;
				updatePlot();
			}
		});
		
		
		// Create a place for the GUI objects to live
		Composite vectorDirectionComposite= new Composite(bottomPane, SWT.NONE);
		// Declare the layout
		vectorDirectionComposite.setLayout(new GridLayout(1, false));
		// Give the combo box a label
		Label vectorDirectionLabel = new Label(vectorDirectionComposite, SWT.RIGHT);
		// Set the title
		vectorDirectionLabel.setText("Vector direction dataset");
		// Insert the combo box
		final Combo vectorDirectionCombo = new Combo(vectorDirectionComposite, SWT.CENTER);
		// Put it in the middle of the composite layout region
		vectorDirectionCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		// Set the dataset names as populable items within the list
		vectorDirectionCombo.setItems(dataNames);
		// Select the default of "None"
		vectorDirectionCombo.select(0);
		// Listen out in case the user should do something to the GUI
		vectorDirectionCombo.addSelectionListener(new SelectionAdapter() {
			// Handle the event
			@Override
			public void widgetSelected(SelectionEvent event) {
				vectorDirectionIndex = vectorDirectionCombo.getSelectionIndex() - 1;
				updatePlot();
			}
		});


		// Create a place for the GUI objects to live
		Composite vectorMagnitudeComposite= new Composite(bottomPane, SWT.NONE);
		// Declare the layout
		vectorMagnitudeComposite.setLayout(new GridLayout(1, false));
		// Give the combo box a label
		Label vectorMagnitudeLabel = new Label(vectorMagnitudeComposite, SWT.RIGHT);
		// Set the title
		vectorMagnitudeLabel.setText("Vector magnitude dataset");
		// Insert the combo box
		final Combo vectorMagnitudeCombo = new Combo(vectorMagnitudeComposite, SWT.CENTER);
		// Put it in the middle of the composite layout region
		vectorMagnitudeCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		// Set the dataset names as populable items within the list
		vectorMagnitudeCombo.setItems(dataNames);
		// Select the default of "None"
		vectorMagnitudeCombo.select(0);
		// Listen out in case the user should do something to the GUI
		vectorMagnitudeCombo.addSelectionListener(new SelectionAdapter() {
			// Handle the event
			@Override
			public void widgetSelected(SelectionEvent event) {
				vectorMagnitudeIndex = vectorMagnitudeCombo.getSelectionIndex() - 1;
				updatePlot();
			}
		});

		// Return the GUI
		return container;
	}

		// Get an estimate for how big the display window needs to be
	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	
	// The user should be able to resize this window
	@Override
	protected boolean isResizable() {
	    return true;
	}

	
	public VectorMapData getVectorMap() {
		return this.vectorMap;
	}


	private void updatePlot() {
		// Check there's some data and if not break out
		if (this.data.isEmpty()) {
			return;
		}

		plotSystem.clear();
		
		if (plotIndex >= 0 ) {
			AbstractMapData plotMapData = this.maps.get(this.plotIndex);
			updatePlotTrace(plotMapData);
		}
		
		// Then let's see what the user wants to plot
		if (vectorDirectionIndex >= 0 && vectorMagnitudeIndex >= 0) {
			// If the user has selected a plot and vector data

			// First, get the plot data from the map
			AbstractMapData vectorDirectionData = this.maps.get(this.vectorDirectionIndex);
			AbstractMapData vectorMagnitudeData = this.maps.get(this.vectorMagnitudeIndex);

			// Update the relevant traces
			updateVectorTrace(vectorDirectionData, vectorMagnitudeData);

			// and return
			return;
		}
	}
		

	private void updatePlotTrace (AbstractMapData plotMapData) {
		// For updating the plot's 'trace' on screen

		// First, check that there's a vector map data holder object and if not create it
		if (this.vectorMap == null) {
			this.vectorMap = new VectorMapData(plotMapData.toString(), plotMapData, plotMapData.getParent().getPath());
		}


		// Update plotting system with values, manually so that the global range is set for the vector trace on top
		
		// First set up a trace object
		IImageTrace plotTrace = MetadataPlotUtils.buildTrace("Vector Plot", plotMapData.getMap(), plotSystem);
		
		// Set the global range and axes
		plotTrace.setGlobalRange(plotMapData.getRange());
		
		// Add it to the plotting system and repaint
		plotSystem.addTrace(plotTrace);
		
		IAxis xAxis = plotSystem.getSelectedXAxis();
		if (xAxis != null) xAxis.setInverted(false);
		
		IAxis yAxis = plotSystem.getSelectedYAxis();
		if (yAxis != null) yAxis.setInverted(true);
		
		plotTrace.getImageServiceBean().setTransposed(false);
		plotTrace.getImageServiceBean().setOrigin(ImageOrigin.TOP_LEFT);
		
		plotSystem.repaint(false);
	}


	private void updateVectorTrace (AbstractMapData vectorDirectionData, AbstractMapData vectorMagnitudeData) {
		// For updating the vector trace on screen
		
		// First, check that there's a vector map data holder object and if not create it
		if (this.vectorMap == null) {
			this.vectorMap = new VectorMapData(vectorDirectionData.toString(), vectorDirectionData, vectorDirectionData.getParent().getPath());
		}

		// Update the vector map data holder object with the vector data
		this.vectorMap.updateVectorData(vectorDirectionData, vectorMagnitudeData);
		
		IVectorTrace vectorPlot = plotSystem.createVectorTrace("Vector Plot");
		// And whilst setting up the plot, also define some plot specific options
		vectorPlot.setVectorNormalization(VectorNormalization.LINEAR);
		vectorPlot.setArrowColor(new int[] {200, 0, 0});
		vectorPlot.setCircleColor(new int[] {0, 200, 0});
		vectorPlot.setArrowConfiguration(ArrowConfiguration.THROUGH_CENTER);
		vectorPlot.setRadians(false);
		vectorPlot.setPercentageThreshold(new double[] {5,95});

		// Set the vector data in the plot object
		vectorPlot.setData(this.vectorMap.getMap(), vectorMap.getAxes());
		
		// Plot this trace on top of the 2D image, if present
		plotSystem.addTrace(vectorPlot);
	}
	
	@Override
	public boolean close() {
		if (plotSystem != null && !plotSystem.isDisposed()) plotSystem.dispose();
		return super.close();
	}
}
