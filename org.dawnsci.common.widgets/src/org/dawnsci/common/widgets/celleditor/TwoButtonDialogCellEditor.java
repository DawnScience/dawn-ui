/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class TwoButtonDialogCellEditor extends DialogCellEditor {


	public TwoButtonDialogCellEditor(Composite control) {
		super(control);
	}

	protected abstract Object openDialogBox2(Control cellEditorWindow);
	
	protected Button button2;
	private FocusListener buttonFocusListener2;
	
    protected Control createControl(Composite parent) {

 
        final Composite editor = (Composite)super.createControl(parent);
        
        Font font = parent.getFont();

        button2 = createButton(editor);
        button2.setFont(font);

        button2.addKeyListener(new KeyAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyReleased(KeyEvent e) {
                if (e.character == '\u001b') { // Escape
                    fireCancelEditor();
                }
            }
        });
        
        button2.addFocusListener(getButtonFocusListener2());
        
        button2.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent event) {
            	// Remove the button's focus listener since it's guaranteed
            	// to lose focus when the dialog opens
            	button2.removeFocusListener(getButtonFocusListener2());
                
            	Object newValue = openDialogBox2(editor);
            	
            	// Re-add the listener once the dialog closes
            	button2.addFocusListener(getButtonFocusListener2());

            	if (newValue != null) {
                    boolean newValidState = isCorrect(newValue);
                    if (newValidState) {
                        markDirty();
                        doSetValue(newValue);
                    } else {
                        // try to insert the current value into the error message.
                        setErrorMessage(MessageFormat.format(getErrorMessage(),
                                new Object[] { newValue.toString() }));
                    }
                    fireApplyEditorValue();
                }
            }
        });

        setValueValid(true);


        return editor;
    }

    public void deactivate() {
    	if (button2 != null && !button2.isDisposed()) {
    		button2.removeFocusListener(getButtonFocusListener2());
    	}
    	
		super.deactivate();
	}
 
    private FocusListener getButtonFocusListener2() {
    	if (buttonFocusListener2 == null) {
    		buttonFocusListener2 = new FocusListener() {

				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
				 */
				public void focusGained(FocusEvent e) {
					// Do nothing
				}

				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
				 */
				public void focusLost(FocusEvent e) {
					TwoButtonDialogCellEditor.this.focusLost();
				}
    		};
    	}
    	
    	return buttonFocusListener2;
	}
    
    protected void doSetFocus() {
        super.doSetFocus();
        
        // add a FocusListener to the button
        button2.addFocusListener(getButtonFocusListener2());
    }

    public void setButton2Enabled(boolean enabled) {
    	button2.setEnabled(enabled);
    }
    public void setButton2Tooltip(String tooltip) {
    	button2.setToolTipText(tooltip);
    }
}
