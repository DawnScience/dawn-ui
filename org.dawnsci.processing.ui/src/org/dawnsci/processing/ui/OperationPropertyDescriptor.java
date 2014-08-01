package org.dawnsci.processing.ui;

import java.lang.reflect.Field;

import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.dawnsci.plotting.roi.RegionCellEditor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;
import uk.ac.diamond.scisoft.analysis.processing.model.OperationModelField;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

public class OperationPropertyDescriptor extends PropertyDescriptor implements Comparable<OperationPropertyDescriptor> {

	private IOperationModel model;
	private String          name;
	private ILabelProvider labelProvider;

	public OperationPropertyDescriptor(IOperationModel model, String name) {
		super(name, getDisplayName(model, name));
		this.model = model;
		this.name  = name;
	}
	
    private static String getDisplayName(IOperationModel model, String fieldName) {
		
		try {
			Field field;
			try {
				field = model.getClass().getDeclaredField(fieldName);
			} catch (Exception ne) {
				field = model.getClass().getSuperclass().getDeclaredField(fieldName);
			}
	        if (field!=null) {
	        	OperationModelField anot = field.getAnnotation(OperationModelField.class);
	        	if (anot!=null) {
	        		String label = anot.label();
	        		if (label!=null && !"".equals(label)) return label;
	        	}
	        }
	        return fieldName;
	        
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return fieldName;
		}
	}

	public ILabelProvider getLabelProvider() {
        if (labelProvider != null) {
			return labelProvider;
		}
		return new LabelProvider();
    }


    public CellEditor createPropertyEditor(Composite parent) {
    	
        Object value;
		try {
			value = model.get(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
        if (value instanceof Boolean) {
        	return new CheckboxCellEditor(parent, SWT.NONE);
        	
        } else if (value instanceof Number) {        	
        	return getNumberEditor((Number)value, parent);
        	
        } else if (value instanceof IROI) {        	
        	return new RegionCellEditor(parent);
        }
        
        return null;
    }

	private CellEditor getNumberEditor(final Number number, Composite parent) {
    	
		final boolean isFloat = number instanceof Double || number instanceof Float;
		final boolean isInt   = number instanceof Integer || number instanceof Long;
		
		
    	final TextCellEditor textEd = new TextCellEditor(parent, SWT.NONE) {
    	    protected void doSetValue(Object value) {
                if (value instanceof Number) value = value.toString();
                super.doSetValue(value);
    	    }
    		protected Object doGetValue() {
    			String stringValue = (String)super.doGetValue();
      			if (number instanceof Double)  return new Double(stringValue);
      			if (number instanceof Float)   return new Float(stringValue);
      			if (number instanceof Integer) return new Integer(stringValue);
      			if (number instanceof Long)    return new Long(stringValue);
    			return stringValue;
    		}
    	};
    	
    	final Text           text   = (Text) textEd.getControl();
    	
    	// TODO Bounds / units!
    	if (isFloat) {
    		new FloatDecorator(text);
    	} else if (isInt) {
       		new IntegerDecorator(text);
    	}
    	
    	return textEd;
	}

	private class LabelProvider extends BaseLabelProvider implements ILabelProvider {

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
			return null;
		}

		/**
		 * The <code>LabelProvider</code> implementation of this
		 * <code>ILabelProvider</code> method returns the element's
		 * <code>toString</code> string. Subclasses may override.
		 */
		public String getText(Object element) {
			return element == null ? "" : element.toString();//$NON-NLS-1$
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
}
