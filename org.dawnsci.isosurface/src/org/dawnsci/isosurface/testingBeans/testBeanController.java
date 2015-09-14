package org.dawnsci.isosurface.testingBeans;

import java.util.List;

import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IFieldWidget;

public interface testBeanController {
	/**
	 * Add a value listener to any UI objects which change in the UIObject.
	 * @param valueListener
	 */
	void addValueListener(ValueListener valueListener) throws Exception;

	/**
	 * Remove a value listener from the controller.
	 * @param listener
	 */
	void removeValueListener(ValueListener listener) throws Exception;

    /**
     * Takes the bean values and inserts them into the UI.
     */
	void beanToUI() throws Exception;

	/**
	 * Takes the ui values and inserts them into the bean
	 * @throws Exception
	 */
	void uiToBean() throws Exception;

    /**
     * Optionally specify the precise name of the field which should be synched.
     * @param fieldName
     */
	void uiToBean(String fieldName)  throws Exception;

	/**
	 * Get the current bean directly (no copy is made, use with caution)
	 * @return
	 */
	Object getBean();

	/**
	 * Used to switch the UI on or off. If off, it will not send events.
	 * 
	 * @param on
	 * @throws Exception
	 */
	void switchState(boolean on) throws Exception;

	/**
	 * Sets the user interface either enabled or disabled with
	 * corresponding grey out of widgets
	 * @param selection
	 */
	void setUIEnabled(boolean selection) throws Exception;

	/**
	 * Call to fire value listeners on the UI.
	 */
	void fireValueListeners() throws Exception;

	/**
	 * Get a bean field by name providing it has been recorded first.
	 * @param fieldName
	 * @param beanClasses
	 * @return
	 */
	IFieldWidget getBeanField(String fieldName, Class<? extends Object>... beanClasses);

	/**
	 * Used to record bean fields for fast lookups, expert use only.
	 * @param bean
	 * @param uiObject
	 * @throws Exception
	 */
	void recordBeanFields() throws Exception;

	/**
	 * If setBeanFields(...) has been called this method should be called to cleanup.
	 * @throws Exception
	 */
	void dispose() throws Exception;

	/**
	 * Get a list of the editing fields from the controller.
	 * @return
	 */
	List<String> getEditingFields()  throws Exception;

	/**
	 * Get the widget for a specific field.
	 * @param fieldName
	 * @param editorUI
	 * @return
	 */
	IFieldWidget getFieldWidget(String fieldName)  throws Exception;

	/**
	 * 
	 * @param class1
	 * @param name
	 * @param widget
	 */
	void addBeanField(String name, IFieldWidget widget);

	/**
	 * Add a listener to the controller for a particular field changing.
	 * @param fieldName
	 * @param listener
	 */
	void addBeanFieldValueListener(String fieldName, ValueListener listener);
}
