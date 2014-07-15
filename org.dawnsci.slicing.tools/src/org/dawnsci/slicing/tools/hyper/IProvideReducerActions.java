package org.dawnsci.slicing.tools.hyper;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.jface.action.IAction;

public interface IProvideReducerActions {

	public List<IAction> getActions(IPlottingSystem system);
	
}
