
package org.dawnsci.fileviewer.parts;

import javax.inject.Inject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;

public class FileViewerE4Part {
	private static final String FILEVIEWER_SAVED_DIRECTORY = "org.dawnsci.fileviewer.saved.directory";
	private FileViewer fileViewer;
	private ScopedPreferenceStore store;

	@Inject
	public FileViewerE4Part() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.fileviewer");
		fileViewer = new FileViewer();

	}

	@PostConstruct
	public void init() {
		String path = store.getString(FILEVIEWER_SAVED_DIRECTORY);
		if (path != null && path!= "")
			fileViewer.setCurrentDirectory(path);
	}

	@PersistState
	public void saveState() {
		final String path = fileViewer.getSavedDirectory();
		store.setValue(FILEVIEWER_SAVED_DIRECTORY, path);
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		fileViewer.getIconCache().initResources(Display.getDefault());
		fileViewer.createCompositeContents(parent);
		fileViewer.notifyRefreshFiles(null);
	}

	@Focus
	public void onFocus() {

	}

	@PreDestroy
	private void partDestroyed() {
		if (fileViewer != null)
			fileViewer.close();
	}

}