/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @see https://www.eclipse.org/articles/Article-Properties-View/properties-view.html
 * 
 * @see https://www.eclipse.org/articles/Article-Tabbed-Properties/tabbed_properties_view.html
 * 
 * We make a properties source to allow editing of the model. We read annotations from the
 * model to provide the correct editor for a field or provide a custom editor.
 * 
 * @author Matthew Gerring
 *
 */
class OperationPropertySource implements IPropertySource {
	
	private IOperationModel model;

	OperationPropertySource(final IOperationModel model) {
		this.model = model;
	}

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		// Decided not to use the obvious BeanMap here because class problems with
		// GDA and we have to read annotations anyway.
		final List<Field> allFields = new ArrayList<Field>(31);
		allFields.addAll(Arrays.asList(model.getClass().getDeclaredFields()));
		allFields.addAll(Arrays.asList(model.getClass().getSuperclass().getDeclaredFields()));
		
		// The returned descriptor
		final List<IPropertyDescriptor> ret = new ArrayList<IPropertyDescriptor>();
		
		// fields
		for (Field field : allFields) {
			
			// If there is a getter/isser for the field we assume it is a model field.
			try {
				if (model.isModelField(field.getName())) {			
					ret.add(new OperationPropertyDescriptor(model, field.getName()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		Collections.sort(ret, new Comparator<IPropertyDescriptor>() {
			@Override
			public int compare(IPropertyDescriptor o1, IPropertyDescriptor o2) {
				return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
			}
		});
		
		return ret.toArray(new IPropertyDescriptor[ret.size()]);
		
	}

	@Override
	public Object getPropertyValue(Object id) {
		try {
			return model.get(id.toString());
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		try {
			return model.get(id.toString())!=null;
		} catch (Exception e) {
			return false; // Why no boolean state for maybe? Discuss...
		}
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		try {
			model.set((String)id, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
