package org.dawnsci.common.widgets.filedataset;

import java.util.EventObject;

public class FileDatasetCompositeStatusChangedEvent extends EventObject {

	private static final long serialVersionUID = 4740710706647407015L;

	private boolean status;
	
	public FileDatasetCompositeStatusChangedEvent(Object source, boolean status) {
		super(source);
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}
}
