package org.dawnsci.common.richbeans.examples;

import java.lang.reflect.Method;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.swt.widgets.Control;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExampleJSONWritingValueListener extends ValueAdapter {

	private BeanController controller;
	private Control value;
	private int textLimit = -1;

	/**
	 * @param controller
	 * @param value A control with a setText(...) method.
	 */
	public ExampleJSONWritingValueListener(BeanController controller, Control value) {

		// The name should be unique and is only hard-coded here for simplicity - see the javadoc for ValueAdapter.
		this.name = "Example listener";

		this.controller = controller;
		this.value = value;
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {
		try {
			// Save the values
			controller.uiToBean();

			// We spit out the bean in JSON since
			// rich bean does not care if bean in XML or
			// whatever at this stage.
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(controller.getBean());
			
			if (textLimit>0) {
				json = json.substring(0, textLimit)+"...";
			}
			
			// The control must have a setText method
			Method m = value.getClass().getMethod("setText", String.class);
			m.invoke(value, json);
			
			value.getParent().layout();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int getTextLimit() {
		return textLimit;
	}

	public void setTextLimit(int textLimit) {
		this.textLimit = textLimit;
	}
}