/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.celleditor.ContentProposalListenerDelegate;
import org.dawnsci.common.widgets.gda.function.jexl.JexlProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Control;

public class JexlContentProposalListener extends ContentProposalListenerDelegate {

	private Control control;

	public JexlContentProposalListener(Control control) {
		this.control = control;
	}

	@Override
	public void proposalAccepted(IContentProposal proposal) {

		if (proposal instanceof JexlProposal) {
			JexlProposal jexlProposal = (JexlProposal) proposal;
			int[] lastMatchBounds = jexlProposal.getProvider()
					.getLastMatchBounds();
			int lastPosition = jexlProposal.getProvider().getLastPosition();
			IControlContentAdapter contentAdapter = adapter.getControlContentAdapter();
			String text = contentAdapter.getControlContents(control);

			if (text.isEmpty()) {
				contentAdapter.setControlContents(control,
						proposal.getContent(), proposal.getContent().length());
				return;
			}

			if (lastPosition > lastMatchBounds[1]) {
				StringBuilder builder = new StringBuilder();
				builder.append(text.substring(0, lastPosition));
				builder.append(proposal.getContent());
				contentAdapter.setControlContents(control, builder.toString(),
						lastPosition + proposal.getContent().length());
				return;
			}

			String match = text.substring(lastMatchBounds[0],
					lastMatchBounds[1]);

			if (match.contains(":")) {
				int index = match.lastIndexOf(":");
				StringBuilder builder = new StringBuilder();
				builder.append(text
						.substring(0, lastMatchBounds[0] + index + 1));
				builder.append(proposal.getContent());
				builder.append(text.substring(lastMatchBounds[1]));
				contentAdapter.setControlContents(control, builder.toString(),
						lastMatchBounds[0] + index
								+ proposal.getContent().length() + 1);
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append(text.substring(0, lastMatchBounds[0]));
				builder.append(proposal.getContent());
				builder.append(text.substring(lastMatchBounds[1]));
				contentAdapter.setControlContents(control, builder.toString(),
						lastMatchBounds[0] + proposal.getContent().length());
			}
		} else {
			IControlContentAdapter contentAdapter = adapter.getControlContentAdapter();
			String content = proposal.getContent();
			contentAdapter.setControlContents(control, content,
					content.length());
		}
	}
}