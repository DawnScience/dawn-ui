package org.dawnsci.datavis.model;

public class LiveServiceManager {

	private static ILiveLoadedFileService lfservice;
	
	public void setILiveFileService(ILiveLoadedFileService s){
		lfservice = s;
	}
	
	public static ILiveLoadedFileService getILiveFileService() {
		return lfservice;
	}
	
}
