package org.dawnsci.datavis.model;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ui.progress.IProgressService;

public class FileControllerUtils {
	
	public static List<String> loadFiles(IFileController controller, String[] paths, IProgressService progressService) {
		return controller.loadFiles(paths, progressService, true);
	}
	
	public static boolean loadFile(IFileController controller, String path) {
		return loadFiles(controller, new String[]{path}, null).isEmpty();
	}

	public static List<LoadedFile> getSelectedFiles(IFileController controller){
		return getSelectedFiles(controller.getLoadedFiles());
	}

	public static List<LoadedFile> getSelectedFiles(List<LoadedFile> files) {
		return files.stream().filter(LoadedFile::isSelected).collect(Collectors.toList());
	}
}
