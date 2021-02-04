package org.dawnsci.datavis.model;

import java.util.List;

/**
 * Interface for UI elements that allow datasets to be selected and plotted
 * in the DataVis perspective
 *
 */
public interface IDatasetStateChanger {
	
	/**
	 * Called when the selection changes in the LoadedFilePart
	 * 
	 * @param file
	 */
	void updateOnSelectionChange(LoadedFile file);
	
	/**
	 * Called when the state changes in the file controller
	 * 
	 * @param event
	 */
	void stateChanged(FileControllerStateEvent event);
	
	/**
	 * Called when the IDynamicDataset dataset size changes in a live SWMR file
	 */
    void refreshRequest();
    
    /**
     * Called when this UI is selected but is empty
     * 
     * @param files
     */
    void initialize(List<LoadedFile> files);
    
    /**
     * Get name of UI for display
     * 
     * @return
     */
    String getChangerName();

}
