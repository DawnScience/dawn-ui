package org.dawnsci.processing.ui;

import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.dawnsci.plotting.roi.RegionCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

public class OperationPropertyDescriptor extends PropertyDescriptor {

	private IOperationModel model;
	private String          name;

	public OperationPropertyDescriptor(IOperationModel model, String name) {
		super(name, name);
		this.model = model;
		this.name  = name;
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

}
