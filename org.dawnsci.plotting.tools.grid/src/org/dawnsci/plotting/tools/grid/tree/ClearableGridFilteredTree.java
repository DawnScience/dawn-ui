/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid.tree;

import javax.swing.tree.TreeNode;

import org.dawnsci.common.widgets.tree.LabelNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;

public class ClearableGridFilteredTree extends FilteredTree {
	
	public ClearableGridFilteredTree(Composite control, 
			                     int swtSwitches,
			                     GridNodeFilter diffractionFilter, boolean useNewLook,
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