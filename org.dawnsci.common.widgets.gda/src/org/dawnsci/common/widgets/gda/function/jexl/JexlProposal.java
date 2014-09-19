/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.jexl;

import org.eclipse.jface.fieldassist.ContentProposal;

public class JexlProposal extends ContentProposal {

	private ExpressionFunctionProposalProvider provider;

	public JexlProposal(String content, ExpressionFunctionProposalProvider provider) {
		super(content);
		this.provider = provider;
	}

	public ExpressionFunctionProposalProvider getProvider() {
		return provider;
	}
}
