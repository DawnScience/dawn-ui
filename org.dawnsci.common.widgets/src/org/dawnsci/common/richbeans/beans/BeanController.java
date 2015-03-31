package org.dawnsci.common.richbeans.beans;

import org.dawnsci.common.richbeans.event.ValueListener;

public class BeanController {

	private Object uiObject;
	private Object beanObject;
	private ValueListener valueListener;
	
	public BeanController() {
		
	}
	public BeanController(Object uiObject, Object beanObject) {
		this.uiObject   = uiObject;
		this.beanObject = beanObject;
	}
	
	public Object getUiObject() {
		return uiObject;
	}
	public void setUiObject(Object uiObject) {
		this.uiObject = uiObject;
	}
	public Object getBeanObject() {
		return beanObject;
	}
	public void setBeanObject(Object beanObject) {
		this.beanObject = beanObject;
	}
	public void setValueListener(ValueListener v) {
		valueListener = v;
	}
	public void start() throws Exception {
		beanToUI();
		BeanUI.switchState(uiObject, true);
		if (valueListener!=null) {
			BeanUI.addValueListener(beanObject, uiObject, valueListener);
		}
	}
	
	/**
	 * Send the bean to the UI
	 * @return uiObject with the bean values merged.
	 * @throws Exception
	 */
	public Object beanToUI() throws Exception {
		BeanUI.beanToUI(beanObject, uiObject);
		return uiObject;
	}
	
	/**
	 * Send the UI values to the bean
	 * @return beab with the ui values inserted
	 * @throws Exception
	 */
	public Object uiToBean() throws Exception {
		BeanUI.uiToBean(uiObject, beanObject);
		return beanObject;
	}

}
