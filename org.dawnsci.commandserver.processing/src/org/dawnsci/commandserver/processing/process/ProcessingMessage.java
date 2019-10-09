package org.dawnsci.commandserver.processing.process;

public class ProcessingMessage {
	
	public enum ProcessingStatus {
		STARTED,
		UPDATED,
		FINISHED;
	}
	
	public enum SwmrStatus {

		/**
		 * Not HDF5 file, or not opened with SWMR enabled
		 */
		DISABLED,

		/**
		 * File has SWMR enabled but it is not yet active
		 */
		ENABLED,

		/**
		 * File has SWMR enabled and it has been activated
		 */
		ACTIVE;
	}

	//Processed output file
	private String filePath;
	
	//Input file
	private String initialFilePath;
	
	private ProcessingStatus status;
	private SwmrStatus swmrStatus;
	
	public ProcessingMessage(String filePath, String initialFilePath, ProcessingStatus status, SwmrStatus swmrStatus) {
		this.filePath = filePath;
		this.initialFilePath = initialFilePath;
		this.status = status;
		this.swmrStatus = swmrStatus;
	}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getInitialFilePath() {
		return initialFilePath;
	}
	public void setInitialFilePath(String initialFilePath) {
		this.initialFilePath = initialFilePath;
	}
	public ProcessingStatus getStatus() {
		return status;
	}
	public void setStatus(ProcessingStatus status) {
		this.status = status;
	}
	public SwmrStatus getSwmrStatus() {
		return swmrStatus;
	}
	public void setSwmrStatus(SwmrStatus swmrStatus) {
		this.swmrStatus = swmrStatus;
	}
	
	
	
}
