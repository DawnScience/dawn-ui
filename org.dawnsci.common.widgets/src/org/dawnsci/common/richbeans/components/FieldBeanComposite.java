/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.dawnsci.common.richbeans.ACTIVE_MODE;
import org.dawnsci.common.richbeans.beans.BeanProvider;
import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.beans.IExpressionManager;
import org.dawnsci.common.richbeans.beans.IExpressionWidget;
import org.dawnsci.common.richbeans.beans.IFieldCollection;
import org.dawnsci.common.richbeans.beans.IFieldProvider;
import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.beans.BeanUI.BeanProcessor;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionListener;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to edit complex types in RichBeanEditors. Also subclasses
 * for editing lists of complex types.
 * 
 * You can either subclass this class directly for use with a complex
 * type or call the setEditorUI(...) with a composite containing the 
 * class to use as the editing UI.
 * 
 * You must call setEditorClass(...) as the template bean is required
 * since the editorBean is allowed to be null.
 * 
 * If subclassing you need to call addValueListeners() after setting
 * the setEditorClass or your values will not take.
 * 
 * @author Matthew Gerring
 *
 */
public class FieldBeanComposite extends FieldComposite implements IFieldCollection, IExpressionWidget, IFieldProvider, BeanProvider {

	private static Logger logger = LoggerFactory.getLogger(FieldBeanComposite.class);
	
	protected Object beanTemplate;
	protected Object editorUI;
	protected Object editorBean;
    protected String listenerName;
    
	/**
	 * @param parent
	 * @param style
	 */
	public FieldBeanComposite(Composite parent, int style) {
		this(parent, style, FieldBeanComposite.class.getName());
		editorUI = this;
	}
	
	/**
	 * Optionally name the listener which will be attached to the editorUI.
	 * This allows nested BeanListEditors etc to work. Otherwise the parent
	 * editor attaches in preference and the UI does not update correctly.
	 * 
	 * @param parent
	 * @param style
	 * @param listenerName
	 */
	public FieldBeanComposite(Composite parent, int style, final String listenerName) {
		super(parent,style);
		this.listenerName = listenerName;
		this.editorUI     = this;
	}

