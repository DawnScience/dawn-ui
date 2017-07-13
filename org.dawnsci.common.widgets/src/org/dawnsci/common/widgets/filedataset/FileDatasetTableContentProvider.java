package org.dawnsci.common.widgets.filedataset;

import java.io.File;

import org.dawnsci.common.widgets.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileDatasetTableContentProvider implements IStructuredContentProvider {

	IFileDatasetFilter filter;
	
	public FileDatasetTableContentProvider(IFileDatasetFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public void dispose() {
		// unused
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// unused
	}

	@Override
	public Object[] getElements(Object inputElement) {
		File file = (File) inputElement;
		if (file.isDirectory())
			return new Object[0];
		
		// try opening the file
		try {
			IDataHolder dh = LocalServiceManager.getLoaderService().getData(file.getAbsolutePath(), null);
			// return only those elements that are accepted by the filter
			return dh.getList().stream().filter(ds -> filter.accept(ds)).toArray();
		} catch (Exception e) {
			return new Object[0];
		}
	}
}
