package org.dawnsci.datavis.api;

import java.util.List;

public interface IRecentPlaces {

	public void addPlace(String path);
	
	public List<String> getRecentPlaces();
	
	public String getCurrentDefaultPlace();
	
}