	/**
	 * Call this method to set the editing class from which a new instance will be
	 * generated and this used to synchronise bean editors.
	 * Failure to call this class before the setValue(...) methods are called,
	 * will result in exceptions for some classes extending this class.
	 * 
	 * If using this class directly the setValue(...) call sets the editorClass
	 * automatically. There is no need to call this method then.
	 * 
	 * @param clazz 
	 * 
	 * 
	 */
	public void setEditorClass(final Class<?> clazz) {
		try {
			beanTemplate = clazz.newInstance();
			if (editorUI==this) addValueListeners(this);
		} catch (Exception e) {
			// In rare cases the editor class cannot be
			// instantiated due to classpath/plugin problems.
			// This is so rare that we do not ask the developer
			// to deal with the exception, throwing instead
			// a runtime.
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set an object with getter and setter methods able to 
	 * hold the values for each item in this editor.
	 * 
	 * The fields as usual must be {@link IFieldWidget}
	 * 
	 * @param ui
	 */
	public void setEditorUI(final Object ui) {
		this.editorUI = ui;
		addValueListeners(editorUI);
	}
	
	/**
	 * Current ui editing.
	 * @return e
	 */
	public Object getEditorUI() {
		return editorUI;
	}
	
	protected void addValueListeners(final Object editorUI) {
		
		if (beanTemplate==null) throw new RuntimeException("You must set the editing class for with setEditorClass(...) before setting the editorUI object.");
		try {
			BeanUI.addValueListener(beanTemplate, editorUI, new ValueAdapter(getListenerName()) {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					try {
						valueChanged(e);
					} catch (Exception e1) {
						logger.error("Cannot process value changed.", e1);
					}
				}	
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Override this method to do unique things on value changed.
	 * @throws Exception 
	 */
	protected void valueChanged(ValueEvent e) throws Exception {
		try {
		    if (e.getFieldName()!=null) {
		    	BeanUI.uiToBean(editorUI, editorBean, e.getFieldName());
		    }
		} catch (NoSuchMethodException ne) {
			// We allow the bean to listen to fields it does not have the value of for now.
			// No action is required.
		}
	}
	
	/**
	 * Note that addValueListener(...) adds listeners to the editing UI
	 * using BeanUI so events will notify if data is changed but do not
	 * encapsulate which item (and do not normally need to).
	 */
	@Override
	public void addValueListener(final ValueListener listener) {
		if (editorUI==null) throw new RuntimeException("You must set the editing UI for each item with setEditorUI(...)");
		if (beanTemplate==null) throw new RuntimeException("You must set the editing class for with setEditorClass(...)");
		try {
			BeanUI.addValueListener(beanTemplate, editorUI, listener);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void removeValueListener(ValueListener l) {
		if (editorUI==null) throw new RuntimeException("You must set the editing UI for each item with setEditorUI(...)");
		if (beanTemplate==null) throw new RuntimeException("You must set the editing class for with setEditorClass(...)");
		try {
			BeanUI.removeValueListener(beanTemplate, editorUI, l);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void dispose() {
		try {
			if (listeners!=null) listeners.clear();
			if (editorUI!=null && beanTemplate!=null) {
				if (editorUI instanceof Widget && ((Widget)editorUI).isDisposed()) return;
				BeanUI.dispose(beanTemplate, editorUI);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			super.dispose();
		}
	}

	@Override
	public Object getValue() {
		return editorBean;
	}
	@Override
	public void setValue(Object value) {
		this.editorBean   = value;
		try {
			off(); // Need to off all beans or get unwanted value changed.
			if (editorUI instanceof Widget && ((Widget)editorUI).isDisposed()) return;
			BeanUI.beanToUI(editorBean, editorUI);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			on();
		}
	}
	
    /**
     * Simply calls setExpressionManager(...) on ay fields this
     * composite has.
     */
	@Override
	public void setExpressionManager(IExpressionManager man) {
		throw new RuntimeException("IExpressionManager not currently supported because it relies on JEP!");
	}
	

	@Override
	public void setExpressionValue(final double value) {
		try {
			if (editorUI instanceof Widget && ((Widget)editorUI).isDisposed()) return;
			BeanUI.notify(editorBean, editorUI, new BeanProcessor() {
				@Override
				public void process(String name, Object val, IFieldWidget box) throws Exception {
					if (box instanceof IExpressionWidget) {
						final IExpressionWidget expressionBox = (IExpressionWidget)box;
						expressionBox.setExpressionValue(value);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Cannot link expression widgets for "+editorBean, e);
		}
	}


	protected List<String> expressionFields;
	/**
	 * Override this method (usually by calling it too) to add values which should be available in expressions.
	 * 
	 * NOTE when overriding that after the first call the expressionFields are cached to avoid too many
	 * interogations of the bean.
	 * 
	 * @return List<String> of possible expression vars.
	 * @throws Exception
	 */
	protected List<String> getExpressionFields() throws Exception {
  	    
		if (expressionFields==null) {
			expressionFields = BeanUI.getEditingFields(editorBean, editorUI);
		}
        return expressionFields;
	}

	/**
	 * Do not normally need to call this method but can be used to synchronise
	 * editing bean with data without changing UI values. Typically used when 
	 * editing bean goes from null to not null.
	 * @param bean
	 */
	public void setEditingBean(final Object bean) {
		boolean newBean   = editorBean==null;
		this.editorBean   = bean;
		if (newBean) setValue(bean);
	}
	
	// Important to start with true!
	@SuppressWarnings("hiding")
	protected boolean active = true;
	/**
	 * @return the active
	 */
	@Override
	public boolean isActivated() {
		return active;
	}

	private ACTIVE_MODE activeMode = ACTIVE_MODE.SET_VISIBLE_AND_ACTIVE;
	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		this.active = active;
		if (activeMode==ACTIVE_MODE.SET_VISIBLE_AND_ACTIVE) {
		    setVisible(active);
		} else if (activeMode==ACTIVE_MODE.SET_ENABLED_AND_ACTIVE){
			setVisible(active);
		} // Nothing is nothing
	}
		
	/**
	 * @return the activeMode
	 */
	public ACTIVE_MODE getActiveMode() {
		return activeMode;
	}

	/**
	 * @param activeMode the activeMode to set
	 */
	public void setActiveMode(ACTIVE_MODE activeMode) {
		this.activeMode = activeMode;
	}

	private boolean isOn = false;
	@Override
	public boolean isOn() {
		return isOn;
	}

	@Override
	public void off() {
		if (editorUI==null)     throw new RuntimeException("You must set the editing UI with setEditorUI(...)");
		if (beanTemplate==null) throw new RuntimeException("You must set the editing class for with setEditorClass(...)");
		isOn = false;
		updateState(isOn);
	}


	@Override
	public void on() {
		if (editorUI==null)     throw new RuntimeException("You must set the editing UI for each item with setEditorUI(...)");
		if (beanTemplate==null) throw new RuntimeException("You must set the editing class for with setEditorClass(...)");
		isOn = true;
		updateState(isOn);
	}

	protected void updateState(boolean isOn) {
		try {
			BeanUI.switchState(beanTemplate, editorUI, isOn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	protected Collection<BeanSelectionListener> listeners;
	private boolean isEnabled = true;
	
	/**
	 * @return if enabled
	 * 
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * @param l
	 */
	public void addBeanSelectionListener(final BeanSelectionListener l) {
		if (listeners==null) listeners = new HashSet<BeanSelectionListener>(7);
		listeners.add(l);
	}
	
	/**
	 * Set enabled state of all contained widgets
	 */
	@Override
	public void setEnabled(final boolean isEnabled) {
		this.isEnabled  = isEnabled;
		try {
			BeanUI.setEnabled(beanTemplate, editorUI, isEnabled);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Returns the listenerName.
	 */
	public String getListenerName() {
		return listenerName;
	}

	/**
	 * @param listenerName The listenerName to set.
	 */
	public void setListenerName(String listenerName) {
		this.listenerName = listenerName;
	}

	@Override
	public Object getBean() {
		return editorBean;
	}

	@Override
	public Object getInstance() throws Exception {
		return beanTemplate.getClass().newInstance();
	}

	@Override
	public IFieldWidget getField(String fieldName) throws Exception {
		return BeanUI.getFieldWiget(fieldName, editorUI);
	}

	@Override
	public Object getFieldValue(String fieldName) throws Exception {
		return getField(fieldName).getValue();
	}

	@Override
	public Control getControl() {
		return null;
	}
	
	@Override
	public boolean isExpressionParseRequired(String value) {
		return true;
	}

	@Override
	public boolean isExpressionAllowed() {
		return true;
	}
}

	