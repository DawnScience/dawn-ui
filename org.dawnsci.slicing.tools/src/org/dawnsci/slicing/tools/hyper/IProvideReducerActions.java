package org.dawnsci.slicing.tools.hyper;

import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.jface.action.IAction;

public interface IProvideReducerActions {

	public List<IAction> getActions(IPlottingSystem system);
	
}
