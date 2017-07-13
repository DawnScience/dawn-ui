package org.dawnsci.common.widgets.filedataset;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileDatasetTreeContentProvider implements ITreeContentProvider {

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
		return (Object[]) inputElement;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		File file = (File) parentElement;
		// exclude hidden files
		File[] ls = file.listFiles(pathname -> !pathname.isHidden());
		if (ls == null)
			return new File[0];
		// comparator chaining can be achieved with org.apache.commons.collections4.ComparatorUtils,
		// but seems like an unnecessary heavy dependency here...
		// alphabetic sort
		Arrays.sort(ls, new FileNameComparator());
		// directories go before files sort
		Arrays.sort(ls, new FileTypeComparator());
		return ls;
	}

	@Override
	public Object getParent(Object element) {
		File file = (File) element;
		return file.getParentFile();
	}

	@Override
	public boolean hasChildren(Object element) {
		File file = (File) element;
		return file.isDirectory();
	}

	class FileNameComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
	
	class FileTypeComparator implements Comparator<File> {

	    @Override
	    public int compare(File file1, File file2) {

	        if (file1.isDirectory() && file2.isFile())
	            return -1;
	        if (file1.isDirectory() && file2.isDirectory()) {
	            return 0;
	        }
	        if (file1.isFile() && file2.isFile()) {
	            return 0;
	        }
	        return 1;
	    }
	}
}
