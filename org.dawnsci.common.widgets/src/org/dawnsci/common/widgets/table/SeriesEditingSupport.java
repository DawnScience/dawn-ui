/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.eclipse.jface.viewers.StructuredSelection;
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
                
                if (ret.contains(ISeriesItemDescriptor.INSERT)) {
                	final int index = ret.indexOf(ISeriesItemDescriptor.INSERT);
                	ret.remove(index);
                	ret.add(index, desc);
                } else {
                    ret.add(desc);
                }
                
                getViewer().setInput(ret);
                getViewer().setSelection(new StructuredSelection(desc));
			}
			
		});
	}
	
	public void setSeriesItemDescriptorProvider(ISeriesItemFilter content) {
		cellEditor.setContentProposalProvider(new SeriesProposalProvider(content));
	}

	@Override
	protected boolean canEdit(Object element) {
		return ISeriesItemDescriptor.NEW.equals(element) || ISeriesItemDescriptor.INSERT.equals(element);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		
		final List<ISeriesItemDescriptor> data = ((SeriesContentProvider)getViewer().getContentProvider()).getSeriesItems();
		
		// We get the last filterable non-add item or null if there is not one.
		ISeriesItemDescriptor previous = null;
		int i = -1;
		if (element.equals(ISeriesItemDescriptor.NEW)) {
			if (data.size() != 0) {
				i = data.size()-1;
			}
		} else {
			i = data.indexOf(element)-1;
		}
		
		while (i >= 0) {
			ISeriesItemDescriptor s = data.get(i--);
			if (s.isFilterable()) {
				previous  = s;
				break;
			}
		}
		
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