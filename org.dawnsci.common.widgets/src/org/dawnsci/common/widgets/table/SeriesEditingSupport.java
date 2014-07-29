package org.dawnsci.common.widgets.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.common.widgets.celleditor.ContentProposalListenerDelegate;
import org.dawnsci.common.widgets.celleditor.TextCellEditorWithContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;

public class SeriesEditingSupport extends EditingSupport {
	
	private TextCellEditorWithContentProposal cellEditor;

	public SeriesEditingSupport(ColumnViewer viewer, ILabelProvider renderer) {
		
		super(viewer);
		cellEditor = new TextCellEditorWithContentProposal((Composite)viewer.getControl(), null, null);
		cellEditor.setLabelProvider(renderer);
		cellEditor.addContentProposalListener(new ContentProposalListenerDelegate() {

			@Override
			public void proposalAccepted(IContentProposal proposal) {
                
				final Collection<ISeriesItemDescriptor> input = (Collection<ISeriesItemDescriptor>) getViewer().getInput();
				final List<ISeriesItemDescriptor> ret   = input!=null
						                                ? new ArrayList<ISeriesItemDescriptor>(input)
						                                : new ArrayList<ISeriesItemDescriptor>(3);
				
                if (ret.size()>0 && ret.get(ret.size()-1).equals(ISeriesItemDescriptor.NEW)) {
                	ret.remove(ret.size()-1);
                }
                
                SeriesItemContentProposal  sprop = (SeriesItemContentProposal)proposal;
                final ISeriesItemDescriptor desc = sprop.getDescriptor();
                ret.add(desc);
                
                getViewer().setInput(ret);
			}
			
		});
	}
	
	public void setSeriesItemDescriptorProvider(ISeriesItemFilter content) {
		cellEditor.setContentProposalProvider(new SeriesProposalProvider(content));
	}

	@Override
	protected boolean canEdit(Object element) {
		return ISeriesItemDescriptor.NEW.equals(element);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		
		final Object[] data = ((IStructuredContentProvider)getViewer().getContentProvider()).getElements(element);
		
		// We get the last non-add item or null if there is not one.
		ISeriesItemDescriptor previous = data.length>1
				                       ? (ISeriesItemDescriptor)data[data.length-2]
				                       : null;
		
		SeriesProposalProvider sprov = (SeriesProposalProvider)cellEditor.getContentProposalProvider();
		sprov.setSeriesItemDescriptor(previous);
		return cellEditor;
	}

	@Override
	protected Object getValue(Object element) {
		final ISeriesItemDescriptor des = (ISeriesItemDescriptor)element;
		// TODO 
		return "";
	}

	@Override
	protected void setValue(Object element, Object value) {
		return; // TODO
	}
}