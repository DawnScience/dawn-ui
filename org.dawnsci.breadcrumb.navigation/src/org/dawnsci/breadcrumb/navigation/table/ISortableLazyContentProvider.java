package org.dawnsci.breadcrumb.navigation.table;

import org.eclipse.jface.viewers.ILazyContentProvider;

public interface ISortableLazyContentProvider extends ILazyContentProvider {

	/**
	 * Used to sort the model
	 * @param view
	 * @param colEnum
	 * @return
	 */
	public int sort(ISortParticipant view, DirectionalIndexedColumnEnum colEnum);

}
