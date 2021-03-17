package org.dawnsci.datavis.api;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.january.dataset.Dataset;

public interface IDataFilePackage {

	public boolean isSelected();
	
	public IDataPackage[] getDataPackages();
	
	public Tree getTree();
	
	public String getFilePath();

	public String getLabelName();

	public Collection<String> getLabelOptions();

	public Dataset getLabelValue(String labelName);
}
