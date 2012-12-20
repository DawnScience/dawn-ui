package org.dawnsci.common.widgets.tree;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;

/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items. It can also be used with a swing tree.
 * 
 * The classes LabelNode, NumericNode and ObjectNode are generic and may be used 
 * elsewhere. They have not been moved somewhere generic yet because they create a 
 * dependency on jscience.

 * @author fcp94556
 *
 */
public class LabelNode implements TreeNode {

	private String           tooltip;
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	private String           label;
	private TreeNode         parent;
	private Vector<TreeNode> children;
	private boolean    editable=false;
	private boolean    defaultExpanded=false;

	public LabelNode() {
		this(null, null);
	}
	public LabelNode(LabelNode parent) {
		this(null, parent);
	}
	public LabelNode(String label) {
		this(label, null);
	}
	public LabelNode(String label, LabelNode parent) {
		this.label  = label;
		this.parent = parent;
		if (parent!=null) parent.addChild(this);
	}
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		if (children==null) return null;
		return children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		if (children==null) return 0;
		return children.size();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		if (children==null) return -1;
		return children.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return children!=null;
	}

	@Override
	public boolean isLeaf() {
		return children==null;
	}

	@Override
	public Enumeration<TreeNode> children() {
		
		return children.elements();
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> c) {
		this.children = new Vector<TreeNode>(c);
	}

	public boolean addChild(TreeNode c) {
		if (children==null) children = new Vector<TreeNode>(7);
		return children.add(c);
	}
	
	public boolean removeChild(TreeNode c) {
		if (children==null) return false;
		return children.remove(c);
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label!=null?label:"";
	}


	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean isDefaultExpanded() {
		return defaultExpanded;
	}
	public void setDefaultExpanded(boolean defaultExpanded) {
		this.defaultExpanded = defaultExpanded;
	}
	
	public String getPath() {
		StringBuilder buf = new StringBuilder();
		getPath(buf);
		return buf.toString();
	}
	
	protected void getPath(StringBuilder buf) {
		
		if (parent instanceof LabelNode) {
			((LabelNode)parent).getPath(buf);
		}
		if (getLabel()!=null) {
			buf.append("/");
			buf.append(getLabel().toLowerCase());
		}
	}
	public void dispose() {
		
		if (children!=null) for (TreeNode tn : children) {
			if (tn instanceof LabelNode) {
				((LabelNode)tn).dispose();
			}
		}
		if (children!=null) children.clear();
		parent=null;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (defaultExpanded ? 1231 : 1237);
		result = prime * result + (editable ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelNode other = (LabelNode) obj;
		if (defaultExpanded != other.defaultExpanded)
			return false;
		if (editable != other.editable)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

}
