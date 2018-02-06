package org.dawnsci.datavis.model;

public class LiveServiceManager {

	private static ILiveFileService lfservice;
	
	public void setILiveFileService(ILiveFileService s){
		lfservice = s;
	}
	
	public static ILiveFileService getILiveFileService() {
		return lfservice;
	}
	
}
