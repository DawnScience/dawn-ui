package org.dawnsci.common.widgets.statuscomposite;

import java.util.EventObject;

public class StatusCompositeChangedEvent extends EventObject {

	private static final long serialVersionUID = -6965519858481074029L;
	private boolean status;
	
	public StatusCompositeChangedEvent(Object source, boolean status) {
		super(source);
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}
}
