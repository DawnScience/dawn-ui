package org.dawnsci.mapping.ui.api;

import java.util.List;

import org.dawnsci.mapping.ui.IRegistrationHelper;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.ui.progress.IProgressService;


/**
 * Interface to the file controller in the Mapping perspective.
 * 
 * Use to gain information about the current state of the file
 * loaded, and perform actions on those files
 * 
 */
public interface IMapFileController {

	void removeAllFromDisplay();

	void removeListener(IMapFileEventListener l);

	void addListener(IMapFileEventListener l);

	void addAssociatedImage(AssociatedImage image);

	MappedDataArea getArea();

	List<PlottableMapObject> getPlottedObjects();

	boolean contains(String path);

	void clearAll();

	boolean containsLiveFiles();

	void clearNonLiveFiles();

	void removeFile(String path);

	void localReloadFile(String path);

	void loadLiveFile(final String path, LiveDataBean bean, String parentFile);

	List<String> loadFile(String path, MappedDataFileBean bean, IProgressService progressService);

	List<String> loadFiles(String[] paths, IProgressService progressService);
	
	void attachLive(String[] paths);

	void toggleDisplay(PlottableMapObject object);

	void setRegistrationHelper(IRegistrationHelper helper);

	void removeFile(MappedDataFile file);
	
	void addLiveStream(LiveStreamMapObject stream);
}

