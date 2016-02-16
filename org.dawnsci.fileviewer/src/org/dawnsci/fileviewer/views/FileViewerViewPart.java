package org.dawnsci.fileviewer.views;

import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

public class FileViewerViewPart extends ViewPart {

	private FileViewer fileViewer;

	@Override
	public void createPartControl(Composite parent) {
		fileViewer = new FileViewer();
		fileViewer.getIconCache().initResources(Display.getDefault());
		fileViewer.createCompositeContents(parent);
		fileViewer.notifyRefreshFiles(null);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if (fileViewer != null)
			fileViewer.close();
	}

}
