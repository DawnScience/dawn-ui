/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import org.dawnsci.common.richbeans.internal.GridUtils;
import org.dawnsci.common.widgets.Activator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;


/**
 * A cell editor that presents a list of items in a spinner box.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PlayCellEditor extends AppliableCellEditor {

	private final String   playJobName;
	private final long     waitTime;
	private int            rowHeight;
	private boolean        isPlaying = false;
	
	// UI
	private Button         play;
	private Text           text;
	private Composite      area;
	private IntegerDecorator boundsDecorator;

	public PlayCellEditor(final TableViewer viewer, 
						  final String      playJobName,
						  final long        waitTime) {
		this(viewer, playJobName, waitTime, -1);
	}

	/**
	 * 
	 * @param viewer
	 * @param playJobName
	 * @param waitTime - ms
	 */
	public PlayCellEditor(final TableViewer viewer, 
			                               final String      playJobName,
			                               final long        waitTime,
			                               final int         rowHeight) {
		
		super(viewer.getTable());		
		this.playJobName = playJobName;
		this.waitTime    = waitTime>0 ? waitTime : 1000;
		this.rowHeight   = rowHeight;
	}
	

	@Override
	protected Control createControl(Composite parent) {
		
		this.area   = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(3, false));
		removeMargins(area);
		
        this.text = new Text(area, SWT.NONE);
        text.setText("0");
        this.boundsDecorator = new IntegerDecorator(text);
        boundsDecorator.setMinimum(0);
        boundsDecorator.setAllowInvalidValues(false); // No coloring red, just do not allow at all.
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        text.addKeyListener(getKeyListener()); 
        
        final Composite buttons = new Composite(area, SWT.NONE);
        buttons.setLayout(new GridLayout(1, false));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        GridUtils.removeMargins(buttons);
        buttons.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        
        final Button up = new Button(buttons, SWT.ARROW | SWT.UP);
        up.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        up.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		incrementValue(SWT.ARROW_UP);
        	}			
		});
      
        final Button down = new Button(buttons, SWT.ARROW | SWT.DOWN);
        down.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        down.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		incrementValue(SWT.ARROW_DOWN);
        	}			
		});

        this.play = new Button(area, SWT.TOGGLE);
        play.setImage(Activator.getImage("icons/control_play_blue.png"));
        final GridData playData = new GridData(SWT.CENTER, SWT.FILL, false, true);
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
	
	
	@Override
	public void dispose() {
		if (keyListener!=null)   text.removeKeyListener(keyListener);
		super.dispose();
	}


	private KeyListener keyListener;
	private KeyListener getKeyListener() {
		if (keyListener!=null) return keyListener;
		keyListener = new KeyListener() {		
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				incrementValue(e.keyCode);
			}
		};
		return keyListener;
	}

	protected void incrementValue(int keyCode) {
		try {
			final int value = ((Number)doGetValue()).intValue();
			String strValue = null;
			if(keyCode==SWT.ARROW_UP) {  
				strValue = String.valueOf(value+1);
			} else if(keyCode==SWT.ARROW_DOWN) {  
				strValue = String.valueOf(value-1);
			} else if(keyCode==SWT.PAGE_UP) {  
				strValue = String.valueOf(value+10);
			} else if(keyCode==SWT.PAGE_DOWN) {  
				strValue = String.valueOf(value-10);
			}
			
			if (strValue!=null) {
				text.setText(strValue);
				final String finalValue = strValue;
				text.getDisplay().asyncExec(new Runnable() {
					public void run() {
						text.setSelection(finalValue.length(), finalValue.length());
					}
				});
			}
		} catch (Throwable ne) {
			return;
		}
	}

	@Override
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((area == null) || area.isDisposed()) {
			layoutData.minimumWidth = 60;
		} else {
			// make the comboBox 10 characters wide
			GC gc = new GC(area);
			layoutData.minimumWidth = (gc.getFontMetrics()
					.getAverageCharWidth() * 10) + 10;
			gc.dispose();
		}
		if (rowHeight>-1) {
			layoutData.minimumHeight =  rowHeight;
		}
		return layoutData;
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
		
		final int currentValue = Integer.parseInt(text.getText());
		final int max          = boundsDecorator.getMaximum().intValue();
		final int min          = boundsDecorator.getMinimum().intValue();
		
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
							if (text.isDisposed()) return;
							text.setText(String.valueOf(newValue));
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
		if (text!=null) {
			try {
			    return Integer.parseInt(text.getText());
			} catch (Throwable ne) {
				return boundsDecorator.getMinimum().intValue();
			}
		} 
		return null;
	}


	@Override
	protected void doSetFocus() {
		if (text!=null) {
			text.setFocus();
		} 
	}


	@Override
	protected void doSetValue(Object value) {
		if (text!=null) {
			try {
				text.setText(value.toString());
			} catch (java.lang.NumberFormatException nfe) {
				text.setSelection(0);
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
		text.addSelectionListener(l);
	}

	/**
	 * @param i
	 */
	public void setMaximum(int i) {
		if (boundsDecorator!=null) boundsDecorator.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		if (boundsDecorator!=null) boundsDecorator.setMinimum(i);
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

	public void addModifyListener(ModifyListener modifyListener) {
		text.addModifyListener(modifyListener);
	}

}
