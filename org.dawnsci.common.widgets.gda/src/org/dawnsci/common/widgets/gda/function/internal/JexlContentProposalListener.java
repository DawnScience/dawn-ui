package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.jexl.JexlProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Control;

public class JexlContentProposalListener implements IContentProposalListener {

	private FunctionContentAssistCommandAdapter contentProposalAdapter;
	private Control control;

	public JexlContentProposalListener(
			FunctionContentAssistCommandAdapter contentProposalAdapter,
			Control control) {
		this.contentProposalAdapter = contentProposalAdapter;
		this.control = control;
	}

	@Override
	public void proposalAccepted(IContentProposal proposal) {

		if (proposal instanceof JexlProposal) {
			JexlProposal jexlProposal = (JexlProposal) proposal;
			int[] lastMatchBounds = jexlProposal.getProvider()
					.getLastMatchBounds();
			int lastPosition = jexlProposal.getProvider().getLastPosition();
			IControlContentAdapter contentAdapter = contentProposalAdapter
					.getControlContentAdapter();
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
			IControlContentAdapter contentAdapter = contentProposalAdapter
					.getControlContentAdapter();
			String content = proposal.getContent();
			contentAdapter.setControlContents(control, content,
					content.length());
		}
	}
}