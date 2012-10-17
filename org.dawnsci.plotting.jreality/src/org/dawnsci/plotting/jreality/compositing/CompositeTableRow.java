/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.compositing;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


/**
 *
 */
public class CompositeTableRow implements SelectionListener {

	private Spinner spinner;
	private Slider slider;
	private Button chkActive;
	private Button chkRed;
	private Button chkGreen;
	private Button chkBlue;
	private Composite panel;
	private Combo op;
	private String name;
	
	public CompositeTableRow(CompositeEntry entry,
						     Table container,
							 CompositingControl control,
							 boolean disableOp) {
		
		this.name = entry.getName();
		TableItem newItem = new TableItem(container,SWT.DOUBLE_BUFFERED);
		TableEditor editor0 = new TableEditor(container);
		editor0.horizontalAlignment = SWT.CENTER;
		editor0.grabHorizontal = true;
		chkActive = new Button(container,SWT.CHECK);
		chkActive.setSelection(true);
		chkActive.addSelectionListener(control);
		editor0.setEditor(chkActive,newItem,0);
		
		TableEditor editor = new TableEditor(container);
		editor.horizontalAlignment = SWT.CENTER;
		editor.grabHorizontal = true;
		panel = new Composite(container, SWT.NONE);
		panel.setLayout(new GridLayout(2,true));
		slider = new Slider(panel,SWT.HORIZONTAL|SWT.NO_TRIM);
		slider.setValues((int)(entry.getWeight()*100), 0, 104, 5, 1, 5);
		slider.addSelectionListener(this);
		slider.addSelectionListener(control);
		spinner = new Spinner(panel,SWT.DOUBLE_BUFFERED);
		spinner.setMinimum(0);
		spinner.setMaximum(100);
		spinner.setSelection((int)(entry.getWeight()*100));
		spinner.addSelectionListener(control);
		spinner.addSelectionListener(this);
		panel.pack();
		editor.setEditor(panel,newItem,2);
		newItem.setText(1,name);
		TableEditor editor2 = new TableEditor(container);
		editor2.horizontalAlignment = SWT.CENTER;
		editor2.grabHorizontal = true;
		editor2.grabVertical = true;
		op = new Combo(container,SWT.NONE);
		op.add("ADD");
		op.add("SUBTRACT");
		op.add("MULTIPLY");
		op.add("DIVIDE");
		op.add("MINIMUM");
		op.add("MAXIMUM");
		op.select(convertOperationToInt(entry.getOperation()));
		op.pack();
		op.addSelectionListener(control);
		op.setEnabled(!disableOp);
		editor2.setEditor(op,newItem,3);
		TableEditor editor3 = new TableEditor(container);
		editor3.horizontalAlignment = SWT.CENTER;
		editor3.grabHorizontal = true;
		editor3.grabVertical = true;
		chkRed = new Button(container,SWT.CHECK);
		chkRed.setSelection(true);
		chkRed.pack();
		chkRed.addSelectionListener(control);
		editor3.setEditor(chkRed,newItem,4);
		TableEditor editor4 = new TableEditor(container);
		editor4.horizontalAlignment = SWT.CENTER;
		editor4.grabHorizontal = true;
		editor4.grabVertical = true;
		chkGreen = new Button(container,SWT.CHECK);
		chkGreen.pack();
		chkGreen.setSelection(true);
		chkGreen.addSelectionListener(control);
		editor4.setEditor(chkGreen,newItem,5);
		TableEditor editor5 = new TableEditor(container);
		editor5.horizontalAlignment = SWT.CENTER;
		editor5.grabHorizontal = true;
		editor5.grabVertical = true;
		chkBlue = new Button(container,SWT.CHECK);
		chkBlue.pack();
		chkBlue.setSelection(true);
		chkBlue.addSelectionListener(control);
		editor5.setEditor(chkBlue,newItem,6);		
		
	}
	
	public CompositeEntry convertToCompositeEntry() {
		byte mask = (byte) (chkRed.getSelection() ? 1 : 0);
		mask += (byte)(chkGreen.getSelection() ? 2 : 0);
		mask += (byte)(chkBlue.getSelection() ? 4 : 0);
		float weight = spinner.getSelection()/100.0f;
		if (!chkActive.getSelection())
			weight = 0.0f;
		CompositeEntry entry = new CompositeEntry(name,weight,
												 convertToOperationFromInt(op.getSelectionIndex()),
												 mask);
		return entry;
	}
	
	private CompositeOp convertToOperationFromInt(int i) {
		switch(i) {
			case 0:
				return CompositeOp.ADD;
			case 1:
				return CompositeOp.SUBTRACT;
			case 2:
				return CompositeOp.MULTIPLY;
			case 3:
				return CompositeOp.DIVIDE;
			case 4:
				return CompositeOp.MAX;
			case 5:
				return CompositeOp.MIN;
			default:
				return CompositeOp.ADD;
		}
	}
	
	private int convertOperationToInt(CompositeOp op) {
		switch (op) {
			case ADD:
				return 0;
			case SUBTRACT:
				return 1;
			case MULTIPLY:
				return 2;							
			case DIVIDE:
				return 3;							
			case MIN:
				return 4;							
			case MAX:
				return 5;
			default:
				return 0;
		}
	}

	public void dispose() {
		spinner.dispose();
		slider.dispose();
		op.dispose();
		chkBlue.dispose();
		chkGreen.dispose();
		chkRed.dispose();
		panel.dispose();
		chkActive.dispose();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(spinner))
			slider.setSelection(spinner.getSelection());
		else
			spinner.setSelection((slider.getSelection() <= 100 ? slider.getSelection() : 100));
	}
	
}
