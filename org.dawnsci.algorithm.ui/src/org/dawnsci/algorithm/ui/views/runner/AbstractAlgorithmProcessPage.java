package org.dawnsci.algorithm.ui.views.runner;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;


public abstract class AbstractAlgorithmProcessPage implements IAlgorithmProcessPage {

	protected IViewPart algorithmViewPart;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException{
	}

	@Override
	public void saveState(IMemento memento) {
		
	}

	@Override
	public void setAlgorithmView(IViewPart view) {
		algorithmViewPart = view;
	}

	protected IViewPart getView() {
		return algorithmViewPart;
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}
}
