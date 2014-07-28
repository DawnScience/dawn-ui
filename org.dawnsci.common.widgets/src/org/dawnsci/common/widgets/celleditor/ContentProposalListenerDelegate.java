package org.dawnsci.common.widgets.celleditor;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener;

public abstract class ContentProposalListenerDelegate implements IContentProposalListener {

    protected ContentProposalAdapter adapter;

	public void setAdapter(ContentProposalAdapter adapter) {
    	this.adapter = adapter;
    }
}
