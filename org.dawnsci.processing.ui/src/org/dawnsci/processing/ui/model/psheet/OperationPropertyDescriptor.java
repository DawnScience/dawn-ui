/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.model.psheet;

import org.dawb.common.util.text.StringUtils;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.model.ModelFieldEditors;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Current supported values in models which can be edited by this descriptor:
 * 
 *  o Boolean/boolean, Double/double, Float/float, Integer/int, Long/long
 *  o Enums (as choice list)
 *  o double[] float[] int[] long[]
 *  o String
 * 
 * 
 * @author Matthew Gerring
 *
 */
class OperationPropertyDescriptor extends PropertyDescriptor implements Comparable<OperationPropertyDescriptor> {


	private ILabelProvider labelProvider;
	private ModelField     field;

	public OperationPropertyDescriptor(ModelField     field) {
		super(field.getName(), field.getDisplayName());
		this.field = field;
	}

	public ILabelProvider getLabelProvider() {
        if (labelProvider != null) {
			return labelProvider;
		}
        labelProvider = new LabelProvider();
        return labelProvider;
    }

	public boolean isFileProperty() {
		return field.isFileProperty();
	}
	

	public void setValue(Object value) throws Exception {
		field.set(value);
	}


    public CellEditor createPropertyEditor(Composite parent) {
    	return ModelFieldEditors.createEditor(field, parent);
    }

	private class LabelProvider extends BaseLabelProvider implements ILabelProvider {

		private Image ticked, unticked;
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
			if (element instanceof Boolean) {
				if (ticked==null)   ticked   = Activator.getImageDescriptor("icons/ticked.png").createImage();
				if (unticked==null) unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
				Boolean val = (Boolean)element;
				return val ? ticked : unticked;
			}
			return null;
		}

		/**
		 * The <code>LabelProvider</code> implementation of this
		 * <code>ILabelProvider</code> method returns the element's
		 * <code>toString</code> string. Subclasses may override.
		 */
		public String getText(Object element) {
			
			if (element == null)            return "";
			if (element instanceof Boolean) return "";
			
			StringBuilder buf = new StringBuilder();
			if (element.getClass().isArray()) {
				buf.append( StringUtils.toString(element) );
			} else {
			    buf.append(element.toString());//$NON-NLS-1$
			}
			
			OperationModelField anot = field.getAnnotation();
			if (anot!=null) buf.append(" "+anot.unit());
			return buf.toString();
		}
		
		public void dispose() {
			if (ticked!=null)   ticked.dispose();
			if (unticked!=null) unticked.dispose();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
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
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public int compareTo(OperationPropertyDescriptor o) {
		return field.getName().compareTo(o.field.getName());
	}
	
	@Override
	public String toString() {
		return field.getName();
	}
}
