package org.dawnsci.rcp.histogram;

import java.util.ArrayList;
import java.util.EventObject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.mihalis.opal.rangeSlider.RangeSlider;


/**
 * Simple Class combining a range slider with two labels which show the % position
 * the upper and lower tabs on the range slider.
 * 
 * To use:
 * Construct the object
 * Call setRangeLimits to set the data range
 * Call getMinValue/getMaxValue to get the data value of the slider tabs
 * 
 * Has selection and key events
 * Keys are:
 * Arrows for fine movement
 * Page Up/Page Down for coarse movement
 * Home/End to move to range stops
 */
public class HistogramRangeSlider {

	Composite composite;
	RangeSlider rangeSlider;
	private SelectionListener listener;
	private KeyListener keyListener;
	//Actual image min and max
	double minValue = 0.0;
	double maxValue = 100.0;
	Label lowerText;
	Label upperText;
	
	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();
	private ArrayList<KeyListener> keylisteners = new ArrayList<KeyListener>();
	
	/**
	 *Sole constructor
	 *
	 *@param parent			Composite of the parent
	 *@param granularity	How fine the scale is (ie a granularity of 1000 allows steps of 0.1%)
	 */
	public HistogramRangeSlider(Composite parent, int granularity) {
		
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(3, false));
		
		lowerText = new Label(composite, SWT.NONE);
		lowerText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false,1,1));
		lowerText.setText("0%");
		
		rangeSlider = new RangeSlider(composite, SWT.HORIZONTAL);
		rangeSlider.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		rangeSlider.setMinimum(0);
		rangeSlider.setMaximum(granularity);
		
		upperText = new Label(composite, SWT.NONE);
		upperText.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false,1,1));
		upperText.setText("100%");
		
		keyListener = new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.HOME:
				case SWT.END:
				case SWT.PAGE_UP:
				case SWT.PAGE_DOWN:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_UP:
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_DOWN:
					updateListeners(e);
					break;
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.HOME:
				case SWT.END:
				case SWT.PAGE_UP:
				case SWT.PAGE_DOWN:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_UP:
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_DOWN:
					updateListeners(e);
					break;
				}
			}
		};
		
		listener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateListeners(e);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateListeners(e);
				
			}
		};
		
		rangeSlider.addSelectionListener(listener);
		rangeSlider.addKeyListener(keyListener);
	}
	/**
	 *Set the slider values 
	 *
	 *@param parent			Composite of the parent
	 *@param granularity	How fine the scale is (ie a granularity of 1000 allows steps of 0.1%)
	 */
	public void setSliderValues(double rangeMin, double rangeMax) {
		
		if (rangeMin < minValue) minValue = rangeMin;
		if (rangeMax > maxValue) maxValue = rangeMax; 
		
		int min = getSliderValue(rangeMin);
		int max = getSliderValue(rangeMax);
		
		rangeSlider.setLowerValue(min);
		rangeSlider.setUpperValue(max);
		
		double toPercent = rangeSlider.getMaximum()/100;
		
		upperText.setText(String.format("%3.2f",(max/toPercent)) + "%");
		upperText.pack();
		lowerText.setText(String.format("%3.2f",min/toPercent) + "%");
		lowerText.pack();
	}
	
	public void setRangeLimits(double min, double max) {
		minValue = min;
		maxValue = max;
	}
	
	public double getMinValue() {
		return getDataValue(rangeSlider.getLowerValue());		
	}
	
	public double getMaxValue() {
		return getDataValue(rangeSlider.getUpperValue());
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
	
	public void addKeyListener(KeyListener listener) {
		keylisteners.add(listener);
	}
	
	public void removeKeyListener(KeyListener listener) {
		keylisteners.remove(listener);
	}

	public void updateListeners(KeyEvent event) {
		for (KeyListener listener : keylisteners) {
			listener.keyPressed(event);	
		}
	}
	
	public boolean isEventSource(EventObject e) {
		
		if (e == null) return false;
		if (e.getSource().equals(rangeSlider)) return true;
		
		return false;
	}
	
	private double getDataValue(int sliderValue) {
		int granularity = rangeSlider.getMaximum();
		double rangeVal = maxValue-minValue;
		double step = rangeVal/granularity;
		return (sliderValue*step) + minValue;

	}
	
	private int getSliderValue(double dataValue) {
		int granularity = rangeSlider.getMaximum();
		double rangeVal = maxValue-minValue;
		double step = rangeVal/granularity;
		return (int)Math.round((dataValue-minValue)/step);
	}

}
