package org.dawnsci.common.richbeans.components.decorators;

import org.dawnsci.common.widgets.decorator.BoundsDecorator;
import org.dawnsci.common.widgets.decorator.IValueChangeListener;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.dawnsci.common.widgets.decorator.ValueChangeEvent;
import org.eclipse.swt.widgets.Text;

/**
 * An object to make any Text field into a field widget.
 * 
 * @author fcp94556
 *
 */
public class TextFieldDecorator extends FieldDecorator<Text> {
	
	private BoundsDecorator boundsDecorator;
	private Object          value;

	public TextFieldDecorator(Text connection, BoundsDecorator boundsDecorator) {
		super(connection);
		this.boundsDecorator = boundsDecorator;
		boundsDecorator.addValueChangeListener(new IValueChangeListener() {		
			@Override
			public void valueValidating(ValueChangeEvent evt) {
				value= evt.getValue();
				fireValueListeners(evt.getValue());
			}
		});
	}

	@Override
	public Object getValue() {
		if (value!=null) return value;
		return boundsDecorator.getValue();
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof Number)) value = Double.parseDouble(value.toString());
	    boundsDecorator.setValue(((Number)value).doubleValue());
	}

}
