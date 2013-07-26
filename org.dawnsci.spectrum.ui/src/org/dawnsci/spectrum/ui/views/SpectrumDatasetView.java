package org.dawnsci.spectrum.ui.views;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class SpectrumDatasetView extends ViewPart {

	private CheckboxTableViewer viewer;
	
	@Override
	public void createPartControl(Composite parent) {
		
		
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		getSite().getPage().addSelectionListener("org.dawnsci.spectrum.ui.views.SpectrumView", new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				viewer.setInput(((IStructuredSelection)selection).getFirstElement());
				viewer.refresh();
			}
		});
		
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();

	}
	
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			
			if (parent instanceof SpectrumFile) {

				return ((SpectrumFile) parent).getDataNames().toArray();
			}
			
			return null;
		}
	}

}
