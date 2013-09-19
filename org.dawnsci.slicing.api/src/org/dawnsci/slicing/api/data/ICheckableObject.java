package org.dawnsci.slicing.api.data;

import java.util.List;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IVariableManager;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public interface ICheckableObject {
	
	public IDataset getData(IMonitor monitor);

	public ILazyDataset getLazyData(IMonitor monitor);
	
	public int[] getShape(boolean squeeze);

	public String getName();

	public void setName(String name);

	public IExpressionObject getExpression();

	public void setExpression(IExpressionObject expression);

	public boolean isExpression();

	public boolean isChecked();

	public void setChecked(boolean checked);

	public String getPath();

	/**
	 * Get the axis, X, Y1..Y4
	 * 
	 * If this object is not in 
	 * 
	 * @param selections
	 * @return
	 */
	public String getAxis(List<ICheckableObject> selections, boolean is2D, boolean isXFirst);

	public int getAxisIndex(List<ICheckableObject> selections, boolean isXFirst);

	public int getYaxis();

	public void setYaxis(int yaxis);

	public String getVariable();

	public void setVariable(String variable);

	public void createExpression(IVariableManager psData, String mementoKey, String memento);

	public String getMemento();

	/**
	 * @return Returns the mementoKey.
	 */
	public String getMementoKey();

	/**
	 * @param mementoKey The mementoKey to set.
	 */
	public void setMementoKey(String mementoKey);

	public String getDisplayName(String rootName);

}