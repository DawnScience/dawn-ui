/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Image;

/**
 * Content proposals for simple functions (not jexl)
 *
 */
public class FunctionContentProposal implements IContentProposal, IAdaptable {
	private static final Image CURVE = Activator.getImage("chart_curve.png");
	private static final Image BULLET_BLUE = Activator
			.getImage("bullet_blue.png");

	private IFunctionDescriptor functionDescriptor;

	public FunctionContentProposal(IFunctionDescriptor functionDescriptor) {
		super();
		this.functionDescriptor = functionDescriptor;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return functionDescriptor.getAdapter(adapter);
	}

	@Override
	public String getContent() {
		return functionDescriptor.getName();
	}

	@Override
	public int getCursorPosition() {
		return functionDescriptor.getName().length();
	}

	@Override
	public String getLabel() {
		return functionDescriptor.getName();
	}

	@Override
	public String getDescription() {
		return functionDescriptor.getLongDescription();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	/**
	 * Returns an image for the content proposal
	 *
	 * @return images for IOperators and IFunctions, else null
	 */
	public Image getImage() {
		if (functionDescriptor.isOperator()) {
			return BULLET_BLUE;
		} else {
			return CURVE;
		}
	}
}