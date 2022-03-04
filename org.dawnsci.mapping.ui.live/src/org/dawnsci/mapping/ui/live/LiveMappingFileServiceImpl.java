package org.dawnsci.mapping.ui.live;

import org.dawnsci.common.live.AbstractLiveFileService;
import org.dawnsci.datavis.api.ILiveFileListener;
import org.dawnsci.mapping.ui.ILiveMapFileListener;
import org.dawnsci.mapping.ui.ILiveMappingFileService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;

public class LiveMappingFileServiceImpl extends AbstractLiveFileService implements ILiveMappingFileService {
	
	private IFilePathService filePathService;
	private IStageScanConfiguration stageScanConfig;
	
	public String[] getAxisNames() {
		
		if (stageScanConfig == null){
			return new String[] {};
		} else {
			String x = stageScanConfig.getPlotXAxisName();
			String y = stageScanConfig.getPlotYAxisName();
			return new String[] {x,y};
		}
	}
	
	public void setFilePathService(IFilePathService filePathService) {
		this.filePathService = filePathService;
	}

	public void setStageScanConfiguration(IStageScanConfiguration scanConfig) {
		this.stageScanConfig = scanConfig;
	}
	
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

	@Override
	public String getSaveName(String name) throws Exception {
		String processingDir = filePathService.getProcessingDir();
		return filePathService.getNextPath(processingDir, name);
	}

}
