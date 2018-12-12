package org.dawnsci.datavis.api;

import java.util.List;

public interface IRecentPlaces {

	public void addFiles(String... path);
	
	public List<String> getRecentDirectories();
	
	public List<String> getRecentFiles();
	
	public String getCurrentDefaultDirectory();
	
}
