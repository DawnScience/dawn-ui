/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dawnsci.common.richbeans.components.scalebox.NumberBox;
import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper.TEXT_TYPE;
import org.dawnsci.common.richbeans.internal.DialogUtils;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.dawnsci.doe.DOEFieldType;
import org.eclipse.dawnsci.doe.DOEUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 * Used by the RangeBox, not intended for exterior use.
 */
public final class RangeDialog extends Dialog {
	
	protected RangeComposite     rangeComposite;
	protected VerticalListEditor listEditor;
	protected DOEFieldType       currentRange;
    protected String             currentValue;
	protected Combo              rangeChoice;
	protected LabelWrapper       singleValueLabel;
	protected boolean            isInteger;
	private boolean              rangeOnly=false;
	
	public RangeDialog(Shell parentShell, boolean isInteger) {
		super(parentShell);
		setShellStyle(SWT.TITLE|SWT.RESIZE);
		setBlockOnOpen(true);
		this.isInteger  = isInteger;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.pack();
		newShell.setText("Range");
		newShell.setSize(500, 380);
		DialogUtils.centerDialog(getParentShell(), newShell);
	}
	
	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
        
		final Composite comp = (Composite)super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		
		Link lblRange = new Link(comp, SWT.NONE);
		GridData gd_lblRange = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblRange.widthHint = 60;
		lblRange.setLayoutData(gd_lblRange);
		lblRange.setText("Range");
// Opening DOE from link currently disabled as not all RangeDialog instances are involved in DOE studies anymore
//		lblRange.setText("<a>Range</a>");
//		lblRange.setToolTipText("Click link to open the 'Design of Experiments' view to show all experiments currently defined. I.e. an expansion of all ranges in the same order that they will be run");
//		lblRange.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if (EclipseUtils.getActivePage()!=null) {
//					try {
//						EclipseUtils.getActivePage().showView("uk.ac.gda.ui.doe.DOEView");
//					} catch (PartInitException e1) {
//						logger.error("Cannot find view 'DOEView'");
//					}
//				}
//			}
//		});
		
		
		this.rangeChoice = new Combo(comp, SWT.READ_ONLY);
		
		rangeChoice.setItems(new String[] {"Single Value", "List", "Range"});
		rangeChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rangeChoice.select(rangeOnly?2:0);
		rangeChoice.setEnabled(!rangeOnly);
		
		new Label(comp, SWT.NONE);

