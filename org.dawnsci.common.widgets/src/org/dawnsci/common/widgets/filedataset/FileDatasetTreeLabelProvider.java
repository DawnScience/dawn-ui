package org.dawnsci.common.widgets.filedataset;

import java.io.File;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class FileDatasetTreeLabelProvider extends LabelProvider {
	private final Image folderImage = Activator.getImage("icons/vogella_folder.gif");
	private final Image driveImage = Activator.getImage("icons/vogella_filenav_nav.gif");
	private final Image fileImage = Activator.getImage("icons/vogella_file_obj.gif");

	@Override
	public String getText(Object element) {
		String fileName = ((File) element).getName();
		if (fileName.length() > 0) {
			return fileName;
		}
		return ((File) element).getPath();
	}
	
	@Override
	public Image getImage(Object element) {
		File file = (File) element;
		if (file.isDirectory())
			return file.getParent() != null ? folderImage : driveImage;
		return fileImage;
	}

	@Override
	public void dispose() {
		super.dispose();
		folderImage.dispose();
		driveImage.dispose();
		fileImage.dispose();
	}
}
