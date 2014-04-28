package org.dawnsci.breadcrumb.navigation.table;


public interface ISortParticipant {

	/**
	 * Enable/disable UI during sort
	 * @param enabled
	 */
	void setEnabled(boolean enabled);
	
	/**
	 * Save a search query
	 * @param searchString
	 */
	void saveSearch(String searchString);
	
	/**
	 * The label provider used to get the strings used for the search.
	 * @return
	 */
	AbstractLazyLabelProvider getLabelProvider();
	
}
