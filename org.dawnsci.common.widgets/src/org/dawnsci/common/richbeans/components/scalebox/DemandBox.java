/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

import java.util.Observable;

import org.dawnsci.common.richbeans.beans.IExpressionManager;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Demand box has the following event properties:
 * 1. Bounds are checked whenever a value is changed.
 * 2. ValueListeners are notified when the enter key is pressed or when focus is lost.
 * 3. Use the demandBegin, demandStep and demandComplete methods to control updating behaviour
 */
public class DemandBox extends NumberBox {

	boolean enabled = true;
	boolean desiredEnabledState = true; //remember what state the GUI should have if disabled from higher levels (MotorPositionViewer)
	
	protected boolean restoreValueWhenFocusLost;
	protected double previousValue;

	public DemandBox(Composite parent, int style) {
		this(parent, style, -1);
	}
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param expressionWidthHint A hint for the width of the expression box, or -1 for no hint
	 */
	public DemandBox(Composite parent, int style, int expressionWidthHint) {
		super(parent, style);
		createExpressionLabel(expressionWidthHint);
	}

	/**
	 * Call when the move is complete, the text gets set to the finalPosition
	 * and the expression view of the move disappears 
	 */
	public void demandComplete(final Object finalPosition) {
		desiredEnabledState = true;
		setRealState();
		setValue(finalPosition);
		GridUtils.setVisible(expressionLabel, false);
	}

	/**
	 * Call when the move is complete when no final position is available.
	 * The expression view of the move disappears 
	 */
	public void demandComplete() {
		desiredEnabledState = true;
		setRealState();
		GridUtils.setVisible(expressionLabel, false);
	}

	/**
	 * Call when the move begins, the expression text gets to the startPosition
	 * and the text becomes disabled
	 */
	public void demandBegin(final double startPosition) {
		desiredEnabledState = false;
		setRealState();
		setExpressionValue(startPosition);
	}

	/**
	 * Call when a move is in progress with a new value
	 */
	public void demandStep(final double stepPosition) {
		desiredEnabledState = false;
		setRealState();
		setExpressionValue(stepPosition);
	}

	
	@Override
	public boolean isExpressionAllowed() {
		return true; // its for the demand value
	}
	
	/**
	 * Stops the setting of an expression manager being possible.
	 */
	@Override
	public void setExpressionManager(IExpressionManager man) {
		this.expressionManager = null;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setRealState();
	}
	
	public void setRealState() {
		super.setEnabled(desiredEnabledState & enabled);
	}
	
	/**
	 * Change listeners to only notify if focus lost or enter pressed.
	 */
	@Override
	protected void createTextListeners(final StyledText text) {
		createFocusListener(text);
		createModifyListener(text);
		createVerifyKeyListener(text);
	}
	
	@Override
	protected void createVerifyKeyListener(StyledText text) {
		
		verifyListener = new VerifyKeyListener() {	
			@Override
			public void verifyKey(VerifyEvent event) {
				if (event.character == SWT.CR) {// They hit enter
					textUpdateAndFireListeners();
				}
			}
		};
		text.addVerifyKeyListener(verifyListener);
	}

	@Override
	protected void createModifyListener(final StyledText text) {
	    this.modifyListener = new ModifyListener()  {
			@Override
			public void modifyText(final ModifyEvent e) {
				textUpdate(); // Checks the entry for bounds (and unit)
				              // Do not fire events.
			} 	
	    };
	    text.addModifyListener(modifyListener);
	}
	
	/**
	 * Change loss of focus to fire value.
	 */
	@Override
	protected void createFocusListener(final StyledText text) {
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (restoreValueWhenFocusLost) {
					restoreText();
				}
				else {
					textUpdateAndFireListeners();
				}
			}
	    };
	    text.addFocusListener(focusListener);
	}

	protected void restoreText() {
		setValue(previousValue);
	}
	
	@Override
	public void setValue(Object value) {
		if (value != null) {
			previousValue = (Double)value;
		}
		super.setValue(value);
	}
	
	public boolean isRestoreValueWhenFocusLost() {
		return restoreValueWhenFocusLost;
	}
	public void setRestoreValueWhenFocusLost(boolean restoreValueWhenFocusLost) {
		this.restoreValueWhenFocusLost = restoreValueWhenFocusLost;
	}


	/**
	 * Just a test class to display the move facility
	 * 
	 * Behaves a little like a motor and uses a timer to notify.
	 */
	private static class TestMover extends Observable {
				
		private boolean busy = false;
		private double value = 0d; 
		private double stepSize = 1d;
		
		TestMover(double stepSize) {
			this.stepSize = stepSize;
		}

		public void setInitialPosition(double d) {
			value = d;
		} 

		public void moveTo(final double target) {
			if (busy)
				return;
			
			busy = true;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					double step;
					if (value > target)
						step = -stepSize;
					else
						step = stepSize;

					while (true) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}

						if (step < 0 && value <= target)
							break;
						else if (step > 0 && value >= target)
							break;

						value += step;
						setChanged();
						notifyObservers(value);
					}
					setChanged();
					notifyObservers();
					busy = false;

				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}

		public double getPosition() {
			return value;
		}
		
		public boolean isBusy(){
			return busy;
		}
	}		
}
