/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class SeriesProposalProvider implements IContentProposalProvider {

	
	private ISeriesItemFilter             delegate;
	private ISeriesItemDescriptor         itemDescriptor;
	
	public SeriesProposalProvider(ISeriesItemFilter delegate) {
		this.delegate = delegate;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		
		List<IContentProposal> proposals = new ArrayList<IContentProposal>();
		
		for (ISeriesItemDescriptor descriptor : delegate.getDescriptors(contents, position, itemDescriptor)) {
			proposals.add(new SeriesItemContentProposal(descriptor));
		}
		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

	public void setSeriesItemDescriptor(ISeriesItemDescriptor itemDescriptor) {
		this.itemDescriptor = itemDescriptor;
	}

}
