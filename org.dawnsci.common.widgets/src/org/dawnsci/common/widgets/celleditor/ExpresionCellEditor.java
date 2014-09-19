/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ExpresionCellEditor extends TextCellEditor {
	
	private ContentProposalAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open

	public ExpresionCellEditor(Composite parent, IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent);

		enableContentProposal(contentProposalProvider, keyStroke, autoActivationCharacters);
	}

	private void enableContentProposal(IContentProposalProvider contentProposalProvider, KeyStroke keyStroke,
			char[] autoActivationCharacters) {
		contentProposalAdapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				contentProposalProvider, keyStroke, autoActivationCharacters);
		
		contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);

		// Listen for popup open/close events to be able to handle focus events correctly
		contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

			public void proposalPopupClosed(ContentProposalAdapter adapter) {
				popupOpen = false;
			}

			public void proposalPopupOpened(ContentProposalAdapter adapter) {
				popupOpen = true;
			}
		});
		
		contentProposalAdapter.addContentProposalListener(new IContentProposalListener() {
			
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				IContentProposalProvider provider = contentProposalAdapter.getContentProposalProvider();

				Control control = ExpresionCellEditor.this.getControl();

				if (provider != null && provider instanceof ExpressionFunctionProposalProvider) {

					int[] lastMatchBounds = ((ExpressionFunctionProposalProvider)provider).getLastMatchBounds();
					int lastPosition = ((ExpressionFunctionProposalProvider)provider).getLastPosition();
					IControlContentAdapter contentAdapter = contentProposalAdapter.getControlContentAdapter();
					String text = contentAdapter.getControlContents(control);
					
					if (text.isEmpty()) {
						contentAdapter.setControlContents(control, proposal.getContent(), proposal.getContent().length());
						return;
					}
					
					if (lastPosition > lastMatchBounds[1]) {
						StringBuilder builder = new StringBuilder();
						builder.append(text.substring(0,lastPosition));
						builder.append(proposal.getContent());
						contentAdapter.setControlContents(control, builder.toString(), lastPosition+proposal.getContent().length());
						return;
					}
					
					String match = text.substring(lastMatchBounds[0], lastMatchBounds[1]);

					if (match.contains(":")) {
						int index = match.lastIndexOf(":");
						StringBuilder builder = new StringBuilder();
						builder.append(text.substring(0,lastMatchBounds[0]+index+1));
						builder.append(proposal.getContent());
						builder.append(text.substring(lastMatchBounds[1]));
						contentAdapter.setControlContents(control, builder.toString(), lastMatchBounds[0]+index+proposal.getContent().length()+1);
					} else {
						StringBuilder builder = new StringBuilder();
						builder.append(text.substring(0,lastMatchBounds[0]));
						builder.append(proposal.getContent());
						builder.append(text.substring(lastMatchBounds[1]));
						contentAdapter.setControlContents(control, builder.toString(), lastMatchBounds[0]+proposal.getContent().length());
					}
				}
			}
		});
	}

	public ContentProposalAdapter getContentProposalAdapter() {
		return contentProposalAdapter;
	}

	protected void focusLost() {
		if (!popupOpen) {
			super.focusLost();
		}
	}

	protected boolean dependsOnExternalFocusListener() {
		return false;
	}

}
