package org.dawnsci.rcp.histogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * With many of the same so that they look ok in a composite together
 * @author ssg37927
 *
 */
public class SpinnerSliderSet {

	// Class Items
	private Map<String, Double> maxs = new HashMap<String,Double>();
	private Map<String, Double> mins = new HashMap<String,Double>();
	private int steps;

	// GUI Components
	private Composite comp;

	// GUI Components for the items
	private Map<String,Label> labels = new HashMap<String,Label>();
	private Map<String,Slider> sliders = new HashMap<String,Slider>();
	private Map<String,FloatSpinner> spinners = new HashMap<String,FloatSpinner>();
	private Map<String,SelectionListener> listeners = new HashMap<String,SelectionListener>();

	private List<SelectionListener> externalListeners = new ArrayList<SelectionListener>();

	public SpinnerSliderSet(Composite parent, int sliderSteps, String... names) {

		// Set up the composite
		comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comp.setLayout(new GridLayout(3, false));

		steps = sliderSteps;


		// now itterate through all the names and create appropriate gui elements

		for (String name : names) {

			GridData gridDataLabel = new GridData(SWT.NONE, SWT.NONE, false, false);
			gridDataLabel.minimumWidth = 100;
			
			Label label = new Label(comp, SWT.NONE);
			label.setText(name);
			
			labels.put(name, label);

			SelectionListener listener = new SelectionListener() {			
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (event.getSource() instanceof Slider) {
						for (String name : sliders.keySet()) {
							if(sliders.get(name) == event.getSource()) {
								updateSpinner(name);
								continue;
							}
						}
					}
					if (event.getSource() instanceof FloatSpinner) {
						for (String name : spinners.keySet()) {
							if(spinners.get(name) == event.getSource()) {
								updateSlider(name);
								continue;
							}
						}
					}
					updateListeners(event);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					widgetSelected(event);
				}
			};
			
			listeners.put(name, listener);

			GridData gridDataSpinner = new GridData(SWT.NONE, SWT.NONE, false, false);
			gridDataSpinner.minimumWidth = 90;

			FloatSpinner spinner = new FloatSpinner(comp, SWT.BORDER);
			spinner.setLayoutData(gridDataSpinner);
			spinner.addSelectionListener(listener);
			
			spinners.put(name, spinner);

			GridData gridDataSlider = new GridData(SWT.FILL, SWT.CENTER, true, false);
			Slider slider = new Slider(comp, SWT.BORDER);
			slider.setLayoutData(gridDataSlider);
			slider.addSelectionListener(listener);
			slider.setMinimum(0);
			slider.setMaximum(steps);
			slider.setIncrement(1);
			
			sliders.put(name, slider);
			
			maxs.put(name, 100.0);
			mins.put(name, -100.0);
		}
		
	}

	public double getMax(String name) {
		return maxs.get(name);
	}

	public void setMax(String name, double max) {
		this.maxs.put(name, max);
		spinners.get(name).setMaximum(max);
	}

	public double getMin(String name) {
		return mins.get(name);
	}

	public void setMin(String name, double min) {
		this.mins.put(name, min);
		spinners.get(name).setMinimum(min);
	}

	public double getValue(String name) {
		return spinners.get(name).getDouble();
	}

	public void setValue(String name, double value) {
		spinners.get(name).setDouble(value);
		updateSlider(name);
	}
	

	public void addSelectionListener(SelectionListener listener) {
		externalListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		externalListeners.remove(listener);
	}

	public void updateListeners(SelectionEvent event) {
		for (SelectionListener listener : externalListeners) {
			listener.widgetSelected(event);	
		}
	}

	private void updateSpinner(String name) {
		int selection = sliders.get(name).getSelection();
		double a = ((double)selection/(double)steps);
		double val = (a*(maxs.get(name)-mins.get(name)))+mins.get(name);
		spinners.get(name).setDouble(val);
	}

	private void updateSlider(String name) {
		double minval = spinners.get(name).getDouble();
		sliders.get(name).setSelection((int) (((minval-mins.get(name))/(maxs.get(name)-mins.get(name)))*steps));
	}



}