		final Composite content = new Composite(comp, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		content.setLayout(new GridLayout(1, false));
		
		this.singleValueLabel = new LabelWrapper(content, SWT.NONE);
		singleValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		singleValueLabel.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		singleValueLabel.setValue(0d);
		GridUtils.setVisible(singleValueLabel, false);
		
		this.rangeComposite = new RangeComposite(content, SWT.NONE);
		rangeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridUtils.setVisible(rangeComposite, false);
		
		this.listEditor  = new VerticalListEditor(content, SWT.NONE);
		listEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		listEditor.setMinItems(1);
		listEditor.setMaxItems(100);
		listEditor.setDefaultName("0.0");
		if (isInteger) {
			listEditor.setEditorClass(IntegerValueBean.class);
		} else {
			listEditor.setEditorClass(DoubleValueBean.class);
		}
		final ListValueComposite listValComp = new ListValueComposite(listEditor, SWT.NONE);
		listEditor.setEditorUI(listValComp);
		listEditor.setNameField("value");
		GridUtils.setVisible(listEditor, false);
		listEditor.on();
		
		rangeChoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRangeType(getRangeType(rangeChoice.getSelectionIndex()), false);
			}
		});
		
		updateRangeType(currentRange, true);
				
        return comp;
	}
	
	private DOEFieldType getRangeType(int selectionIndex) {
		if (selectionIndex==0) return DOEFieldType.SINGLE_VALUE;
		if (selectionIndex==1) return DOEFieldType.LIST;
		if (selectionIndex==2) return DOEFieldType.RANGE;
		return DOEFieldType.SINGLE_VALUE;
	}
	

	protected void updateRangeType(DOEFieldType range, boolean setChoice) {
		
		List<? extends Number> vals = null;
		if (currentRange!=null) {
			vals = DOEUtils.expand(getValueInternal());
		}
		
		currentRange = range;
		if (range==DOEFieldType.SINGLE_VALUE) {
			if (vals!=null) singleValueLabel.setValue(vals.get(0));
			GridUtils.setVisible(singleValueLabel, true);
			GridUtils.setVisible(rangeComposite,   false);
			GridUtils.setVisible(listEditor,       false);
			if (setChoice) rangeChoice.select(0);
			
		} else if (range==DOEFieldType.LIST) {
			if (vals!=null) {
				final Object beans = isInteger ? IntegerValueBean.getBeanList(vals) : DoubleValueBean.getBeanList(vals);
				listEditor.setValue(beans);
			}
			GridUtils.setVisible(singleValueLabel, false);
			GridUtils.setVisible(rangeComposite,   false);
			GridUtils.setVisible(listEditor,       true);
			if (setChoice) rangeChoice.select(1);
			
		} else if (range==DOEFieldType.RANGE) {
			GridUtils.setVisible(singleValueLabel, false);
			GridUtils.setVisible(rangeComposite,   true);
			GridUtils.setVisible(listEditor,       false);
			if (setChoice) rangeChoice.select(2);
			
			if (vals!=null) {
				Collections.sort(vals, new Comparator<Number>() {
					@Override
					public int compare(Number n1, Number n2) {
						return (new Double(n1.doubleValue())).compareTo(new Double(n2.doubleValue()));
					}
				});
				if (vals.size()>1) {
					if (rangeComposite.isForwards()) {
					    rangeComposite.setValue(vals.get(0), vals.get(vals.size()-1), 1d);
					} else {
						rangeComposite.setValue(vals.get(vals.size()-1), vals.get(0), 1d);
					}
				}
			}
		}
		
		listEditor.getParent().layout();
	}

	public DOEFieldType getRangeType() {
		return DOEFieldType.SINGLE_VALUE;
	}

	/**
	 * Sets the value of all the range types based on the current
	 * value entered into the box.
	 * 
	 * @param currentValue
	 */
	public void setValue(String currentValue) {
		
		DOEFieldType   range = DOEFieldType.getRangeType(currentValue);
		if (rangeOnly) range = DOEFieldType.RANGE;
		if (range == DOEFieldType.SINGLE_VALUE) {
			final Double ov = Double.parseDouble(currentValue);
			singleValueLabel.setValue(ov);
			
		} else if (range == DOEFieldType.LIST) {
			final Object beans = isInteger ? IntegerValueBean.getBeanList(currentValue) : DoubleValueBean.getBeanList(currentValue);
			listEditor.setValue(beans);
			
		} else if (range==DOEFieldType.RANGE) {
			rangeComposite.setValue(currentValue);
			
		}

		updateRangeType(range, true);
	}
	public void setUnit(String unit) {
		((ListValueComposite)listEditor.getEditorUI()).getValue().setUnit(unit);
		rangeComposite.setUnit(unit);
		singleValueLabel.setUnit(unit);
	}

	public String getValue() {
		return currentValue;
	}
	
	@Override
	protected void cancelPressed() {
		// Should test the value of open() and 
		// not use getValue if it's != OK
        currentRange = null;
        currentValue = null;
		super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		currentValue = getValueInternal();
		super.okPressed();
	}

	private String getValueInternal() {
		
		if (currentRange==null) return null;
		if (currentRange==DOEFieldType.SINGLE_VALUE) {
			if (singleValueLabel==null) return null;
			return singleValueLabel.getValue().toString();
			
		} else if (currentRange==DOEFieldType.LIST) {
			if (listEditor==null) return null;
			@SuppressWarnings("unchecked")
			final List<? extends NumberValueBean> vals = (List<? extends NumberValueBean>)listEditor.getValue();
			return NumberValueBean.getString(vals);
			
		} else if (currentRange==DOEFieldType.RANGE) {
			if (rangeComposite==null) return null;
			return rangeComposite.getValue();
		}
		return null;
	}

	/**
	 * Sets the bounds for these widgets
	 * @param numBox
	 */
	public void setBounds(NumberBox numBox) {
		final NumberBox list = ((ListValueComposite)listEditor.getEditorUI()).getValue();
		list.copySettings(numBox);
		
		rangeComposite.setBounds(numBox);
	}

	public void setRangeOnly(boolean rangeOnly) {
		this.rangeOnly = rangeOnly;
	}
}
