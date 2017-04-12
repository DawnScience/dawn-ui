package org.dawnsci.common.widgets.statuscomposite;

import java.util.EventListener;

public interface IStatusCompositeChangedListener extends EventListener {
	public void compositeStatusChanged(StatusCompositeChangedEvent event);
}
