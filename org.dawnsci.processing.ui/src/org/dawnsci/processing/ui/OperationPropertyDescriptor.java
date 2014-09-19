/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.dawb.common.util.text.StringUtils;
import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.ClassCellEditor;
import org.dawnsci.common.widgets.celleditor.FileDialogCellEditor;
import org.dawnsci.plotting.roi.RegionCellEditor;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Current supported values in models which can be edited by this descriptor:
 * 
 *  o Boolean/boolean, Double/double, Float/float, Integer/int, Long/long
 *  o Enums (as choice list)
 *  o double[] float[] int[] long[]
 *  o String
 * 
 * 
 * @author Matthew Gerring
 *
 */
public class OperationPropertyDescriptor extends PropertyDescriptor implements Comparable<OperationPropertyDescriptor> {


	private ILabelProvider labelProvider;
	private IOperationModel model;
	private String name;

	public OperationPropertyDescriptor(IOperationModel model, String name) {
		super(name, getDisplayName(model, name));
		this.model = model;
		this.name  = name;
	}
	
	private static OperationModelField getAnnotation(IOperationModel model, String fieldName) {
		
		try {
			Field field = getField(model, fieldName);
	        if (field!=null) {
	        	OperationModelField anot = field.getAnnotation(OperationModelField.class);
	        	if (anot!=null) {
	        		return anot;
	        	}
	        }
	        return null;
	        
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
    private static Field getField(IOperationModel model, String fieldName) throws NoSuchFieldException, SecurityException {
		
    	Field field;
		try {
			field = model.getClass().getDeclaredField(fieldName);
		} catch (Exception ne) {
			field = model.getClass().getSuperclass().getDeclaredField(fieldName);
		}
		return field;
	}

	private static String getDisplayName(IOperationModel model, String fieldName) {
    	
    	OperationModelField anot = getAnnotation(model, fieldName);
    	if (anot!=null) {
    		String label = anot.label();
    		if (label!=null && !"".equals(label)) return label;
    	}
    	return fieldName;
	}

	public ILabelProvider getLabelProvider() {
        if (labelProvider != null) {
			return labelProvider;
		}
        labelProvider = new LabelProvider();
        return labelProvider;
    }


    public CellEditor createPropertyEditor(Composite parent) {
    	
        Object value;
		try {
			value = model.get(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		Class<? extends Object> clazz = null;
		if (value!=null) {
			clazz = value.getClass();
		} else {
			try {
				Field field = getField(model, name);
				clazz = field.getType();
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
        
		CellEditor ed = null;
    	final OperationModelField anot = getAnnotation(model, name);
    	
        if (clazz == Boolean.class) {
        	ed = new CheckboxCellEditor(parent, SWT.NONE);
        	
        } else if (Number.class.isAssignableFrom(clazz) || isNumberArray(clazz)) {        	
        	ed = getNumberEditor(clazz, parent);
        	
        } else if (IROI.class.isAssignableFrom(clazz)) {        	
        	ed = new RegionCellEditor(parent);
        	
        } else if (Enum.class.isAssignableFrom(clazz)) {
        	ed = getChoiceEditor((Class<? extends Enum>)clazz, parent);
        	
        } else if (FileDialogCellEditor.isEditorFor(clazz) || (anot!=null && anot.file()!=FileType.NONE)) {
        	FileDialogCellEditor fe = new FileDialogCellEditor(parent);
        	fe.setValueClass(clazz);
        	ed = fe;
        	if (anot!=null) {
        		fe.setDirectory(anot.file().isDirectory());
        		fe.setNewFile(anot.file().isNewFile());
        	}
        
        } else if (String.class.equals(clazz)) {
        	ed = new TextCellEditor(parent);
        }
        
        // Show the tooltip, if there is one
        if (ed!=null) {
        	if (anot!=null) {
        		String hint = anot.hint();
        		if (hint!=null && !"".equals(hint)) {
        			showHint(hint, ed);
        		}
        	}
        }
        
        return ed;
    }

	private void showHint(final String hint, final CellEditor cellEd) {
		
		final Control control = cellEd.getControl();
		control.getDisplay().asyncExec(new Runnable() {
			public void run() {
				
				final DefaultToolTip tooltip = new DefaultToolTip(control, ToolTip.NO_RECREATE, true);
				tooltip.setText(hint);
				tooltip.setHideOnMouseDown(true);
				control.addListener(SWT.Dispose, new Listener() {
					
					@Override
					public void handleEvent(Event event) {
						if (!control.isDisposed()) tooltip.hide();
					}
				});

				tooltip.show(new Point(control.getSize().x, 0));
				
			}
		});
	}

	private boolean isNumberArray(Class<? extends Object> clazz) {
		
		if (clazz==null)      return false;
		if (!clazz.isArray()) return false;
		
		return double[].class.isAssignableFrom(clazz) || float[].class.isAssignableFrom(clazz) ||
               int[].class.isAssignableFrom(clazz)    || long[].class.isAssignableFrom(clazz);
	}

	private CellEditor getChoiceEditor(final Class<? extends Enum> clazz, Composite parent) {
		
		final Enum[]   values = clazz.getEnumConstants();
	    final String[] items  = Arrays.toString(values).replaceAll("^.|.$", "").split(", ");
		
		CComboCellEditor cellEd = new CComboCellEditor(parent, items) {
    	    protected void doSetValue(Object value) {
                if (value instanceof Enum) value = ((Enum) value).ordinal();
                super.doSetValue(value);
    	    }
    		protected Object doGetValue() {
    			Integer ordinal = (Integer)super.doGetValue();
    			return values[ordinal];
    		}
		};
		
		return cellEd;
	}

	private CellEditor getNumberEditor(final Class<? extends Object> clazz, Composite parent) {
    	
		OperationModelField anot = getAnnotation(model, name);
		CellEditor textEd = null;
	    if (anot!=null) {
	    	textEd = new ClassCellEditor(parent, clazz, anot.min(), anot.max(), anot.unit(), SWT.NONE);
	    } else {
	    	textEd = new ClassCellEditor(parent, clazz, SWT.NONE);
	    }

    	return textEd;
	}

	private class LabelProvider extends BaseLabelProvider implements ILabelProvider {

		private Image ticked, unticked;
		/**
		 * Creates a new label provider.
		 */
		public LabelProvider() {
		}


		/**
		 * The <code>LabelProvider</code> implementation of this
		 * <code>ILabelProvider</code> method returns <code>null</code>.
		 * Subclasses may override.
		 */
		public Image getImage(Object element) {
			if (element instanceof Boolean) {
				if (ticked==null)   ticked   = Activator.getImageDescriptor("icons/ticked.png").createImage();
				if (unticked==null) unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
				Boolean val = (Boolean)element;
				return val ? ticked : unticked;
			}
			return null;
		}

		/**
		 * The <code>LabelProvider</code> implementation of this
		 * <code>ILabelProvider</code> method returns the element's
		 * <code>toString</code> string. Subclasses may override.
		 */
		public String getText(Object element) {
			
			if (element == null)            return "";
			if (element instanceof Boolean) return "";
			
			StringBuilder buf = new StringBuilder();
			if (element.getClass().isArray()) {
				buf.append( StringUtils.toString(element) );
			} else {
			    buf.append(element.toString());//$NON-NLS-1$
			}
			
			OperationModelField anot = getAnnotation(model, name);
			if (anot!=null) buf.append(" "+anot.unit());
			return buf.toString();
		}
		
		public void dispose() {
			if (ticked!=null)   ticked.dispose();
			if (unticked!=null) unticked.dispose();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationPropertyDescriptor other = (OperationPropertyDescriptor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(OperationPropertyDescriptor o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
