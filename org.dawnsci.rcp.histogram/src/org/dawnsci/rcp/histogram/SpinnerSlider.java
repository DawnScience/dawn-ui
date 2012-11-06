package org.dawnsci.rcp.histogram;

import java.util.ArrayList;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

/**
 * Simple Class which combines a spinner and a slider so that they have the same bounds etc.
 * @author ssg37927
 *
 */
public class SpinnerSlider {

	// Class Items
	private double max = 100.0;
	private double min = -100.0;	
	private int steps;

	// GUI Components
	private Composite comp;
	private Label label;
	private Slider slider;
	private FloatSpinner spinner;
	private SelectionListener listener;
	
	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();
	
	public SpinnerSlider(String name, Composite parent, int sliderSteps) {
		
		comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comp.setLayout(new GridLayout(3, false));
		
		steps = sliderSteps;
		
		GridData gridDataLabel = new GridData(SWT.NONE, SWT.NONE, false, false);
		gridDataLabel.minimumWidth = 100;
		label = new Label(comp, SWT.NONE);
		label.setText(name);
		
		listener = new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(event.getSource() == slider) {
					updateSpinner();
				}
				if(event.getSource() == spinner.getControl()) {
					updateSlider();	
				}	
				updateListeners(event);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
				updateListeners(event);
			}
		};
		
		GridData gridDataSpinner = new GridData(SWT.NONE, SWT.NONE, false, false);
		gridDataSpinner.minimumWidth = 90;
		
		spinner = new FloatSpinner(comp, SWT.BORDER);
		spinner.setLayoutData(gridDataSpinner);
		spinner.addSelectionListener(listener);
		
		GridData gridDataSlider = new GridData(SWT.FILL, SWT.CENTER, true, false);
		slider = new Slider(comp, SWT.BORDER);
		slider.setLayoutData(gridDataSlider);
		slider.addSelectionListener(listener);
		slider.setMinimum(0);
		slider.setMaximum(steps);
		slider.setIncrement(1);
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
		spinner.setMaximum(max);
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
		spinner.setMinimum(min);
	}
	
	public double getValue() {
		return spinner.getDouble();
	}
	
	public void setValue(double value) {
		spinner.setDouble(value);
		updateSlider();
	}
	
	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	public void updateListeners(SelectionEvent event) {
		for (SelectionListener listener : listeners) {
			listener.widgetSelected(event);	
		}
	}

	private void updateSpinner() {
		int selection = slider.getSelection();
		double a = ((double)selection/(double)steps);
		double val = (a*(max-min))+min;
		spinner.setDouble(val);
	}

	private void updateSlider() {
		double minval = spinner.getDouble();
		slider.setSelection((int) (((minval-min)/(max-min))*steps));
	}

}
