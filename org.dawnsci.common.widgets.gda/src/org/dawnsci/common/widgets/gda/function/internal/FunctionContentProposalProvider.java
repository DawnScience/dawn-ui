package org.dawnsci.common.widgets.gda.function.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class FunctionContentProposalProvider implements
		IContentProposalProvider {

	private IFunctionDescriptor[] descriptors;
	private ProposalComparator proposalComparator = new ProposalComparator();
	private static class ProposalComparator implements Comparator<IContentProposal> {

		@Override
		public int compare(IContentProposal o1, IContentProposal o2) {
			return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
		}

	}

	public FunctionContentProposalProvider(IFunctionDescriptor[] descriptors) {
		this.descriptors = descriptors;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		List<IContentProposal> proposals = new ArrayList<IContentProposal>();
		for (IFunctionDescriptor descriptor : descriptors) {
			final IContentProposalProvider provider = (IContentProposalProvider) descriptor
					.getAdapter(IContentProposalProvider.class);
			if (provider != null) {
				IContentProposal[] proposals2 = provider.getProposals(contents,
						position);
				proposals.addAll(Arrays.asList(proposals2));
			}
		}
		Collections.sort(proposals, proposalComparator);
		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

}
