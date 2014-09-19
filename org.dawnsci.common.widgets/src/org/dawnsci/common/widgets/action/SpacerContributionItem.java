/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.action;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Can be used to add space to a toolbar.
 * 
 * @author Matthew Gerring
 *
 */
public class SpacerContributionItem extends ContributionItem {

	public int width;
	public int flags;
	
	/**
	 * The widget created for this item; <code>null</code> before creation and
	 * after disposal.
	 */
	private Widget widget = null;
	
	public SpacerContributionItem() {
		this(100);
	}

	public SpacerContributionItem(int width) {
		this(width, SWT.NONE);
	}

	public SpacerContributionItem(int width, int flags) {
		this.width = width;
		this.flags = flags;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * fill to composite
	 */
	public void fill(Composite parent) {
		if (widget == null && parent != null) {
			
			widget = new Composite(parent, flags);
			((Composite)widget).setSize(width, parent.getSize().y);

			update(null);
		}
	}

	/**
     * Not possible
	 */
	public void fill(Menu parent, int index) {
		throw new RuntimeException("Cannot add a spacer to a menu at the moment!");
	}

	/**
	 * Fill toolbar
	 */
	public void fill(ToolBar parent, int index) {
		if (widget == null && parent != null) {

			if (index >= 0) {
			    widget = new ToolItem(parent, SWT.SEPARATOR, index);
			} else {
				widget = new ToolItem(parent, SWT.SEPARATOR);
			}
			((ToolItem)widget).setWidth(width);

			update(null);
			
		}
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

}
