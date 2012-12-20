package org.dawnsci.common.widgets.tree;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;

public class ClearableFilteredTree extends FilteredTree {
	public ClearableFilteredTree(Composite control, int i,
			NodeFilter diffractionFilter, boolean b) {
		super(control,i,diffractionFilter,b);
		filterText.setToolTipText("Enter search string to filter the tree.\nThis will match on name, value or units");
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