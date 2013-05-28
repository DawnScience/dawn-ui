package org.dawnsci.algorithm.ui.views.runner;

import org.eclipse.ui.IViewPart;


public abstract class AbstractAlgorithmProcessPage implements IAlgorithmProcessPage {

	protected IViewPart algorithmViewPart;

	@Override
	public void setAlgorithmView(IViewPart view) {
		algorithmViewPart = view;
	}

}
