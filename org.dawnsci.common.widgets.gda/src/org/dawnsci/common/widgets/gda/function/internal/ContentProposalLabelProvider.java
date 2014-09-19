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
import org.dawnsci.common.widgets.gda.function.jexl.JexlProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class ContentProposalLabelProvider extends BaseLabelProvider implements ILabelProvider {

	private static final Image LINK = Activator.getImage("link.png");

	@Override
	public Image getImage(Object element) {
		if (element instanceof JexlProposal){
			return LINK;
		} else if (element instanceof FunctionContentProposal){
			return ((FunctionContentProposal)element).getImage();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IContentProposal){
			IContentProposal iContentProposal = (IContentProposal) element;
			return iContentProposal.getLabel();
		}
		return element.toString();
	}


}
