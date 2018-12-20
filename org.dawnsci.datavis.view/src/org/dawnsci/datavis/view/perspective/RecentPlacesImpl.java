package org.dawnsci.datavis.view.perspective;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.view.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class RecentPlacesImpl implements IRecentPlaces {

	private LinkedList<String> lastDirectories = new LinkedList<>();
	private LinkedList<String> lastFiles = new LinkedList<>();
	private static final String RECENT_PLACES = "org.dawnsci.datavis.recentplaces";
	private static final String RECENT_FILES = "org.dawnsci.datavis.recentfiles";

	private static final int MAX_SIZE = 10;

	@Override
	public void addFiles(String... path) {
		File f = new File(path[0]);
		File parentFile = f.getParentFile();
		if (parentFile != null) {
			String parentPath = parentFile.getAbsolutePath();
			addOrBringToTop(lastDirectories,new String[] {parentPath},RECENT_PLACES);
		}

		addOrBringToTop(lastFiles,path,RECENT_FILES);

	}

	private void addOrBringToTop(LinkedList<String> list, String[] strings, String preference) {

		if (strings.length > MAX_SIZE) {
			//clear list and add last MAX_SIZE elements
			list.clear();

			for (int i = strings.length - MAX_SIZE; i < strings.length; i++) {
				list.addFirst(strings[i]);
			}
		} else {
			//add to list and remove when gets above MAX_SIZE
			for (int i = 0; i < strings.length; i++) {

				String s = strings[i];

				if (!list.contains(s)){
					list.addFirst(s);
				} else {
					list.remove(s);
					list.addFirst(s);
				}

				if (list.size() > MAX_SIZE) list.removeLast();
			}
		}

		StringBuilder builder = new StringBuilder();
		for (String file : list) {
			builder.append(file);
			builder.append(File.pathSeparator);
		}
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(preference, builder.toString());
	}

	@Override
	public List<String> getRecentDirectories() {

		if (lastDirectories.isEmpty()) {
			fillListFromPreference(lastDirectories, RECENT_PLACES);
		}

		return new ArrayList<String>(lastDirectories);
	}

	private void fillListFromPreference(List<String> list, String preference) {

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String string = store.getString(preference);
		String[] split = string.split(File.pathSeparator);
		for (String s : split) {
			if (!s.isEmpty()) {
				list.add(s);
			}
		}
	}

	@Override
	public String getCurrentDefaultDirectory() {
		if (!lastDirectories.isEmpty()) {
			return lastDirectories.get(0);
		}

		return System.getProperty("user.home");
	}

	@Override
	public List<String> getRecentFiles() {
		if (lastFiles.isEmpty()) {
			fillListFromPreference(lastFiles, RECENT_FILES);
		}

		return new ArrayList<String>(lastFiles);
	}
}
