/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.slice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.dataset.impl.SliceND;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceNDGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ChangeSliceWidget {
	
//	private PositionIterator iterator;
	private int current = 0;
	private int max = 0;
	private int[] dataDims;
	private Button startBtn;
	private Button stepbackBtn;
	private Button stepforwardBtn;
	private Button endBtn;
	private List<SliceND> input;
	private List<SliceND> output;
	private SliceND subsampling;
	private int[] shape;
	
	private HashSet<ISliceChangeListener> listeners;
	
	public ChangeSliceWidget(Composite parent) {
		input = new ArrayList<SliceND>();
		max = 0;
		current = 0;
		listeners = new HashSet<ISliceChangeListener>();
		
		Composite baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setLayout(new GridLayout(4, true));
		
		startBtn = new Button(baseComposite, SWT.NONE);
		startBtn.setLayoutData(new GridData());
		startBtn.setImage(Activator.getImageDescriptor("icons/control-stop-180.png").createImage());
		
		stepbackBtn = new Button(baseComposite, SWT.NONE);
		stepbackBtn.setLayoutData(new GridData());
		stepbackBtn.setImage(Activator.getImageDescriptor("icons/control-180.png").createImage());
		
		
		stepforwardBtn = new Button(baseComposite, SWT.NONE);
		stepforwardBtn.setLayoutData(new GridData());
		stepforwardBtn.setImage(Activator.getImageDescriptor("icons/control.png").createImage());
		
		endBtn = new Button(baseComposite, SWT.NONE);
		endBtn.setLayoutData(new GridData());
		endBtn.setImage(Activator.getImageDescriptor("icons/control-stop.png").createImage());		
		
		startBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				iterator.reset();
//				iterator.hasNext();
				current = 0;
				updateButtons();
				fireListeners();
			}

		});
		
		stepbackBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				current--;
				
				updateButtons();
				//Todo add step back to iterator
//				iterator.reset();
//				iterator.hasNext();
//				for (int i = 0; i < current; i++) iterator.hasNext();
				fireListeners();
			}

		});
		
		stepforwardBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
//				iterator.hasNext();
				current++;
				updateButtons();
				fireListeners();
			}

		});
		
		endBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				current = max - 1;
				
				updateButtons();
				
				fireListeners();
			}

		});
	}
	
	public void setDatasetShapeInformation(int[] shape, int[] dataDims, Slice[] slices) {
		this.shape = shape;
		this.dataDims = dataDims;
		subsampling = new SliceND(shape, slices);
		SliceNDGenerator gen = new SliceNDGenerator(shape, dataDims, new SliceND(shape, slices));
		output = new ArrayList<SliceND>();
		input = gen.generateDataSlices(output);
		max = input.size();
		current = 0;

		updateButtons();
	}
	
	public void addSliceChangeListener(ISliceChangeListener listener){
		listeners.add(listener);
	}
	
	public void removeSliceChangeListener(ISliceChangeListener listener) {
		listeners.remove(listener);
	}
	
	
	private void fireListeners() {
		SliceChangeEvent event = new SliceChangeEvent(this, getCurrentSlice());
		for (ISliceChangeListener listener : listeners) listener.sliceChanged(event);
	}
	
	public Slice[] getCurrentSlice() {
		if (input.isEmpty()) return null;
		return input.get(current).convertToSlice();
	}
	
	public SliceInformation getCurrentSliceInformation() {
		
		return new SliceInformation(input.get(current), output.get(current), subsampling,shape, dataDims, max, current);
		
	}
	
	private void updateButtons() {
		
		startBtn.setEnabled(true);
		stepbackBtn.setEnabled(true);
		endBtn.setEnabled(true);
		stepforwardBtn.setEnabled(true);
		
		if (current == 0 && max == 0) {
			startBtn.setEnabled(false);
			stepbackBtn.setEnabled(false);
			endBtn.setEnabled(false);
			stepforwardBtn.setEnabled(false);
		}
		
		if (current == 0) {
			startBtn.setEnabled(false);
			stepbackBtn.setEnabled(false);
		}
		
		if (current == max) {
			endBtn.setEnabled(false);
			stepforwardBtn.setEnabled(false);
		}
	}
	
	public void disable() {
		startBtn.setEnabled(false);
		stepbackBtn.setEnabled(false);
		endBtn.setEnabled(false);
		stepforwardBtn.setEnabled(false);
	}

}
