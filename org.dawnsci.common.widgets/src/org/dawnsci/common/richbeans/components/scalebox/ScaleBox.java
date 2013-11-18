/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


/**
 * This class is a bounded, united object which can be used for a general
 * value input in gda SWT UI.
 * 
 * You have to call the on method on this object (after initiation) to
 * get events to fire with it. This is done automatically by using the widget on a 
 * RichBeanEditorPart usually.
 * 
 *
 * ScaleBox box has the following event properties:
 * 1. Bounds are checked whenever a value is changed.
 * 2. ValueListeners are notified whenever the value changes.
 * 
 * @author fcp94556
 *
 */
public class ScaleBox extends NumberBox  {

	protected Shell scaleShell;
	protected DecimalScale scale;
	protected double increment=1, pageIncrement=100;
	protected int scaleDecimalPlaces=0;
	protected boolean showingScale = false;
	protected boolean shellCreated = false;
	
	/**
	 * Create the composite scalebox for use in GDA UI.
	 * 
	 * After creating the composite set the scale properties (max, min etc.).
	 * These are used at run time by a lazy initiated form.
	 *  
	 * @param parent
	 * @param style
	 */
	public ScaleBox(final Composite parent, final int style) {
		super(parent, style);
		buttonSelection = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (scaleShell!=null&&scaleShell.isVisible()) return;
				setShowingScale(true);
			}
		};
	}
	
	@Override
	public void dispose() {
		if (scaleShell!=null&&!scaleShell.isDisposed()) 
			scaleShell.dispose();		
		super.dispose();
	}
	
	/**
	 * Lazy initiation of the scale shell used by users
	 * to slide a value rather than enter by text.
	 */
	protected void createScaleShell() {
		if (shellCreated) 
			return;
		
		scaleShell = new Shell(this.getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
		scaleShell.setLayout(new GridLayout(2, false));
		
		final Listener closeListener = new Listener() {
			@Override
			public void handleEvent(final Event e) {
				setShowingScale(false);
			}
		};

		// Listeners on this popup's shell
		scaleShell.addListener(SWT.Deactivate, closeListener);
		scaleShell.addListener(SWT.Close, closeListener);

		// Listeners on the target control
		text.addListener(SWT.MouseDoubleClick, closeListener);
		text.addListener(SWT.MouseDown, closeListener);
		text.addListener(SWT.Dispose, closeListener);
		text.addListener(SWT.FocusOut, closeListener);
		// Listeners on the target control's shell
		Shell controlShell = getShell();
		controlShell.addListener(SWT.Move, closeListener);
		controlShell.addListener(SWT.Resize, closeListener);


        scale = new DecimalScale(scaleShell, SWT.NO_FOCUS);		
        scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
        scale.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
               text.setText(scale.getSelection()+"");
            }
        });
        
        scale.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) { }
			@Override
			public void focusLost(FocusEvent e) {
				setShowingScale(false);
			}
        });
        
        scaleShell.pack();
        shellCreated = true;
	}
	@Override
	public void closeDialog() {
		if (scaleShell!=null&&!scaleShell.isDisposed()) {
			scaleShell.setVisible(false);
		}
	}
	
    protected void toggleShowingScale() {
     	setShowingScale(!showingScale);
    }
    
	protected void setShowingScale(final boolean isShow) {
		try {
			if (isShow) {
				createScaleShell();	
				if (button != null)
					button.removeSelectionListener(buttonSelection);
				Rectangle sizeT= text.getBounds();
				Point     pntT = text.toDisplay(-2, sizeT.height-4);
				Rectangle rect = new Rectangle(pntT.x, pntT.y, sizeT.width-2, scaleShell.getBounds().height);
				scale.setDecimalPlaces(getScaleDecimalPlaces());
				scale.setMaximum(getMaximum());
				scale.setMinimum(getMinimum());
				scale.setIncrement(getIncrement());
				scale.setPageIncrement(getPageIncrement());
				scale.setSelection(getIntegerValue());
				scaleShell.setBounds(rect);
				scaleShell.setVisible(true);
				text.setFocus();
			} else {
				if (scaleShell!=null&&!scaleShell.isDisposed()) {
					scaleShell.setVisible(false);
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (button != null)
								button.addSelectionListener(buttonSelection);
						}
					});
				}
			}		
		} finally {
			showingScale = isShow;
		}
	}
	
	
	/**
	 * Get the increment, default is 1.
	 * @return the increment
	 */
	public double getIncrement() {
		return increment;
	}

	/**
	 * Set the increment default is 1.
	 * @param increment the increment to set
	 */
	public void setIncrement(double increment) {
		this.increment = increment;
	}


	/**
	 * The page increment, default is 100.
	 * @return the pageIncrement
	 */
	public double getPageIncrement() {
		return pageIncrement;
	}


	/**
	 * The page increment, default is 100.
	 * @param pageIncrement the pageIncrement to set
	 */
	public void setPageIncrement(double pageIncrement) {
		this.pageIncrement = pageIncrement;
	}
	/**
	 * @return Returns the scaleDecimalPlaces.
	 */
	public int getScaleDecimalPlaces() {
		return scaleDecimalPlaces;
	}

	/**
	 * @param scaleDecimalPlaces The scaleDecimalPlaces to set.
	 */
	public void setScaleDecimalPlaces(int scaleDecimalPlaces) {
		this.scaleDecimalPlaces = scaleDecimalPlaces;
	}

}