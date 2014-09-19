/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui;

import java.util.HashSet;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ChangeSliceWidget {
	
	PositionIterator iterator;
	private int current = 0;
	int[] dataDims;
	
	private HashSet<ISliceChangeListener> listeners;
	
	public ChangeSliceWidget(Composite parent) {
		
		dataDims = new int[]{1,2};
		iterator = new PositionIterator(new int[]{2,3,4}, dataDims);
		
		
		listeners = new HashSet<ISliceChangeListener>();
		
		Composite baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setLayout(new GridLayout(4, true));
		
		Button startBtn = new Button(baseComposite, SWT.NONE);
		startBtn.setLayoutData(new GridData());
		startBtn.setImage(Activator.getImageDescriptor("icons/control-stop-180.png").createImage());
		startBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				iterator.reset();
				iterator.hasNext();
				current = 0;
				fireListeners();
			}

		});
		
		Button stepbackBtn = new Button(baseComposite, SWT.NONE);
		stepbackBtn.setLayoutData(new GridData());
		stepbackBtn.setImage(Activator.getImageDescriptor("icons/control-180.png").createImage());
		stepbackBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				current--;
				//Todo add step back to iterator
				iterator.reset();
				for (int i = 0; i < current-1; i++) iterator.hasNext();
				fireListeners();
			}

		});
		
		Button stepforwardBtn = new Button(baseComposite, SWT.NONE);
		stepforwardBtn.setLayoutData(new GridData());
		stepforwardBtn.setImage(Activator.getImageDescriptor("icons/control.png").createImage());
		stepforwardBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				iterator.hasNext();
				current++;
				fireListeners();
			}

		});
		
		Button endBtn = new Button(baseComposite, SWT.NONE);
		endBtn.setLayoutData(new GridData());
		endBtn.setImage(Activator.getImageDescriptor("icons/control-stop.png").createImage());
		endBtn.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				iterator.reset();
				while (iterator.hasNext()) {
					current++;
				}
				
				iterator.reset();
				for (int i = 0; i < current; i++) iterator.hasNext();
				fireListeners();
			}

		});
	}
	
	public void setDatasetShapeInformation(int[] shape, int[] dataDims, Slice[] slices) {
		iterator = new PositionIterator(shape,slices,dataDims);
		iterator.hasNext();
		this.dataDims = dataDims;
		current = 0;
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
		int[] pos = iterator.getPos();
		int[] end = pos.clone();
		for (int i = 0; i<pos.length;i++) {
			end[i]++;
		}

		for (int i = 0; i < this.dataDims.length; i++){
			end[this.dataDims[i]] = iterator.getShape()[this.dataDims[i]];
		}

		int[] st = pos.clone();
		for (int i = 0; i < st.length;i++) st[i] = 1;
		
		return Slice.convertToSlice(pos, end, st);
	}

}
