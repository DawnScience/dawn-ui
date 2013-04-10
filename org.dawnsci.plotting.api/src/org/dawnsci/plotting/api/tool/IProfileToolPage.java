package org.dawnsci.plotting.api.tool;

import org.dawnsci.plotting.api.tool.IToolPage;

/**
 * Interface designed to hide special tool pages.
 * @author fcp94556
 *
 */
public interface IProfileToolPage extends IToolPage {

	/**
	 * Line type for Box Line Profiles
	 * @param lineType either SWT.VERTICAL, or SWT.HORIZONTAL
	 */
	void setLineType(int lineType);

}
