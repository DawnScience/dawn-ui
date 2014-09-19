/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.selector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.beans.BeansFactory;
import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ListEditor extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(ListEditor.class);

	protected final List<BeanWrapper> beans;
	protected final Map<BeanWrapper, String> takenNames;
	protected String nameField;
	protected int listHeight;
	protected int listWidth;
	protected int maxItems;
	protected int minItems;
	protected String defaultName;
	private ListEditorUI listEditorUI;

	public abstract StructuredViewer getViewer();

	public void setListVisible(boolean isVisible) {
		GridUtils.setVisible(getViewer().getControl(), isVisible);
		getViewer().getControl().getParent().layout();
	}

	public ListEditor(Composite parent, int style, String listenerName) {
		super(parent, style, listenerName);
		this.beans = new ArrayList<BeanWrapper>(7);
		this.takenNames = new HashMap<BeanWrapper, String>(7);
	}

	protected void updateEditingUIVisibility() {
		if (editorUI instanceof Control) {
			((Control) this.editorUI).setVisible(!beans.isEmpty());
		}
	}

	@Override
	public Object getValue() {
		final List<Object> valueList = new ArrayList<Object>(7);
		for (BeanWrapper wrap : beans)
			valueList.add(wrap.getBean());
		return valueList;
	}

	/**
	 * Has its own value notification for when the bean changes add/remove/move
	 */
	@Override
	public void addValueListener(final ValueListener listener) {
		this.eventDelegate.addValueListener(listener);
		super.addValueListener(listener);
	}

	@Override
	public void removeValueListener(ValueListener l) {
		this.eventDelegate.removeValueListener(l);
		super.removeValueListener(l);
	}

	@Override
	public void setEditorUI(final Object ui) {
		if (ui instanceof Control) {
			final Control control = (Control) ui;
			if (control.getLayoutData() == null) {
				control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			}
			control.setVisible(false);
		}
		super.setEditorUI(ui);
	}

	/**
	 * @param index
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public void setValue(final int index, final String fieldName, final Object value) throws Exception {

		final BeanWrapper wrapper = beans.get(index);
		BeansFactory.setBeanValue(wrapper.getBean(), fieldName, value);

		if (index == getSelectedIndex()) {
			BeanUI.beanToUI(wrapper.getBean(), editorUI);
		}
	}

	public abstract int getSelectedIndex();

	protected abstract void updateButtons();

	public void setSelectedIndex(final int index) {
		setSelectedBean(beans.get(index), true);
	}

	public void setField(String fieldName, Object value) throws Exception {
		final IFieldWidget box = BeanUI.getFieldWiget(fieldName, this.editorUI);
		box.setValue(value);
		box.fireValueListeners();
	}

	@Override
	public Object getBean() {
		return beans.get(getSelectedIndex()).getBean();
	}

	/**
	 * Number of beans being edited.
	 * 
	 * @return size
	 */
	public int getListSize() {
		return beans.size();
	}

	/**
	 * @return the nameMethod
	 */
	public String getNameField() {
		return nameField;
	}

	/**
	 * The name of the method to look at in the bean to provide the name in the list of items.
	 * 
	 * @param nameMethod
	 *            the nameMethod to set
	 */
	public void setNameField(String nameMethod) {
		this.nameField = nameMethod;
	}

	/**
	 * @return the listHeight
	 */
	public int getListHeight() {
		return listHeight;
	}

	/**
	 * @param listHeight
	 *            the listHeight to set
	 */
	public void setListHeight(int listHeight) {
		this.listHeight = listHeight;
		final GridData data = (GridData) getViewer().getControl().getLayoutData();
		data.heightHint = listHeight;
	}

	/**
	 * @return the listHeight
	 */
	public int getListWidth() {
		return listWidth;
	}

	/**
	 * @param listWidth
	 *            the listWidth to set
	 */
	public void setListWidth(int listWidth) {
		this.listWidth = listWidth;
		final GridData data = (GridData) getViewer().getControl().getLayoutData();
		data.widthHint = listWidth;
	}

	protected void notifyValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		this.eventDelegate.notifyValueListeners(evt);
	}

	protected BeanWrapper lastSelectionBean = null;

	protected void setSelectedBean(BeanWrapper wrapper, boolean fireListeners) {
		if (wrapper == null) {
			lastSelectionBean = null;
			return;
		}

		boolean wasOn = isOn();
		try {
			if (wasOn)
				off(); // Need to off all beans or get unwanted value changed.

			BeanUI.beanToUI(wrapper.getBean(), editorUI);
			lastSelectionBean = wrapper;
			getListEditorUI().notifySelected(this);

			updateButtons();

			if (fireListeners && listeners != null) {
				final BeanSelectionEvent evt = new BeanSelectionEvent(this, getSelectedIndex(),
						lastSelectionBean.getBean());
				for (BeanSelectionListener l : listeners)
					l.selectionChanged(evt);
			}

			// Needed only when above listener changes data.
			if (getNameField() != null) {
				updateName(lastSelectionBean);
			}
			if (lastSelectionBean != null && getViewer() != null)
				getViewer().refresh(lastSelectionBean);

		} catch (Throwable e) {
			logger.error("Cannot select value", e);
		} finally {
			if (wasOn)
				on();
			try {
				BeanUI.fireBoundsUpdaters(wrapper.getBean(), editorUI);
			} catch (Exception e) {
				logger.error("Cannot notify value listeners", e);
			}
		}
	}

	@Override
	protected void valueChanged(ValueEvent e) throws Exception {
		try {
			if (lastSelectionBean != null) {
				if (e.getFieldName() != null) {
					BeanUI.uiToBean(editorUI, lastSelectionBean.getBean(), e.getFieldName());
				}
			}
		} catch (NoSuchMethodException ne) {
			// We allow the bean to listen to fields it does not have the value of for now.
			// No action is required.
		}
	}

	protected BeanWrapper getSelectedBeanWrapper() {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		return (BeanWrapper) selection.getFirstElement();
	}

	/**
	 * Returns the name of the currently selected bean, or null
	 * 
	 * @return the name of the currently selected bean, or null
	 */
	public String getSelectedBeanName() {
		BeanWrapper selectedBean = getSelectedBeanWrapper();
		if (selectedBean != null)
			return selectedBean.getName();
		return null;
	}

	/**
	 * @return the maxItems
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * @param maxItems
	 *            the maxItems to set
	 */
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}

	/**
	 * @return the minItems
	 */
	public int getMinItems() {
		return minItems;
	}

	/**
	 * @param minItems
	 *            the minItems to set
	 */
	public void setMinItems(int minItems) {
		this.minItems = minItems;
	}

	protected void clear() {
		this.beans.clear();
		this.takenNames.clear();
	}

	@Override
	public void setValue(final Object value) {

		final List<?> obs = (List<?>) value;

		this.clear();
		for (int i = 0; i < obs.size(); i++) {
			final Object bean = obs.get(i);
			final BeanWrapper wrapper = new BeanWrapper(bean);
			wrapper.setName(getFreeName(wrapper, getTemplateName(), i));
			beans.add(wrapper);
		}

	}

	protected String getFreeName(final BeanWrapper wrapper, final String templateName, int index) {

		if (getNameField() != null) {
			updateName(wrapper);
			if (wrapper.isValidName()) {
				takenNames.put(wrapper, wrapper.getName());
				return wrapper.getName();
			}
		}
		if (takenNames.containsKey(wrapper))
			return takenNames.get(wrapper);

		if (index < 1)
			index = 1;
		final String suggestedName = templateName + " " + index;
		if (!takenNames.values().contains(suggestedName)) {
			takenNames.put(wrapper, suggestedName);
			return suggestedName;
		}
		final String name = getFreeName(wrapper, templateName, index + 1);
		takenNames.put(wrapper, name);
		return name;
	}

	protected void updateName(BeanWrapper wrapper) {
    	final String methodName = BeansFactory.getGetterName(getNameField());
    	try {
    		if (wrapper==null) return;
			final Method method = wrapper.getBean().getClass().getMethod(methodName);
			final Object ob = method.invoke(wrapper.getBean());
			final String name = ob != null ? ob.toString() : getDefaultName();
			if (name != null)
				wrapper.setName(name);

		} catch (Exception e) {
			logger.error("Cannot set the name field", e);
		}
	}

	private String templateName;

	/**
	 * The name used for the items if they do not have a getName() method. This name will have the index appended to the
	 * end in the view.
	 * 
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * The name used for the items if they do not have a getName() method. This name will have the index appended to the
	 * end in the view.
	 * 
	 * @param templateName
	 *            the templateName to set
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	/**
	 * @return Returns the defaultName.
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * @param defaultName
	 *            The defaultName to set.
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	/**
	 * Get the ListEditorUI instance that provides advanced feature control for list editors.
	 * 
	 * @return an instance of the list editor. Guaranteed to return non-null as an instance of ListEditorUI with all the
	 *         default settings is returned if none was set.
	 */
	public ListEditorUI getListEditorUI() {
		if (listEditorUI == null)
			listEditorUI = ListEditorUIAdapter.getDefault();
		return listEditorUI;
	}

	/**
	 * Set the advanced ListEditorUI settings. Setting this to null results in using the default settings.
	 * 
	 * @param listEditorUI
	 *            new settings, or null to use defaults
	 */
	public void setListEditorUI(ListEditorUI listEditorUI) {
		this.listEditorUI = listEditorUI;
	}

}
