package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.jexl.JexlProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class ContentProposalLabelProvider extends BaseLabelProvider implements ILabelProvider {

	private static final Image LINK = Activator.getImage("link.png");

	@Override
	public Image getImage(Object element) {
		if (element instanceof JexlProposal){
			return LINK;
		} else if (element instanceof FunctionContentProposal){
			return ((FunctionContentProposal)element).getImage();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IContentProposal){
			IContentProposal iContentProposal = (IContentProposal) element;
			return iContentProposal.getLabel();
		}
		return element.toString();
	}


}
