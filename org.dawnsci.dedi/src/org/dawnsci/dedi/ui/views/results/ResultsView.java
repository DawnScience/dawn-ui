package org.dawnsci.dedi.ui.views.results;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.measure.unit.Unit;

import org.dawnsci.dedi.configuration.calculations.NumericRange;
import org.dawnsci.dedi.configuration.calculations.results.controllers.AbstractResultsController;
import org.dawnsci.dedi.configuration.calculations.results.models.ResultConstants;
import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.calculations.scattering.D;
import org.dawnsci.dedi.configuration.calculations.scattering.DoubleTheta;
import org.dawnsci.dedi.configuration.calculations.scattering.Q;
import org.dawnsci.dedi.configuration.calculations.scattering.S;
import org.dawnsci.dedi.configuration.calculations.scattering.ScatteringQuantity;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.TextUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ResultsView extends ViewPart implements PropertyChangeListener {
	// The controller used to access and modify the results.
	private AbstractResultsController controller;
	
	private ScatteringQuantity<?> currentQuantity;
	private Unit<?> currentUnit;
	// One of the scattering quantities 
	private DoubleTheta doubleTheta;
	
	/*
	 * Fields that store the current requested values locally, i.e. these values are not managed by the results controller -
	 * the user input is likely to be invalid, at times, so requested values should be sent to the controller only once 
	 * they become valid, in order to protect other views from working with invalid data.
	 */
	private Double requestedMin;
	private Double requestedMax;
	
	
	// UI elements
	private Composite resultsPanel;
	private ComboViewer scatteringQuantitiesUnitsCombo;
	private Label minValueLabel;
	private Label maxValueLabel;
	private Label requestedMinValueLabel;
	private Label requestedMaxValueLabel;
	private Label minValue;
	private Label maxValue;
	private Text requestedMinValueText;
	private Text requestedMaxValueText;
	private Canvas drawingArea;
	
	
	
	/**
	 * Flag that indicates whether the values displayed in this view are currently being modified by 
	 * the user or programmatically. Set this flag to false when programmatically setting the values
	 * of a text field.
	 */
	private boolean isUserEdited = true;
	
	public static final String ID = "dedi.views.results";
	
	
	public ResultsView() {
		controller = ResultsService.getInstance().getController();
		controller.addView(this);
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		
		resultsPanel = new Composite(scrolledComposite, SWT.NONE);
		resultsPanel.setLayout(new GridLayout(3, true));
		
		GuiHelper.createLabel(resultsPanel, "Scattering quantity:");
		
		
		/*
		 * Scattering quantities and units combos.
		 */
		
		Combo scatteringQuantitiesCombo = new Combo(resultsPanel, SWT.READ_ONLY | SWT.RIGHT);
		ComboViewer scatteringQuantitiesComboViewer = new ComboViewer(scatteringQuantitiesCombo);
		scatteringQuantitiesComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		scatteringQuantitiesComboViewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element){
				if(element instanceof ScatteringQuantity){
					ScatteringQuantity<?> quantity = (ScatteringQuantity<?>) element;
					return quantity.getQuantityName();
				}
				return super.getText(element);
			}
		});
		
		
		scatteringQuantitiesCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		scatteringQuantitiesComboViewer.addSelectionChangedListener(e -> {
			IStructuredSelection selection = (IStructuredSelection) e.getSelection();
		    if (selection.size() > 0){
		    	ScatteringQuantity<?> newQuantity = (ScatteringQuantity<?>) selection.getFirstElement();
		    	Unit<?> baseUnit = newQuantity.getBaseUnit();
		    	requestedMin = controller.convertValue(requestedMin, currentQuantity, newQuantity, currentUnit, baseUnit);
		    	requestedMax = controller.convertValue(requestedMax, currentQuantity, newQuantity, currentUnit, baseUnit);
		    	currentQuantity = newQuantity;
		    	currentUnit = baseUnit;
		    	String quantityName = currentQuantity.getQuantityName();
		    	minValueLabel.setText("Min " + quantityName + " value:");
				maxValueLabel.setText("Max " + quantityName + " value:");
				requestedMinValueLabel.setText("Requested min " + quantityName + " value:");
				requestedMaxValueLabel.setText("Requested max " + quantityName + " value:");
		    	scatteringQuantitiesUnitsCombo.setInput(currentQuantity.getUnits());
		    	scatteringQuantitiesUnitsCombo.setSelection(new StructuredSelection(currentQuantity.getUnits().get(0)));
		    }
		});
		
		
		scatteringQuantitiesUnitsCombo = GuiHelper.createUnitsCombo(resultsPanel, null);
		scatteringQuantitiesUnitsCombo.addSelectionChangedListener(e -> {
			IStructuredSelection selection = (IStructuredSelection) e.getSelection();
			if (selection.size() > 0){
				 Unit<?> newUnit = (Unit<?>) selection.getFirstElement();
				 requestedMin = controller.convertValue(requestedMin, currentQuantity, currentQuantity, currentUnit, newUnit);
			     requestedMax = controller.convertValue(requestedMax, currentQuantity, currentQuantity, currentUnit, newUnit);
				 currentUnit =  newUnit;
				 updateVisibleQRange();  // Convert the visible q range to the new unit.
				 showRequestedMinMax();  // (see javadoc).
			}
		});
		
		
		
		/*
		 * UI elements for displaying results.
		 */
		
		minValueLabel = GuiHelper.createLabel(resultsPanel, "");
		minValue = GuiHelper.createLabel(resultsPanel, "");
		minValue.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		new Label(resultsPanel, SWT.NONE);  // Placeholder
		
		
		maxValueLabel = GuiHelper.createLabel(resultsPanel, "");
		maxValue = GuiHelper.createLabel(resultsPanel, "");
		maxValue.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		new Label(resultsPanel, SWT.NONE); // Placeholder
		
		
		requestedMinValueLabel = GuiHelper.createLabel(resultsPanel, "");
		requestedMinValueText = GuiHelper.createText(resultsPanel);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(70, 20).applyTo(requestedMinValueText);
		requestedMinValueText.addModifyListener(e -> requestedRangeTextInputChanged());
		requestedMinValueText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				showRequestedMinMax(); // Ensures requested min <= requested max.
			}
		}); 
		new Label(resultsPanel, SWT.NONE); // Placeholder
		
		
		requestedMaxValueLabel = GuiHelper.createLabel(resultsPanel, "");
		requestedMaxValueText = GuiHelper.createText(resultsPanel);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(70, 20).applyTo(requestedMaxValueText);
		requestedMaxValueText.addModifyListener(e -> requestedRangeTextInputChanged());
		requestedMaxValueText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				showRequestedMinMax(); // Ensures requested min <= requested max.
			}
		}); 
		new Label(resultsPanel, SWT.NONE); // Placeholder
		
		
		
		/*
		 * The drawing that displays the results.
		 */
		drawingArea = new Canvas(resultsPanel, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 4;
		data.widthHint = 700;
		data.heightHint = 70;
		drawingArea.setLayoutData(data);
		drawingArea.addPaintListener(this::repaint);
		
		
		
		/*
		 * Initialise the scattering quantities.
		 */
		ArrayList<ScatteringQuantity<?>> quantities = new ArrayList<>();
		quantities.add(new Q());
		quantities.add(new D());
		quantities.add(new S());
		doubleTheta = new DoubleTheta();
		quantities.add(doubleTheta);
		scatteringQuantitiesComboViewer.setInput(quantities);
        scatteringQuantitiesComboViewer.setSelection(new StructuredSelection(quantities.get(0)));		
		
        
		resultsPanel.layout();
		scrolledComposite.setContent(resultsPanel);	
		scrolledComposite.setMinSize(resultsPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		init();
	}


	@Override
	public void setFocus() {
		requestedMinValueText.setFocus();
	}

	
	public void repaint(PaintEvent e){
		Rectangle bounds = drawingArea.getClientArea();
		
		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		
		if(!controller.getHasSolution()) {
			 e.gc.drawText("No solution", bounds.width/2 - 40, bounds.height/2 - 10);
			 return;
		 }
		
		NumericRange requestedRange = controller.getRequestedRange(currentQuantity, currentUnit);
		NumericRange fullRange = controller.getFullRange(currentQuantity, currentUnit);
		NumericRange visibleRange = controller.getVisibleRange(currentQuantity, currentUnit);
		
		 if(requestedRange == null || fullRange == null || visibleRange == null) 
			 return;
		 
		 
		 if(controller.getIsSatisfied()) 
			 e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_GREEN));
		 else 
			 e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_RED));
		 
		 
		 double offset = 60;
         double slope = (bounds.width-120)/ (Math.log(fullRange.getMax()) - Math.log(fullRange.getMin()));
         double minRequestedX = slope*(Math.log(requestedRange.getMin()) - Math.log(fullRange.getMin())) + offset;
         double maxRequestedX = slope*(Math.log(requestedRange.getMax()) - Math.log(fullRange.getMin())) + offset;
         double minValueX = slope*(Math.log(visibleRange.getMin()) - Math.log(fullRange.getMin())) + offset;
         double maxValueX = slope*(Math.log(visibleRange.getMax()) - Math.log(fullRange.getMin())) + offset;
         
         
         // Restrict the requested coordinates to the area of the drawing canvas.
         if(!Double.isNaN(minRequestedX) && !Double.isNaN(maxRequestedX)) {
	         NumericRange requested = new NumericRange(minRequestedX, maxRequestedX).intersect(new NumericRange(60, bounds.width - 60));
	         minRequestedX = requested.getMin();
	         maxRequestedX = requested.getMax();
         }
         
         e.gc.fillRectangle((int) minValueX, bounds.height/2, (int) (maxValueX - minValueX), bounds.height/2);
         if(!Double.isNaN(minRequestedX)) e.gc.drawLine((int) minRequestedX, 5, (int) minRequestedX, bounds.height);
         if(!Double.isNaN(maxRequestedX)) e.gc.drawLine((int) maxRequestedX, 20, (int) maxRequestedX, bounds.height);
         
         e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
         if(!Double.isNaN(minRequestedX)) e.gc.drawText("Requested min", (int) minRequestedX - 40, 5);
         if(!Double.isNaN(maxRequestedX)) e.gc.drawText("Requested max", (int) maxRequestedX - 40, 20);
	}


	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if(!isUserEdited) return;
		
		if(e.getPropertyName().equals(ResultConstants.REQUESTED_Q_RANGE_PROPERTY))
			updateRequestedQRange();
		else if(e.getPropertyName().equals(ResultConstants.VISIBLE_Q_RANGE_PROPERTY))
			updateVisibleQRange();
		else if(e.getPropertyName().equals(AbstractResultsController.BEAMLINE_CONFIGURATION_PROPERTY))
			doubleTheta.setWavelength(controller.getBeamlineConfiguration().getWavelength());
	
		drawingArea.redraw();
		resultsPanel.layout();	
	}
	
	
	/*
	 * Gets any already existing results from the controller, displays them in the UI, 
	 * and initialises the private fields that hold the current state of the UI.
	 */
	private void init() {
		updateRequestedQRange();
		updateVisibleQRange();
		drawingArea.redraw();
		resultsPanel.layout();
	}
	
	
	/**
	 * Gets the requested range from the controller and displays it.
	 * Should be used only when the requested range changed due to an external action,
	 * (e.g. the requested values were changed in another view),
	 * otherwise use the requested min/max values stored locally inside this class' private fields.
	 */
	private void updateRequestedQRange() {
		NumericRange newRequestedRange = controller.getRequestedRange(currentQuantity, currentUnit);
		
		// If the range is null (which could possibly be because the user input was invalid), 
		// do not overwrite the text fields - allow the user to correct the input.
		if(newRequestedRange == null) return;
		
		try {
			isUserEdited = false;
			
			requestedMin = newRequestedRange.getMin();
			requestedMinValueText.setText(TextUtil.format(requestedMin));
			requestedMax = newRequestedRange.getMax();
			requestedMaxValueText.setText(TextUtil.format(requestedMax));
		} finally {
			isUserEdited = true;
		}
	}
	
	
	/**
	 * Gets the visible range from the controller and displays it.
	 */
	private void updateVisibleQRange(){
		NumericRange newRange = controller.getVisibleRange(currentQuantity, currentUnit);
		
		if(newRange == null) {
			minValue.setText("");
			maxValue.setText("");
			return;
		}
		
		Double newMinValue = newRange.getMin();
		if(!TextUtil.equalAsDoubles(TextUtil.format(newMinValue), minValue.getText())) 
			minValue.setText(TextUtil.format(newMinValue));
		
		Double newMaxValue = newRange.getMax();
		if(!TextUtil.equalAsDoubles(TextUtil.format(newMaxValue), maxValue.getText())) 
			maxValue.setText(TextUtil.format(newMaxValue));
	}
	
	
	/**
	 * Ensures requested min <= requested max.
	 */
	private void checkRequestedMinMax() {
		if(requestedMin != null && requestedMax != null && requestedMin > requestedMax) {
			// Swap the values
			double temp = requestedMin;
			requestedMin = requestedMax;
			requestedMax = temp;
		}
	}
		
	
	/**
	 * Displays the locally stored requested min/max values in the UI without triggering 
	 * a notification to the controller that the range has changed, because it hasn't.
	 */
	private void showRequestedMinMax() {
		checkRequestedMinMax();
		
		try {
			isUserEdited = false;
			if(requestedMax != null) requestedMaxValueText.setText(TextUtil.format(requestedMax));
			if(requestedMin != null) requestedMinValueText.setText(TextUtil.format(requestedMin));
		} finally {
			isUserEdited = true;
		}
		
		resultsPanel.layout();
		drawingArea.redraw();
	}
	
	
	/**
	 * Parses the user input and updates the results if the input is valid
	 * (does not check whether the input makes sense, i.e. whether requested min 
	 * is less than requested max, but checks that the entered strings are numbers and positive).
	 */
	private void requestedRangeTextInputChanged() {
		if(!isUserEdited) return;
		
		requestedMax = TextUtil.parseDouble(requestedMaxValueText.getText());
		requestedMin = TextUtil.parseDouble(requestedMinValueText.getText());
		
		try {
			// Want to update the value in the model, but ignore the subsequent notification that will
			// be sent by the model via the propertyChange() method.
			isUserEdited = false;
			// Only update the range when both boundary values are specified.
			// Otherwise the range is invalid so set it to null. 
			if(requestedMax != null && requestedMin != null && requestedMin > 0 && requestedMax > 0) 
				controller.updateRequestedRange(new NumericRange(requestedMin, requestedMax), currentQuantity, currentUnit);
			else
				controller.updateRequestedQRange(null);
		} finally {
			isUserEdited = true;
			resultsPanel.layout();
			drawingArea.redraw();
		}
	}
	
	
	@Override
	public void dispose() {
		controller.removeView(this);
		controller = null;
		doubleTheta = null;
		super.dispose();
	}
}
