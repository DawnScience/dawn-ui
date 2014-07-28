package org.dawnsci.common.widgets.celleditor;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TextCellEditorWithContentProposal extends TextCellEditor {

	private OpenableContentAssistCommandAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open
	private KeyStroke keyStroke;
	private char[] autoActivationCharacters;
	private ILabelProvider labelProvider;
	private IContentProposalProvider contentProposalProvider;
	private IContentProposalListener contentProposalListener;

	public TextCellEditorWithContentProposal(Composite parent, KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent);
		this.keyStroke = keyStroke;
		this.autoActivationCharacters = autoActivationCharacters;
	}

	@Override
	protected void doSetFocus() {
		super.doSetFocus();
		// Prompt with completions only if the cell is empty
		if (text != null && text.getText().isEmpty()) {
			this.openPopup();
		}
	}
	
	public void setLabelProvider(ILabelProvider prov) {
		this.labelProvider = prov;
	}

	private void enableContentProposal(IContentProposalProvider contentProposalProvider,
			                           KeyStroke keyStroke, char[] autoActivationCharacters) {

		contentProposalAdapter = new OpenableContentAssistCommandAdapter(text,
				new TextContentAdapter(), contentProposalProvider, null, null,
				true);
		contentProposalAdapter.setAutoActivationDelay(0);

		contentProposalAdapter.setLabelProvider(labelProvider);
		contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
		// Listen for popup open/close events to be able to handle focus
		// events correctly
		contentProposalAdapter
				.addContentProposalListener(new IContentProposalListener2() {

					@Override
					public void proposalPopupClosed(
							ContentProposalAdapter adapter) {
						popupOpen = false;
					}

					@Override
					public void proposalPopupOpened(
							ContentProposalAdapter adapter) {
						popupOpen = true;
					}
				});
		
		if (contentProposalListener!=null) {
			registerListener(contentProposalListener);
		}

	}
	private void registerListener( IContentProposalListener listener) {
		if (listener instanceof ContentProposalListenerDelegate) {
			((ContentProposalListenerDelegate)contentProposalListener).setAdapter(contentProposalAdapter);
		}
		contentProposalAdapter.addContentProposalListener(listener);	
	}

	public void addContentProposalListener(IContentProposalListener l) {
		if (contentProposalAdapter!=null) {
			registerListener(l);
		} else {
			contentProposalListener = l;
		}
	}

	/**
	 * Return the {@link ContentProposalAdapter} of this cell editor.
	 *
	 * @return the {@link ContentProposalAdapter}
	 */
	public ContentProposalAdapter getContentProposalAdapter() {
		return contentProposalAdapter;
	}

	public void openPopup() {
		contentProposalAdapter.openProposalPopup();
	}

	@Override
	protected void focusLost() {
		if (!popupOpen) {
			// Focus lost deactivates the cell editor.
			// This must not happen if focus lost was caused by activating
			// the completion proposal popup.
			super.focusLost();
		}
	}

	@Override
	protected boolean dependsOnExternalFocusListener() {
		// Always return false;
		// Otherwise, the ColumnViewerEditor will install an additional
		// focus listener
		// that cancels cell editing on focus lost, even if focus gets lost
		// due to
		// activation of the completion proposal popup. See also bug 58777.
		return false;
	}

	public void setContentProposalProvider( IContentProposalProvider contentProposalProvider) {
		
		this.contentProposalProvider = contentProposalProvider;
		enableContentProposal(contentProposalProvider, keyStroke, autoActivationCharacters);
	}

	public IContentProposalProvider getContentProposalProvider() {
		return contentProposalProvider;
	}
}