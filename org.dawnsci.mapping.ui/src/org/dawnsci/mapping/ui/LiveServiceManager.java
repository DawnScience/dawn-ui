package org.dawnsci.mapping.ui;

public class LiveServiceManager {

	private static ILiveMappingFileService liveMappingFileService;

	public static ILiveMappingFileService getLiveMappingFileService() {
		return liveMappingFileService;
	}

	public static void setLiveMappingFileService(ILiveMappingFileService liveMappingFileService) {
		LiveServiceManager.liveMappingFileService = liveMappingFileService;
	}
	
}
