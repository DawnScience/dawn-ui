package org.dawnsci.common.widgets.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class SeriesProposalProvider implements IContentProposalProvider {

	private ProposalComparator proposalComparator = new ProposalComparator();
	
	private ISeriesItemDescriptorProvider delegate;
	private ISeriesItemDescriptor         itemDescriptor;
	
	private static class ProposalComparator implements Comparator<IContentProposal> {

		@Override
		public int compare(IContentProposal o1, IContentProposal o2) {
			return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
		}

	}

	public SeriesProposalProvider(ISeriesItemDescriptorProvider delegate) {
		this.delegate = delegate;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		
		List<IContentProposal> proposals = new ArrayList<IContentProposal>();
		
		for (ISeriesItemDescriptor descriptor : delegate.getDescriptors(itemDescriptor)) {
			proposals.add(new SeriesItemContentProposal(descriptor));
		}
		Collections.sort(proposals, proposalComparator);
		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

	public void setSeriesItemDescriptor(ISeriesItemDescriptor itemDescriptor) {
		this.itemDescriptor = itemDescriptor;
	}

}
