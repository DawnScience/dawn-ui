package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.jexl.JexlProposal;
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

public class TextCellEditorWithContentProposal extends TextCellEditor {

	private final class IContentProposalListenerImplementation implements
			IContentProposalListener {
		@Override
		public void proposalAccepted(IContentProposal proposal) {
			Control control = TextCellEditorWithContentProposal.this
					.getControl();

			if (proposal instanceof JexlProposal) {
				JexlProposal jexlProposal = (JexlProposal) proposal;
				int[] lastMatchBounds = jexlProposal.getProvider()
						.getLastMatchBounds();
				int lastPosition = jexlProposal.getProvider().getLastPosition();
				IControlContentAdapter contentAdapter = contentProposalAdapter
						.getControlContentAdapter();
				String text = contentAdapter.getControlContents(control);

				if (text.isEmpty()) {
					contentAdapter.setControlContents(control, proposal
							.getContent(), proposal.getContent().length());
					return;
				}

				if (lastPosition > lastMatchBounds[1]) {
					StringBuilder builder = new StringBuilder();
					builder.append(text.substring(0, lastPosition));
					builder.append(proposal.getContent());
					contentAdapter.setControlContents(control,
							builder.toString(), lastPosition
									+ proposal.getContent().length());
					return;
				}

				String match = text.substring(lastMatchBounds[0],
						lastMatchBounds[1]);

				if (match.contains(":")) {
					int index = match.lastIndexOf(":");
					StringBuilder builder = new StringBuilder();
					builder.append(text.substring(0, lastMatchBounds[0] + index
							+ 1));
					builder.append(proposal.getContent());
					builder.append(text.substring(lastMatchBounds[1]));
					contentAdapter.setControlContents(control,
							builder.toString(), lastMatchBounds[0] + index
									+ proposal.getContent().length() + 1);
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append(text.substring(0, lastMatchBounds[0]));
					builder.append(proposal.getContent());
					builder.append(text.substring(lastMatchBounds[1]));
					contentAdapter.setControlContents(control,
							builder.toString(), lastMatchBounds[0]
									+ proposal.getContent().length());
				}
			} else {
				IControlContentAdapter contentAdapter = contentProposalAdapter
						.getControlContentAdapter();
				String content = proposal.getContent();
				contentAdapter.setControlContents(control, content,
						content.length());
			}
		}
	}

	private FunctionContentAssistCommandAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open
	private KeyStroke keyStroke;
	private char[] autoActivationCharacters;

	public TextCellEditorWithContentProposal(Composite parent,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent);
		this.keyStroke = keyStroke;
		this.autoActivationCharacters = autoActivationCharacters;
	}

	@Override
	protected void doSetFocus() {
		super.doSetFocus();
		//Prompt with completions only if the cell is empty
		if (text!= null && text.getText().isEmpty()){
			this.openPopup();
		}
	}

	private void enableContentProposal(
			IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {

		// contentProposalAdapter = new ContentProposalAdapter(text,
		// new TextContentAdapter(), contentProposalProvider,
		// keyStroke, autoActivationCharacters);

		contentProposalAdapter = new FunctionContentAssistCommandAdapter(text,
				new TextContentAdapter(), contentProposalProvider, null,
				new char[] { ':' }, true);
		contentProposalAdapter.setAutoActivationDelay(0);

		// Listen for popup open/close events to be able to handle focus
		// events correctly
		contentProposalAdapter.setLabelProvider(new ContentProposalLabelProvider());
		contentProposalAdapter
				.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
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

		contentProposalAdapter
				.addContentProposalListener(new IContentProposalListenerImplementation());
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

	public void setContentProposalProvider(
			IContentProposalProvider contentProposalProvider) {
		enableContentProposal(contentProposalProvider, keyStroke,
				autoActivationCharacters);
	}
}