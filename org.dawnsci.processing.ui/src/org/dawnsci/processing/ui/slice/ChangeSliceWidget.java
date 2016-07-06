/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.slice;

import java.util.Arrays;
import java.util.HashSet;

import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.january.dataset.AbstractDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ChangeSliceWidget {
	
	private int current = 0;
	private int max = 0;
	private int skip = 1;
	private int[] dataDims;
	private Button startBtn;
	private Button stepbackBtn;
	private Button skipBackBtn;
	private Button stepforwardBtn;
	private Button skipForwardBtn;
	private Button endBtn;
	private SliceND subsampling;
	
	private HashSet<ISliceChangeListener> listeners;
	
	public ChangeSliceWidget(Composite parent) {
		max = 0;
		current = 0;
		listeners = new HashSet<ISliceChangeListener>();
		
		Composite baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setLayout(new GridLayout(6, true));
		
		startBtn = new Button(baseComposite, SWT.NONE);
		startBtn.setLayoutData(new GridData());
		startBtn.setImage(Activator.getImageDescriptor("icons/control-stop-180.png").createImage());
		
		skipBackBtn = new Button(baseComposite, SWT.NONE);
		skipBackBtn.setLayoutData(new GridData());
		skipBackBtn.setImage(Activator.getImageDescriptor("icons/control-double-180.png").createImage());
		
		stepbackBtn = new Button(baseComposite, SWT.NONE);
		stepbackBtn.setLayoutData(new GridData());
		stepbackBtn.setImage(Activator.getImageDescriptor("icons/control-180.png").createImage());
		
		stepforwardBtn = new Button(baseComposite, SWT.NONE);
		stepforwardBtn.setLayoutData(new GridData());
		stepforwardBtn.setImage(Activator.getImageDescriptor("icons/control.png").createImage());
		
		skipForwardBtn = new Button(baseComposite, SWT.NONE);
		skipForwardBtn.setLayoutData(new GridData());
		skipForwardBtn.setImage(Activator.getImageDescriptor("icons/control-double.png").createImage());
		
		endBtn = new Button(baseComposite, SWT.NONE);
		endBtn.setLayoutData(new GridData());
		endBtn.setImage(Activator.getImageDescriptor("icons/control-stop.png").createImage());		
		
		startBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
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
				fireListeners();
			}

		});
		
		stepforwardBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
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
		
		skipBackBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				current = current - skip;
				current = Math.max(current, 0);
				updateButtons();
				fireListeners();
			}
		});
		
		skipForwardBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				current = current + skip;
				current = Math.min(current, max - 1);
				updateButtons();
				fireListeners();
			}
		});
	}
	
	public void setDatasetShapeInformation(int[] shape, int[] dataDims, Slice[] slices) {
		this.dataDims = dataDims;
		subsampling = new SliceND(shape, slices);
		max = calculateTotal(subsampling, dataDims);
		current = 0;
		skip = (int)(max*0.05);
		skip = Math.max(1, skip);

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
		
		return getInputOutputPosition()[0].convertToSlice();
	}
	
	public SliceInformation getCurrentSliceInformation() {
		
		SliceND[] slices = getInputOutputPosition();
		
		return new SliceInformation(slices[0], slices[1], subsampling, dataDims, max, current);
	}
	
	private SliceND[] getInputOutputPosition() {
		int[] shape = subsampling.getShape().clone();
		int[] scanShape = shape.clone();
		for (int i = 0; i< dataDims.length; i++) scanShape[dataDims[i]] = 1;
		int[] start = AbstractDataset.getNDPositionFromShape(current, scanShape);
		int[] stop = start.clone();
		for (int i = 0; i< stop.length; i++) stop[i]++;
		for (int i = 0; i< dataDims.length; i++) stop[dataDims[i]] = shape[dataDims[i]];
		int[] step = new int[stop.length];
		Arrays.fill(step, 1);

		SliceND output = new SliceND(shape,start,stop,step);

		int[] inStart = start.clone();


		int[] subStep = subsampling.getStep();
		for (int i = 0 ; i < start.length; i++) inStart[i] *= subStep[i];
		for (int i = 0; i < start.length; i++) inStart[i] += subsampling.getStart()[i];
		int[] subStop = inStart.clone();
		for (int i = 0; i< subStop.length; i++) subStop[i]++;
		for (int i = 0; i< dataDims.length; i++) subStop[dataDims[i]] = shape[dataDims[i]];

		SliceND input = new SliceND(subsampling.getSourceShape(),inStart,subStop,step);
		return new SliceND[]{input,output};
	}
	
	private void updateButtons() {
		
		startBtn.setEnabled(true);
		stepbackBtn.setEnabled(true);
		endBtn.setEnabled(true);
		stepforwardBtn.setEnabled(true);
		skipForwardBtn.setEnabled(true);
		skipBackBtn.setEnabled(true);
		
		if (current == 0 && max == 0) {
			startBtn.setEnabled(false);
			stepbackBtn.setEnabled(false);
			endBtn.setEnabled(false);
			stepforwardBtn.setEnabled(false);
			skipBackBtn.setEnabled(false);
			skipForwardBtn.setEnabled(false);
		}
		
		if (current == 0) {
			startBtn.setEnabled(false);
			skipBackBtn.setEnabled(false);
			stepbackBtn.setEnabled(false);
		}
		
		if (current == max-1) {
			endBtn.setEnabled(false);
			stepforwardBtn.setEnabled(false);
			skipForwardBtn.setEnabled(false);
		}
	}
	
	public void disable() {
		startBtn.setEnabled(false);
		stepbackBtn.setEnabled(false);
		endBtn.setEnabled(false);
		stepforwardBtn.setEnabled(false);
		skipForwardBtn.setEnabled(false);
		skipBackBtn.setEnabled(false);
	}
	
	private int calculateTotal(SliceND slice, int[] axes) {
		int[] nShape = slice.getShape();

		int[] dd = axes.clone();
		Arrays.sort(dd);
		
		 int n = 1;
		 for (int i = 0; i < nShape.length; i++) {
			 if (Arrays.binarySearch(dd, i) < 0) n *= nShape[i];
		 }
		return n;
	}
	
	public void dispose(){
		if (startBtn != null && startBtn.getImage()!= null) startBtn.getImage().dispose();
		if (skipBackBtn != null && skipBackBtn.getImage()!= null) skipBackBtn.getImage().dispose();
		if (stepbackBtn != null && stepbackBtn.getImage()!= null) stepbackBtn.getImage().dispose();
		if (stepforwardBtn != null && stepforwardBtn.getImage()!= null) stepforwardBtn.getImage().dispose();
		if (skipForwardBtn != null && skipForwardBtn.getImage()!= null) skipForwardBtn.getImage().dispose();
		if (endBtn != null && endBtn.getImage()!= null) endBtn.getImage().dispose();
	}

}
