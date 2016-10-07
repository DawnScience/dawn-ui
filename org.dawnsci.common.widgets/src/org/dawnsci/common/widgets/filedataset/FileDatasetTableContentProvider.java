package org.dawnsci.common.widgets.filedataset;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class FileDatasetTableContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		File file = (File) inputElement;
		if (file.isDirectory())
			return new Object[0];
		
		// try opening the file
		try {
			IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
			return dh.getList().toArray();
		} catch (Exception e) {
			return new Object[0];
		}
	}

}
