package org.dawnsci.plotting.api.trace;

import java.util.EventListener;

/**
 * Interface used to notify of the stack position changing.
 * @author fcp94556
 *
 */
public interface IStackPositionListener extends EventListener {

	public void stackPositionChanged(final StackPositionEvent evt);
}
