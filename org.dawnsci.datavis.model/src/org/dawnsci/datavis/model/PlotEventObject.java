package org.dawnsci.datavis.model;

public class PlotEventObject {
	
	public enum PlotEventType {
		READY,LOADING,PAINTING,ERROR;
	}
	
	private PlotEventType eventType;
	private String message;
	
	public PlotEventObject(PlotEventType eventType, String message) {
		this.eventType = eventType;
		this.message = message;
	}

	public PlotEventType getEventType() {
		return eventType;
	}

	public String getMessage() {
		return message;
	}
	
}
