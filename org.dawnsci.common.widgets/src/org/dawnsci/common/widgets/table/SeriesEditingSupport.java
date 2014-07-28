package org.dawnsci.common.widgets.table;

import org.dawnsci.common.widgets.celleditor.TextCellEditorWithContentProposal;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

public class SeriesEditingSupport extends EditingSupport {
	
	private TextCellEditorWithContentProposal cellEditor;

	public SeriesEditingSupport(ColumnViewer viewer, ILabelProvider renderer) {
		
		super(viewer);
		cellEditor = new TextCellEditorWithContentProposal((Composite)viewer.getControl(), null, null);
		cellEditor.setLabelProvider(renderer);
	}
	
	public void setSeriesItemDescriptorProvider(ISeriesItemDescriptorProvider content) {
		cellEditor.setContentProposalProvider(new SeriesProposalProvider(content));
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		SeriesProposalProvider sprov = (SeriesProposalProvider)cellEditor.getContentProposalProvider();
		sprov.setSeriesItemDescriptor((ISeriesItemDescriptor)element);
		return cellEditor;
	}

	@Override
	protected Object getValue(Object element) {
		return "Fred"; // TODO
	}

	@Override
	protected void setValue(Object element, Object value) {
		return; // TODO
	}
}