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
