package org.dawb.workbench.plotting.system;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;

public class ActionContainer {

	private String  groupName;
	private IAction action;
	private IContributionManager manager;

	public ActionContainer(String groupName, IAction action, IContributionManager manager) {
		this.groupName = groupName;
		this.action  = action;
		this.manager = manager;
	}

	public String getId() {
		return action.getId();
	}

	public IAction getAction() {
		return action;
	}

	@SuppressWarnings("unused")
	public void setAction(IAction action) {
		this.action = action;
	}

	public IContributionManager getManager() {
		return manager;
	}

	@SuppressWarnings("unused")
	public void setManager(IContributionManager manager) {
		this.manager = manager;
	}

	public String toString() {
		return action.toString();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


}
