package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/* 
 * Inspired by http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html#tutorial-using-a-filter
 */

public class MappedDataFilter extends ViewerFilter {

	private String searchString;

    public void setSearchText(String s) {
        // ensure that the value can be used for matching
        this.searchString = ".*" + s + ".*";
    }
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
	
		if (searchString == null || searchString.length() == 0) {
            return true;
        }
		if (element instanceof MappedDataFile) {
			return true;
		} else if (element instanceof PlottableMapObject) {
			PlottableMapObject object = (PlottableMapObject) element;
			return object.toString().matches(searchString);
		} 	
		return true;
	}

}
