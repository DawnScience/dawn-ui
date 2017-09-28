package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;

public class BatchRunner {

	public static void batchRun(String[][] datFiles, 
						 String[] imageFolderPaths,
						 String[] xNames, 
						 MethodSetting[] correctionSelections,
						 String[] paramFiles,
						 String[] nexusSaveFilePaths){
		
		
		for(int i = 0; i<datFiles.length; i++){
			
			SetupModel stmi = new SetupModel();
			stmi.setImageFolderPath(imageFolderPaths[i]);
			
			SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
			sspi.setStm(stmi);
			sspi.createGm();
			sspi.surfaceScatterPresenterBuildWithFrames(datFiles[i], 
					xNames[i],
					correctionSelections[i]);
			
			sspi.loadParameters(paramFiles[i]);
			
			BatchTracking bat = new BatchTracking();
			bat.setSsp(sspi);
			
			sspi.writeNexus(nexusSaveFilePaths[i]);
		}
		
		
	}
}
