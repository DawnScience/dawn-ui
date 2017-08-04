package org.dawnsci.common.widgets.filedataset;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.math3.util.Pair;
import org.dawnsci.common.widgets.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.ILazyDataset;
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
			@SuppressWarnings("unchecked")
			Pair<String, ILazyDataset>[] rv = Arrays.stream(dh.getNames())
				.map(datasetName -> new Pair<String, ILazyDataset>(datasetName, dh.getLazyDataset(datasetName)))
				.filter(pair -> filter.accept(pair.getValue()))
				.toArray(Pair[]::new);
			return rv;
		} catch (Exception e) {
			return new Object[0];
		}
	}
}
