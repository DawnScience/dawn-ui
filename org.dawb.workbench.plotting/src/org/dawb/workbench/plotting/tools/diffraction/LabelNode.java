package org.dawb.workbench.plotting.tools.diffraction;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;

/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items. It can also be used with a swing tree.
 * 
 * @author fcp94556
 *
 */
public class LabelNode implements TreeNode {

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
		// TODO Auto-generated method stub
		
	}

}
