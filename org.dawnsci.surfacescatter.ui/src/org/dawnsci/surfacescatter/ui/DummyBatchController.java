package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;

public class DummyBatchController {

	
	public static void DummyBatchControl(){
		
		String[][] datFiles = new String[2][];
		
		datFiles[0] = new String[] {"/dls/i07/data/2017/si16564-1/292317.dat",
									"/dls/i07/data/2017/si16564-1/292318.dat",
									"/dls/i07/data/2017/si16564-1/292319.dat",
									"/dls/i07/data/2017/si16564-1/292320.dat",
									"/dls/i07/data/2017/si16564-1/292321.dat"};
		
		datFiles[1] = new String[] {"/dls/i07/data/2017/si16564-1/292317.dat",
				"/dls/i07/data/2017/si16564-1/292318.dat",
				"/dls/i07/data/2017/si16564-1/292319.dat",
				"/dls/i07/data/2017/si16564-1/292320.dat",
				"/dls/i07/data/2017/si16564-1/292321.dat"};
		
		String[] imageFolderPaths = new String[] {null, null};
//		String[] xNames = new String[] {"qdcd", "qdcd"}; 
		MethodSetting[] correctionSelections = new MethodSetting[] {MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile, MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile};
		String[] paramFiles = new String[]{"/scratch/Nexus_Tests/gm_test_193.nxs", "/scratch/Nexus_Tests/gm_test_193.nxs"};
		String[] nexusSaveFilePaths = new String[]{"/scratch/Nexus_Tests/Dawn_Of_Batch_00.nxs", "/scratch/Nexus_Tests/Dawn_Of_Batch_01.nxs"};
	
	
		BatchRunner.batchRun(datFiles, 
				 imageFolderPaths,
//				 correctionSelections,
				 paramFiles,
				 nexusSaveFilePaths);
	
	
	}
		
	
}
