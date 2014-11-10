/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawb.common.ui.components.cell.ScaleCellEditor;
import org.dawnsci.common.widgets.celleditor.PlayCellEditor;
import org.dawnsci.slicing.Activator; // On purpose! Gets preference from expected place.
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


class SliceEditingSupport extends EditingSupport {

	/**
	 * Format used to show value in nexus axes
	 */
	private NumberFormat format;

	private ScaleCellEditor  scaleEditor;
	private PlayCellEditor   spinnerEditor;
	private TextCellEditor   rangeEditor;
	private SliceSystemImpl  system;

	public SliceEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
		
		super(viewer);
		this.system = system;
		this.format = DecimalFormat.getNumberInstance();
		
		scaleEditor = new ScaleCellEditor((Composite)viewer.getControl(), SWT.NO_FOCUS);
		final Scale scale = (Scale)scaleEditor.getControl();
		scale.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		scaleEditor.setMinimum(0);
		scale.setIncrement(1);
		scale.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseUp(MouseEvent e) {
				if (!system.is3D()) return;
				updateSlice(true);
			}
		});
		scaleEditor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSlice(!system.is3D());
			}
		});
		
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
		spinnerEditor = new PlayCellEditor((TableViewer)viewer, "Play through slices", store.getInt("data.format.slice.play.speed"), 45);
		spinnerEditor.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		spinnerEditor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
                final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
                final int value = ((Number)spinnerEditor.getValue()).intValue();
                data.setSlice(value);
                data.setSliceRange(null, false);
         		if (system.synchronizeSliceData(data)) system.slice(false);
			}
			
		});

		rangeEditor = new TextCellEditor((Composite)viewer.getControl(), SWT.NONE);
		((Text)rangeEditor.getControl()).addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
                final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
				final Text text = (Text)e.getSource();
				final String range = text.getText();
				
				final Matcher matcher = Pattern.compile("(\\d+)\\:(\\d+)").matcher(range);
				if ("all".equals(range)) {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				} else if (matcher.matches()) {
					final int[] shape = system.getLazyDataset().getShape();
					int start = Integer.parseInt(matcher.group(1));
					int end   = Integer.parseInt(matcher.group(2));
					if (start>-1&&end>-1&&end>start&&start<shape[data.getDimension()]&&end<=shape[data.getDimension()]) {
					    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					} else {
						text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
				} else {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}			
			}
		});
		((Text)rangeEditor.getControl()).setToolTipText("Please enter \"all\" or a range of the form <start>:<stop>.");

	}
	
	protected void updateSlice(boolean doSlice) {
		final DimsData data = (DimsData)((IStructuredSelection)getViewer().getSelection()).getFirstElement();
		final Scale   scale = (Scale)scaleEditor.getControl();
		final int     value = scale.getSelection();
		data.setSlice(value);
		data.setSliceRange(null, false);
		scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));		
		if (doSlice&&system.synchronizeSliceData(data)) system.slice(false);
	}
	
	protected String getScaleTooltip(DimsData data, int minimum, int maximum) {
		
		int value = data.getSlice();
        final StringBuffer buf = new StringBuffer();
        
        IDataset axis = null;
        if (system.isAxesVisible()) try {
            axis = SliceUtils.getAxis(system.getCurrentSlice(), system.getData().getVariableManager(), data, null); 
        } catch (Throwable ne) {
        	axis = null;
        }
        
        String min = String.valueOf(minimum);
        String max = String.valueOf(maximum);
        String val = String.valueOf(value);
        
        int ispan = value+data.getSliceSpan();
        if (ispan>=maximum) ispan = maximum;
        String span= String.valueOf(value+data.getSliceSpan());
        try {
	        if (axis!=null) {
				min = format.format(axis.getDouble(minimum));
				max = format.format(axis.getDouble(maximum));
				val = format.format(axis.getDouble(value));
				if (data.getPlotAxis().isAdvanced()) {
					span = format.format(axis.getDouble(ispan));
				}
	        } 
        } catch (Throwable ignored) {
        	// Use indices
        }
    
        if (data.getPlotAxis().isAdvanced()) {
        	buf.append(data.getPlotAxis().getName());
        	buf.append("(");
        	buf.append(val);
        	buf.append(":");
        	buf.append(span);
        	buf.append(")");
        } else {
            buf.append(min);
            buf.append(" <= ");
            buf.append(val);
            buf.append(" <= ");
            buf.append(max);
        }
        return buf.toString();
	}


	@Override
	protected CellEditor getCellEditor(Object element) {
		
		int[] dataShape = system.getLazyDataset().getShape();
		final DimsData data = (DimsData)element;
		
		CellEditor ret = null;
		final String     text;
		if (data.isTextRange()) {
			ret = rangeEditor;
			text = "Please enter a range <start>:<stop>, for instance '0:10' or 'all'.\n\n"+
				   "This range will slice a whole section out of the dimension '"+(data.getDimension()+1)+"'.\n"+
				   "Change the 'Type' column to '(Slice)' for a discrete value or to set as an axis.\n"+
				   "Currently ranges must be done using the data indices not the axis value.";
			
		}else if (Activator.getDefault().getPreferenceStore().getInt(SliceConstants.SLICE_EDITOR)==1) {
            spinnerEditor.setMaximum(dataShape[data.getDimension()]-1);
            ret = spinnerEditor;
            
    		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
    		int time = store.getInt("data.format.slice.play.speed");

			text = "This is the slice value which can by typing or using the up/down arrows.\n\n"+
				   "The play button, when pressed, will play the slices like a movie.\n"+
				   "There is a preference to change the play speed; the current frame rate is 1 per "+time+" ms.\n"+
				   "Click the play button again to stop playback.";
		} else {
			final Scale scale = (Scale)scaleEditor.getControl();
			scale.setMaximum(dataShape[data.getDimension()]-1);
			scale.setPageIncrement(scale.getMaximum()/10);

			scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));
			ret  = scaleEditor;
			
			text = "Moving this slider changes the slice for the dimension '"+(data.getDimension()+1)+"' of the data.\n"+
				   "If you are slicing large data at the moment, it might take a moment to do the slice.\n\n"+
			       "There is a menu button on the toolbar to change from a slider to entering a value directly.\n"+
				   "(It also has a play button to play through slices like video.)";
		}
		
		Hinter.showHint(ret, text);

		return ret;
	}

	@Override
	protected boolean canEdit(Object element) {
		final DimsData data = (DimsData)element;
		final int[] dataShape = system.getLazyDataset().getShape();
		if (dataShape[data.getDimension()]<2) return false;
		if (data.isTextRange()) return true;
				
		return data.getPlotAxis()==AxisType.SLICE || data.getPlotAxis().isAdvanced();
	}

	@Override
	protected Object getValue(Object element) {
		final DimsData data = (DimsData)element;
		if (data.isTextRange()) return data.getSliceRange(true) != null ? data.getSliceRange(true) : "all";
		return data.getSlice();
	}

	@Override
	protected void setValue(Object element, Object value) {
		final DimsData data = (DimsData)element;
		if (value instanceof Integer) {
			data.setSlice((Integer)value);
		} else {
			data.setSliceRange((String)value, data.getPlotAxis()!=AxisType.Y_MANY);
		}
		system.update(data, true);
	}

	void setPlayButtonVisible(boolean vis) {
		spinnerEditor.setPlayButtonVisible(vis);
	}
}
