package org.dawnsci.datavis.live;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.common.live.AbstractLiveFileService;
import org.dawnsci.datavis.api.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveLoadedFileListener;
import org.dawnsci.datavis.model.ILiveLoadedFileService;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class LiveFileServiceImpl extends AbstractLiveFileService implements ILiveLoadedFileService {

	private EventAdmin eventAdmin;
	private IRemoteDatasetService remoteService;
	private ILoaderService localService;

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	public void setRemoteDataService(IRemoteDatasetService remote) {
		this.remoteService = remote;
	}
	
	public void setLocalDataService(ILoaderService local) {
		this.localService = local;
	}
	
	private void fireListeners(LoadedFile f) {
		for (ILiveFileListener l : listeners) {
			if (l instanceof ILiveLoadedFileListener) {
				((ILiveLoadedFileListener)l).fileLoaded(f);
			}
			
		}
	}

	@Override
	protected void handleFileLoad(String[] paths, String parent, boolean live) {
		
		if (live) {
			String host = getDataServerHost();
			int port    = getDataServerPort();

			for (String p : paths) {
				LiveLoadedFile f = new LiveLoadedFile(p, host, port, remoteService, localService);

				fireListeners(f);
			}
			
		} else {
			
			Map<String,String[]> props = new HashMap<>();
			props.put(PlottingEventConstants.MULTIPLE_FILE_PROPERTY, paths);
			
			eventAdmin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
			
		}
		
		
	}

}
