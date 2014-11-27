/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.model.psheet;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
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
public class OperationPropertySource implements IPropertySource {
	
	private IOperationModel model;

	public OperationPropertySource(final IOperationModel model) {
		this.model = model;
	}

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		try {
			final Collection<ModelField> fields = model.getModelFields();
			final Collection<IPropertyDescriptor> ret = new ArrayList<IPropertyDescriptor>(fields.size());
			for (ModelField field : fields) ret.add(new OperationPropertyDescriptor(field));
			return ret.toArray(new IPropertyDescriptor[ret.size()]);
		} catch (Exception ne) {
			ne.printStackTrace();
			return null;
		}
		
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
