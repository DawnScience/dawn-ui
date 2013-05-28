package org.dawnsci.algorithm.ui.views.runner;


public abstract class AbstractAlgorithmProcessPage implements IAlgorithmProcessPage {

	protected AlgorithmView workflowRunView;

	@Override
	public void setAlgorithmView(AlgorithmView view) {
		workflowRunView = view;
	}

}
