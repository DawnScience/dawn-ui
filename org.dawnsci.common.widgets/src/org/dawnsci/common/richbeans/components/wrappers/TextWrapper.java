/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawnsci.common.richbeans.components.ButtonComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Designed to wrap Text objects to allow then to work with BeanUI
 * @author fcp94556
 *
 */
public class TextWrapper extends ButtonComposite {
	
	protected static final Color BLUE      = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	protected static final Color RED       = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	protected static final Color BLACK     = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	protected static final Color DARK_RED  = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
	
	/**
	 * The text type, effects how the text is checked.
	 */
	public enum TEXT_TYPE {
		/**
		 * Any text
		 */
		FREE_TXT,
		/**
		 * Legal expressions
		 */
		@Deprecated
		EXPRESSION,
		/**
		 * Legal Linux filenames
		 */
		FILENAME
	}
	
	private TEXT_TYPE textType = TEXT_TYPE.FREE_TXT;
	
	/**
	 * @return Returns the textType.
	 */
	public TEXT_TYPE getTextType() {
		return textType;
	}

	/**
	 * @param textType The textType to set.
	 */
	public void setTextType(TEXT_TYPE textType) {
		if (textType==TEXT_TYPE.EXPRESSION) throw new RuntimeException("Text type "+TEXT_TYPE.EXPRESSION+" is not supported in this version because it required JEP!");
		this.textType = textType;
	}

	protected StyledText text;
	private ModifyListener modifyListener;

	/**
	 * The variables to use in expression validation.
	 */
	private Map<String, Object> expressionVariables;

	/**
	 * Simply calls super and adds some listeners.
	 * @param parent
	 * @param style
	 */
	public TextWrapper(Composite parent, int style) {
		
		super(parent, SWT.NONE);
//		GridLayoutFactory.fillDefaults().applyTo(this);
		setLayout(new GridLayout(1, false));
		
		this.text = new StyledText(this, style);
//		GridDataFactory.fillDefaults().applyTo(text);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		mainControl = text;
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				final Object newValue = getValue();
				
				if (textType==TEXT_TYPE.EXPRESSION) {
					// Do nothing
				} else if (textType == TEXT_TYPE.FILENAME) {
					String testString = newValue.toString().trim();
					if (testString.contains(" ") || testString.startsWith("-")
							|| testString.contains(";") || testString.contains("<") || testString.contains("\t")
							|| testString.contains("'") || testString.contains("\"") || testString.contains("\\")
							|| testString.contains("\n")|| testString.contains("..")) {
						if (!RED.isDisposed()) {
							text.setForeground(RED);
						}
						text.setToolTipText("Expression has invalid syntax");

					} else {
						text.setToolTipText("Enter a valid filename. Do NOT use spaces, commas, backslash etc.");
						if (!BLACK.isDisposed()) {
							text.setForeground(BLACK);
						}
					}
				}

				final ValueEvent evt = new ValueEvent(text,getFieldName());
				evt.setValue(newValue);
				eventDelegate.notifyValueListeners(evt);
			}
		};
		text.addModifyListener(modifyListener);

	}
	
	@Override
	public void setToolTipText(String text) {
		this.text.setToolTipText(text);
	}
	
	@Override
	public void dispose() {
		if (text!=null&&!text.isDisposed()) text.removeModifyListener(modifyListener);
		super.dispose();
	}

	private boolean multiLineMode = false;
	
	@Override
	public Object getValue() {
		if (multiLineMode) {
			final String [] sa = getText().split(text.getLineDelimiter());
			return Arrays.asList(sa);
		}
		
	    return getText();
	}
	
	/**
	 * @return text
	 */
	public String getText() {
		if (text.isDisposed()) {
			return null;
		}
		return text.getText();
	}

	@Override
	public void setValue(Object value) {
		if (isDisposed()) return;
		if (value instanceof List<?>) {
			multiLineMode = true;
			final List<?> lines = (List<?>)value;
			final StringBuilder buf  = new StringBuilder();
			for (Object line : lines) {
				buf.append(line.toString());
				buf.append(text.getLineDelimiter());
			}
			text.setText(buf.toString());
		} else {
			multiLineMode = false;
			text.setText(value!=null?value.toString():"");
		}
	}
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}

	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		setVisible(active);
	}

	/**
	 * @param i
	 */
	public void setTextLimit(int i) {
		text.setTextLimit(i);
	}

	/**
	 * If you have a variable set with values which the box in expression
	 * mode should check, send them here. Otherwise the expression box simply
	 * checks legal syntax.
	 * 
	 * Variables are Jep ones therefore Strings or Numbers
	 * 
	 * @param vars
	 */
	public void setExpressionVariables(final Map<String, Object> vars) {
		this.expressionVariables = vars;
	}
	
	/*******************************************************************/

}

	