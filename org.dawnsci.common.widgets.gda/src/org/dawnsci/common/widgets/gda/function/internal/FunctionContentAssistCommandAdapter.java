package org.dawnsci.common.widgets.gda.function.internal;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

/**
 * An subclass of ContentAssistCommandAdapter that allows
 * us to provide access to the openProposalPopup method
 *
 */
public class FunctionContentAssistCommandAdapter extends
		ContentAssistCommandAdapter {


	public FunctionContentAssistCommandAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, String commandId,
			char[] autoActivationCharacters, boolean installDecoration) {
		super(control, controlContentAdapter, proposalProvider, commandId,
				autoActivationCharacters, installDecoration);
	}

	@Override
	public void openProposalPopup() {
		super.openProposalPopup();
	}
}
