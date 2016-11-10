package org.dawnsci.common.widgets.filedataset;

import java.util.EventListener;

public interface IFileDatasetCompositeStatusChangedListener extends EventListener {
	public void compositeStatusChanged(FileDatasetCompositeStatusChangedEvent event);
}
