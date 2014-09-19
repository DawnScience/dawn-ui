/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionDescriptor;
import org.dawnsci.common.widgets.gda.function.jexl.ExpressionFunctionProposalProvider;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunctionDescriptor;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

@SuppressWarnings("rawtypes")
public class FunctionContentProposalAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IContentProposalProvider.class)
			if (adaptableObject instanceof JexlExpressionFunctionDescriptor) {
				JexlExpressionFunctionDescriptor jexlDesc = (JexlExpressionFunctionDescriptor) adaptableObject;
				if (jexlDesc.getEngine() != null) {
					return new ExpressionFunctionProposalProvider(jexlDesc.getEngine().getFunctions());
				}
			} else if (adaptableObject instanceof FunctionDescriptor) {
				return new SimpleFunctionContentProposalProvider((FunctionDescriptor)adaptableObject);
			}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] {IContentProposalProvider.class};
	}
}
