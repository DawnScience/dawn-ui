package org.dawnsci.datavis.view.perspective;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.view.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class RecentPlacesImpl implements IRecentPlaces {

	private LinkedList<String> lastFile = new LinkedList<String>();
	private final static String RECENT_PLACES = "org.dawnsci.datavis.recentplaces";
	
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
			
			StringBuilder builder = new StringBuilder();
			for (String file : lastFile) {
				builder.append(file);
				builder.append(File.pathSeparator);
			}
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			store.setValue(RECENT_PLACES, builder.toString());
		}
		
	}

	@Override
	public List<String> getRecentPlaces() {
		
		if (lastFile.isEmpty()) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String string = store.getString("org.dawnsci.datavis.recentplaces");
			String[] split = string.split(File.pathSeparator);
			for (String s : split) {
				if (!s.isEmpty()) {
					lastFile.add(s);
				}
			}
		}
		
		return new ArrayList<String>(lastFile);
	}

	@Override
	public String getCurrentDefaultPlace() {
		if (!lastFile.isEmpty()) {
			return lastFile.get(0);
		}
		
		return System.getProperty("user.home");
		
	}

}
