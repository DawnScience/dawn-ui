package org.dawnsci.datavis.view.perspective;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.datavis.api.IRecentPlaces;

public class RecentPlacesImpl implements IRecentPlaces {

	private LinkedList<String> lastFile = new LinkedList<String>(); 
	
	@Override
	public void addPlace(String path) {
		File f = new File(path);
		File parentFile = f.getParentFile();
		if (parentFile != null) {
			String parentPath = parentFile.getAbsolutePath();
			if (lastFile.size() > 5) lastFile.removeLast();
			if (!lastFile.contains(parentPath)){
				lastFile.addFirst(parentPath);
			} else {
				lastFile.remove(parentPath);
				lastFile.addFirst(parentPath);
			}
		}
		
	}

	@Override
	public List<String> getRecentPlaces() {
		return new ArrayList<String>(lastFile);
	}

}
