package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.progress.IProgressService;

public class FileControllerUtils {
	
	public static List<String> loadFiles(IFileController controller, String[] paths, IProgressService progressService) {
		return controller.loadFiles(paths, progressService, true);
	}
	
	public static boolean loadFile(IFileController controller, String path) {
		return loadFiles(controller, new String[]{path}, null).isEmpty();
	}

	public static List<LoadedFile> getSelectedFiles(IFileController controller){
		
		List<LoadedFile> checked = new ArrayList<>();
		
		for (LoadedFile f : controller.getLoadedFiles()) {
			if (f.isSelected()) checked.add(f);
		}
		return checked;
	}
}
