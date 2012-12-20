package org.dawnsci.common.widgets.tree;

import java.util.EventListener;

public interface ValueListener extends EventListener {

	void valueChanged(ValueEvent evt);
}
