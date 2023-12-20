package org.dawnsci.datavis.view.table;

import org.dawnsci.datavis.model.DataOptions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class DataOptionViewerFilter extends ViewerFilter {
	
	private String filterString = null;
	
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		if (filterString == null || filterString.isEmpty()) {
			return true;
		}
		
		if (element instanceof DataOptions) {
			
			DataOptions dop = (DataOptions) element;
			
			if (dop.isSelected()) {
				return true;
			}
			
			String name = dop.getName();
			
			return name.contains(filterString);
			
		}
		return true;
	}

}
