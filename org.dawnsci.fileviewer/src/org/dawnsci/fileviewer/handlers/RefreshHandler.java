
package org.dawnsci.fileviewer.handlers;

import javax.inject.Inject;

import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.e4.core.di.annotations.Execute;

public class RefreshHandler {

	private FileViewer fileviewer;

	@Inject
	public RefreshHandler(FileViewer viewer) {
		fileviewer = viewer;
	}

	@Execute
	public void execute() {
		fileviewer.doRefresh();
	}
}