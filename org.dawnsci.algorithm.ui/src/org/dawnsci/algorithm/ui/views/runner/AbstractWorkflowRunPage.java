package org.dawnsci.algorithm.ui.views.runner;


public abstract class AbstractWorkflowRunPage implements IWorkflowRunPage {

	protected WorkflowRunView workflowRunView;

	@Override
	public void setWorkflowView(WorkflowRunView view) {
		workflowRunView = view;
	}

}
