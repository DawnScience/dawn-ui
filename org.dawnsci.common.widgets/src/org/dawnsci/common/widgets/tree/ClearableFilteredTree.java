/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;

public class ClearableFilteredTree extends FilteredTree {
	
	public ClearableFilteredTree(Composite control, 
			                     int swtSwitches,
			                     NodeFilter diffractionFilter, boolean useNewLook,
			                     final String tooltip) {
		super(control,swtSwitches,diffractionFilter,useNewLook);
		filterText.setToolTipText(tooltip);
	}

	public void clearText() {
		super.clearText();
	}
	
	public void expand(Object element) {
		TreeViewer viewer = getViewer();
        if (element instanceof LabelNode) {
        	if (((LabelNode)element).isDefaultExpanded()) {
        		viewer.setExpandedState(element, true);
        	}
        }
        if (element instanceof TreeNode) {
        	TreeNode node = (TreeNode)element;
        	for (int i = 0; i < node.getChildCount(); i++) {
        		expand(node.getChildAt(i));
			}
        }
	}

}