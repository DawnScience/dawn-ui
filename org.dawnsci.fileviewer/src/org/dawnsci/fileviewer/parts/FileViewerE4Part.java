
package org.dawnsci.fileviewer.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.e4.ui.di.Focus;

public class FileViewerE4Part {
	private FileViewer fileViewer;

	@Inject
	public FileViewerE4Part() {
		fileViewer = new FileViewer();

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