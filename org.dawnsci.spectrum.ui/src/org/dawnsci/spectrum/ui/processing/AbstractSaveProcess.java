package org.dawnsci.spectrum.ui.processing;

public abstract class AbstractSaveProcess extends AbstractProcess {

	String path;
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public abstract String getDefaultName();

}
