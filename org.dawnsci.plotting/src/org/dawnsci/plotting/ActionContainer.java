package org.dawnsci.plotting;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;

class ActionContainer {

	private String  groupId;
	private IAction action;
	private IContributionManager manager;

	public ActionContainer(String groupName, IAction action, IContributionManager manager) {
		this.groupId = groupName;
		this.action  = action;
		this.manager = manager;
	}

	public void setAction(IAction action) {
		this.action = action;
	}

	public void setManager(IContributionManager manager) {
		this.manager = manager;
	}

	public String toString() {
		return action.toString();
	}

	public void setGroupId(String groupName) {
		this.groupId = groupName;
	}

	public void insert(boolean force) {
		if (!force && isActive()) return;
		manager.appendToGroup(groupId, action);
	}
	
	public void remove() {
		manager.remove(action.getId());
	}

	public boolean isActive() {
		IContributionItem act = manager.find(action.getId());
		if (act==null) return false;
		return true;
	}

	public boolean isId(String id) {
		return this.action.getId()!=null && this.action.getId().equals(id);
	}

	public IAction getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((manager == null) ? 0 : manager.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionContainer other = (ActionContainer) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (manager == null) {
			if (other.manager != null)
				return false;
		} else if (!manager.equals(other.manager))
			return false;
		return true;
	}


}
