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
package org.dawnsci.common.widgets.celleditor;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;


/**
 * A cell editor that presents a list of items in a spinner box.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SpinnerCellEditorWithPlayButton extends AppliableCellEditor {

	private final String      playJobName;
	private final long        waitTime;
	
	private boolean        isPlaying = false;
	private Button         play;
	private Spinner        spinner;
	private Composite area;

	/**
	 * 
	 * @param viewer
	 * @param playJobName
	 * @param waitTime - ms
	 */
	public SpinnerCellEditorWithPlayButton(final TableViewer viewer, 
			                               final String      playJobName,
			                               final long        waitTime) {
		
		super(viewer.getTable());		
		this.playJobName = playJobName;
		this.waitTime    = waitTime>0 ? waitTime : 1000;
	}
	

	@Override
	protected Control createControl(Composite parent) {
		
		this.area   = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(3, false));
		removeMargins(area);
		
        this.spinner = new Spinner(area, SWT.NONE);
        spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
       
        this.play = new Button(area, SWT.TOGGLE);
        play.setImage(Activator.getImage("icons/control_play_blue.png"));
        final GridData playData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        playData.widthHint  = 24;
        playData.heightHint = 24;
        play.setLayoutData(playData);
        play.setToolTipText("Play through the slices");
        
        play.addSelectionListener(new SelectionAdapter() {
           	@Override
       	    public void widgetDefaultSelected(SelectionEvent e) {
           		isPlaying = !isPlaying;
           		updatePlaying();
        	}
        	@Override
			public void widgetSelected(SelectionEvent e) {
        		isPlaying = !isPlaying;
        		updatePlaying();
        	}
		});
        
       
        return area;
	}
	
	private IProgressMonitor playMonitor;
	/**
	 * Plays or stops the playing
	 */
	protected void updatePlaying() {
		
		if (playJobName==null) return;
		
		if (!isPlaying) {
			if (playMonitor!=null) playMonitor.setCanceled(true);
			playMonitor = null;
			return;
		}
		
		final int currentValue = spinner.getSelection();
		final int max          = spinner.getMaximum();
		final int min          = spinner.getMinimum();
		
		final Job job = new Job(playJobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
			
				playMonitor = monitor;
				int val = currentValue;
				while(!monitor.isCanceled() && !getControl().isDisposed()) {
					val++;
					if (val>max) val = min;
					
					final int newValue = val;
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (spinner.isDisposed()) return;
							spinner.setSelection(newValue);
						}
					});
					
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException ignored) {
						
					}
				}
				
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setUser(false);
		job.schedule();
	}


	@Override
	public void activate() {
		((Composite)this.getControl()).getParent().layout();
	}

	@Override
	public void deactivate() {
        super.deactivate();
        play.setSelection(false);
        isPlaying = false;
        updatePlaying();
	}


	@Override
	protected Object doGetValue() {
		if (spinner!=null) {
			return spinner.getSelection();
		} 
		return null;
	}


	@Override
	protected void doSetFocus() {
		if (spinner!=null) {
			spinner.setFocus();
		} 
	}


	@Override
	protected void doSetValue(Object value) {
		if (spinner!=null) {
			try {
				spinner.setSelection(Integer.parseInt(value.toString()));
			} catch (java.lang.NumberFormatException nfe) {
				spinner.setSelection(0);
			}
		}
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	@Override
	public void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);
		fireApplyEditorValue();
		deactivate();
	}

	public void addSelectionListener(SelectionListener l) {
		spinner.addSelectionListener(l);
	}

	/**
	 * @param i
	 */
	public void setMaximum(int i) {
		if (spinner!=null) spinner.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		if (spinner!=null) spinner.setMinimum(i);
	}


	public void setPlayButtonVisible(boolean isVisible) {
		setVisible(play, isVisible);
		play.getParent().layout(play.getParent().getChildren());
	}
	
	public void setBackground(Color back) {
		area.setBackground(back);
	}
	
	@Override
	protected int getDoubleClickTimeout() {
		return 0;
	}
	
	public static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;

	}
	public static void setVisible(final Control widget, final boolean isVisible) {
		
		if (widget == null) return;
		if (widget.getLayoutData() instanceof GridData) {
			final GridData data = (GridData) widget.getLayoutData();
			data.exclude = !isVisible;
		}
		widget.setVisible(isVisible);
	}

}
