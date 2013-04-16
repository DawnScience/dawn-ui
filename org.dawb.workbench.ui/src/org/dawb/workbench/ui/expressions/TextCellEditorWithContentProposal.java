package org.dawb.workbench.ui.expressions;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TextCellEditorWithContentProposal extends TextCellEditor {
	
	private ContentProposalAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open

	public TextCellEditorWithContentProposal(Composite parent, IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent);

		enableContentProposal(contentProposalProvider, keyStroke, autoActivationCharacters);
	}

	private void enableContentProposal(IContentProposalProvider contentProposalProvider, KeyStroke keyStroke,
			char[] autoActivationCharacters) {
		contentProposalAdapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				contentProposalProvider, keyStroke, autoActivationCharacters);

		// Listen for popup open/close events to be able to handle focus events correctly
		contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

			public void proposalPopupClosed(ContentProposalAdapter adapter) {
				popupOpen = false;
			}

			public void proposalPopupOpened(ContentProposalAdapter adapter) {
				popupOpen = true;
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
