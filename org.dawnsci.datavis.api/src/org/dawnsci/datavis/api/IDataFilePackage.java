package org.dawnsci.datavis.api;

import org.eclipse.dawnsci.analysis.api.tree.Tree;

public interface IDataFilePackage {

	public boolean isSelected();
	
	public IDataPackage[] getDataPackages();
	
	public Tree getTree();
	
	public String getFilePath();
}
