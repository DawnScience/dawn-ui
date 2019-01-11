package org.dawnsci.mapping.ui.live;

import org.dawnsci.common.live.AbstractLiveFileService;
import org.dawnsci.common.live.ILiveFileListener;
import org.dawnsci.mapping.ui.ILiveMapFileListener;
import org.dawnsci.mapping.ui.ILiveMappingFileService;

public class LiveMappingFileServiceImpl extends AbstractLiveFileService implements ILiveMappingFileService {

	
	@Override
	public void setInitialFiles(String[] files) {
		if (files == null) return;
		initialFiles = files.clone();
	}

	private void fireListeners(String[] fs, String parent, boolean live) {
		for (ILiveFileListener l : listeners)  {
			if (l instanceof ILiveMapFileListener) {
				((ILiveMapFileListener)l).fileLoadRequest(fs,live ? getDataServerHost() : null, getDataServerPort(), parent);
			}
			
		}
	}

	@Override
	protected void handleFileLoad(String[] files, String parent, boolean live) {
		
		fireListeners(files,parent, live);
		
	}

}
